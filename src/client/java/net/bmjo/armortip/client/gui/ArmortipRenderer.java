package net.bmjo.armortip.client.gui;

import net.bmjo.armortip.Armortip;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

@Environment(EnvType.CLIENT)
public class ArmortipRenderer {
    protected static final int SIZE = 24;
    public static final int WIDTH = SIZE;
    public static final int HEIGHT = SIZE * 2;
    public static final int MARGIN = 6;
    public static int time = 0;

    public static void renderArmorTip(DrawContext drawContext, ItemStack armorStack, int mouseX, int mouseY, PlayerEntity player, TooltipPositioner tooltipPositioner, boolean drawBG) {
        if (!(ArmortipUtil.isTipItem(armorStack)))
            return;

        Vector2ic vector2ic = tooltipPositioner.getPosition(drawContext.getScaledWindowWidth(), drawContext.getScaledWindowHeight(), mouseX, mouseY, WIDTH, HEIGHT);

        int startX = vector2ic.x();
        int startY = vector2ic.y();
        drawContext.getMatrices().push();

        if (drawBG)
            TooltipBackgroundRenderer.render(drawContext, startX, startY, WIDTH, HEIGHT, 400);
        drawContext.getMatrices().translate(0.0F, 0.0F, 400.0F);

        try {
            renderEntityWithArmor(drawContext, armorStack, startX + WIDTH / 2, startY + HEIGHT - 2, player);
        } catch (IllegalArgumentException e) {
            Armortip.LOGGER.error("Item is not an equipment item", e);
        }
        drawContext.getMatrices().pop();
    }

    private static void renderEntityWithArmor(DrawContext drawContext, ItemStack armorStack, int x, int y, PlayerEntity player) {
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

        float bodyYaw = player.bodyYaw;
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float headYaw = player.headYaw;
        float prevHeadYaw = player.prevHeadYaw;

        float yRot = (float) Math.atan(80 * Math.cos(time / 300.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * time / 300.0F) / 40.0F);
        time++;
        time %= (int) (2 * Math.PI * 300.0F);

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        try {
            inventory.set(slotId, armorStack);

            player.bodyYaw = 180.0F + yRot * 10.0F;
            player.setYaw(180.0F + yRot * 20.0F);
            player.setPitch(-xRot * 10.0F);
            player.headYaw = player.getYaw();
            player.prevHeadYaw = player.getYaw();

            InventoryScreen.drawEntity(drawContext, x, y, ArmortipRenderer.SIZE, new Vector3f(), quaternionf, quaternionf2, player);
        } finally {
            inventory.set(slotId, originalArmor);

            player.bodyYaw = bodyYaw;
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.headYaw = headYaw;
            player.prevHeadYaw = prevHeadYaw;
        }
    }
}
