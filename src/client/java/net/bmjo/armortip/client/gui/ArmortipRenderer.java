package net.bmjo.armortip.client.gui;

import net.bmjo.armortip.ArmortipClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.joml.Vector3f;

public class ArmortipRenderer {
    public static final int SIZE = 24;

    public static void renderArmorTip(DrawContext drawContext, ItemStack armorStack, int mouseX, int mouseY, PlayerEntity player, TooltipPositioner tooltipPositioner) {
        if (!(armorStack.getItem() instanceof Equipment || armorStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof Equipment))
            return;
        int armortipWidth = SIZE;
        int armortipHeight = SIZE * 2;

        Vector2ic vector2ic = tooltipPositioner.getPosition(drawContext.getScaledWindowWidth(), drawContext.getScaledWindowHeight(), mouseX, mouseY, armortipWidth, armortipHeight);
        int startX = vector2ic.x();
        int startY = vector2ic.y();
        drawContext.getMatrices().push();
        drawContext.draw(() -> TooltipBackgroundRenderer.render(drawContext, startX, startY, armortipWidth, armortipHeight, 400));
        drawContext.getMatrices().translate(0.0F, 0.0F, 400.0F);

        try {

            renderEntityWithArmor(drawContext, armorStack, startX + SIZE / 2, startY + SIZE * 2 - 2, SIZE, mouseX, mouseY, player);
        } catch (IllegalArgumentException e) {
            ArmortipClient.LOGGER.error("Item is not an equipment item", e);
        }

        drawContext.getMatrices().pop();
    }

    private static void renderEntityWithArmor(DrawContext drawContext, ItemStack armorStack, int x, int y, int size, float mouseX, float mouseY, PlayerEntity player) {
        EquipmentSlot slot;
        if (armorStack.getItem() instanceof Equipment equipment)
            slot = equipment.getSlotType();
        else if (armorStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof Equipment equipment) {
            slot = equipment.getSlotType();
            if (!slot.isArmorSlot())
                slot = EquipmentSlot.OFFHAND;
        } else
            throw new IllegalArgumentException("Item is not an equipment item");

        DefaultedList<ItemStack> inventory = slot.isArmorSlot() ? player.getInventory().armor : player.getInventory().offHand;

        ItemStack originalArmor = player.getEquippedStack(slot);
        int slotId = slot == EquipmentSlot.OFFHAND ? 0 : slot.getEntitySlotId();

        float m = player.bodyYaw;
        float n = player.getYaw();
        float o = player.getPitch();
        float q = player.headYaw;
        float p = player.prevHeadYaw;

        float h = (float)Math.atan((mouseX - drawContext.getScaledWindowWidth() / 2.0F) / 40.0F);
        float l = (float)Math.atan((mouseY - drawContext.getScaledWindowHeight() / 2.0F) / 40.0F);

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(l * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);


        try {
            inventory.set(slotId, armorStack);

            player.bodyYaw = 180.0F + h * 10.0F;
            player.setYaw(180.0F + h * 20.0F);
            player.setPitch(-l * 10.0F);
            player.headYaw = player.getYaw();
            player.prevHeadYaw = player.getYaw();

            InventoryScreen.drawEntity(drawContext, x, y, size, new Vector3f(), quaternionf, quaternionf2, player);
        } finally {
            inventory.set(slotId, originalArmor);

            player.bodyYaw = m;
            player.setYaw(n);
            player.setPitch(o);
            player.headYaw = q;
            player.prevHeadYaw = p;
        }
    }
}
