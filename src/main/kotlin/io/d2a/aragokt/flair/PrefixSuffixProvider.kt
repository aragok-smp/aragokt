package io.d2a.aragokt.flair

import org.bukkit.entity.Player

interface PrefixSuffixProvider {

    fun prefix(player: Player): String
    fun suffix(player: Player): String

}