package com.azharkova.kspgenprocessor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated

class NavigationProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return NavigationProcessor(environment)
    }

}

class NavigationProcessor constructor(private val env: SymbolProcessorEnvironment): SymbolProcessor {
    protected val logger: KSPLogger = env.logger
    protected val codeGenerator = env.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {
        return emptyList()
    }
}