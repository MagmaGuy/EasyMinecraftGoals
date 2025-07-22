package com.magmaguy.easyminecraftgoals.v1_21_R1.packets;

import com.magmaguy.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.easyminecraftgoals.internal.PacketModelEntity;
import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.EulerAngle;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.InvocationTargetException;
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

    public void initializeModel(Location location, int modelID) {
        itemDisplay = entity;
        itemDisplay.setTransformationInterpolationDelay(-1);
        itemDisplay.setTransformationInterpolationDuration(1);

        //This is for teleport interpolation
        try {
            Display display = itemDisplay;

            // Get the private method
            Method setPosRotInterpolationDuration = Display.class.getDeclaredMethod("d", int.class);

            // Make the method accessible
            setPosRotInterpolationDuration.setAccessible(true);

            // Invoke the method with an argument of 1
            setPosRotInterpolationDuration.invoke(display, 1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        leatherHorseArmor = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        LeatherArmorMeta itemMeta = (LeatherArmorMeta) leatherHorseArmor.getItemMeta();
        itemMeta.setCustomModelData(modelID);
        itemMeta.setColor(Color.WHITE);
        leatherHorseArmor.setItemMeta(itemMeta);
        nmsLeatherHorseArmor = CraftItemStack.asNMSCopy(leatherHorseArmor);
        itemDisplay.setItemStack(nmsLeatherHorseArmor);
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
        sendPacket(createEntityDataPacket());
    }

        @Override
    public void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scale) {
        generateLocationAndRotationAndScalePackets(new PacketBundle(), location, eulerAngle, scale).send();
    }

    @Override
    public AbstractPacketBundle generateLocationAndRotationAndScalePackets(AbstractPacketBundle packetBundle, Location location, EulerAngle eulerAngle, float scale) {
        //translation
        packetBundle.addPacket(generateMovePacket(location), getViewersAsPlayers());
        //rotation
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));
        Transformation transformation = getTransformation();
        transformation = new Transformation(transformation.getTranslation(), quaternionf, new Vector3f(scale,scale,scale), transformation.getRightRotation());
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
