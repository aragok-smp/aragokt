package io.d2a.aragokt.sleep

import io.papermc.paper.event.player.PlayerDeepSleepEvent
import net.kyori.adventure.sound.Sound.Emitter
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import kotlin.math.ceil

class SleepListener : Listener {

    val playerData: MutableMap<UUID, Long> = mutableMapOf()
    val afkTimeout: Long = 15 * 60 * 1000 // 15 minutes in milliseconds

    fun isPlayerAfk(playerId: UUID): Boolean {
        val lastActiveTime = playerData[playerId] ?: return true
        return (System.currentTimeMillis() - lastActiveTime) > afkTimeout
    }

    fun advanceNightIfNeeded(world: World) {
        val sleepingPlayers = world.players.count { it.isSleeping }
        val requiredPlayers = ceil(world.players.count { !isPlayerAfk(it.uniqueId) } / 2.0).toInt()
        if (sleepingPlayers >= requiredPlayers) {
            world.time = 0 // set time to day
            world.weatherDuration = 0 // clear weather
            world.isThundering = false
            world.sendActionBar(
                Component.text("Night skipped! Good morning!")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            world.playSound(
                sound()
                    .type(Sound.ENTITY_PLAYER_LEVELUP)
                    .volume(1f)
                    .build(), Emitter.self()
            )
        } else {
            val remaining = requiredPlayers - sleepingPlayers
            world.sendActionBar(
                Component.text("$remaining more player(s) need to sleep to skip the night.")
                    .color(NamedTextColor.YELLOW)
                    .decorate(TextDecoration.BOLD)
            )
        }
    }

    @EventHandler
    fun onPlayerJoin(event : PlayerJoinEvent) {
        val player = event.player
        playerData[player.uniqueId] = System.currentTimeMillis()
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val from = event.from
        val to = event.to
        if (from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ) {
            playerData[player.uniqueId] = System.currentTimeMillis()
        }
    }

    @EventHandler
    fun onPlayerDeepSleep(event: PlayerDeepSleepEvent) {
        advanceNightIfNeeded(event.player.world)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        advanceNightIfNeeded(event.player.world)
        playerData.remove(event.player.uniqueId)
    }

}