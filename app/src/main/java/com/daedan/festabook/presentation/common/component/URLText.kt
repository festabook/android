package com.daedan.festabook.presentation.common.component

import android.util.Patterns
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.daedan.festabook.presentation.theme.FestabookColor

@Composable
fun URLText(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val uriHandler = LocalUriHandler.current
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val linkedText =
        buildAnnotatedString {
            append(text)
            val urlPattern = Patterns.WEB_URL
            val matcher = urlPattern.matcher(text)
            while (matcher.find()) {
                addStyle(
                    style =
                        SpanStyle(
                            color = FestabookColor.gray500,
                            textDecoration = TextDecoration.Underline,
                        ),
                    start = matcher.start(),
                    end = matcher.end(),
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = matcher.group(),
                    start = matcher.start(),
                    end = matcher.end(),
                )
            }
        }
    Text(
        text = linkedText,
        modifier =
            modifier.pointerInput(Unit) {
                detectTapGestures {
                    onClick()
                    layoutResult?.let { result ->
                        val position = result.getOffsetForPosition(it)
                        linkedText
                            .getStringAnnotations("URL", position, position)
                            .firstOrNull()
                            ?.let { annotation ->
                                uriHandler.openUri(annotation.item)
                            }
                    }
                }
            },
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = {
            layoutResult = it
            onTextLayout(it)
        },
        style = style,
    )
}
