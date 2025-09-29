package io.d2a.aragokt.sleep

import io.papermc.paper.event.player.PlayerDeepSleepEvent
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import kotlin.math.ceil

class SleepListener : Listener {

    val playerData: MutableMap<UUID, Long> = mutableMapOf()
    val afkTimeout: Long = 15 * 60 * 1000 // 15 minutes in milliseconds

    fun isPlayerAfk(playerId: UUID): Boolean {
        val lastActiveTime = playerData[playerId] ?: return false
        return (System.currentTimeMillis() - lastActiveTime) > afkTimeout
    }

    fun advanceNightIfNeeded(world: World) {
        val sleepingPlayers = world.players.count { it.isSleeping }
        val requiredPlayers = ceil(world.players.count { isPlayerAfk(it.uniqueId) } / 2.0).toInt()
        if (sleepingPlayers >= requiredPlayers) {
            world.time = 0 // set time to day
            world.weatherDuration = 0 // clear weather
            world.isThundering = false
            world.players.forEach { player ->
                player.sendMessage("§a§lNight skipped! Good morning!")
                player.playSound(player.location, "minecraft:entity.player.levelup", 1.0f, 1.0f)
            }
        } else {
            val remaining = requiredPlayers - sleepingPlayers
            world.players.forEach { player ->
                player.sendMessage("§e§l$remaining more player(s) need to sleep to skip the night.")
            }
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        playerData[player.uniqueId] = System.currentTimeMillis()
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        playerData[player.uniqueId] = System.currentTimeMillis()
    }

    @EventHandler
    fun onPlayerDeepSleep(event: PlayerDeepSleepEvent) {
        advanceNightIfNeeded(event.player.world)
    }


    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        advanceNightIfNeeded(event.player.world)
    }

    @EventHandler
    fun onPlayerKick(event: PlayerKickEvent) {
        advanceNightIfNeeded(event.player.world)
    }
}