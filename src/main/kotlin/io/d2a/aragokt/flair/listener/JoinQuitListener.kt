package io.d2a.aragokt.flair.listener

import io.d2a.aragokt.flair.NametagService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinQuitListener(
    val service: NametagService
) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) =
        service.applyTo(event.player)

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) =
        service.remove(event.player)

}