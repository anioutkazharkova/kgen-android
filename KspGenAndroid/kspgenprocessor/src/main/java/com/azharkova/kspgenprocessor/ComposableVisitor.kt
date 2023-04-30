package com.azharkova.kspgenprocessor

import com.azharkova.kspgenprocessor.util.*
import com.azharkova.kspgenprocessor.util.abstractComposeViewClassName
import com.azharkova.kspgenprocessor.util.attributeSetClassName
import com.azharkova.kspgenprocessor.util.composableClassName
import com.azharkova.kspgenprocessor.util.contextClassName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.*

class ComposableVisitor(val resolver: Resolver, val logger: KSPLogger, val codeGenerator: CodeGenerator) : KSVisitorVoid() {

    private var parameterCount = 0
    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {

        val functionName = function.simpleName.asString()
        val packageName = function.packageName.asString()
        val fileName = "${functionName}ComposableView"
        val dependencies = Dependencies(false)
        val file = codeGenerator.createNewFile(dependencies, packageName, fileName)
       function.parameters.forEach {
           logger.warn(it.name!!.asString() + it.type.resolve().resolveTypeName() + it.isFunction())
       }
       val primitivePropertySpecs = function.parameters.filter { it.isPrimitive }.map {
            val name = it.name?.asString() ?: "property$parameterCount".also { parameterCount++ }
            val ksType = it.type.resolve()
            val typeDeclaration = ksType.declaration
            val type = ClassName(
                typeDeclaration.packageName.asString(),
                typeDeclaration.simpleName.asString(),
            )
            val functionRepresentation = it.isFunction()

                PropertySpec.builder(name, type)
                    .mutable(mutable = true)
                    .delegate("""mutableStateOf(${it.defaultValue()})""")
                    .build()

        }
        val otherPropertySpecs = function.parameters.filter { !it.isPrimitive }.map {
            val name = it.name?.asString() ?: "prop$parameterCount".also { parameterCount++ }
            val ksType = it.type.resolve()
            val typeDeclaration = ksType.declaration
            val type = ClassName(
                typeDeclaration.packageName.asString(),
                typeDeclaration.simpleName.asString(),
            ).copy(nullable = true)
            val functionRepresentation = it.isFunction()
            if (functionRepresentation.isEmpty()) {
                PropertySpec.builder(name, type)
                    .mutable(mutable = true)
                    .delegate("""mutableStateOf(null)""")
                    .build()
            } else {
                PropertySpec.builder(name, TypeVariableName(functionRepresentation).copy(true))
                    .mutable(true)
                    .initializer("null")
                    .build()
            }
        }
        val allPropertySpecs = primitivePropertySpecs + otherPropertySpecs
        val fileSpecBuilder = FileSpec.builder(packageName, fileName)
        val typeSpecBuilder = TypeSpec.classBuilder(fileName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addAnnotation(JvmOverloads::class)
                    .addParameter("context", contextClassName)
                    .addParameter(
                        ParameterSpec.builder("attrs", attributeSetClassName.copy(nullable = true))
                            .defaultValue("null")
                            .build()
                    )
                    .addParameter(
                        ParameterSpec.builder("defStyle", Int::class)
                            .defaultValue("0")
                            .build()
                    )
                    .build()
            )
            .superclass(abstractComposeViewClassName)
            .addSuperclassConstructorParameter("context")
            .addSuperclassConstructorParameter("attrs")
            .addSuperclassConstructorParameter("defStyle")
            .addProperties(allPropertySpecs)

        val funSpecBuilder = FunSpec.builder("Content")
            .addAnnotation(composableClassName)
            .addModifiers(KModifier.OVERRIDE)

        if (otherPropertySpecs.isNotEmpty()) {
            funSpecBuilder.beginControlFlow("if (listOf(${otherPropertySpecs.joinToString { it.name }}).all { it != null })")
        }

        funSpecBuilder
            .addCode(
                if (allPropertySpecs.isEmpty()) {
                    "$functionName()"
                } else {
                    """
                       |$functionName(
                       |${allPropertySpecs.joinToString(separator = ",\n") { "  ${it.name} = ${it.name}${if (!it.isPrimitive) "!!" else ""}" }}
                       |)
                       |
                     """.trimMargin()
                }
            )

       if (otherPropertySpecs.isNotEmpty()) {
            funSpecBuilder.endControlFlow()
        }

        typeSpecBuilder.addFunction(funSpecBuilder.build())

        if (allPropertySpecs.isNotEmpty()) {
            fileSpecBuilder
                .addImport("androidx.compose.runtime", "getValue")
                .addImport("androidx.compose.runtime", "mutableStateOf")
                .addImport("androidx.compose.runtime", "setValue")
        }

        fileSpecBuilder.addType(typeSpecBuilder.build())

        fileSpecBuilder.writeTo(file)
    }
}