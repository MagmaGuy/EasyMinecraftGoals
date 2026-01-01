package com.magmaguy.easyminecraftgoals;

import com.magmaguy.easyminecraftgoals.constants.OverridableWanderPriority;
import com.magmaguy.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.easyminecraftgoals.internal.FakeTextSettings;
import com.magmaguy.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.easyminecraftgoals.internal.PacketModelEntity;
import com.magmaguy.easyminecraftgoals.internal.PacketTextEntity;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class NMSAdapter {
    /**
     * Simply makes an entity move to a point.
     *
     * @param livingEntity   The entity to move
     * @param speedModifier  The speed modifier. Set 1.0 for the base movement speed attribute.
     * @param targetLocation The target location
     * @return Whether the objective is reachable.
     */
    public abstract boolean move(LivingEntity livingEntity, double speedModifier, Location targetLocation);

    public abstract boolean forcedMove(LivingEntity livingEntity, double speedModifier, Location location);

    /**
     * Pathfinding method which will move an entity to a point with a complete disregard for any pathfinding. This means
     * that no pathfinding will be done by Minecraft - the entity can get stuck on blocks, won't be able to jump or really
     * hardly any obstacle. Highly efficient, highly predictable.
     *
     * @param livingEntity  Entity to move
     * @param speedModifier Speed modifier
     * @param location      Location to move to
     */
    public abstract void universalMove(LivingEntity livingEntity, double speedModifier, Location location) ;

    public abstract AbstractWanderBackToPoint wanderBackToPoint(LivingEntity livingEntity,
                                                       Location blockLocation,
                                                       double maximumDistanceFromPoint,
                                                       int maxDurationTicks,
                                                       OverridableWanderPriority overridableWanderPriority);

    /**
     * Sets a custom hitbox size for an entity
     * @param entity The entity whose hitbox is about to get resized
     * @param width Width of the hitbox. Sets the X and Z axis at the same time, that's a Minecraft limitation
     * @param height The height of the hitbox, has to be lower than 64.
     * @param fixed Whether the hitbox can scale
     * @return
     */
    public abstract boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed);

    public abstract float getBodyRotation(Entity entity);

    public abstract PacketModelEntity createPacketArmorStandEntity(Location location);

    public abstract PacketModelEntity createPacketDisplayEntity(Location location);

    public abstract PacketTextEntity createPacketTextArmorStandEntity(Location location);

    public abstract void doNotMove(LivingEntity livingEntity);

    /**
     * Makes a mob wander back to a point when it reaches a certain distance from that point, acting as a leash.
     * Please note that not all mobs in minecraft are able to pathfind - mobs like slimes can't. This leash can be
     * set to teleport them back, but if they're not set to teleport and they are set to walk back they will be unable
     * to do anything.
     * Pathfinding entities should extend Creature from the Spigot API. Non-creature entities are only allowable here
     * under the assumption that they will be set to teleport.
     *
     * @param livingEntity             Entity to leash
     * @param blockLocation            Location to leash to
     * @param maximumDistanceFromPoint Maximum distance before the leash is applied
     * @param maxDurationTicks         Maximum duration of the navigation resulting form the leash
     * @return The instance of the WanderBackToPoint. Can be used in a builder pattern!
     */
    @Nullable
    public AbstractWanderBackToPoint wanderBackToPoint(@NonNull LivingEntity livingEntity,
                                                       @NonNull Location blockLocation,
                                                       double maximumDistanceFromPoint,
                                                       int maxDurationTicks) {
        OverridableWanderPriority overridableWanderPriority;
        try {
            overridableWanderPriority = OverridableWanderPriority.valueOf(livingEntity.getType().name());
        } catch (Exception ex) {
            NMSManager.pluginProvider.getLogger().warning("[EasyMinecraftPathfinding] Attempted to assign return point to entity type " + livingEntity.getType().name() + " which is not a currently accepted entity type!");
            return null;
        }
        return wanderBackToPoint(
                livingEntity,
                blockLocation,
                maximumDistanceFromPoint,
                maxDurationTicks,
                overridableWanderPriority);
    }

    public boolean canReach(LivingEntity livingEntity, Location location){
        //Gets overriden by the correct adapter
        return false;
    }

    public abstract void setBlockInNativeDataPalette(World world, int x, int y, int z, BlockData blockData, boolean applyPhysics);

    public abstract AbstractPacketBundle createPacketBundle();

    /**
     * Creates a generic packet entity of the specified type.
     * The entity exists only in packets - not added to the world.
     * Use getBukkitEntity() to modify properties, then syncMetadata() to update viewers.
     *
     * @param entityType The Bukkit entity type to create
     * @param location   The spawn location
     * @return A packet entity that can be shown to players
     */
    public PacketEntityInterface createPacketEntity(EntityType entityType, Location location) {
        throw new UnsupportedOperationException("createPacketEntity is only supported in Minecraft 1.21.11+");
    }

    /**
     * Creates a FakeText that automatically uses TextDisplay for Java Edition players
     * and ArmorStand for Bedrock Edition players.
     *
     * @param location The spawn location
     * @param settings The text display settings
     * @return A FakeText instance
     */
    public abstract FakeText createFakeText(Location location, FakeTextSettings settings);

    /**
     * Creates a builder for FakeText with customizable styling options.
     *
     * @return A new FakeText.Builder
     */
    public FakeText.Builder fakeTextBuilder() {
        return new FakeTextBuilderImpl(this);
    }

    /**
     * Default implementation of FakeText.Builder.
     */
    private static class FakeTextBuilderImpl implements FakeText.Builder {
        private final NMSAdapter adapter;
        private final FakeTextSettings settings = new FakeTextSettings();

        FakeTextBuilderImpl(NMSAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public FakeText.Builder text(String text) {
            settings.setText(text);
            return this;
        }

        @Override
        public FakeText.Builder backgroundColor(org.bukkit.Color color) {
            settings.setBackgroundColor(color);
            return this;
        }

        @Override
        public FakeText.Builder backgroundColor(int argb) {
            settings.setBackgroundArgb(argb);
            return this;
        }

        @Override
        public FakeText.Builder textOpacity(byte opacity) {
            settings.setTextOpacity(opacity);
            return this;
        }

        @Override
        public FakeText.Builder billboard(org.bukkit.entity.Display.Billboard billboard) {
            settings.setBillboard(billboard);
            return this;
        }

        @Override
        public FakeText.Builder alignment(FakeText.TextAlignment alignment) {
            settings.setAlignment(alignment);
            return this;
        }

        @Override
        public FakeText.Builder shadow(boolean shadow) {
            settings.setShadow(shadow);
            return this;
        }

        @Override
        public FakeText.Builder seeThrough(boolean seeThrough) {
            settings.setSeeThrough(seeThrough);
            return this;
        }

        @Override
        public FakeText.Builder lineWidth(int width) {
            settings.setLineWidth(width);
            return this;
        }

        @Override
        public FakeText.Builder viewRange(float range) {
            settings.setViewRange(range);
            return this;
        }

        @Override
        public FakeText.Builder scale(float scale) {
            settings.setScale(scale);
            return this;
        }

        @Override
        public FakeText build(Location location) {
            return adapter.createFakeText(location, settings);
        }
    }

}
