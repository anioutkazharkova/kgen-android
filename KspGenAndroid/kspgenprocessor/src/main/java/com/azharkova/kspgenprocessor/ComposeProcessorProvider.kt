package com.azharkova.kspgenprocessor

import com.azharkova.annotations.Adapter
import com.azharkova.annotations.ToComposable
import com.azharkova.annotations.ToView
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

class ComposeProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ComposeProcessor(environment)
    }

}

class ComposeProcessor constructor(private val env: SymbolProcessorEnvironment): SymbolProcessor {
    protected val logger: KSPLogger = env.logger
    protected val codeGenerator = env.codeGenerator


    override fun process(resolver: Resolver): List<KSAnnotated> {
       val toViewFunctions = getToViewFunctions(resolver)
        val viewVisitor = ViewVisitor(resolver,codeGenerator, logger)
        val views = toComposables(resolver)
        views.forEach {
            it.accept(viewVisitor,Unit)
        }
       val functionVisitor = ComposableVisitor(resolver, logger, codeGenerator)
       toViewFunctions.forEach {
           it.accept(functionVisitor, Unit)
       }
        return emptyList()
    }

    private fun getToViewFunctions(resolver: Resolver): Sequence<KSFunctionDeclaration> {
        return resolver.getSymbolsWithAnnotation((ToView::class.java).name)
            .filterIsInstance<KSFunctionDeclaration>().distinct()
    }

    private fun toComposables(resolver: Resolver):Sequence<KSClassDeclaration> {
        return resolver.getSymbolsWithAnnotation((ToComposable::class.java).name)
            .filterIsInstance<KSClassDeclaration>().distinct()
    }
}