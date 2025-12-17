package net.bmjo.armortip.client.gui;

import com.mojang.math.Constants;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ArmortipRenderer {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private static final Item[] DEFAULT_ARMOR = {Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET};
    private static final Map<EntityType<?>, Mob> ENTITY_CACHE = new HashMap<>();
    private static final Map<Item, Holder<TrimPattern>> PATTERN_CACHE = new HashMap<>();
    private static List<Holder.Reference<TrimMaterial>> MATERIAL_CACHE;
    private static final Map<Holder<TrimMaterial>, Holder<Item>> ITEM_CACHE = new HashMap<>();

    public static void renderArmorTip(GuiGraphics guiGraphics, ItemStack armorStack, int mouseX, int mouseY, Player player, ClientTooltipPositioner tooltipPositioner, boolean drawBG) {
        if (!(ArmortipUtil.isTipItem(armorStack)))
            return;
        var vector2ic = tooltipPositioner.positionTooltip(guiGraphics.guiWidth(), guiGraphics.guiHeight(), mouseX, mouseY, ArmortipUtil.SIZE, ArmortipUtil.SIZE);

        int x = vector2ic.x();
        int y = vector2ic.y();
        guiGraphics.pose().pushPose();

        if (drawBG)
            TooltipRenderUtil.renderTooltipBackground(guiGraphics, x, y, ArmortipUtil.SIZE + ArmortipUtil.MARGIN * 2, ArmortipUtil.SIZE, 400, ResourceLocation.withDefaultNamespace("tooltip/background"));
        guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);

        renderEquipment(player, guiGraphics, armorStack, x, y);
        guiGraphics.pose().popPose();
    }

    private static void renderEquipment(Player player, GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        if (ArmortipUtil.isTipItem(itemStack)) {
            if (player == null)
                return;
            if (itemStack.getItem() instanceof SmithingTemplateItem)
                renderTrim(player, itemStack, guiGraphics, x, y);
            else
                renderEquipment(player, itemStack, guiGraphics, x, y);
        }
    }

    private static void renderEquipment(Player player, ItemStack itemStack, GuiGraphics guiGraphics, int x, int y) {
        Equippable equippableComponent = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippableComponent != null) {
            var slot = equippableComponent.slot();
            switch (slot.getType()) {
                case HAND, HUMANOID_ARMOR -> renderPlayer(player, itemStack, slot, guiGraphics, x, y);
                case ANIMAL_ARMOR, SADDLE -> renderAnimal(player, itemStack, equippableComponent, slot, guiGraphics, x, y);
            }
            return;
        }
        renderPlayer(player, itemStack, EquipmentSlot.MAINHAND, guiGraphics, x, y);
    }

    private static void renderPlayer(Player player, ItemStack itemStack, EquipmentSlot slot, GuiGraphics guiGraphics, int x, int y) {
        ItemStack originalStack = player.getItemBySlot(slot);
        player.setItemSlot(slot, itemStack);
        renderEntity(player, guiGraphics, x, y);
        player.setItemSlot(slot, originalStack);
    }

    private static void renderAnimal(Player player, ItemStack itemStack, Equippable equippableComponent, EquipmentSlot slot, GuiGraphics guiGraphics, int x, int y) {
        var entities = equippableComponent.allowedEntities();
        if (entities.isEmpty())
            return;
        var animalType = entities.get().get(0).value();
        LivingEntity animal = getCachedEntity(player.level(), animalType);
        ItemStack originalStack = animal.getItemBySlot(slot);
        animal.setItemSlot(slot, itemStack);
        renderEntity(animal, guiGraphics, x, y);
        animal.setItemSlot(slot, originalStack);
    }

    private static void renderTrim(Player player, ItemStack itemStack, GuiGraphics guiGraphics, int x, int y) {
        var material = getCachedTrimMaterial(player.level());
        var pattern = getCachedTrimPattern(player.level(), itemStack.getItem());
        if (material == null || pattern == null) return;

        ItemStack[] originalArmor = new ItemStack[4];
        for (int i = 0; i < ARMOR_SLOTS.length; i++) originalArmor[i] = player.getItemBySlot(ARMOR_SLOTS[i]).copy();
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            var equipStack = player.getItemBySlot(ARMOR_SLOTS[i]);
            if (equipStack.isEmpty()) {
                var armorStack = DEFAULT_ARMOR[i].getDefaultInstance();
                armorStack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
                player.setItemSlot(ARMOR_SLOTS[i], armorStack);
            } else {
                equipStack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
            }
        }
        renderEntity(player, guiGraphics, x, y);
        renderMaterial(material, player.level(), guiGraphics, x, y);
        for (int i = 0; i < ARMOR_SLOTS.length; i++) player.setItemSlot(ARMOR_SLOTS[i], originalArmor[i]);
    }

    private static void renderEntity(LivingEntity entity, GuiGraphics guiGraphics, int x, int y) {
        float yBodyRot = entity.yBodyRot;
        float yaw = entity.getYRot();
        float pitch = entity.getXRot();
        float yHeadRot = entity.yHeadRot;
        float yHeadRotO = entity.yHeadRotO;

        float yRot = (float) Math.atan(80 * Math.cos(ArmortipUtil.ticks / 60.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * ArmortipUtil.ticks / 60.0F) / 40.0F);

        var size = ArmortipUtil.SIZE * 0.8F / Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (!(entity instanceof Player)) {
            size *= 0.8F;
        }

        Quaternionf quaternionf = new Quaternionf().rotateZ(Constants.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * Constants.DEG_TO_RAD);
        quaternionf.mul(quaternionf2);

        entity.yBodyRot = 210.0F + yRot * 10.0F;
        entity.setYRot(180.0F + yRot * 10.0F);
        entity.setXRot(-xRot * 10.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        InventoryScreen.renderEntityInInventory(guiGraphics, x + ArmortipUtil.MARGIN + ArmortipUtil.SIZE / 2.0F, y + ArmortipUtil.SIZE, size, new Vector3f(0, 0, 0), quaternionf, quaternionf2, entity);

        entity.yBodyRot = yBodyRot;
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yHeadRot = yHeadRot;
        entity.yHeadRotO = yHeadRotO;
    }

    private static void renderMaterial(Holder<TrimMaterial> material, Level level, GuiGraphics guiGraphics, int x, int y) {
        var item = getCachedMaterialItem(level, material);
        if (item == null) return;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + ArmortipUtil.SIZE - ArmortipUtil.MARGIN, y, 0);
        guiGraphics.pose().scale(0.5F, 0.5F, 0.5F);
        guiGraphics.renderFakeItem(item.value().getDefaultInstance(), 0, 0);
        guiGraphics.pose().popPose();
    }

    private static Mob getCachedEntity(Level level, EntityType<?> type) {
        return ENTITY_CACHE.computeIfAbsent(type, t -> (Mob) t.create(level, EntitySpawnReason.MOB_SUMMONED));
    }

    private static Holder<TrimPattern> getCachedTrimPattern(Level level, Item item) {
        return PATTERN_CACHE.computeIfAbsent(item, i -> {
            var itemId = BuiltInRegistries.ITEM.getKey(item);
            var trimId = itemId.toString().split("_", 2)[0];

            var registryAccess = level.registryAccess();
            var registry = registryAccess.lookupOrThrow(Registries.TRIM_PATTERN);
            return registry.listElements().filter(trim -> trim.value().assetId().toString().equals(trimId)).findFirst().orElse(null);
        });
    }

    private static Holder<TrimMaterial> getCachedTrimMaterial(Level level) {
        if (MATERIAL_CACHE == null) {
            var registryAccess = level.registryAccess();
            var registry = registryAccess.lookupOrThrow(Registries.TRIM_MATERIAL);
            MATERIAL_CACHE = registry.listElements().toList();
        }
        return MATERIAL_CACHE.get((ArmortipUtil.ticks / 40) % MATERIAL_CACHE.size());
    }

    private static Holder<Item> getCachedMaterialItem(Level level, Holder<TrimMaterial> material) {
        return ITEM_CACHE.computeIfAbsent(material, m -> {
            var registryAccess = level.registryAccess();
            var registry = registryAccess.lookupOrThrow(Registries.ITEM);
            return registry.listElements().filter(item -> {
                var materialProvider = item.value().getDefaultInstance().get(DataComponents.PROVIDES_TRIM_MATERIAL);
                if (materialProvider == null) return false;
                var itemMaterial = materialProvider.unwrap(registryAccess);
                return itemMaterial.map(trimMaterialHolder -> trimMaterialHolder.value().equals(m.value())).orElse(false);
            }).findFirst().orElse(null);});
    }
}
