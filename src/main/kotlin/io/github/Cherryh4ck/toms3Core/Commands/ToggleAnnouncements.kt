package io.github.Cherryh4ck.toms3Core.Commands

import io.github.Cherryh4ck.toms3Core.Toms3Core
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player

class ToggleAnnouncements(private val plugin : Toms3Core) : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                val playerData = plugin.playerDataFile(sender.name) ?: return@Runnable
                val config = YamlConfiguration.loadConfiguration(playerData)
                val toggle = config.getBoolean("toggle-announcements")
                config.set("toggle-announcements", !toggle)
                try {
                    config.save(playerData)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    val message: Component
                    if (toggle) {
                        plugin.haveAnnouncementsDisabled.add(sender.uniqueId)
                        message = plugin.minimessage.deserialize("${plugin.prefix} ${plugin.toggleannouncements_message_off}")
                    } else {
                        plugin.haveAnnouncementsDisabled.remove(sender.uniqueId)
                        message = plugin.minimessage.deserialize("${plugin.prefix} ${plugin.toggleannouncements_message_on}")
                    }
                    sender.sendMessage(message)
                })
            })
        }
        else{
            plugin.logToConsole("<red>You must be a player to execute this command.")
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        return emptyList()
    }
}