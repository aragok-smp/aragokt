package io.d2a.aragokt.flair

import net.luckperms.api.LuckPerms
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

fun LuckPerms.subscribeFlairLiveUpdates(plugin: Plugin, service: NametagService) =
    this.eventBus.subscribe(plugin, UserDataRecalculateEvent::class.java) { event ->
        val player = plugin.server.getPlayer(event.user.uniqueId) ?: return@subscribe
        if (!player.isOnline) return@subscribe

        plugin.server.scheduler.runTask(plugin) { _: BukkitTask ->
            service.applyTo(player)
        }
    }
