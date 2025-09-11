package io.d2a.aragokt.flair

import io.d2a.aragokt.commands.PrivilegesCommand
import net.luckperms.api.LuckPerms
import net.luckperms.api.event.group.GroupDataRecalculateEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

fun LuckPerms.subscribeFlairUserLiveUpdates(plugin: Plugin, service: NametagService) =
    this.eventBus.subscribe(plugin, UserDataRecalculateEvent::class.java) { event ->
        val player = plugin.server.getPlayer(event.user.uniqueId) ?: return@subscribe
        if (!player.isOnline) return@subscribe

        plugin.server.scheduler.runTask(plugin) { _: BukkitTask ->
            service.applyTo(player)
        }
    }

fun LuckPerms.subscribeFlairGroupLiveUpdates(plugin: Plugin, service: NametagService) =
    this.eventBus.subscribe(plugin, GroupDataRecalculateEvent::class.java) { event ->
        // we only care about the super-user group changes
        if (event.group.name != PrivilegesCommand.SUPER_USER_GROUP_NAME) {
            return@subscribe
        }
        plugin.server.scheduler.runTask(plugin) { _: BukkitTask ->
            plugin.server.onlinePlayers.forEach(service::applyTo)
        }
    }