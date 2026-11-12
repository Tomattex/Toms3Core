package io.github.Cherryh4ck.toms3Core.Commands

import io.github.Cherryh4ck.toms3Core.DiscordWebhook
import io.github.Cherryh4ck.toms3Core.Toms3Core
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Dupe(private val plugin : Toms3Core) : TabExecutor {
    val drunkness = PotionEffect(PotionEffectType.NAUSEA, 30 * 20, 0, false, true, true)
    val darkness = PotionEffect(PotionEffectType.DARKNESS, 60 * 20, 0, false, true, true)
    val poison = PotionEffect(PotionEffectType.POISON, 120 * 20, 2, false, true, true)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            val playerDataCache = plugin.playerDataFile(sender.name) ?: return true
            val config = YamlConfiguration.loadConfiguration(playerDataCache)
            val alreadyFallen = config.getBoolean("fallen-for-dupe")

            if (alreadyFallen) {
                sender.sendMessage(plugin.minimessage.deserialize("${plugin.prefix} <red>Don't be an idiot, newfag.</red>"))
                return true
            }

            if (plugin.discordWebhook.toString().isNotEmpty()){
                Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                    val message = plugin.dupe_webhook_message.toString().replace("%username%", sender.name)
                    DiscordWebhook.sendDiscordWebhook(plugin.discordWebhook.toString(), message)
                })
            }

            sender.sendMessage(plugin.minimessage.deserialize("<gray>popbob has joined the server.</gray>"))
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                sender.sendMessage("<${sender.name}> popbob my coords are ${sender.x.toInt().toString()} ${sender.z.toInt().toString()} come get me please")
            }, 20L)
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                sender.sendMessage("<popbob>  ur fucked, im coming")
                val health = sender.health - 1.0
                sender.damage(health)
                sender.addPotionEffect(drunkness)
                sender.addPotionEffect(darkness)
                sender.addPotionEffect(poison)
                sender.playSound(sender.location, Sound.AMBIENT_CAVE, 2.0f, 0.5f)
                sender.playSound(sender.location, Sound.ITEM_TOTEM_USE, 2.0f, 0.5f)
            }, 50L)
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                val world = Bukkit.getWorld(sender.world.name)
                val location = Location(world, sender.location.x, sender.location.y, sender.location.z)
                world?.strikeLightning(location)
                sender.sendMessage(plugin.minimessage.deserialize("<red>You have been added to the Hall of Shame.</red>"))
                sender.playSound(sender.location, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.15f)
                Bukkit.getOnlinePlayers()
                    .filter { it != sender }
                    .forEach { player ->
                        player.sendMessage(plugin.minimessage.deserialize("<gray>${sender.name} has been added to the <light_purple>Hall of Shame</light_purple>."))
                }
            }, 100L)

            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                config.set("fallen-for-dupe", true)
                try {
                    config.save(playerDataCache)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            })
        }
        else{
            plugin.logToConsole("<red>You must be a player to execute this command.")
        }
        return true
    }

    override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, p3: Array<out String> ): List<String?>? {
        return emptyList()
    }
}