package com.magmaguy.easyminecraftgoals.v1_21_R2.massblockedit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R2.block.data.CraftBlockData;

public class MassEditBlocks {
    public static void setBlockInNativeDataPalette(World world, int x, int y, int z, BlockData blockData, boolean applyPhysics) {
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        BlockPos blockPos = new BlockPos(x, y, z);
        LevelChunk nmsChunk = nmsWorld.getChunkAt(blockPos);
        BlockState blockState = ((CraftBlockData)blockData).getState();

        int sectionY = Math.floorDiv(y, 16);
        int sectionIndex = sectionY - nmsWorld.getMinSectionY();
        LevelChunkSection[] sections = nmsChunk.getSections();
        LevelChunkSection chunkSection = sections[sectionIndex];

        if (applyPhysics) {
            chunkSection.setBlockState(x & 15, y & 15, z & 15, blockState);
        } else {
            chunkSection.setBlockState(x & 15, y & 15, z & 15, blockState, false);
        }
    }
}
