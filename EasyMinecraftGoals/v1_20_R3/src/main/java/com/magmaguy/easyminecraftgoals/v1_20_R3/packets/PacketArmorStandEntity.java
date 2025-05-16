package com.magmaguy.easyminecraftgoals.v1_20_R3.packets;

import com.magmaguy.easyminecraftgoals.internal.PacketModelEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.EulerAngle;

import java.util.List;
import java.util.UUID;

public class PacketArmorStandEntity extends AbstractPacketEntity<ArmorStand> implements PacketModelEntity {

    private ItemStack nmsLeatherHorseArmor;
    private org.bukkit.inventory.ItemStack leatherHorseArmor;
    private ArmorStand armorStand;

    public PacketArmorStandEntity(Location location) {
        super(location);
    }

    @Override
    protected ArmorStand createEntity(Location location) {
        //This doesn't create a real entity until it gets added to the world, which for packet entity purposes is never
        return new ArmorStand(EntityType.ARMOR_STAND, getNMSLevel(location));
    }

    public void initializeModel(Location location, int modelID) {
        armorStand = entity;
        armorStand.setInvisible(true);
        armorStand.setMarker(true);
        leatherHorseArmor = new org.bukkit.inventory.ItemStack(Material.LEATHER_HORSE_ARMOR);
        LeatherArmorMeta itemMeta = (LeatherArmorMeta) leatherHorseArmor.getItemMeta();
        itemMeta.setCustomModelData(modelID);
        itemMeta.setColor(Color.WHITE);
        leatherHorseArmor.setItemMeta(itemMeta);

        nmsLeatherHorseArmor = CraftItemStack.asNMSCopy(leatherHorseArmor);
        //Actually useless, managed by the packet that sends equipment info
        armorStand.setItemSlot(EquipmentSlot.HEAD, nmsLeatherHorseArmor);
    }

    @Override
    public void setScale(float scale) {
        //Actually not possible for armor stands, sorry
    }

    @Override
    public void setHorseLeatherArmorColor(Color color) {
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) leatherHorseArmor.getItemMeta();
        leatherArmorMeta.setColor(color);
        leatherHorseArmor.setItemMeta(leatherArmorMeta);
        nmsLeatherHorseArmor = CraftItemStack.asNMSCopy(leatherHorseArmor);
        armorStand.setItemSlot(EquipmentSlot.HEAD, nmsLeatherHorseArmor);
    }

    @Override
    public void sendLocationAndRotationPacket(Location location, EulerAngle eulerAngle) {
        move(location);
        rotate(eulerAngle);
    }

    @Override
    public void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scale) {
        sendLocationAndRotationPacket(location, eulerAngle);
    }

    @Override
    public void displayTo(Player player) {
        super.displayTo(player);
        sendPacket(player, new ClientboundSetEquipmentPacket(entity.getId(), List.of(Pair.of(EquipmentSlot.HEAD, nmsLeatherHorseArmor))));
    }

    public void displayTo(UUID player) {
        displayTo(Bukkit.getPlayer(player));
    }

    @Override
    public void addViewer(UUID player) {
        super.addViewer(player);
        displayTo(player);
    }

    private void rotate(EulerAngle eulerAngle) {
        if (eulerAngle == null) return;
        entity.setHeadPose(new Rotations((float) Math.toDegrees(eulerAngle.getX()), (float) Math.toDegrees(eulerAngle.getY()), (float) Math.toDegrees(eulerAngle.getZ())));
        sendPacket(createEntityDataPacket());
    }

}
