package io.d2a.aragokt

import io.d2a.aragokt.coal.CoalListener
import io.d2a.aragokt.coal.CoalType
import io.d2a.aragokt.commands.PrivilegesCommand
import io.d2a.aragokt.flair.LuckPermsLiveUpdateExtension
import io.d2a.aragokt.flair.LuckPermsPrefixSuffixProvider
import io.d2a.aragokt.flair.NametagService
import io.d2a.aragokt.flair.PrefixSuffixProvider
import io.d2a.aragokt.flair.listener.ChatListener
import io.d2a.aragokt.flair.listener.JoinQuitListener
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.luckperms.api.LuckPerms
import org.bukkit.plugin.java.JavaPlugin

class AragoktPlugin : JavaPlugin() {

    private var luckPermsLiveUserUpdate: LuckPermsLiveUpdateExtension? = null

    override fun onEnable() {
        val luckPerms = server.servicesManager.load(LuckPerms::class.java)
            ?: return disableWithError("LuckPerms not found, disabling plugin")


        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            logger.info("Registering commands...")
            commands.registrar().apply {
                register(PrivilegesCommand(luckPerms).build(), "Gain Admin Privileges")
            }
        }

        registerCoalFeature()
        registerFlairFeature(luckPerms)

        logger.info("Enabled AragoktPlugin")
    }

    override fun onDisable() {
        luckPermsLiveUserUpdate?.close()
    }

    ///

    fun registerCoalFeature() {
        logger.info("Registering coal item and listener...")
        CoalType.registerRecipes(this)

        server.pluginManager.apply {
            registerEvents(CoalListener(), this@AragoktPlugin)
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

}
