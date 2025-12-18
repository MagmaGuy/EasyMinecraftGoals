package com.magmaguy.easyminecraftgoals.v1_21_R7_common.packets;

import com.magmaguy.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.easyminecraftgoals.internal.PacketModelEntity;
import com.magmaguy.easyminecraftgoals.v1_21_R7_common.CraftBukkitBridge;
import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.util.EulerAngle;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;


public class PacketDisplayEntity extends AbstractPacketEntity<Display.ItemDisplay> implements PacketModelEntity {

    private ItemStack carrierItem;
    private net.minecraft.world.item.ItemStack nmsCarrierItem;
    private Display.ItemDisplay itemDisplay;

    public PacketDisplayEntity(Location location) {
        super(location);
    }

    private static Quaternionf eulerToQuaternion(double originalX, double originalY, double originalZ) {
        double yaw = Math.toRadians(originalZ);
        double pitch = Math.toRadians(originalY);
        double roll = Math.toRadians(originalX);

        double cy = Math.cos(yaw * 0.5);
        double sy = Math.sin(yaw * 0.5);
        double cp = Math.cos(pitch * 0.5);
        double sp = Math.sin(pitch * 0.5);
        double cr = Math.cos(roll * 0.5);
        double sr = Math.sin(roll * 0.5);

        double w = cr * cp * cy + sr * sp * sy;
        double x = sr * cp * cy - cr * sp * sy;
        double y = cr * sp * cy + sr * cp * sy;
        double z = cr * cp * sy - sr * sp * cy;

        return new Quaternionf(x, y, z, w);
    }

    @Override
    protected Display.ItemDisplay createEntity(Location location) {
        // This doesn't create a real entity until it gets added to the world, which for packet entity purposes is never
        return new Display.ItemDisplay(EntityType.ITEM_DISPLAY, getNMSLevel(location));
    }

    /**
     * Initializes the client-side item model and wires up tinting via CustomModelData colors.
     * Your items JSON should include:
     * "tints": [ { "type": "minecraft:custom_model_data", "index": 0, "default": 16777215 } ]
     * and the bone model's faces should have "tintindex": 0.
     */
    public void initializeModel(Location location, String modelID) {
        itemDisplay = entity;

        // Interpolation defaults (safe on R5)
        itemDisplay.setTransformationInterpolationDelay(-1);
        itemDisplay.setTransformationInterpolationDuration(0);

        // Set teleport interpolation duration - Paper uses Mojang mappings
        try {
            Display display = itemDisplay;
            // Try Mojang-mapped method name first
            Method setPosRotInterpolationDuration = Display.class.getDeclaredMethod("setTeleportDuration", int.class);
            setPosRotInterpolationDuration.setAccessible(true);
            setPosRotInterpolationDuration.invoke(display, 1);
        } catch (Exception e) {
            // Fallback - try other possible method names
            try {
                Display display = itemDisplay;
                Method setPosRotInterpolationDuration = Display.class.getDeclaredMethod("setPosRotInterpolationDuration", int.class);
                setPosRotInterpolationDuration.setAccessible(true);
                setPosRotInterpolationDuration.invoke(display, 1);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        // Carrier item (any item works; LEATHER_HORSE_ARMOR kept for continuity)
        carrierItem = new ItemStack(Material.LEATHER_HORSE_ARMOR);

        // Set the explicit item model ID (Paper API)
        ItemMeta meta = carrierItem.getItemMeta();
        meta.setItemModel(NamespacedKey.fromString(modelID));

        // Initialize CustomModelData component with a default white tint (index 0)
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setColors(List.of(Color.WHITE)); // index 0 -> tintindex: 0 on your model
        meta.setCustomModelDataComponent(cmd);

        carrierItem.setItemMeta(meta);

        // Hand to the display
        nmsCarrierItem = CraftBukkitBridge.asNMSCopy(carrierItem);
        itemDisplay.setItemStack(nmsCarrierItem);
        itemDisplay.setWidth(0);
        itemDisplay.setHeight(0);
        itemDisplay.setViewRange(30);
    }

    /**
     * Updates the tint color by writing to CustomModelData.colors[0].
     * This is what the client reads for "minecraft:custom_model_data" tints.
     */
    @Override
    public void setHorseLeatherArmorColor(Color color) {
        if (carrierItem == null) return;

        ItemMeta meta = carrierItem.getItemMeta();
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();

        // If the pack ever uses multiple tint slots, add more entries here in index order.
        cmd.setColors(List.of(color));

        meta.setCustomModelDataComponent(cmd);
        carrierItem.setItemMeta(meta);

        nmsCarrierItem = CraftBukkitBridge.asNMSCopy(carrierItem);
        itemDisplay.setItemStack(nmsCarrierItem);
    }

    @Override
    public void sendLocationAndRotationPacket(Location location, EulerAngle eulerAngle) {
        move(location);
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));
        rotate(quaternionf);
        sendPacketToAll(createEntityDataPacket());
    }

    @Override
    public void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scale) {
        generateLocationAndRotationAndScalePackets(new PacketBundle(), location, eulerAngle, scale).send();
    }

    @Override
    public void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scaleX, float scaleY, float scaleZ) {
        generateLocationAndRotationAndScalePackets(new PacketBundle(), location, eulerAngle, scaleX, scaleY, scaleZ).send();
    }

    @Override
    public AbstractPacketBundle generateLocationAndRotationAndScalePackets(
            AbstractPacketBundle packetBundle, Location location, EulerAngle eulerAngle, float scale) {
        return generateLocationAndRotationAndScalePackets(packetBundle, location, eulerAngle, scale, scale, scale);
    }

    @Override
    public AbstractPacketBundle generateLocationAndRotationAndScalePackets(
            AbstractPacketBundle packetBundle, Location location, EulerAngle eulerAngle, float scaleX, float scaleY, float scaleZ) {

        if (!getLocation().getWorld().equals(location.getWorld())) {
            packetBundle.addPacket(generateMovePacket(location), getViewersAsPlayers());
        } else {
            // Always move — keeps things in sync for display entities
            packetBundle.addPacket(generateMovePacket(location), getViewersAsPlayers());
        }

        // Always update transformation for rotation/scale
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));

        Transformation transformation = getTransformation();
        transformation = new Transformation(
                new Vector3f(0, 0, 0),  // keep translation out of the transformation
                quaternionf,
                new Vector3f(scaleX, scaleY, scaleZ),
                transformation.getRightRotation()
        );

        entity.setTransformation(transformation);
        packetBundle.addPacket(createEntityDataPacket(), getViewersAsPlayers());

        return packetBundle;
    }

    @Override
    public void displayTo(Player player) {
        super.displayTo(player);
    }

    public void displayTo(UUID player) {
        displayTo(Bukkit.getPlayer(player));
    }

    @Override
    public void addViewer(UUID player) {
        super.addViewer(player);
        displayTo(player);
    }

    public Vector3f getScale() {
        Vector3fc scale = getTransformation().getScale();
        return new Vector3f(scale.x(), scale.y(), scale.z());
    }

    @Override
    public void setScale(float scale) {
        setScale(new Vector3f(scale, scale, scale));
    }

    public void setScale(Vector3f scale) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(
                transformation.getTranslation(),
                transformation.getLeftRotation(),
                scale,
                transformation.getRightRotation());
        setTransformation(newTransformation);
    }

    public Vector3f getTranslation() {
        Vector3fc translation = getTransformation().getTranslation();
        return new Vector3f(translation.x(), translation.y(), translation.z());
    }

    public void setTranslation(Vector3f translation) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(
                translation,
                transformation.getLeftRotation(),
                transformation.getScale(),
                transformation.getRightRotation());
        setTransformation(newTransformation);
    }

    public Quaternionf getLeftRotation() {
        Quaternionfc leftRotation = getTransformation().getLeftRotation();
        return new Quaternionf(leftRotation.x(), leftRotation.y(), leftRotation.z(), leftRotation.w());
    }

    public void setLeftRotation(Quaternionf rotation) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(
                transformation.getTranslation(),
                rotation,
                transformation.getScale(),
                transformation.getRightRotation());
        setTransformation(newTransformation);
    }

    public Quaternionf getRightRotation() {
        Quaternionfc rightRotation = getTransformation().getRightRotation();
        return new Quaternionf(rightRotation.x(), rightRotation.y(), rightRotation.z(), rightRotation.w());
    }

    public void setRightRotation(Quaternionf rotation) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(
                transformation.getTranslation(),
                transformation.getLeftRotation(),
                transformation.getScale(),
                rotation);
        setTransformation(newTransformation);
    }

    public Transformation getTransformation() {
        Transformation nms = Display.createTransformation(this.entity.getEntityData());
        return new Transformation(nms.getTranslation(), nms.getLeftRotation(), nms.getScale(), nms.getRightRotation());
    }

    private void setTransformation(Transformation transformation) {
        entity.setTransformation(transformation);
    }

    private void rotate(Quaternionf rotation) {
        if (rotation == null) return;
        setLeftRotation(rotation);
    }
}
