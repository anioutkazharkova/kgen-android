package com.azharkova.kspgenandroid

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.azharkova.annotations.ToView

data class TextValues(val primaryText: String, val secondaryText: String)
@ToView
@Composable
fun Test(
    textValues: TextValues,
    isVisible: Boolean,
    footer: String,
    onClick: () -> Unit,
    onText:(Int,String)->Unit,
) {
    if (isVisible) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = textValues.primaryText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h1,
            )
            Text(
                text = textValues.secondaryText,
                style = MaterialTheme.typography.body1,
            )
            Text(text = footer, modifier = Modifier.clickable {
                onClick.invoke()
            })
        }
    }
}