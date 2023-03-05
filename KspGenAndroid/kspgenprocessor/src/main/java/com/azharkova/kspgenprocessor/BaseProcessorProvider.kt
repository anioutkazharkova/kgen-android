package com.azharkova.kspgenprocessor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated

abstract class BaseProcessorProvider<T: BaseProcessor> : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return createBaseProcessor(environment)
    }

    abstract fun createBaseProcessor(environment: SymbolProcessorEnvironment):T

}

abstract class BaseProcessor constructor(private val env: SymbolProcessorEnvironment): SymbolProcessor {
    protected val logger: KSPLogger = env.logger
    protected val codeGenerator = env.codeGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {

        return emptyList()
    }
}