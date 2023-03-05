package com.azharkova.kspgenprocessor.data

import com.azharkova.kspgenprocessor.util.ReturnsTypeData

data class AdapterData(
    val name: String,
    var packageName: String = "",
    var imports: List<String> = emptyList(),
    val viewholders: List<ViewHolderData> = emptyList(),
    val itemsType: ReturnsTypeData? = null
)

data class LayoutData(
    val name: String = "",
    var packageName: String = "",
    var imports: List<String> = emptyList(),
)

data class ViewHolderData(
    val layoutData: LayoutData? = null,
    val name: String,
    var packageName: String = "",
    var imports: List<String> = emptyList(),
    val bindType: ReturnsTypeData? = null,
    val setupFunc: String = ""
)