package net.rx.modules

import net.minecraft.text.*
import net.minecraft.util.Formatting

fun text(block: TextBuilder.() -> Unit) = TextBuilder(block).build()


//class StyleBuilder {
//    var color: TextColor? = null
//    var bold: Boolean? = null
//    var italic: Boolean? = null
//    var strikethrough: Boolean? = null
//    var underline: Boolean? = null
//    var reset: Boolean? = null
//
//    fun build(): Style {
//        return Style()
//    }
//}


class TextBuilder(block: TextBuilder.() -> Unit) {
    var root: MutableText = MutableText.of(TextContent.EMPTY)
    private var children: MutableList<Text> = mutableListOf()

    val NEW_LINE: MutableText
        get() = MutableText.of(LiteralTextContent("\n"))

    init {
        block()
    }

    operator fun Formatting.plus(format: Formatting): Style {
        return Style.EMPTY.withFormatting(this).withFormatting(format)
    }

    operator fun Style.plus(format: Formatting): Style {
        return this.withFormatting(format)
    }

    operator fun Style.plus(style: Style): Style {
        return style.withParent(this)
    }

    infix fun MutableText.styled(formatting: Formatting): MutableText {
        return this.formatted(formatting)
    }

    infix fun MutableText.styled(style: Style): MutableText {
        return this.setStyle(style)
    }

    infix fun String.styled(format: Formatting): MutableText {
        return root.append(MutableText.of(LiteralTextContent(this)).formatted(format))
    }

    infix fun String.styled(style: Style): Text {
        return root.append(MutableText.of(LiteralTextContent(this)).setStyle(style))
    }

    infix fun String.styled(hex: Int): Text {
        return root.append(MutableText.of(LiteralTextContent(this)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(hex))))
    }

//    infix fun String.styled(block: StyleBuilder.() -> Unit): MutableText {
//        return root.append(LiteralText(this).formatted(format))
//    }

    infix fun String.onClick(block: ClickEventBuilder.() -> Unit): Text {
        return root.append(MutableText.of(LiteralTextContent(this)).setStyle(Style.EMPTY.withClickEvent(
            ClickEventBuilder().apply{ block }.build()
        )))
    }

    /**
     * Nested control
     */
    operator fun String.invoke(block: TextBuilder.() -> Unit): Text {
        return root.append(
            MutableText.of(LiteralTextContent(this)).append(TextBuilder(block).build())
        )
    }

    /**
     * Use only when alone, returns nothing
     */
    operator fun String.unaryPlus() {
        children.add(MutableText.of(LiteralTextContent(this)))
    }

    operator fun String.unaryMinus() {
        children.add(MutableText.of(LiteralTextContent(this)))
    }

//    infix fun String.onHover(block: Unit.() -> Unit): MutableText {
//        return LiteralText(this).setStyle(Style.EMPTY.withHoverEvent(
//            HoverEventBuilder().apply{ block }.build()
//        ))
//    }

    infix fun styled(format: Formatting) {
        root.formatted(format)
    }

    infix fun styled(style: Style) {
        root.styled(style)
    }

    fun build(): MutableText {
        children.forEach { root.append(it) }
        return root
    }
}

interface BaseBuilder<T> {
    fun build(): T
}

class ClickEventBuilder : BaseBuilder<ClickEvent> {
    var action: ClickEvent.Action? = null
    var value: String = ""

    override fun build() = ClickEvent(action, value)
}

class HoverEventBuilder<T> : BaseBuilder<HoverEvent> {
    var action: HoverEvent.Action<T>? = null
    var content: T? = null

    override fun build() = HoverEvent(action, content)
}

val test = text {
    "hello world" styled Formatting.BOLD + Formatting.GREEN
    NEW_LINE
    "a more complex string" {
        NEW_LINE
        "testing" onClick {
            action = ClickEvent.Action.RUN_COMMAND
            value = "/git status"
        }
    }
    -"testing"
}

//class HoverEventBuilder {
//    var action: <T> HoverEvent.Action<T>? = null
//    var contents: <T>? = null
//
//    fun build(): HoverEvent {
//        return HoverEvent(action, contents)
//    }
//}
