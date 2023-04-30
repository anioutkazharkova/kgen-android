package com.azharkova.kspgenprocessor

import com.azharkova.kspgenprocessor.util.resolveTypeName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.PropertySpec

fun getFunction(resolver: Resolver, clazz: String, filter: String = "", logger: KSPLogger): List<KSFunctionDeclaration> {
    val annotated = resolver.getSymbolsWithAnnotation(clazz).toList()

    val filtered = mutableListOf<KSFunctionDeclaration>()
    logger.warn(filter)
    annotated.forEach { annotatedF ->
        annotatedF.annotations.forEach {
            it.arguments.forEach {

                if (filter.isNotEmpty() && it.value.toString() == filter) {
                    logger.warn("add")
                    logger.warn("name: ${it.name?.asString()} value: ${it.value?.toString()}")
                    filtered.add(annotatedF as KSFunctionDeclaration)
                } else {
                    filtered.add(annotatedF as KSFunctionDeclaration)
                }
            }
        }
    }
    return filtered//annotated.map { it as KSFunctionDeclaration }
}

internal val primitiveDefaults = mapOf(
    Byte::class to "0",
    Short::class to "0",
    Int::class to "0",
    Long::class to "0",
    Float::class to "0f",
    Double::class to "0.0",
    Boolean::class to "false",
    Char::class to "\' \'",
    String::class to "\"\"",
)

internal fun KSValueParameter.defaultValue(): String? {
    val key = primitiveDefaults.keys.find {
        it.simpleName == this.type.resolve().declaration.simpleName.asString()
    }
    return primitiveDefaults[key]
}

internal val KSValueParameter.isPrimitive: Boolean
    get() = this.type.resolve().declaration.simpleName.asString() in primitiveDefaults.keys.map { kClass ->
        kClass.simpleName
    }
internal val PropertySpec.isPrimitive: Boolean
    get() = this.type.toString() in primitiveDefaults.keys.map { kClass -> kClass.qualifiedName }

fun KSValueParameter.isFunction():String  {
    if (!this.type.resolve().resolveTypeName().startsWith("Function")){
        return ""
    }
    val name = this.type.resolve().resolveTypeName()
    var matches = Regex("(?:Function[0-9]<)*([^\\W]+[^,\\s]*)*(?<!>)").findAll(name).toList()
    //matches = matches.takeLast(matches.count() - 1)
    var values = matches.map { it.groups.last()?.value.orEmpty() }.filter { it.isNotEmpty() }

    if (values.isNotEmpty()) {
        if (values.count() == 1) {
            return "(()->${values.first()})"
        } else {
            return "((${values.take(values.count()-1).joinToString(",")})->${values.last()})"
        }
    }
    return ""

}

fun KSFunctionDeclaration.isFunction():String  {
    val name = this.returnType?.resolve().resolveTypeName().orEmpty()
    var matches = Regex("(?:Function[0-9]<)*([^\\W]+[^,\\s]*)*(?<!>)").findAll(name).toList()
    //matches = matches.takeLast(matches.count() - 1)
    var values = matches.map { it.groups.last()?.value.orEmpty() }.filter { it.isNotEmpty() }

    if (values.isNotEmpty()) {
        if (values.count() == 1) {
            return "(()->${values.first()})"
        } else {
            return "((${values.take(values.count()-1).joinToString(",")})->${values.last()})"
        }
    }
    return ""

}
//(?:Function[0-9]<)*([^\W]+[^,\s]*)*(?<!>)