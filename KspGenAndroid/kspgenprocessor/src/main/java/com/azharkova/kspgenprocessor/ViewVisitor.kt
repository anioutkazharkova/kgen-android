package com.azharkova.kspgenprocessor

import com.azharkova.annotations.ClickModifier
import com.azharkova.annotations.ToComposable
import com.azharkova.kspgenprocessor.util.*
import com.azharkova.kspgenprocessor.util.androidViewClassName
import com.azharkova.kspgenprocessor.util.composableClassName
import com.azharkova.kspgenprocessor.util.modifierClassName
import com.azharkova.kspgenprocessor.util.writeTo
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.*

class ViewVisitor(private val resolver: Resolver,
                  private val codeGenerator: CodeGenerator, val logger: KSPLogger,
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val generateFunctionsAsParameters = classDeclaration.annotations.find {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == ToComposable::class.java.name
        }?.arguments?.singleOrNull()?.value as? Boolean ?: true
        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()
        val functionName = className.removeSuffix("View")
        val dependencies = Dependencies(false)
        val file = codeGenerator.createNewFile(dependencies, packageName, functionName)
        val fileSpecBuilder = FileSpec.builder(packageName, functionName)

        val paramFunctionMap = mutableMapOf<KSFunctionDeclaration, List<KSValueParameter>>()
        var  paramSpecBuilders = mutableListOf<ParameterSpec>()
        var modifiersList = mutableListOf<String>()
       paramSpecBuilders.addAll( classDeclaration.getAllFunctions().mapNotNull { declaration ->
            getAnnotation(declaration,"ClickModifier",ClickModifier::class.java.name)?.let {
               logger.warn(declaration.returnType?.resolve()?.resolveTypeName().orEmpty())
                modifiersList += "${declaration.simpleName.asString()}().apply{\n${declaration.simpleName.asString().replace("set","").lowercase()}.invoke()}"
               logger.warn(modifiersList.joinToString())
                ParameterSpec.builder(
                   name = declaration.simpleName.asString().replace("set","").lowercase(),
                   type = TypeVariableName(declaration.isFunction())
               ).build()
            }
        }.toList())
       paramSpecBuilders.addAll(if (generateFunctionsAsParameters) {
            classDeclaration.getAllFunctions()
                .filter { declaration ->
                    declaration.parameters.isNotEmpty()
                            && declaration.returnType?.resolve() == resolver.builtIns.unitType
                            && declaration.isPublic()
                            && declaration.findOverridee() == null
                }
                .flatMap { declaration ->
                    paramFunctionMap[declaration] = declaration.parameters
                    declaration.parameters.mapNotNull { valueParam ->
                        valueParam.name?.asString()?.let { name ->
                            val type = valueParam.type.resolve().declaration
                            val builder = ParameterSpec.builder(
                                name = name,
                                type = ClassName(
                                    type.packageName.asString(),
                                    type.simpleName.asString(),
                                )
                            )
                            builder.addAnnotations(
                                valueParam.annotations.map {
                                    val annotation = it.annotationType.resolve().declaration
                                    AnnotationSpec.builder(
                                        ClassName(
                                            annotation.packageName.asString(),
                                            annotation.simpleName.asString(),
                                        )
                                    ).build()
                                }.toList()
                            )
                            builder.build()
                        }
                    }
                }
                .toList()
        } else {
            emptyList()
        })

        fun addApplyBlockIfNecessary(): String {
            return if (paramSpecBuilders.isNotEmpty()) {
                """
                |apply {
                |      ${
                    paramFunctionMap.keys.joinToString("\n      ") { function ->
                        "${function.simpleName.asString()}(" +
                                "${
                                    paramFunctionMap[function].orEmpty().joinToString { param ->
                                        param.name?.asString() ?: ""
                                    }
                                })"
                    }
                }
                | ${modifiersList.joinToString("\n")}
                |      viewConfig()
                |    }
                """.trimMargin()
            } else {
                "apply {\n ${modifiersList.joinToString("\n")} \nviewConfig() }"
            }
        }

        val viewConfigLambdaTypeName = LambdaTypeName.get(
            receiver = ClassName(packageName, className),
            returnType = UNIT,
        )
        val funSpecBuilder = FunSpec.builder(functionName)
            .addAnnotation(composableClassName)
            .addParameters(paramSpecBuilders)
            .addParameter(
                ParameterSpec.builder("modifier", modifierClassName)
                    .defaultValue("%T", modifierClassName)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("viewConfig", viewConfigLambdaTypeName)
                    .build()
            )
            .addCode(
                """
                |%T(
                |  factory = { context -> $className(context) },
                |  modifier = modifier,
                |  update = { view ->
                |    view.${addApplyBlockIfNecessary()}
                |  }
                |)
                """.trimMargin(),
                androidViewClassName,
            )
        fileSpecBuilder.addFunction(funSpecBuilder.build())
        fileSpecBuilder.writeTo(file)
    }
}
