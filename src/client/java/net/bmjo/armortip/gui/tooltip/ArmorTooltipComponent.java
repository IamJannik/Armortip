package net.bmjo.armortip.gui.tooltip;

import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorTooltipComponent implements ClientTooltipComponent {
    private static final Map<EntityType<?>, LivingEntity> ENTITY_CACHE = new HashMap<>();
    private static final Map<Item, Holder<TrimPattern>> PATTERN_CACHE = new HashMap<>();
    private static List<Holder.Reference<TrimMaterial>> MATERIAL_CACHE;
    private static final Map<Holder<TrimMaterial>, Holder<Item>> ITEM_CACHE = new HashMap<>();
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private static final Item[] DEFAULT_ARMOR = {Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET};

    private final ItemStack itemStack;

    public ArmorTooltipComponent(ArmorTooltipData data) {
        this.itemStack = data.itemStack();
    }

    @Override
    public int getHeight(@NotNull Font font) {
        return 0;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return 0;
    }

    @Override
    public void extractImage(@NotNull Font font, int x, int y, int width, int height, @NotNull GuiGraphicsExtractor gui) {
        if (ArmortipUtil.isTipItem(this.itemStack)) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null)
                return;
            if (itemStack.getItem() instanceof SmithingTemplateItem)
                this.renderTrim(player, x, y, width, gui);
            /*
            else if (itemStack.has(DataComponents.PROVIDES_BANNER_PATTERNS))
                this.renderBanner(x, y, width, gui);
            else if (itemStack.has(DataComponents.ENTITY_DATA))
                this.renderEgg(player, x, y, width, gui);
             */
            else
                this.renderEquipment(player, x, y, width, gui);
        }
    }

    private void renderEquipment(Player player, int x, int y, int width, GuiGraphicsExtractor gui) {
        Equippable equippableComponent = this.itemStack.get(DataComponents.EQUIPPABLE);
        if (equippableComponent != null) {
            var slot = equippableComponent.slot();
            switch (slot.getType()) {
                case HAND, HUMANOID_ARMOR -> this.renderPlayer(player, slot, x, y, width, gui);
                case ANIMAL_ARMOR, SADDLE ->
                        this.renderAnimal(player, slot, equippableComponent, x, y, width, gui);
                default -> throw new IllegalArgumentException("Item is not an equipment item");
            }
            return;
        }
        this.renderPlayer(player, EquipmentSlot.MAINHAND, x, y, width, gui);
        }

    private void renderPlayer(Player player, EquipmentSlot slot, int x, int y, int width, GuiGraphicsExtractor gui) {
        ItemStack originalStack = player.getItemBySlot(slot);
        player.setItemSlot(slot, this.itemStack);
        this.renderEntity(player, x, y, width, gui);
        player.setItemSlot(slot, originalStack);
    }

    private void renderAnimal(Player player, EquipmentSlot slot, Equippable equippableComponent, int x, int y, int width, GuiGraphicsExtractor gui) {
        var entities = equippableComponent.allowedEntities();
        if (entities.isEmpty())
            return;
        var animalType = entities.get().get(0).value();
        LivingEntity animal = getCachedEntity(player.level(), animalType);
        ItemStack originalStack = animal.getItemBySlot(slot);
        animal.setItemSlot(slot, this.itemStack);
        this.renderEntity(animal, x, y, width, gui);
        animal.setItemSlot(slot, originalStack);
    }

    private void renderTrim(Player player, int x, int y, int width, GuiGraphicsExtractor gui) {
        var pattern = getCachedTrimPattern(player.level(), this.itemStack.getItem());
        var material = getCachedTrimMaterial(player.level());
        if (pattern == null || material == null) return;

        ItemStack[] originalArmor = new ItemStack[4];
        for (int i = 0; i < ARMOR_SLOTS.length; i++) originalArmor[i] = player.getItemBySlot(ARMOR_SLOTS[i]).copy();
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            var itemStack = player.getItemBySlot(ARMOR_SLOTS[i]);
            if (itemStack.isEmpty()) {
                var armor = DEFAULT_ARMOR[i].getDefaultInstance();
                armor.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
                player.setItemSlot(ARMOR_SLOTS[i], armor);
            } else {
                itemStack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
            }
        }
        renderEntity(player, x, y, width, gui);
        renderMaterial(material, x, y, width, gui, player.level());
        for (int i = 0; i < ARMOR_SLOTS.length; i++) player.setItemSlot(ARMOR_SLOTS[i], originalArmor[i]);
    }

    private void renderEntity(LivingEntity entity, int x, int y, int width, GuiGraphicsExtractor gui) {
        if (entity == null)
            return;

        float yRot = (float) Math.atan(80 * Math.cos(ArmortipUtil.ticks / 64.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * ArmortipUtil.ticks / 64.0F) / 40.0F);

        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        var size = ArmortipUtil.SIZE * 0.8F / Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (!(entity instanceof Player)) {
            size *= 0.8F;
        }

        Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() * 0.5F, 0.0F);
        if (entity instanceof AbstractHorse) {
            vector3f = new Vector3f(0.0F, entity.getBbHeight() * 0.75F, 0.0F);
        }

        var entityRenderState = getEntityRenderState(entity);
        if (entityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
            livingEntityRenderState.bodyRot = 200.0F + yRot * 10.0F;
            livingEntityRenderState.yRot = yRot * 5.0F;
            livingEntityRenderState.xRot = -xRot * 10.0F;

            livingEntityRenderState.boundingBoxWidth /= livingEntityRenderState.scale;
            livingEntityRenderState.boundingBoxHeight /= livingEntityRenderState.scale;
            livingEntityRenderState.scale = 1;
        }
        gui.entity(entityRenderState, size, vector3f, quaternionf, quaternionf2, -ArmortipUtil.PADDING_X + x + width - ArmortipUtil.SIZE, -ArmortipUtil.PADDING_Y + y - 10, -ArmortipUtil.PADDING_X + x + width, ArmortipUtil.PADDING_Y + y - 10 + ArmortipUtil.SIZE);

    }

    private void renderMaterial(Holder<TrimMaterial> material, int x, int y, int width, GuiGraphicsExtractor gui, Level world) {
        var item = getCachedMaterialItem(world, material);
        if (item == null) return;

        gui.pose().pushMatrix();
        gui.pose().translate(x + width - ArmortipUtil.MARGIN * 2, y - 10);
        gui.pose().scale(0.5F);
        gui.item(item.value().getDefaultInstance(), 0, 0);
        gui.pose().popMatrix();
    }

    /*
    private void renderEgg(Player player, int x, int y, int width, GuiGraphicsExtractor gui) {
        var type = getEntityType(itemStack);
        if (type == null) return;
        var entity = getCachedEntity(player.level(), type);
        renderEntity(entity, x, y, width, gui);
    }

    private void renderBanner(int x, int y, int width, GuiGraphicsExtractor gui) {
        var pattern = getBannerPattern(itemStack);
        if (pattern == null) return;

        var layer = new BannerPatternLayers.Layer(pattern, DyeColor.BLACK);
        var bannerPatternLayers = new BannerPatternLayers(List.of(layer));

        var modelPart = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG);
        var flag = new BannerFlagModel(modelPart);
        gui.bannerPattern(flag, DyeColor.WHITE, bannerPatternLayers, -ArmortipUtil.PADDING_X + x + width - ArmortipUtil.SIZE, 0, -ArmortipUtil.PADDING_X + x + width, y - 15 + ArmortipUtil.SIZE);
    }
     */

    private static EntityRenderState getEntityRenderState(LivingEntity entity) {
        var entityRenderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        var entityRenderer = entityRenderManager.getRenderer(entity);
        var entityRenderState = entityRenderer.createRenderState(entity, 1.0F);
        entityRenderState.lightCoords = 15728880;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        return entityRenderState;
    }

    private static LivingEntity getCachedEntity(Level level, EntityType<?> type) {
        return ENTITY_CACHE.computeIfAbsent(type, entityType -> (LivingEntity) entityType.create(level, EntitySpawnReason.MOB_SUMMONED));
    }

    @Nullable
    private static Holder<TrimPattern> getCachedTrimPattern(Level world, Item item) {
        return PATTERN_CACHE.computeIfAbsent(item, i -> {
            var itemId = BuiltInRegistries.ITEM.getKey(i);
            var trimId = itemId.toString().split("_", 2)[0];

            var registryAccess = world.registryAccess();
            var registry = registryAccess.lookupOrThrow(Registries.TRIM_PATTERN);
            return registry.listElements().filter(trim -> trim.value().assetId().toString().equals(trimId)).findFirst().orElse(null);
        });
    }

    private static Holder<TrimMaterial> getCachedTrimMaterial(Level world) {
        if (MATERIAL_CACHE == null) {
            var registryAccess = world.registryAccess();
            var registry = registryAccess.lookupOrThrow(Registries.TRIM_MATERIAL);
            MATERIAL_CACHE = registry.listElements().toList();
        }
        return MATERIAL_CACHE.get((ArmortipUtil.ticks / 40) % MATERIAL_CACHE.size());
    }

    private static Holder<Item> getCachedMaterialItem(Level world, Holder<TrimMaterial> material) {
        return ITEM_CACHE.computeIfAbsent(material, m -> {
            var registryAccess = world.registryAccess();
            var registry = registryAccess.lookupOrThrow(Registries.ITEM);
            return registry.listElements().filter(item -> {
                var materialProvider = item.value().getDefaultInstance().get(DataComponents.PROVIDES_TRIM_MATERIAL);
                if (materialProvider == null) return false;
                return materialProvider.value().equals(m.value());
            }).findFirst().orElse(null);});
    }

    /*
    private static Holder<BannerPattern> getBannerPattern(final ItemStack patternStack) {
        var itemPatterns = patternStack.get(DataComponents.PROVIDES_BANNER_PATTERNS);
        return itemPatterns != null && itemPatterns.size() > 0 ? itemPatterns.get(0) : null;
    }

    public static EntityType<?> getEntityType(final ItemStack itemStack) {
        var entityData = itemStack.get(DataComponents.ENTITY_DATA);
        return entityData != null ? entityData.type() : null;
    }
     */
}