package com.azharkova.kspgenprocessor

import com.azharkova.annotations.Adapter
import com.azharkova.annotations.BindLayout
import com.azharkova.annotations.BindSetup
import com.azharkova.annotations.BindVH
import com.azharkova.kspgenprocessor.data.AdapterData
import com.azharkova.kspgenprocessor.data.LayoutData
import com.azharkova.kspgenprocessor.data.ViewHolderData
import com.azharkova.kspgenprocessor.util.*
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import java.io.OutputStreamWriter

class AdapterProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AdapterProcessor(environment)
    }

}

class AdapterProcessor constructor(private val env: SymbolProcessorEnvironment): SymbolProcessor {
    protected val logger: KSPLogger = env.logger
    protected val codeGenerator = env.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val adapters = getAdapters(resolver)
        val data = adapters.mapNotNull {
            it.toAdapterData(resolver)
        }.toList()
        generateImplClass(data, codeGenerator)
        return emptyList()
    }
    private fun KSClassDeclaration.toAdapterData(resolver: Resolver):AdapterData? {
        val name = this.simpleName.getShortName()
        val packageName = this.packageName.asString()
        val adapter = this
        logger.warn("Adapter: ${this.qualifiedName?.asString().orEmpty()}")
        val annotation = getAnnotation(adapter, "Adapter", Adapter::class.java.name)
        return  annotation?.let {
            val bindType = annotation?.let {
                getParamValue(annotation, "bindType")
            }
            val viewholdersParams = getParamValueList(annotation, "holders")
            val viewholders = getViewHolders(
                resolver,
                viewholdersParams.orEmpty()
                    .map { it.declaration.qualifiedName?.asString().orEmpty() })


            AdapterData(name, packageName, viewholders = viewholders.map {
                it.toViewHolderData(resolver)
            }.toList(), itemsType = ReturnsTypeData(bindType?.declaration?.simpleName?.getShortName().orEmpty(),
                bindType?.declaration?.qualifiedName?.asString().orEmpty()))
        }

        return null
    }

    private fun KSClassDeclaration.toViewHolderData(resolver: Resolver): ViewHolderData {
        val annotation = getAnnotation(this, "BindVH", BindVH::class.java.name)
        val bindType = annotation?.let {
            getParamValue(annotation, "bindType")
        }
        val returnType = ReturnsTypeData(bindType?.declaration?.simpleName?.getShortName().orEmpty(),
        bindType?.declaration?.qualifiedName?.asString().orEmpty())
        val name = this.qualifiedName?.getShortName().orEmpty()
        val packageName = this.packageName?.asString().orEmpty()

        val function = this.getAllFunctions().firstOrNull{ function ->
            logger.warn(function.qualifiedName?.asString().orEmpty())
            logger.warn(function.annotations.map{it.shortName.getShortName()}.joinToString (" "))
            function.annotations.any { it.shortName.getShortName() == BindSetup::class.java.simpleName.orEmpty()}
        }



        //var setup = getFunction(resolver, BindSetup::class, filter = )

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
            // logger.warn(layoutData?.name.orEmpty())
        }
        return ViewHolderData(layoutData, name, packageName, bindType = returnType, setupFunc = function?.simpleName?.getShortName().orEmpty())
    }


    private fun getAdapters(resolver: Resolver): Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation((Adapter::class.java).name)
            .filterIsInstance<KSClassDeclaration>().distinct()
    }

    private fun getViewHolders(resolver: Resolver, filterNames: List<String>): Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation((BindVH::class.java).name)
            .filterIsInstance<KSClassDeclaration>().distinct().filter {
               filterNames.contains(it.qualifiedName?.asString().orEmpty())
            }
    }


    private fun getLayout(resolver: Resolver): Sequence<KSValueParameter> {
        return resolver.getSymbolsWithAnnotation((BindLayout::class.java).name)
            .filterIsInstance<KSValueParameter>().distinct()
    }


    fun generateImplClass(adapters:  List<AdapterData>, codeGenerator: CodeGenerator) {
        adapters.forEach { classData ->
            val fileSource = classData.generateClassSource()

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
}