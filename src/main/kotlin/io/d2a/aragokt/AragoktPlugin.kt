package io.d2a.aragokt

import io.d2a.aragokt.border.BorderTask
import io.d2a.aragokt.coal.CoalListener
import io.d2a.aragokt.coal.CoalType
import io.d2a.aragokt.commands.PrivilegesCommand
import io.d2a.aragokt.devnull.DevNullItem
import io.d2a.aragokt.devnull.DevNullListener
import io.d2a.aragokt.extension.requiresPermission
import io.d2a.aragokt.extension.success
import io.d2a.aragokt.flair.LuckPermsLiveUpdateExtension
import io.d2a.aragokt.flair.LuckPermsPrefixSuffixProvider
import io.d2a.aragokt.flair.NametagService
import io.d2a.aragokt.flair.PrefixSuffixProvider
import io.d2a.aragokt.flair.listener.ChatListener
import io.d2a.aragokt.flair.listener.JoinQuitListener
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.luckperms.api.LuckPerms
import org.bukkit.plugin.java.JavaPlugin
import java.io.Closeable
import java.util.logging.Level

class AragoktPlugin : JavaPlugin() {

    private var luckPermsLiveUserUpdate: LuckPermsLiveUpdateExtension? = null
    private var borderTask: BorderTask? = null

    override fun onEnable() {
        val luckPerms = server.servicesManager.load(LuckPerms::class.java)
            ?: return disableWithError("LuckPerms not found, disabling plugin")

        borderTask = BorderTask(plugin = this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            logger.info("Registering commands...")
            commands.registrar().apply {
                register(PrivilegesCommand(luckPerms).build(), "Gain Admin Privileges")
                register(
                    Commands.literal("testadvance")
                    .requiresPermission("aragokt.testadvance")
                    .then(
                        Commands.literal("check")
                        .requiresPermission("aragokt.testadvance.check")
                        .executes { ctx ->
                            borderTask?.run()
                            ctx.success("Checked border advancement")
                        }
                        .build())
                    .then(
                        Commands.literal("advance")
                        .requiresPermission("aragokt.testadvance.advance")
                        .executes { ctx ->
                            borderTask?.advance()
                            ctx.success("Advanced border")
                        }
                        .build())
                    .build())
            }
        }

        registerCoalFeature()
        registerDevNullFeature()
        registerFlairFeature(luckPerms)


        logger.info("Enabled AragoktPlugin")
    }

    override fun onDisable() {
        closeQuietly(luckPermsLiveUserUpdate, "LuckPermsLiveUpdateExtension")
        closeQuietly(borderTask, "BorderTask")

        logger.info("Disabled AragoktPlugin")
    }

    ///

    fun registerCoalFeature() {
        logger.info("Registering coal item and listener...")
        CoalType.registerRecipes(this)

        server.pluginManager.apply {
            registerEvents(CoalListener(), this@AragoktPlugin)
        }
    }

    fun registerDevNullFeature() {
        logger.info("Registering dev/null item and listener...")
        DevNullItem.registerRecipe(this)

        server.pluginManager.apply {
            registerEvents(DevNullListener(), this@AragoktPlugin)
        }
    }

    fun registerFlairFeature(luckPerms: LuckPerms) {
        logger.info("Registering flair listeners and services...")

        val scoreboard = server.scoreboardManager.mainScoreboard

        val prefixSuffixProvider: PrefixSuffixProvider = LuckPermsPrefixSuffixProvider(luckPerms)
        val nametagService = NametagService(logger, scoreboard, prefixSuffixProvider)

        server.apply {
            // register listeners
            pluginManager.apply {
                registerEvents(ChatListener(prefixSuffixProvider), this@AragoktPlugin)
                registerEvents(JoinQuitListener(nametagService), this@AragoktPlugin)
            }
        }

        // subscribe to rank / prefix / suffix changes
        luckPermsLiveUserUpdate = LuckPermsLiveUpdateExtension(this, logger, nametagService, luckPerms).apply {
            subscribeFlairUserLiveUpdates()
            subscribeFlairGroupLiveUpdates()
        }

        // apply nametags to online players (in case of reload)
        server.onlinePlayers.forEach(nametagService::applyTo)
    }

    ///

    private fun disableWithError(message: String) {
        logger.severe(message)
        server.pluginManager.disablePlugin(this)
    }

    private fun closeQuietly(resource: Closeable?, name: String) {
        resource?.runCatching { close() }
            ?.onFailure { logger.log(Level.WARNING, "Failed to close $name", it) }
    }

}
