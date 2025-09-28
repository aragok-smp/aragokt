package io.d2a.aragokt.border

import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.IOException
import java.time.LocalDateTime
import java.time.LocalTime


class BorderTask(
    private val plugin: Plugin,
) {

    val runTime: LocalTime = LocalTime.of(2, 20) // 6 PM server time
    val advanceAmount = 1000.0 // Amount to advance the border by each day
    val advanceTime = 60L // Time in seconds for the border to advance

    fun init() {
        plugin.logger.info("World border will advance every day at lalalal server time.")
        plugin.server.scheduler.runTaskTimer(plugin, advanceBorderTask(), 0, 20)
    }

    fun advanceBorderTask(): Runnable = Runnable {
        val stateFile = plugin.dataFolder.resolve("state.yml")

        // Create the file if it doesn't exist
        if (!stateFile.exists()) {
            try {
                stateFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val config = YamlConfiguration.loadConfiguration(stateFile)
        val lastAdvancedTime =
            config.getString("borders.lastAdvancedTime")
                ?.let { LocalDateTime.parse(it) }
                ?: LocalDateTime.now().minusDays(1)

        if (LocalTime.now().isBefore(runTime) || lastAdvancedTime.dayOfYear == LocalDateTime.now().dayOfYear) {
            // Not time yet, or already advanced today
            return@Runnable
        }
        plugin.logger.info("Advancing borders for each world...")
        plugin.server.worlds
            .forEach { world ->
                plugin.logger.info("Advancing borders for ${world.name}")
                advanceBorder(world)
            }
        config.set("borders.lastAdvancedTime", LocalDateTime.now().toString())
        config.save(stateFile)
    }

    fun advanceBorder(world: World) {
        val border = world.worldBorder
        val currentBorderSize = border.size
        val newSize = currentBorderSize + advanceAmount

        world.sendMessage(Component.text("Advancing world border from $currentBorderSize to $newSize."))
        border.setSize(newSize, advanceTime)
    }
}