package com.azharkova.kspgenprocessor.data

import com.azharkova.annotations.Field
import com.google.devtools.ksp.symbol.KSType

data class ListScreenData(
    var name: String = "",
    var packageName: String = "",
    var model: KSType? = null,
    var view: KSType? = null,
    var propertyName: String = "",
    var returnType: KSType? = null,
    var imports: List<String> = emptyList(),
)

data class MapViewData(
    var name: String = "",
    var packageName: String = "",
    var model: KSType? = null,
    val layout: LayoutData? = null,
    var imports: List<String> = emptyList(),
    var modelData: MapModelData? = null
)

data class FieldData(
    var fieldName: String,
    var id: Int? = 0,
    var valueName: String = "",
    var type: Field = Field.TEXT
)

data class MapModelData(
    var fields: List<FieldData>
)