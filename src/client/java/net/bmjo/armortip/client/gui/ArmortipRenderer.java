package net.bmjo.armortip.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.joml.Vector3f;

public class ArmortipRenderer {
    public static final int SIZE = 24;
    public static void renderArmorTip(DrawContext drawContext, ItemStack armorStack, int mouseX, int mouseY, PlayerEntity player, TooltipPositioner tooltipPositioner) {
        if (!(armorStack.getItem() instanceof Equipment)) return;
        int armortipWidth = SIZE;
        int armortipHeight = SIZE * 2;

        Vector2ic vector2ic = tooltipPositioner.getPosition(drawContext.getScaledWindowWidth(), drawContext.getScaledWindowHeight(), mouseX, mouseY, armortipWidth, armortipHeight);
        int startX = vector2ic.x();
        int startY = vector2ic.y();
        drawContext.getMatrices().push();
        drawContext.draw(() -> TooltipBackgroundRenderer.render(drawContext, startX, startY, armortipWidth, armortipHeight, 400));
        drawContext.getMatrices().translate(0.0F, 0.0F, 400.0F);

        renderEntityWithArmor(drawContext, armorStack, startX + SIZE / 2, startY + SIZE * 2 - 2, SIZE, mouseX, mouseY, player);

        drawContext.getMatrices().pop();
    }

    public static void renderEntityWithArmor(DrawContext drawContext, ItemStack armorStack, int x, int y, int size, float mouseX, float mouseY, PlayerEntity player) {
        if (!(armorStack.getItem() instanceof Equipment equipment)) return;
        System.out.println("HI");
        EquipmentSlot slot = equipment.getSlotType();
        if (slot.getType() != EquipmentSlot.Type.ARMOR) return;

        ItemStack originalArmor = player.getEquippedStack(slot);

        float m = player.bodyYaw;
        float n = player.getYaw();
        float o = player.getPitch();
        float q = player.headYaw;
        float p = player.prevHeadYaw;

        PlayerInventory inventory = player.getInventory();

        float h = (float)Math.atan((mouseX - drawContext.getScaledWindowWidth() / 2.0F) / 40.0F);
        float l = (float)Math.atan((mouseY - drawContext.getScaledWindowHeight() / 2.0F) / 40.0F);

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(l * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        try {
            inventory.armor.set(slot.getEntitySlotId(), armorStack);

            player.bodyYaw = 180.0F + h * 10.0F;
            player.setYaw(180.0F + h * 20.0F);
            player.setPitch(-l * 10.0F);
            player.headYaw = player.getYaw();
            player.prevHeadYaw = player.getYaw();

            InventoryScreen.drawEntity(drawContext, x, y, size, new Vector3f(), quaternionf, quaternionf2, player);
        } finally {
            inventory.armor.set(slot.getEntitySlotId(), originalArmor);

            player.bodyYaw = m;
            player.setYaw(n);
            player.setPitch(o);
            player.headYaw = q;
            player.prevHeadYaw = p;
        }
    }
}
