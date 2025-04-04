package com.magmaguy.easyminecraftgoals.v1_19_R3.massblockedit;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;

public class MassEditBlocks {
    public static void setBlockInNativeDataPalette(World world, int x, int y, int z, BlockData blockData, boolean applyPhysics) {
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        BlockPos blockPos = new BlockPos(x, y, z);
        LevelChunk nmsChunk = nmsWorld.getChunkAt(blockPos);
        BlockState blockState = ((CraftBlockData) blockData).getState();

        int sectionY = Math.floorDiv(y, 16);
        LevelChunkSection chunkSection = nmsChunk.getSections()[sectionY - nmsWorld.getMinSection()];

        if (applyPhysics) {
            chunkSection.setBlockState(x & 15, y & 15, z & 15, blockState);
        } else {
            chunkSection.setBlockState(x & 15, y & 15, z & 15, blockState, false);
        }
    }
}
