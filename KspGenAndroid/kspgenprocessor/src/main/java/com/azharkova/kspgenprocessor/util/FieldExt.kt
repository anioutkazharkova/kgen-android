package com.azharkova.kspgenprocessor.util

import com.azharkova.annotations.Field
import com.azharkova.kspgenprocessor.data.FieldData

fun FieldData.statement():String {
    return when (this.type) {
         Field.TEXT -> " as? TextView)?.text = model.${this.valueName}"
        Field.IMAGE -> " as? ImageView)?.load(model.${this.valueName})"
    }
}