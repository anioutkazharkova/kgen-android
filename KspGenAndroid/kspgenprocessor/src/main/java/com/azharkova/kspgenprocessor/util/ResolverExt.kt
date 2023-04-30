package com.azharkova.kspgenprocessor.util

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

fun getParamValueType(annotation: KSAnnotation, paramName: String): KSType? {
    val annotationArgument = annotation.arguments
        .find { argument -> argument.name?.asString() == paramName }
    //logger.warn(annotationArgument?.value.toString())
    val annotationArgumentValue = annotationArgument?.value as? KSType

    return annotationArgumentValue
}

fun getParamValueList(annotation: KSAnnotation, paramName: String): List<KSType>? {
    val annotationArgument = annotation.arguments
        .find { argument -> argument.name?.asString() == paramName }
    //logger.warn(annotationArgument?.value.toString())
    val annotationArgumentValue = annotationArgument?.value as? List<KSType>

    return annotationArgumentValue
}

fun getParamValue(annotation: KSAnnotation, paramName: String): Any? {
    val annotationArgument = annotation.arguments
        .find { argument -> argument.name?.asString() == paramName }
    return annotationArgument?.value
}
fun getParamValueSimple(annotation: KSAnnotation, paramName: String): Any? {
    val annotationArgument = annotation.arguments
        .find { argument -> argument.name?.asString() == paramName }
    return annotationArgument?.value.toString()
}

fun getAnnotation(declaration: KSAnnotated, moduleName: String, modulePath: String): KSAnnotation? {
    return declaration.annotations
        .filter { annotation -> annotation.shortName.asString() == moduleName }
        .find { annotation ->
            annotation.annotationType
                .resolve()
                .declaration
                .qualifiedName
                ?.asString() == modulePath
        }
}

/**
 * Returns a list of all [KSFunctionDeclaration]
 */
fun getFunction(resolver: Resolver, clazz: String, filter: String, logger: KSPLogger): List<KSFunctionDeclaration> {
    val annotated = resolver.getSymbolsWithAnnotation(clazz).toList()

    val filtered = mutableListOf<KSFunctionDeclaration>()
    logger.warn(filter)
    annotated.forEach { annotatedF ->
        annotatedF.annotations.forEach {
            it.arguments.forEach {

                if (it.value.toString() == filter) {
                    logger.warn("add")
                    logger.warn("name: ${it.name?.asString()} value: ${it.value?.toString()}")
                    filtered.add(annotatedF as KSFunctionDeclaration)
                }
            }
        }
    }
    return filtered//annotated.map { it as KSFunctionDeclaration }
}