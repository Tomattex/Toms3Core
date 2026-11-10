package io.github.Cherryh4ck.toms3Core.Commands

import io.github.Cherryh4ck.toms3Core.Toms3Core
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

class Playtime(private val plugin: Toms3Core) : TabExecutor {
    val minimessage = MiniMessage.miniMessage()

    fun validateUsername(username: String): Boolean {
        if (!plugin.usernameValidationRegexEnabled) {
            val regex = Regex(plugin.usernameValidationRegex)
            return regex.matches(username)
        }
        else {
            return true
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val targetUser : String

        if (sender is Player){
            targetUser = if (args.isEmpty()) {
                sender.name
            } else{
                args[0]
            }
        }
        else{
            if (args.isEmpty()){
                plugin.logToConsole("<red>You need to specify a player to use this command.")
                return true
            }
            else {
                targetUser = args[0]
            }
        }

        if (!validateUsername(targetUser)) {
            sender.sendMessage(minimessage.deserialize("${plugin.prefix} <red>$targetUser is not a valid player name.</red>"))
            return true
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val playerDataCache = plugin.playerDataFile(targetUser) ?: return@Runnable
            val offlineplayer = if (!playerDataCache.exists()) {
                plugin.logToConsole("<red>$targetUser's UUID file doesn't exist.")
                Bukkit.getOfflinePlayer(targetUser)
            }
            else{
                val config = YamlConfiguration.loadConfiguration(playerDataCache)
                val configUuid = config.getString("uuid")
                if (configUuid == null) {
                    plugin.logToConsole("<yellow>UUID value is null.")
                    Bukkit.getOfflinePlayer(targetUser)
                }
                else{
                    val uuid = java.util.UUID.fromString(configUuid)
                    plugin.logToConsole("<green>UUID file found: ${uuid.toString()}")
                    Bukkit.getOfflinePlayer(uuid)
                }
            }
            if (!offlineplayer.isOnline && offlineplayer.firstPlayed == 0L){
                sender.sendMessage(minimessage.deserialize("${plugin.prefix} <red>$targetUser has never entered the server or is not in the server cache.</red>"))
                return@Runnable
            }

            val statTicks = offlineplayer.getStatistic(Statistic.PLAY_ONE_MINUTE).toLong()
            val ms = statTicks * 50
            val result = ms.milliseconds
            val resultHs = "%.2f".format(result.toDouble(DurationUnit.HOURS))

            val mensaje = if (offlineplayer.name != sender.name){
                minimessage.deserialize("<gold>${plugin.prefix} <bold>${offlineplayer.name}</bold> has a playtime of <bold>$result</bold> (<bold>${resultHs}hs</bold>).</gold>")
            }
            else{
                minimessage.deserialize("<gold>${plugin.prefix} You have a playtime of <bold>$result</bold> (<bold>${resultHs}hs</bold>).</gold>")
            }
            sender.sendMessage(mensaje)
        })

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        return if (args.size == 1){
            Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter { it.startsWith(args[0], ignoreCase = true) }
        }
        else{
            emptyList()
        }
    }
}