package com.azharkova.kspgenprocessor.util

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toClassName
import java.io.File

fun KSType?.resolveTypeName(): String {
    //TODO: Find better way to handle type alias Types
    return this.toString().removePrefix("[typealias ").removeSuffix("]")
}

fun KSFunctionDeclaration.getAnnotationByName(name: String): KSAnnotation? {
    return this.annotations.toList().firstOrNull { it.shortName.asString() == name }
}

fun <T> KSAnnotation.getArgumentValueByName(name: String): T? {
    return this.arguments.firstOrNull { it.name?.asString() == name }?.value as? T
}


val KSFunctionDeclaration.isSuspend: Boolean
    get() = (this).modifiers.contains(Modifier.SUSPEND)

fun KSClassDeclaration.getFileImports(): List<String> {
    val importList =
        File(this.containingFile!!.filePath)
            .readLines()
            .filter { it.trimStart().startsWith("import") }
            .toMutableSet()

    //importList.add(ktorfitClass.packageName + "." + ktorfitClass.name)
    //importList.add("de.jensklingenberg.ktorfit.internal.*")

    return importList.map { it.removePrefix("import ") }
}


fun String.removeWhiteSpaces(): String {
    return this.replace("\\s".toRegex(), "")
}


fun FileSpec.Builder.addImports(imports: List<String>): FileSpec.Builder {

    imports.forEach {
        /**
         * Wildcard imports are not allowed by KotlinPoet, as a workaround * is replaced with WILDCARDIMPORT, and it will be replaced again
         * after Kotlin Poet generated the source code
         */
        val packageName = it.substringBeforeLast(".")
        val className = it.substringAfterLast(".").replace("*", WILDCARDIMPORT)

        this.addImport(packageName, className)
    }
    return this
}

const val WILDCARDIMPORT = "WILDCARDIMPORT"



fun String.prefixIfNotEmpty(s: String): String {
    return (s + this).takeIf { this.isNotEmpty() } ?: this
}

fun String.postfixIfNotEmpty(s: String): String {
    return (this + s).takeIf { this.isNotEmpty() } ?: this
}

fun String.surroundIfNotEmpty(prefix: String = "", postFix: String = ""): String {
    return this.prefixIfNotEmpty(prefix).postfixIfNotEmpty(postFix)
}

