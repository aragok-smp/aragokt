package io.d2a.aragokt.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

val MINI_MESSAGE: MiniMessage = MiniMessage.miniMessage()
val LEGACY_SERIALIZER: LegacyComponentSerializer = LegacyComponentSerializer.legacyAmpersand()

enum class TextFormat {
    LEGACY, MINI_MESSAGE
}

fun String.toComponent(format: TextFormat): Component = when (format) {
    TextFormat.LEGACY -> LEGACY_SERIALIZER.deserialize(this)
    TextFormat.MINI_MESSAGE -> MINI_MESSAGE.deserialize(this)
}

fun String.toComponent(): Component =
    if (this.indexOf('<') <= 0) toComponent(TextFormat.MINI_MESSAGE)
    else toComponent(TextFormat.LEGACY)

fun Component.namedColor(): NamedTextColor? =
    this.color()?.let { NamedTextColor.nearestTo(it) }
