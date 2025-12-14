package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorTooltipComponent implements TooltipComponent {
    private static final Map<EntityType<?>, LivingEntity> ENTITY_CACHE = new HashMap<>();
    private static final Map<Item, RegistryEntry<ArmorTrimPattern>> PATTERN_CACHE = new HashMap<>();
    private static List<RegistryEntry.Reference<ArmorTrimMaterial>> MATERIAL_CACHE;
    private static final Map<RegistryEntry<ArmorTrimMaterial>, RegistryEntry<Item>> ITEM_CACHE = new HashMap<>();
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private static final Item[] DEFAULT_ARMOR = {Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET};

    private final ItemStack itemStack;

    public ArmorTooltipComponent(ArmorTooltipData data) {
        this.itemStack = data.itemStack();
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return 0;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return 0;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
        if (ArmortipUtil.isTipItem(this.itemStack)) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null)
                return;
            if (itemStack.getItem() instanceof SmithingTemplateItem)
                this.renderTrim(player, x, y, width, context);
            else
                this.renderEquipment(player, x, y, width, context);
        }
    }

    private void renderEquipment(PlayerEntity player, int x, int y, int width, DrawContext drawContext) {
        EquippableComponent equippableComponent = this.itemStack.get(DataComponentTypes.EQUIPPABLE);
        if (equippableComponent != null) {
            var slot = equippableComponent.slot();
            switch (slot.getType()) {
                case HAND, HUMANOID_ARMOR -> this.renderPlayer(player, slot, x, y, width, drawContext);
                case ANIMAL_ARMOR, SADDLE -> this.renderAnimal(player, slot, equippableComponent, x, y, width, drawContext);
            }
            return;
        }
        this.renderPlayer(player, EquipmentSlot.MAINHAND, x, y, width, drawContext);
    }

    private void renderPlayer(PlayerEntity player, EquipmentSlot slot, int x, int y, int width, DrawContext drawContext) {
        ItemStack originalStack = player.getEquippedStack(slot);
        player.equipStack(slot, this.itemStack);
        this.renderEntity(player, x, y, width, drawContext);
        player.equipStack(slot, originalStack);
    }

    private void renderAnimal(PlayerEntity player, EquipmentSlot slot, EquippableComponent equippableComponent, int x, int y, int width, DrawContext drawContext) {
        var entities = equippableComponent.allowedEntities();
        if (entities.isEmpty())
            return;
        var animalType = entities.get().get(0).value();
        LivingEntity animal = getCachedEntity(player.getEntityWorld(), animalType);
        ItemStack originalStack = animal.getEquippedStack(slot);
        animal.equipStack(slot, this.itemStack);
        this.renderEntity(animal, x, y, width, drawContext);
        animal.equipStack(slot, originalStack);
    }

    private void renderTrim(PlayerEntity player, int x, int y, int width, DrawContext drawContext) {
        var pattern = getCachedTrimPattern(player.getEntityWorld(), this.itemStack.getItem());
        var material = getCachedTrimMaterial(player.getEntityWorld());
        if (pattern == null || material == null) return;

        ItemStack[] originalArmor = new ItemStack[4];
        for (int i = 0; i < ARMOR_SLOTS.length; i++) originalArmor[i] = player.getEquippedStack(ARMOR_SLOTS[i]).copy();
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            var itemStack = player.getEquippedStack(ARMOR_SLOTS[i]);
            if (itemStack.isEmpty()) {
                var armor = DEFAULT_ARMOR[i].getDefaultStack();
                armor.set(DataComponentTypes.TRIM, new ArmorTrim(material, pattern));
                player.equipStack(ARMOR_SLOTS[i], armor);
            } else {
                itemStack.set(DataComponentTypes.TRIM, new ArmorTrim(material, pattern));
            }
        }
        renderEntity(player, x, y, width, drawContext);
        renderMaterial(material, x, y, width, drawContext, player.getEntityWorld());
        for (int i = 0; i < ARMOR_SLOTS.length; i++) player.equipStack(ARMOR_SLOTS[i], originalArmor[i]);
    }

    private void renderEntity(LivingEntity entity, int x, int y, int width, DrawContext drawContext) {
        if (entity == null)
            return;

        float yRot = (float) Math.atan(80 * Math.cos(ArmortipUtil.ticks / 64.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * ArmortipUtil.ticks / 64.0F) / 40.0F);

        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        var size = ArmortipUtil.SIZE * 0.8F / Math.max(entity.getWidth(), entity.getHeight());
        if (!(entity instanceof PlayerEntity)) {
            size *= 0.8F;
        }

        Vector3f vector3f = new Vector3f(0.0F, entity.getHeight() * 0.5F, 0.0F);
        if (entity instanceof AbstractHorseEntity) {
            vector3f = new Vector3f(0.0F, entity.getHeight() * 0.75F, 0.0F);
        }

        var entityRenderState = getEntityRenderState(entity);
        if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
            livingEntityRenderState.bodyYaw = 200.0F + yRot * 10.0F;
            livingEntityRenderState.relativeHeadYaw = yRot * 5.0F;
            livingEntityRenderState.pitch = -xRot * 10.0F;

            livingEntityRenderState.width /= livingEntityRenderState.baseScale;
            livingEntityRenderState.height /= livingEntityRenderState.baseScale;
            livingEntityRenderState.baseScale = 1;
        }
        drawContext.addEntity(entityRenderState, size, vector3f, quaternionf, quaternionf2, -ArmortipUtil.PADDING + x  + width - ArmortipUtil.SIZE, -ArmortipUtil.PADDING + y - 10, ArmortipUtil.PADDING + x + width, ArmortipUtil.PADDING + y - 10 + ArmortipUtil.SIZE);
    }

    private void renderMaterial(RegistryEntry<ArmorTrimMaterial> material, int x, int y, int width, DrawContext drawContext, World world) {
        var item = getCachedMaterialItem(world, material);
        if (item == null) return;

        drawContext.getMatrices().pushMatrix();
        drawContext.getMatrices().translate(x + width - ArmortipUtil.MARGIN * 2, y - 10);
        drawContext.getMatrices().scale(0.5F);
        drawContext.drawItem(item.value().getDefaultStack(), 0, 0);
        drawContext.getMatrices().popMatrix();
    }

    private static EntityRenderState getEntityRenderState(LivingEntity entity) {
        EntityRenderManager entityRenderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> entityRenderer = entityRenderManager.getRenderer(entity);
        EntityRenderState entityRenderState = entityRenderer.getAndUpdateRenderState(entity, 1.0F);
        entityRenderState.light = 15728880;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        return entityRenderState;
    }

    private static LivingEntity getCachedEntity(World world, EntityType<?> type) {
        return ENTITY_CACHE.computeIfAbsent(type, t -> (LivingEntity) t.create(world, SpawnReason.COMMAND));
    }

    @Nullable
    private static RegistryEntry<ArmorTrimPattern> getCachedTrimPattern(World world, Item item) {
        return PATTERN_CACHE.computeIfAbsent(item, i -> {
            var itemId = Registries.ITEM.getId(item);
            var trimId = itemId.toString().split("_", 2)[0];

            var registryAccess = world.getRegistryManager();
            var registry = registryAccess.getOrThrow(RegistryKeys.TRIM_PATTERN);
            return registry.streamEntries().filter(trim -> trim.value().assetId().toString().equals(trimId)).findFirst().orElse(null);
        });
    }

    private static RegistryEntry<ArmorTrimMaterial> getCachedTrimMaterial(World world) {
        if (MATERIAL_CACHE == null) {
            var registryAccess = world.getRegistryManager();
            var registry = registryAccess.getOrThrow(RegistryKeys.TRIM_MATERIAL);
            MATERIAL_CACHE = registry.streamEntries().toList();
        }
        return MATERIAL_CACHE.get((ArmortipUtil.ticks / 40) % MATERIAL_CACHE.size());
    }

    private static RegistryEntry<Item> getCachedMaterialItem(World world, RegistryEntry<ArmorTrimMaterial> material) {
        return ITEM_CACHE.computeIfAbsent(material, m -> {
            var registryAccess = world.getRegistryManager();
            var registry = registryAccess.getOrThrow(RegistryKeys.ITEM);
            return registry.streamEntries().filter(item -> {
                var materialProvider = item.value().getDefaultStack().get(DataComponentTypes.PROVIDES_TRIM_MATERIAL);
                if (materialProvider == null) return false;
                var itemMaterial = materialProvider.getMaterial(registryAccess);
                return itemMaterial.map(trimMaterialHolder -> trimMaterialHolder.value().equals(m.value())).orElse(false);
            }).findFirst().orElse(null);});
    }
}
