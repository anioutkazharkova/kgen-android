package com.azharkova.kspgenprocessor

import com.azharkova.kspgenprocessor.data.AdapterData
import com.azharkova.kspgenprocessor.util.WILDCARDIMPORT
import com.azharkova.kspgenprocessor.util.addImports
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

fun AdapterData.generateClassSource(): String {
    val classData = this
    val implClassName = "${classData.name}Impl"

    val inputType = classData.itemsType?.qualifiedName
    val vh = this.viewholders.firstOrNull()
    val type = vh?.let { "${it.packageName}.${it.name}" }

    val itemsProperty = PropertySpec.builder("items", MutableList::class.asTypeName().parameterizedBy(TypeVariableName(inputType!!)))
        .mutable(true)
        .initializer("mutableListOf()")
        .build()

    val setupFunc = FunSpec.builder("setupItems")
        .addParameter("items",List::class.asTypeName().parameterizedBy(TypeVariableName(inputType!!)))
        .addStatement("this.items = items")
        .addStatement("notifyDataSetChanged()")
        .build()

    val createFunc = FunSpec.builder("onCreateViewHolder")
        .addParameter("parent",TypeVariableName("android.view.ViewGroup"))
        .addParameter("viewType",Int::class.asTypeName())
        .addModifiers(KModifier.OVERRIDE)
        .addStatement("return ${vh!!.name}(${vh!!.layoutData!!.packageName}.${vh!!.layoutData!!.name}.inflate(LayoutInflater.from(parent.context), parent, false))")
        .returns(TypeVariableName(type!!))
        .build()

    val itemsCount =  FunSpec.builder("getItemCount").
            returns(Int::class.asTypeName())
        .addModifiers(KModifier.OVERRIDE)
        .addStatement("return items.count()")
        .build()

    val bindFunc = FunSpec.builder("onBindViewHolder")
        .addParameter("holder",TypeVariableName("${vh!!.name}"))
        .addParameter("position",Int::class.asTypeName())
        .addModifiers(KModifier.OVERRIDE)
        .addStatement("holder.bindItem(items.get(position))")
        .build()

    val implClassSpec = com.squareup.kotlinpoet.TypeSpec.classBuilder(implClassName)
        .superclass(ClassName("","androidx.recyclerview.widget.RecyclerView.Adapter").parameterizedBy(TypeVariableName(type!!)))
        .addProperty(itemsProperty)
        .addFunction(createFunc)
        .addFunction(bindFunc)
        .addFunction(itemsCount)
        .addFunction(setupFunc)
        .build()


    val bindExt = FunSpec.builder("bindItem")
        .receiver(TypeVariableName("${vh!!.name}"))
        .addStatement("${vh!!.setupFunc}(item)")
        .addParameter("item", TypeVariableName(inputType!!))
        .build()

    return com.squareup.kotlinpoet.FileSpec.builder(classData.packageName, implClassName)
        .addFileComment("Generated automatically")
        .addType(implClassSpec)
        .addFunction(bindExt)
        .addImports(classData.imports + listOf("android.view.LayoutInflater"))
        //.addImports(classData.imports + listOf("com.azharkova.kmmkspcases.data.*","com.azharkova.kmmkspcases.resolve","${classData.packageName}.${classData.name}" ,"${classData.requestClazz?.packageName}.${classData.requestClazz?.name}","${UseCase::class.java.name}", "kotlinx.coroutines.*"))
        .build().toString().replace(WILDCARDIMPORT, "*")

}