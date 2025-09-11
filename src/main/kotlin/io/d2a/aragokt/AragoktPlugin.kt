package io.d2a.aragokt

import io.d2a.aragokt.commands.PrivilegesCommand
import io.d2a.aragokt.flair.*
import io.d2a.aragokt.flair.listener.ChatListener
import io.d2a.aragokt.flair.listener.JoinQuitListener
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.luckperms.api.LuckPerms
import net.luckperms.api.event.EventSubscription
import net.luckperms.api.event.group.GroupDataRecalculateEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.plugin.java.JavaPlugin

class AragoktPlugin : JavaPlugin() {

    private var luckPermsLiveUserUpdateSubscription: EventSubscription<UserDataRecalculateEvent>? = null
    private var luckPermsLiveGroupUpdateSubscription: EventSubscription<GroupDataRecalculateEvent>? = null

    override fun onEnable() {
        val luckPerms = server.servicesManager.load(LuckPerms::class.java)
            ?: return disableWithError("LuckPerms not found, disabling plugin")

        val scoreboard = server.scoreboardManager.mainScoreboard

        val prefixSuffixProvider: PrefixSuffixProvider = LuckPermsPrefixSuffixProvider(luckPerms)
        val nametagService = NametagService(logger, scoreboard, prefixSuffixProvider)

        server.pluginManager.apply {
            registerEvents(ChatListener(prefixSuffixProvider), this@AragoktPlugin)
            registerEvents(JoinQuitListener(nametagService), this@AragoktPlugin)
        }

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            logger.info("Registering commands...")
            commands.registrar().apply {
                register(PrivilegesCommand(luckPerms).build(), "Gain Admin Privileges")
            }
        }

        // subscribe to rank / prefix / suffix changes
        luckPermsLiveUserUpdateSubscription = luckPerms.subscribeFlairUserLiveUpdates(this, nametagService)
        luckPermsLiveGroupUpdateSubscription = luckPerms.subscribeFlairGroupLiveUpdates(this, nametagService)

        // apply nametags to online players (in case of reload)
        server.onlinePlayers.forEach(nametagService::applyTo)

        logger.info("Enabled AragoktPlugin")
    }

    override fun onDisable() {
        luckPermsLiveUserUpdateSubscription?.close()
        luckPermsLiveGroupUpdateSubscription?.close()
    }

    private fun disableWithError(message: String) {
        logger.severe(message)
        server.pluginManager.disablePlugin(this)
    }

}
