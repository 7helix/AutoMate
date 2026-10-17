package com.automate.app.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

sealed interface Block
data class Heading(val level: Int, val text: String) : Block
data class Para(val text: String) : Block
data class Bullet(val marker: String, val text: String) : Block
data class Note(val text: String) : Block

/** Tiny markdown reader: #, ##, ###, lists, numbered lists, > notes, **bold**, `code`. The first "# " is the title and is skipped. */
fun parseMarkdown(src: String): List<Block> {
    val out = mutableListOf<Block>()
    val para = StringBuilder()
    var titleSkipped = false
    val numbered = Regex("^\\d+\\. .*")

    fun flush() {
        if (para.isNotBlank()) out.add(Para(para.toString().trim()))
        para.clear()
    }

    for (raw in src.lines()) {
        val l = raw.trimEnd()
        when {
            l.isBlank() -> flush()
            l.startsWith("### ") -> { flush(); out.add(Heading(3, l.drop(4))) }
            l.startsWith("## ") -> { flush(); out.add(Heading(2, l.drop(3))) }
            l.startsWith("# ") -> {
                flush()
                if (titleSkipped) out.add(Heading(1, l.drop(2)))
                titleSkipped = true
            }
            l.startsWith("> ") -> { flush(); out.add(Note(l.drop(2))) }
            l.startsWith("- ") || l.startsWith("* ") -> { flush(); out.add(Bullet("•", l.drop(2))) }
            numbered.matches(l) -> { flush(); out.add(Bullet(l.substringBefore(". ") + ".", l.substringAfter(". "))) }
            else -> {
                if (para.isNotEmpty()) para.append(' ')
                para.append(l.trim())
            }
        }
    }
    flush()
    return out
}

fun annotate(text: String, codeBg: Color): AnnotatedString = buildAnnotatedString {
    val sb = StringBuilder()
    var bold = false
    var code = false

    fun flush() {
        if (sb.isEmpty()) return
        val style = SpanStyle(
            fontWeight = if (bold) FontWeight.Bold else null,
            fontFamily = if (code) FontFamily.Monospace else null,
            background = if (code) codeBg else Color.Unspecified
        )
        withStyle(style) { append(sb.toString()) }
        sb.clear()
    }

    var i = 0
    while (i < text.length) {
        if (!code && text.startsWith("**", i)) { flush(); bold = !bold; i += 2; continue }
        if (text[i] == '`') { flush(); code = !code; i++; continue }
        sb.append(text[i])
        i++
    }
    flush()
}
