package net.bmjo.armortip.client.gui.tooltip;

import net.bmjo.armortip.Armortip;
import net.bmjo.armortip.util.ArmortipUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ArmorTooltipComponent implements TooltipComponent {
    private static final Map<EntityType<?>, AnimalEntity> CACHE = new HashMap<>();
    private static final Map<Item, RegistryEntry<ArmorTrimPattern>> ARMOR_TRIM_PATTERN = new HashMap<>();
    private static final Map<RegistryKey<ArmorTrimMaterial>, RegistryEntry<ArmorTrimMaterial>> ARMOR_TRIM_MATERIAL = new HashMap<>();
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static int time;

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
        this.drawEmptyTooltip(x, y, width, context);
    }

    private void drawEmptyTooltip(int x, int y, int width, DrawContext drawContext) {
        if (ArmortipUtil.isTipItem(this.itemStack)) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null)
                return;
            this.renderEquipment(player, x, y, width, drawContext);
        }
    }

    private void renderEquipment(PlayerEntity player, int x, int y, int width, DrawContext drawContext) {
        EquippableComponent equippableComponent = this.itemStack.get(DataComponentTypes.EQUIPPABLE);
        if (equippableComponent == null)
            return;
        var slot = equippableComponent.slot();
        try {
            switch (slot.getType()) {
                case HAND, HUMANOID_ARMOR -> this.renderEntity(player, slot, x, y, width, drawContext);
                case ANIMAL_ARMOR, SADDLE ->
                        this.renderAnimal(player, slot, equippableComponent, x, y, width, drawContext);
                default -> throw new IllegalArgumentException("Item is not an equipment item");
            }
        } catch (IllegalArgumentException e) {
            Armortip.LOGGER.error("Item is not an equipment item", e);
        }
    }

    private void renderAnimal(PlayerEntity player, EquipmentSlot slot, EquippableComponent equippableComponent, int x, int y, int width, DrawContext drawContext) {
        var entities = equippableComponent.allowedEntities();
        if (entities.isEmpty())
            return;
        var animalType = entities.get().get(0).value();
        LivingEntity animal = getCached(player.getEntityWorld(), animalType);
        this.renderEntity(animal, slot, x, y, width, drawContext);
    }

    private void renderEntity(LivingEntity entity, EquipmentSlot slot, int x, int y, int width, DrawContext drawContext) {
        if (entity == null)
            return;

        ItemStack originalStack = entity.getEquippedStack(slot);


        float bodyYaw = entity.bodyYaw;
        float yaw = entity.getYaw();
        float pitch = entity.getPitch();
        float headYaw = entity.headYaw;
        float lastHeadYaw = entity.lastHeadYaw;

        float yRot = (float) Math.atan(80 * Math.cos(time / 300.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * time / 300.0F) / 40.0F);
        time++;
        time %= (int) (2 * Math.PI * 300.0F);

        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        double size = ArmortipUtil.SIZE * 0.8 / Math.max(entity.getWidth(), entity.getHeight());
        Vector3f vector3f = new Vector3f(0.0F, entity.getHeight() * 0.5F, 0.0F);
        if (entity instanceof AbstractHorseEntity) {
            size *= 0.75;
            vector3f = new Vector3f(0.0F, entity.getHeight() * 0.75F, 0.0F);
        }

        try {
            entity.bodyYaw = 200.0F + yRot * 10.0F;
            entity.setYaw(180.0F + yRot * 20.0F);
            entity.setPitch(-xRot * 10.0F);

            entity.headYaw = entity.getYaw();
            entity.lastHeadYaw = entity.getYaw();

            entity.equipStack(slot, this.itemStack);
            InventoryScreen.drawEntity(drawContext, x + width - ArmortipUtil.SIZE, y - 10, x + width, y - 10 + ArmortipUtil.SIZE, (float) size, vector3f, quaternionf, quaternionf2, entity);
        } finally {
            entity.equipStack(slot, originalStack);

            entity.bodyYaw = bodyYaw;
            entity.setYaw(yaw);
            entity.setPitch(pitch);
            entity.headYaw = headYaw;
            entity.lastHeadYaw = lastHeadYaw;
        }
    }

    private static <E extends LivingEntity> E getCached(World world, EntityType<?> type) {
        return (E) CACHE.computeIfAbsent(type, t -> (AnimalEntity) t.create(world, SpawnReason.COMMAND));
    }
}
