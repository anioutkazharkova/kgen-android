package com.azharkova.kspgenprocessor

import com.azharkova.annotations.BindClick
import com.azharkova.kspgenprocessor.data.ClassEventData
import com.azharkova.kspgenprocessor.data.EventData
import com.azharkova.kspgenprocessor.util.WILDCARDIMPORT
import com.azharkova.kspgenprocessor.util.addImports
import com.azharkova.kspgenprocessor.util.getAnnotation
import com.azharkova.kspgenprocessor.util.getParamValueSimple
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import java.io.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class EventProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return EventProcessor(environment)
    }

}

class EventProcessor constructor(private val env: SymbolProcessorEnvironment): SymbolProcessor {
    protected val logger: KSPLogger = env.logger
    protected val codeGenerator = env.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val functions = getFunction(resolver, BindClick::class.java.name, logger = logger)
        functions.groupBy { it.closestClassDeclaration()!! }.map { (classDesc, funcs) ->
             classDesc.toEventData(funcs).apply {
                 val fileSource = this.generateSource()

                 val packageName = this.packageName
                 val className = this.name
                 val fileName = "_${className}Impl"

                 codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, fileName , "kt").use { output ->
                     OutputStreamWriter(output).use { writer ->
                         writer.write(fileSource)
                     }
                 }
             }
        }

        return emptyList()
    }
}

fun KSClassDeclaration.toEventData(functions: List<KSFunctionDeclaration>):ClassEventData {
    val name = this.simpleName.asString()
    val packageName = this.packageName.asString()


    val events = functions.map { func ->
        var resourse: Int = 0
        getAnnotation(func, "BindClick", "com.azharkova.annotations.BindClick")?.let {
            val id = getParamValueSimple(it, "id").toString()
            resourse = id.toInt()
        }

        EventData(
            name,
            packageName,
            eventName = func.simpleName.asString(),
            eventResource = resourse
        )
    }
    return ClassEventData(name, packageName, listOf("${packageName}.${name}"), events)
}


fun ClassEventData.generateSource():String {
    val classData = this
    val implClassName = "${classData.name}Ext"

    val setupFunc = FunSpec.builder("setupEvents")
        .receiver(TypeVariableName(classData.name))
        .apply {
            events.forEach {
                addStatement("(view?.findViewById(${it.eventResource}) as? View)?.setOnClickListener{${it.eventName}()}")
            }
        }
        .build()


    return com.squareup.kotlinpoet.FileSpec.builder(classData.packageName, implClassName)
        .addFileComment("Generated automatically")
        .addFunction(setupFunc)
        .addImports(classData.imports + listOf("android.view.View"))
        .build().toString().replace(WILDCARDIMPORT, "*")
}