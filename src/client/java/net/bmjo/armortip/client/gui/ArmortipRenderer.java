package net.bmjo.armortip.client.gui;

import net.bmjo.armortip.Armortip;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.item.equipment.trim.ArmorTrimMaterials;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ArmortipRenderer {
    private static final Map<EntityType<?>, AnimalEntity> CACHE = new HashMap<>();
    protected static final int SIZE = 24;
    public static final int WIDTH = SIZE;
    public static final int HEIGHT = SIZE * 2;
    public static final int MARGIN = 6;
    public static int time;

    public static void renderArmorTip(DrawContext drawContext, ItemStack armorStack, int mouseX, int mouseY, PlayerEntity player, TooltipPositioner tooltipPositioner, boolean drawBG) {
        if (!(ArmortipUtil.isTipItem(armorStack)))
            return;
        Vector2ic vector2ic = tooltipPositioner.getPosition(drawContext.getScaledWindowWidth(), drawContext.getScaledWindowHeight(), mouseX, mouseY, WIDTH, HEIGHT);

        int startX = vector2ic.x();
        int startY = vector2ic.y();

        if (drawBG)
            TooltipBackgroundRenderer.render(drawContext, startX, startY, WIDTH, HEIGHT, Identifier.ofVanilla("tooltip/background"));

        renderEquipment(player, drawContext, armorStack, startX, startY);
    }

    private static void renderEquipment(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, int x, int y) {
        EquippableComponent equippableComponent = itemStack.get(DataComponentTypes.EQUIPPABLE);
        if (equippableComponent == null)
            return;
        var slot = equippableComponent.slot();
        try {
            if (itemStack.getItem() instanceof SmithingTemplateItem) {
                renderPlayerWithArmorTrim(player, drawContext, itemStack, x, y);
            } else
                switch (slot.getType()) {
                    case HAND -> renderPlayerWithEquipment(player, drawContext, itemStack, slot, x, y);
                    case HUMANOID_ARMOR -> renderPlayerWithArmor(player, drawContext, itemStack, slot, x, y);
                    case ANIMAL_ARMOR -> renderAnimalWithArmor(player, drawContext, itemStack, x, y);
                    default -> throw new IllegalArgumentException("Item is not an equipment item");
                }
        } catch (IllegalArgumentException e) {
            Armortip.LOGGER.error("Item is not an equipment item", e);
        }
    }

    private static void renderPlayerWithArmor(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, EquipmentSlot slot, int x, int y) {
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
            throw new IllegalArgumentException("Item is not an armor equipment item");
        ItemStack originalStack = player.getEquippedStack(slot);

        float bodyYaw = player.bodyYaw;
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float headYaw = player.headYaw;
        float lastHeadYaw = player.lastHeadYaw;

        float yRot = (float) Math.atan(80 * Math.cos(time / 300.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * time / 300.0F) / 40.0F);
        time++;
        time %= (int) (2 * Math.PI * 300.0F);

        int size = (int) (42 / Math.max(player.getWidth(), player.getHeight()));

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        try {
            player.equipStack(slot, itemStack);

            player.bodyYaw = 180.0F + yRot * 10.0F;
            player.setYaw(180.0F + yRot * 20.0F);
            player.setPitch(-xRot * 10.0F);
            player.headYaw = player.getYaw();
            player.lastHeadYaw = player.getYaw();

            InventoryScreen.drawEntity(drawContext, x - WIDTH / 2, y, x + WIDTH / 2, y + HEIGHT, size, new Vector3f(0, 0, 0), quaternionf, quaternionf2, player);
        } finally {
            player.equipStack(slot, originalStack);

            player.bodyYaw = bodyYaw;
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.headYaw = headYaw;
            player.lastHeadYaw = lastHeadYaw;
        }
    }

    private static void renderPlayerWithEquipment(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, EquipmentSlot slot, int x, int y) {
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND)
            throw new IllegalArgumentException("Item is not a hand equipment item");
        ItemStack originalStack = player.getEquippedStack(slot);

        float bodyYaw = player.bodyYaw;
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float headYaw = player.headYaw;
        float lastHeadYaw = player.lastHeadYaw;

        float yRot = (float) Math.atan(80 * Math.cos(time / 300.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * time / 300.0F) / 40.0F);
        time++;
        time %= (int) (2 * Math.PI * 300.0F);

        int size = (int) (42 / Math.max(player.getWidth(), player.getHeight()));

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        try {
            player.equipStack(slot, itemStack);

            player.bodyYaw = 180.0F + yRot * 10.0F;
            player.setYaw(180.0F + yRot * 20.0F);
            player.setPitch(-xRot * 10.0F);
            player.headYaw = player.getYaw();
            player.lastHeadYaw = player.getYaw();

            InventoryScreen.drawEntity(drawContext, (int) (x + WIDTH / 2.0), (int) (x + WIDTH / 2.0) + size, y + HEIGHT, y + HEIGHT + size, size, new Vector3f(0, 0, 0), quaternionf, quaternionf2, player);
        } finally {
            player.equipStack(slot, originalStack);

            player.bodyYaw = bodyYaw;
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.headYaw = headYaw;
            player.lastHeadYaw = lastHeadYaw;
        }
    }

    private static void renderPlayerWithArmorTrim(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, int x, int y) {
        float bodyYaw = player.bodyYaw;
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float headYaw = player.headYaw;
        float lastHeadYaw = player.lastHeadYaw;

        float yRot = (float) Math.atan(80 * Math.cos(time / 300.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * time / 300.0F) / 40.0F);
        time++;
        time %= (int) (2 * Math.PI * 300.0F);

        int size = (int) (42 / Math.max(player.getWidth(), player.getHeight()));

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        var wrapperLookup = MinecraftClient.getInstance().world.getRegistryManager();
        var optMaterial = ArmorTrimMaterials.get(wrapperLookup, Items.DIAMOND.getDefaultStack());
        //var optPattern = ArmorTrimPatterns.get(wrapperLookup, itemStack);

        //if (optMaterial.isEmpty() || optPattern.isEmpty()) return;

        /*
        try {
            for (int i = 0; i < armor.size(); i++) {
                ItemStack trimStack = armor.get(i).copy();
                //trimStack.set(DataComponentTypes.TRIM, new ArmorTrim(optMaterial.get(), optPattern.get()));
                armor.set(i, trimStack);
            }

            player.bodyYaw = 180.0F + yRot * 10.0F;
            player.setYaw(180.0F + yRot * 20.0F);
            player.setPitch(-xRot * 10.0F);
            player.headYaw = player.getYaw();
            player.lastHeadYaw = player.getYaw();

            InventoryScreen.drawEntity(drawContext, (int) (x + WIDTH / 2.0), (int) (x + WIDTH / 2.0) + size, y + HEIGHT, y + HEIGHT + size, size, new Vector3f(0, 0, 0), quaternionf, quaternionf2, player);
        } finally {
            for (int i = 0; i < armor.size(); i++) {
                armor.set(i, originalArmor.get(i));
            }

            player.bodyYaw = bodyYaw;
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.headYaw = headYaw;
            player.lastHeadYaw = lastHeadYaw;
        }

         */
    }

    private static void renderAnimalWithArmor(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, int x, int y) {
        throw new IllegalArgumentException("Item is not an animal armor item");
    }

    private static <E extends AnimalEntity> void renderAnimalWithArmor(PlayerEntity player, EntityType<E> animalType, DrawContext drawContext, ItemStack armorStack, int x, int y) {
        E animal = getCached(player, animalType);
        if (animal == null)
            return;

        animal.equipBodyArmor(armorStack);

        float yRot = (float) Math.atan(80 * Math.cos(time / 300.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * time / 300.0F) / 40.0F);
        time++;
        time %= (int) (2 * Math.PI * 300.0F);

        animal.lastBodyYaw = animal.bodyYaw;
        animal.lastHeadYaw = animal.headYaw;
        animal.lastPitch = animal.getYaw();

        animal.bodyYaw = 225.0F + yRot * 10.0F;
        animal.setHeadYaw(180.0F + yRot * 20.0F);
        animal.setPitch(-xRot * 10.0F);

        int size = (int) (42 / Math.max(animal.getWidth(), animal.getHeight()));

        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        InventoryScreen.drawEntity(drawContext, (int) (x + WIDTH / 2.0), (int) (x + WIDTH / 2.0) + size, y + HEIGHT, y + HEIGHT + size, size, new Vector3f(0, 0, 0), quaternionf, quaternionf2, player);

        animal.equipBodyArmor(ItemStack.EMPTY);
    }

    private static <E extends AnimalEntity> E getCached(PlayerEntity player, EntityType<E> type) {
        return (E) CACHE.computeIfAbsent(type, t -> (AnimalEntity) t.create(player.getEntityWorld(), SpawnReason.COMMAND));
    }
}
