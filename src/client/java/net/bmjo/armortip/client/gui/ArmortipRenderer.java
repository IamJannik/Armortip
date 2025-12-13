package net.bmjo.armortip.client.gui;

import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.trim.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector2ic;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ArmortipRenderer {
    private static List<RegistryEntry.Reference<ArmorTrimMaterial>> MATERIAL_CACHE;
    private static final Item[] DEFAULT_ARMOR = {Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET};
    protected static final int SIZE = 24;
    public static final int WIDTH = SIZE;
    public static final int HEIGHT = SIZE * 2;
    public static final int MARGIN = 6;

    public static void renderArmortip(DrawContext drawContext, ItemStack itemStack, int mouseX, int mouseY, PlayerEntity player, TooltipPositioner tooltipPositioner, boolean drawBG) {
        if (!(ArmortipUtil.isTipItem(itemStack)))
            return;

        Vector2ic vector2ic = tooltipPositioner.getPosition(drawContext.getScaledWindowWidth(), drawContext.getScaledWindowHeight(), mouseX, mouseY, WIDTH, HEIGHT);

        int startX = vector2ic.x();
        int startY = vector2ic.y();
        drawContext.getMatrices().push();

        if (drawBG)
            TooltipBackgroundRenderer.render(drawContext, startX, startY, WIDTH, HEIGHT, 400);
        drawContext.getMatrices().translate(0.0F, 0.0F, 400.0F);

        if (itemStack.getItem() instanceof SmithingTemplateItem) {
            renderArmorTrim(player, drawContext, itemStack, startX, startY);
        } else {
            renderPlayer(drawContext, itemStack, startX, startY, player);
        }

        drawContext.getMatrices().pop();
    }

    private static void renderPlayer(DrawContext drawContext, ItemStack itemStack, int x, int y, PlayerEntity player) {
        EquipmentSlot slot;
        if (itemStack.getItem() instanceof Equipment equipment)
            slot = equipment.getSlotType();
        else if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof Equipment equipment) {
            slot = equipment.getSlotType();
            if (!slot.isArmorSlot())
                slot = EquipmentSlot.OFFHAND;
        } else
            return;

        DefaultedList<ItemStack> inventory = slot.isArmorSlot() ? player.getInventory().armor : player.getInventory().offHand;
        ItemStack originalArmor = player.getEquippedStack(slot);
        int slotId = slot == EquipmentSlot.OFFHAND ? 0 : slot.getEntitySlotId();
        inventory.set(slotId, itemStack);
        renderEntity(drawContext, x, y, player);
        inventory.set(slotId, originalArmor);
    }

    private static void renderArmorTrim(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, int x, int y) {
        DefaultedList<ItemStack> armor = player.getInventory().armor;

        ItemStack[] originalArmor = new ItemStack[4];
        for (int i = 0; i < armor.size(); i++) originalArmor[i] = armor.get(i).copy();

        var wrapperLookup = player.getEntityWorld().getRegistryManager();
        var optMaterial = getCachedTrimMaterial(player.getEntityWorld());
        var optPattern = ArmorTrimPatterns.get(wrapperLookup, itemStack);

        if (optMaterial == null || optPattern.isEmpty()) return;
        ArmorTrim armorTrim = new ArmorTrim(optMaterial, optPattern.get());
        for (int i = 0; i < armor.size(); i++) {
            ItemStack armorStack = armor.get(i);
            if (armorStack.isEmpty()) {
                var defaultStack = DEFAULT_ARMOR[i].getDefaultStack();
                ArmorTrim.apply(wrapperLookup, defaultStack, armorTrim);
                armor.set(i, defaultStack);
            } else {
                ArmorTrim.apply(wrapperLookup, armorStack, armorTrim);
            }
        }
        renderEntity(drawContext, x, y, player);
        renderMaterial(optMaterial, x, y, drawContext, player.getEntityWorld());
        for (int i = 0; i < armor.size(); i++) {
            armor.set(i, originalArmor[i]);
        }
    }

    private static void renderEntity(DrawContext drawContext, int x, int y, PlayerEntity player) {
        float bodyYaw = player.getBodyYaw();
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float headYaw = player.getHeadYaw();

        float yRot = (float) Math.atan(80 * Math.cos(ArmortipUtil.ticks / 64.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * ArmortipUtil.ticks / 64.0F) / 40.0F);

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        player.setBodyYaw(200.0F + yRot * 10.0F);
        player.setYaw(180.0F + yRot * 20.0F);
        player.setPitch(-xRot * 10.0F);
        player.setHeadYaw(player.getYaw());

        InventoryScreen.drawEntity(drawContext, x - MARGIN + WIDTH / 2, y + HEIGHT, ArmortipRenderer.SIZE, quaternionf, quaternionf2, player);

        player.setBodyYaw(bodyYaw);
        player.setYaw(yaw);
        player.setPitch(pitch);
        player.setHeadYaw(headYaw);
    }

    private static void renderMaterial(RegistryEntry<ArmorTrimMaterial> material, int x, int y, DrawContext drawContext) {
        var item = material.value().ingredient().value();
        if (item == null) return;

        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(x + ArmortipRenderer.WIDTH - MARGIN, y, 0);
        drawContext.getMatrices().scale(0.5F, 0.5F, 0.5F);
        drawContext.drawItem(item.getDefaultStack(), 0, 0);
        drawContext.getMatrices().pop();
    }

    private static RegistryEntry<ArmorTrimMaterial> getCachedTrimMaterial(World world) {
        if (MATERIAL_CACHE == null) {
            var registryAccess = world.getRegistryManager();
            var registry = registryAccess.get(RegistryKeys.TRIM_MATERIAL);
            MATERIAL_CACHE = registry.streamEntries().toList();
        }
        return MATERIAL_CACHE.get((ArmortipUtil.ticks / 40) % MATERIAL_CACHE.size());
    }
}
