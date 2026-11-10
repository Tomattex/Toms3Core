package io.github.Cherryh4ck.toms3Core.Listeners

import io.github.Cherryh4ck.toms3Core.Toms3Core
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.vehicle.VehicleMoveEvent

class PlayerNetherRoofListener(private val plugin: Toms3Core) : Listener {
    // Cheap patch for VClip travel exploit
    // also prevents players from being killed by the void damage thing on the nether roof

    private fun doChecks(to: Location, player: Player): Boolean {
        if (to.y > 127 && plugin.block_nether_roof) {
            player.sendMessage(plugin.minimessage.deserialize("<gold>${plugin.prefix} Nether Roof is currently disabled."))
            plugin.logToConsole("<yellow>${player.name} tried to access Nether Roof.")
            return true
        }

        if (to.y < 0 && plugin.patch_vclip_exploit) {
            val isElytraEnabled = player.inventory.chestplate?.type == Material.ELYTRA && plugin.patch_vclip_exploit_mode == "ELYTRA"
            val isStrictEnabled = plugin.patch_vclip_exploit_mode == "STRICT"

            if (isElytraEnabled || isStrictEnabled) {
                player.sendMessage(plugin.minimessage.deserialize("<gold>${plugin.prefix} This travel exploit is currently disabled."))
                plugin.logToConsole("<yellow>${player.name} tried to execute the VClip exploit.")
                return true
            }
        }

        return false
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val from = event.from
        val to = event.to
        if (from.blockY == to.blockY) return

        val player = event.player
        if (player.world.environment != World.Environment.NETHER) return

        if (doChecks(to, player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val to = event.to
        if (to.world.environment != World.Environment.NETHER) return

        if (doChecks(to, event.player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onVehicleMove(event: VehicleMoveEvent) {
        val vehicle = event.vehicle
        val from = event.from
        val to = event.to ?: return
        if (from.blockY == to.blockY) {
            return
        }

        val world = vehicle.world
        if (world.environment == World.Environment.NETHER) {
            if (to.y > 127 && plugin.block_nether_roof){
                vehicle.remove()
            }
            else if (to.y < 0  && plugin.patch_vclip_exploit) {
                vehicle.remove()
            }
        }
    }
}