package net.bmjo.armortip.client.gui;

import net.bmjo.armortip.Armortip;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DyedCarpetBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.util.collection.DefaultedList;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
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
        drawContext.getMatrices().push();

        if (drawBG)
            TooltipBackgroundRenderer.render(drawContext, startX, startY, WIDTH, HEIGHT, 400);
        drawContext.getMatrices().translate(0.0F, 0.0F, 400.0F);

        renderEquipment(player, drawContext, armorStack, startX, startY);
        drawContext.getMatrices().pop();
    }

    private static void renderEquipment(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, int x, int y) {
        try {
            if (itemStack.getItem() instanceof Equipment equipment) {
                var slot = equipment.getSlotType();
                switch (slot.getType()) {
                    case HAND -> renderPlayerWithEquipment(player, drawContext, itemStack, slot, x, y);
                    case HUMANOID_ARMOR -> renderPlayerWithArmor(player, drawContext, itemStack, slot, x, y);
                    case ANIMAL_ARMOR -> renderAnimalWithArmor(player, drawContext, itemStack, x, y);
                    default -> throw new IllegalArgumentException("Item is not an equipment item");
                }
            } else if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof Equipment equipment) {
                var slot = equipment.getSlotType();
                switch (slot.getType()) {
                    case HAND -> renderPlayerWithEquipment(player, drawContext, itemStack, slot, x, y);
                    case HUMANOID_ARMOR -> renderPlayerWithArmor(player, drawContext, itemStack, slot, x, y);
                    case ANIMAL_ARMOR -> renderAnimalWithArmor(player, drawContext, itemStack, x, y);
                    default -> throw new IllegalArgumentException("Item is not an equipment item");
                }
            } else if (itemStack.getItem() instanceof SmithingTemplateItem) {
                renderPlayerWithArmorTrim(player, drawContext, itemStack, x, y);
            }
        } catch (IllegalArgumentException e) {
            Armortip.LOGGER.error("Item is not an equipment item", e);
        }
    }

    private static void renderPlayerWithArmor(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, EquipmentSlot slot, int x, int y) {
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
            throw new IllegalArgumentException("Item is not an armor equipment item");
        DefaultedList<ItemStack> inventory = player.getInventory().armor;
        int slotId = slot.getEntitySlotId();
        ItemStack originalArmor = inventory.get(slotId);

        float bodyYaw = player.bodyYaw;
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float headYaw = player.headYaw;
        float prevHeadYaw = player.prevHeadYaw;

        float yRot = (float) Math.atan(80 * Math.cos(time / 300.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * time / 300.0F) / 40.0F);
        time++;
        time %= (int) (2 * Math.PI * 300.0F);

        float size = 42 / Math.max(player.getWidth(), player.getHeight());

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        try {
            inventory.set(slotId, itemStack);

            player.bodyYaw = 180.0F + yRot * 10.0F;
            player.setYaw(180.0F + yRot * 20.0F);
            player.setPitch(-xRot * 10.0F);
            player.headYaw = player.getYaw();
            player.prevHeadYaw = player.getYaw();

            InventoryScreen.drawEntity(drawContext, x + WIDTH / 2.0F, y + HEIGHT, size, new Vector3f(0, 0, 0), quaternionf, quaternionf2, player);
        } finally {
            inventory.set(slotId, originalArmor);

            player.bodyYaw = bodyYaw;
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.headYaw = headYaw;
            player.prevHeadYaw = prevHeadYaw;
        }
    }

    private static void renderPlayerWithEquipment(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, EquipmentSlot slot, int x, int y) {
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND)
            throw new IllegalArgumentException("Item is not a hand equipment item");
        DefaultedList<ItemStack> inventory = slot == EquipmentSlot.MAINHAND ? player.getInventory().main : player.getInventory().offHand;
        ItemStack originalArmor = inventory.get(0);

        float bodyYaw = player.bodyYaw;
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float headYaw = player.headYaw;
        float prevHeadYaw = player.prevHeadYaw;

        float yRot = (float) Math.atan(80 * Math.cos(time / 300.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * time / 300.0F) / 40.0F);
        time++;
        time %= (int) (2 * Math.PI * 300.0F);

        float size = 42 / Math.max(player.getWidth(), player.getHeight());

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        try {
            inventory.set(0, itemStack);

            player.bodyYaw = 180.0F + yRot * 10.0F;
            player.setYaw(180.0F + yRot * 20.0F);
            player.setPitch(-xRot * 10.0F);
            player.headYaw = player.getYaw();
            player.prevHeadYaw = player.getYaw();

            InventoryScreen.drawEntity(drawContext, x + WIDTH / 2.0F, y + HEIGHT, size, new Vector3f(0, 0, 0), quaternionf, quaternionf2, player);
        } finally {
            inventory.set(0, originalArmor);

            player.bodyYaw = bodyYaw;
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.headYaw = headYaw;
            player.prevHeadYaw = prevHeadYaw;
        }
    }

    private static void renderPlayerWithArmorTrim(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, int x, int y) {
        DefaultedList<ItemStack> armor = player.getInventory().armor;

        List<ItemStack> originalArmor = List.copyOf(armor);

        float bodyYaw = player.bodyYaw;
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float headYaw = player.headYaw;
        float prevHeadYaw = player.prevHeadYaw;

        float yRot = (float) Math.atan(80 * Math.cos(time / 300.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * time / 300.0F) / 40.0F);
        time++;
        time %= (int) (2 * Math.PI * 300.0F);

        float size = 42 / Math.max(player.getWidth(), player.getHeight());

        Quaternionf quaternionf = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        var wrapperLookup = MinecraftClient.getInstance().world.getRegistryManager();
        var optMaterial = ArmorTrimMaterials.get(wrapperLookup, Items.DIAMOND.getDefaultStack());
        var optPattern = ArmorTrimPatterns.get(wrapperLookup, itemStack);

        if (optMaterial.isEmpty() || optPattern.isEmpty()) return;

        try {
            for (int i = 0; i < armor.size(); i++) {
                ItemStack trimStack = armor.get(i).copy();
                trimStack.set(DataComponentTypes.TRIM, new ArmorTrim(optMaterial.get(), optPattern.get()));
                armor.set(i, trimStack);
            }

            player.bodyYaw = 180.0F + yRot * 10.0F;
            player.setYaw(180.0F + yRot * 20.0F);
            player.setPitch(-xRot * 10.0F);
            player.headYaw = player.getYaw();
            player.prevHeadYaw = player.getYaw();

            InventoryScreen.drawEntity(drawContext, x + WIDTH / 2.0F, y + HEIGHT, size, new Vector3f(0, 0, 0), quaternionf, quaternionf2, player);
        } finally {
            for (int i = 0; i < armor.size(); i++) {
                armor.set(i, originalArmor.get(i));
            }

            player.bodyYaw = bodyYaw;
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.headYaw = headYaw;
            player.prevHeadYaw = prevHeadYaw;
        }
    }

    private static void renderAnimalWithArmor(PlayerEntity player, DrawContext drawContext, ItemStack itemStack, int x, int y) {
        if (itemStack.getItem() instanceof AnimalArmorItem animalArmorItem) {
            AnimalArmorItem.Type type = animalArmorItem.getType();
            switch (type) {
                case EQUESTRIAN -> renderAnimalWithArmor(player, EntityType.HORSE, drawContext, itemStack, x, y);
                case CANINE -> renderAnimalWithArmor(player, EntityType.WOLF, drawContext, itemStack, x, y);
                default -> throw new IllegalArgumentException("Item is not an animal armor item");
            }
        } else if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof DyedCarpetBlock) {
            renderAnimalWithArmor(player, EntityType.LLAMA, drawContext, itemStack, x, y);
        } else
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

        animal.prevBodyYaw = animal.bodyYaw;
        animal.prevHeadYaw = animal.headYaw;
        animal.prevPitch = animal.getYaw();

        animal.bodyYaw = 225.0F + yRot * 10.0F;
        animal.setHeadYaw(180.0F + yRot * 20.0F);
        animal.setPitch(-xRot * 10.0F);

        float size = 42 / Math.max(animal.getWidth(), animal.getHeight());

        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        InventoryScreen.drawEntity(drawContext, x + WIDTH / 2.0F, y + HEIGHT, size, new Vector3f(0, 0, 0), quaternionf, new Quaternionf(), animal);

        animal.equipBodyArmor(ItemStack.EMPTY);
    }

    private static <E extends AnimalEntity> E getCached(PlayerEntity player, EntityType<E> type) {
        return (E) CACHE.computeIfAbsent(type, t -> (AnimalEntity) t.create(player.getWorld()));
    }
}
