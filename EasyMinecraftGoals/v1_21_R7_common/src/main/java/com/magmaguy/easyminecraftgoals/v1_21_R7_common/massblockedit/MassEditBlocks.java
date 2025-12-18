package com.magmaguy.easyminecraftgoals.v1_21_R7_common.massblockedit;

import com.magmaguy.easyminecraftgoals.v1_21_R7_common.CraftBukkitBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

public class MassEditBlocks {
    public static void setBlockInNativeDataPalette(World world, int x, int y, int z, BlockData blockData, boolean applyPhysics) {
        ServerLevel nmsWorld = CraftBukkitBridge.getServerLevel(world);
        BlockPos blockPos = new BlockPos(x, y, z);
        LevelChunk nmsChunk = nmsWorld.getChunkAt(blockPos);
        BlockState blockState = CraftBukkitBridge.getBlockState(blockData);

        int sectionY = Math.floorDiv(y, 16);
        LevelChunkSection chunkSection = nmsChunk.getSections()[sectionY - nmsWorld.getMinSectionY()];

        if (applyPhysics) {
            chunkSection.setBlockState(x & 15, y & 15, z & 15, blockState);
        } else {
            chunkSection.setBlockState(x & 15, y & 15, z & 15, blockState, false);
        }
    }
}
