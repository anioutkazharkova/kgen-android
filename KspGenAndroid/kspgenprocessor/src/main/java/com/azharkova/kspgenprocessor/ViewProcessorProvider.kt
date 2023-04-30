package com.azharkova.kspgenprocessor

import com.azharkova.annotations.*
import com.azharkova.kspgenprocessor.data.*
import com.azharkova.kspgenprocessor.util.*
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toClassName
import java.io.*

class ViewProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ViewProcessor(environment)
    }

}

class ViewProcessor constructor(private val env: SymbolProcessorEnvironment) :
    SymbolProcessor {
    protected val logger: KSPLogger = env.logger
    protected val codeGenerator = env.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {
        var views = getViews(resolver)
        generateImplClass( views.mapNotNull {
            it.toViewData(resolver)
        }.toList(),codeGenerator)
        var lists = getLists(resolver)
        logger.warn(lists.count().toString())
        val listData = lists.mapNotNull {
            it.toListData()
        }.toList()
        logger.warn(listData.count().toString())
       generateList(listData, codeGenerator,logger)
        return emptyList()
    }

    fun getLists(resolver: Resolver): Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation((ListScreen::class.java).name)
            .filterIsInstance<KSClassDeclaration>().distinct()
    }

    fun getViews(resolver: Resolver): Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation((MapView::class.java).name)
            .filterIsInstance<KSClassDeclaration>().distinct()
    }

    fun getModels(resolver: Resolver, name: String): Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation((MapModel::class.java).name)
            .filterIsInstance<KSClassDeclaration>().distinct()
            .filter { it.simpleName.asString() == name }
    }

    fun KSClassDeclaration.toListData():ListScreenData? {
        val declaration = this
        val name = declaration.simpleName.asString()
        val packageName = declaration.packageName.asString()
       return getAnnotation(this, "ListScreen", ListScreen::class.java.name)?.let {
            annotation ->
            val model = getParamValueType(annotation, "vm")
            val view =  getParamValueType(annotation, "item")
           val imports = mutableListOf<String>().apply {
               model?.declaration?.let {
                   this.add("${it.packageName.asString()}.${it.simpleName.asString()}")
               }
               view?.declaration?.let {
                   this.add("${it.packageName.asString()}.${it.simpleName.asString()}Composable")
               }
           }
            var propertyName: String = ""
            var propertyType: KSType? = null
            model?.declaration?.let {
               val result = (it as? KSClassDeclaration)?.getAllProperties()?.toList()?.firstOrNull() {
                    it.annotations.any{
                        it.shortName.getShortName() == VmResult::class.simpleName.orEmpty()
                    }
                }
              propertyName = result?.simpleName?.asString().orEmpty()
                propertyType = result?.type?.resolve()

            }
            ListScreenData(name,packageName,model,view,propertyName,propertyType,imports)
        } ?: null
    }

    fun KSClassDeclaration.toViewData(resolver: Resolver): MapViewData? {
        val declaration = this
        val name = declaration.simpleName.asString()
        val packageName = declaration.packageName.asString()
        val annotation = getAnnotation(this, "MapView", MapView::class.java.name)
        return annotation?.let {

            val model = getParamValueType(it, "model")
            val layoutParameter = this.getConstructors()?.map{
                it.parameters.forEach {
                    logger.warn("${it.name?.asString().orEmpty()} ${it.type.toString()}")
                }
                it.parameters.firstOrNull {
                    it.annotations.any{
                        it.shortName.getShortName() == BindLayout::class.simpleName.orEmpty()
                    }
                }
            }?.firstOrNull()
            val layoutData = layoutParameter?.let {
                LayoutData(it.type.toString(), "com.azharkova.kspgenandroid.databinding")
            }

            var imports = mutableListOf<String>().apply {
                this.add("${packageName}.${name}")
                model?.declaration?.let {
                    this.add("${it.packageName.asString()}.${it.simpleName.asString()}")
                }
                    this.add("com.azharkova.kspgenandroid.databinding.${layoutParameter?.type?.toString().orEmpty()}")

            }

            val modelData = getModels(
                resolver,
                model?.declaration?.simpleName?.asString().orEmpty()
            )?.firstOrNull()?.toModelData()
            logger.warn(modelData?.toString().orEmpty())

            MapViewData(
                name,
                packageName,
                model = model,
                layout = layoutData,
                imports,
                modelData = modelData
            )
        } ?: null
    }

    fun KSClassDeclaration.toModelData(): MapModelData? {
        val declaration = this
        val properties = declaration.getAllProperties().toList()
        val textFields = properties.mapNotNull { property ->
            getAnnotation(property, "MapText", MapText::class.java.name)?.let {
                logger.warn(it.arguments.joinToString { it.name?.asString().orEmpty() })
                val id = (getParamValue(it, "id") as? Int)

                FieldData(
                    id = id ?: 0,
                    fieldName = "",
                    valueName = property.simpleName.asString(),
                    type = Field.TEXT
                )

            } ?: null
        }
        val imageFields = properties.mapNotNull { property ->
            getAnnotation(property, "MapImage", MapImage::class.java.name)?.let {
                logger.warn(it.arguments.joinToString { it.name?.asString().orEmpty() })
                val id = (getParamValue(it, "id") as? Int)

                FieldData(
                    id = id ?: 0,
                    fieldName = "",
                    valueName = property.simpleName.asString(),
                    type = Field.IMAGE
                )

            } ?: null
        }

        return MapModelData(textFields + imageFields)
    }
}

fun ListScreenData.generateSource():String {
    val classData = this
    val implClassName = "${classData.name}Composable"
    val funSpecBuilder = FunSpec.builder(implClassName)
        .addAnnotation(composableClassName)
       .addParameter(ParameterSpec.builder("viewModel", TypeVariableName(model.resolveTypeName())).build())
        .addStatement("LaunchedEffect(Unit) {\n" +
                "        viewModel.loadData()\n" +
                "    }")
        .addStatement("val data by viewModel.${propertyName}.collectAsState()")
        .addStatement("LazyColumn(\n" +
                "        contentPadding =  PaddingValues(horizontal = 16.dp, vertical = 8.dp)\n) {\n" +
                "        items(data.orEmpty()) {\n" +
                "           ${classData.view?.resolveTypeName()}Composable(it)\n" +
                "        }\n" +
                "    }")
        .build()
    return com.squareup.kotlinpoet.FileSpec.builder(classData.packageName, implClassName)
        .addFileComment("Generated automatically")
        .addFunction(funSpecBuilder)
        .addImports(classData.imports + listOf("android.view.LayoutInflater", "androidx.compose.ui.viewinterop.AndroidViewBinding","android.widget.TextView","android.widget.ImageView", "coil.load",
            "androidx.compose.foundation.layout.PaddingValues",
                  "androidx.compose.foundation.lazy.LazyColumn",
                    "androidx.compose.runtime.Composable",
                    "androidx.compose.runtime.LaunchedEffect",
                    "androidx.compose.runtime.collectAsState",
                    "androidx.compose.runtime.getValue",
            "androidx.compose.foundation.lazy.items",
                    "androidx.compose.ui.unit.dp"))
        .build().toString().replace(WILDCARDIMPORT, "*")
}

fun MapViewData.generateSource():String {
    val classData = this
    val implClassName = "${classData.name}Composable"

    val funSpecBuilder = FunSpec.builder(implClassName)
        .addAnnotation(composableClassName)
        .addParameter(ParameterSpec.builder("model", TypeVariableName(model.resolveTypeName())).build())
        .addStatement("AndroidViewBinding(${classData.layout?.name}::inflate) {")
        .apply {
            classData.modelData?.fields?.forEach {
                addStatement("(this.root.rootView.findViewById(${it.id})${(it.statement())}")
            }
        }
        .addStatement("}")
        .build()

    return com.squareup.kotlinpoet.FileSpec.builder(classData.packageName, implClassName)
        .addFileComment("Generated automatically")

        .addImports(classData.imports + listOf("android.view.LayoutInflater", "androidx.compose.ui.viewinterop.AndroidViewBinding","android.widget.TextView","android.widget.ImageView", "coil.load"))
        .addFunction(funSpecBuilder)
        .build().toString().replace(WILDCARDIMPORT, "*")

}

private fun generateList(data:  List<ListScreenData>, codeGenerator: CodeGenerator, logger: KSPLogger) {
    if (data.isNotEmpty()) {
        data.forEach { classData ->
            val fileSource = classData.generateSource()
            logger.warn(classData.name)
            val packageName = classData.packageName
            val className = classData.name
            val fileName = "_${className}Composable"

            codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, fileName, "kt")
                .use { output ->
                    OutputStreamWriter(output).use { writer ->
                        writer.write(fileSource)
                    }
                }
        }
    }
}

private fun generateImplClass(data:  List<MapViewData>, codeGenerator: CodeGenerator) {
    data.forEach { classData ->
        val fileSource = classData.generateSource()

        val packageName = classData.packageName
        val className = classData.name
        val fileName = "_${className}Impl"

        codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, fileName , "kt").use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write(fileSource)
            }
        }
    }
}