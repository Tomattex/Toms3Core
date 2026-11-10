package io.github.Cherryh4ck.toms3Core.Listeners

import io.github.Cherryh4ck.toms3Core.Toms3Core
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlaceListener(private val plugin: Toms3Core) : Listener {
    // This is only for shulkers and any class of chests
    @EventHandler

    fun onBlockPlace(event: BlockPlaceEvent) {
        val block = event.block
        val chunk = block.chunk

        if (isATileEntity(block) && plugin.chunklimits_enable){
            val count = chunk.getTileEntities(false).size
            val maxCount = plugin.tile_entities_limit

            if (count > maxCount){
                event.isCancelled = true

                val player = event.player

                if (plugin.tile_entities_log){
                    val chunkX = chunk.x
                    val chunkZ = chunk.z
                    val regionX = chunkX shr 5
                    val regionZ = chunkZ shr 5
                    val chunkFileName = "r.$regionX.$regionZ.mca"
                    plugin.logToConsole("<yellow>Tile entity limit exceeded by ${player.name} at X: ${block.location.block.x} Y: ${block.location.block.y} Z: ${block.location.block.z} (World: ${chunk.world.name}) (Chunk: $chunkFileName)")
                }

                player.sendMessage(plugin.minimessage.deserialize("<gold>${plugin.prefix} Tile entities are limited to $maxCount per chunk."))
            }
            return
        }
    }

    fun isATileEntity(block: Block): Boolean {
        return block.getState(false) is TileState
    }
}