package com.azharkova.kspgenprocessor.util

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toKModifier

data class ClazzInfo(
    val name: String,
    val packageName: String,
    var functions: List<FunctionsData>,
    val imports: List<String>,
    val superClasses: List<String> = emptyList(),
    val properties: List<KSPropertyDeclaration> = emptyList(),
    val modifiers: List<KModifier> = emptyList()
)

data class FunctionsData(
    val name: String,
    val returnType: ReturnsTypeData,
    val isSuspend: Boolean = false,
    val parameterDataList: List<ParametersData>,
)

data class ParametersData(
    val name: String,
    val type: ReturnsTypeData)

fun KSValueParameter.createParametersData(logger: KSPLogger): ParametersData {
    val ksValueParameter = this

    val parameterName = ksValueParameter.name?.asString() ?: ""
    val parameterType = ksValueParameter.type.resolve()


    val type =
        ReturnsTypeData(
            parameterType.resolveTypeName(),
            parameterType.declaration.qualifiedName?.asString() ?: ""
        )

    return ParametersData(
        parameterName,
        type
    )
}

data class ReturnsTypeData(val name: String, val qualifiedName: String)


fun KSFunctionDeclaration.toFunctionsData(
    logger: KSPLogger,
    imports: List<String>,
    packageName: String,
    resolver: Resolver
): FunctionsData {
    val funcDeclaration = this
    val functionName = funcDeclaration.simpleName.asString()
    val functionParameters = funcDeclaration.parameters.map { it.createParametersData( logger) }

    val typeData = TypeData.getMyType(
        funcDeclaration.returnType?.resolve().resolveTypeName().removeWhiteSpaces(),
        imports,
        packageName,
        resolver
    )

    val returnType = ReturnsTypeData(
        funcDeclaration.returnType?.resolve().resolveTypeName(),
        typeData.toString()
    )

    return FunctionsData(
        functionName,
        returnType,
        funcDeclaration.isSuspend,
        functionParameters
    )
}

fun KSClassDeclaration.toClazzData(logger: KSPLogger, resolver: Resolver, functions: List<KSFunctionDeclaration> = emptyList()): ClazzInfo {
    val ksClassDeclaration = this
    val imports = ksClassDeclaration.getFileImports().toMutableList()
    val packageName = ksClassDeclaration.packageName.asString()
    val className = ksClassDeclaration.simpleName.asString()


    val functionDataList: List<FunctionsData> = (if (functions.isNotEmpty()) functions else
        ksClassDeclaration.getDeclaredFunctions().toList()).map { funcDeclaration ->
        return@map funcDeclaration.toFunctionsData(logger, imports, packageName, resolver)
    }

    val supertypes =
        ksClassDeclaration.superTypes.toList().filterNot {
            /** In KSP Any is a supertype of an interface */
            it.resolve().resolveTypeName() == "Any"
        }.mapNotNull { it.resolve().declaration.qualifiedName?.asString() }
    val properties = ksClassDeclaration.getAllProperties().toList()


    return ClazzInfo(
        name = className,
        packageName = packageName,
        functions = functionDataList,
        imports = imports,
        superClasses = supertypes,
        properties = properties,
        modifiers = ksClassDeclaration.modifiers.mapNotNull { it.toKModifier() })
}