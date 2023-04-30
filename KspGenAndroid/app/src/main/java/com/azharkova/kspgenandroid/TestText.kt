package com.azharkova.kspgenandroid

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.azharkova.annotations.ToComposable
import com.azharkova.annotations.ToView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.azharkova.annotations.ClickModifier
import com.azharkova.annotations.PropertyModifier

@ToComposable
class TestText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    @PropertyModifier
    var testProperty: String = ""

    @ClickModifier
    fun setClick() {
        this.setOnClickListener {  }
    }

    fun setText(text: String) {

    }
}
