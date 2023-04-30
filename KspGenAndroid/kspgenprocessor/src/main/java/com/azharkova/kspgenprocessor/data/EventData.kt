package com.azharkova.kspgenprocessor.data

data class EventData(
    val name: String,
    var packageName: String = "",
    var imports: List<String> = emptyList(),
    var eventName: String = "",
    var eventResource: Int
)

data class ClassEventData(
    val name: String,
    var packageName: String = "",
    var imports: List<String> = emptyList(),
    var events: List<EventData> = emptyList()
)