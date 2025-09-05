package com.magmaguy.easyminecraftgoals.v1_21_R4.packets;

import com.magmaguy.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.easyminecraftgoals.internal.PacketModelEntity;
import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.EulerAngle;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Method;
import java.util.UUID;

public class PacketDisplayEntity extends AbstractPacketEntity<Display.ItemDisplay> implements PacketModelEntity {

    private ItemStack leatherHorseArmor;
    private net.minecraft.world.item.ItemStack nmsLeatherHorseArmor;
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
        //This doesn't create a real entity until it gets added to the world, which for packet entity purposes is never
        return new Display.ItemDisplay(EntityType.ITEM_DISPLAY, getNMSLevel(location));
    }

    public void initializeModel(Location location, String modelID) {
        itemDisplay = entity;

        // Set interpolation explicitly with correct defaults
        itemDisplay.setTransformationInterpolationDelay(-1);  // Was -1, which may not work in R4
        itemDisplay.setTransformationInterpolationDuration(0);

        // For teleport interpolation, this reflection call might be failing
        try {
            Display display = itemDisplay;

            // Get the private method
            Method setPosRotInterpolationDuration = Display.class.getDeclaredMethod("d", int.class);

            setPosRotInterpolationDuration.setAccessible(true);
            setPosRotInterpolationDuration.invoke(display, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        leatherHorseArmor = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        LeatherArmorMeta itemMeta = (LeatherArmorMeta) leatherHorseArmor.getItemMeta();
        itemMeta.setItemModel(NamespacedKey.fromString(modelID));
        itemMeta.setColor(Color.WHITE);
        leatherHorseArmor.setItemMeta(itemMeta);
        nmsLeatherHorseArmor = CraftItemStack.asNMSCopy(leatherHorseArmor);
        itemDisplay.setItemStack(nmsLeatherHorseArmor);
        itemDisplay.setWidth(0);
        itemDisplay.setHeight(0);
        itemDisplay.setViewRange(30);
    }

    @Override
    public void setHorseLeatherArmorColor(Color color) {
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) leatherHorseArmor.getItemMeta();
        leatherArmorMeta.setColor(color);
        leatherHorseArmor.setItemMeta(leatherArmorMeta);
        nmsLeatherHorseArmor = CraftItemStack.asNMSCopy(leatherHorseArmor);
        itemDisplay.setItemStack(nmsLeatherHorseArmor);
    }

    @Override
    public void sendLocationAndRotationPacket(Location location, EulerAngle eulerAngle) {
        move(location);
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));
        rotate(quaternionf);
        sendPacketToAll(createEntityDataPacket());  // Changed from sendPacket to sendPacketToAll
    }

    @Override
    public void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scale) {
        generateLocationAndRotationAndScalePackets(new PacketBundle(), location, eulerAngle, scale).send();
    }


    @Override
    public AbstractPacketBundle generateLocationAndRotationAndScalePackets(
            AbstractPacketBundle packetBundle, Location location, EulerAngle eulerAngle, float scale) {

        // Since we're now always using teleport packets (absolute positioning),
        // increase the threshold to avoid unnecessary packets for tiny movements
        Location currentLoc = getLocation();
        double distSq = currentLoc.distanceSquared(location);

        if (distSq > 0.01) {  // Increased from 0.001 to 0.01 (0.1 block movement)
            packetBundle.addPacket(generateMovePacket(location), getViewersAsPlayers());
        }

        // Always update transformation for rotation/scale
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));

        Transformation transformation = getTransformation();
        transformation = new Transformation(
                new Vector3f(0, 0, 0),  // Don't use translation in transformation!
                quaternionf,
                new Vector3f(scale, scale, scale),
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
        return getTransformation().getScale();
    }

    @Override
    public void setScale(float scale) {
        setScale(new Vector3f(scale, scale, scale));
    }

    public void setScale(Vector3f scale) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), scale, transformation.getRightRotation());
        setTransformation(newTransformation);
    }

    public Vector3f getTranslation() {
        return getTransformation().getTranslation();
    }

    public void setTranslation(Vector3f translation) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(translation, transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation());
        setTransformation(newTransformation);
    }

    public Quaternionf getLeftRotation() {
        return getTransformation().getLeftRotation();
    }

    public void setLeftRotation(Quaternionf rotation) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(transformation.getTranslation(), rotation, transformation.getScale(), transformation.getRightRotation());
        setTransformation(newTransformation);
    }

    public Quaternionf getRightRotation() {
        return getTransformation().getRightRotation();
    }

    public void setRightRotation(Quaternionf rotation) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), rotation);
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