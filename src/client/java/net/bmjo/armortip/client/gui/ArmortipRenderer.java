package net.bmjo.armortip.client.gui;

import com.mojang.math.Constants;
import net.bmjo.armortip.util.ArmortipUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WoolCarpetBlock;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ArmortipRenderer {
    private static final Item[] DEFAULT_ARMOR = {Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET};
    private static final Map<EntityType<?>, Mob> CACHE = new HashMap<>();

    public static void renderArmorTip(GuiGraphics guiGraphics, ItemStack armorStack, int mouseX, int mouseY, Player player, ClientTooltipPositioner tooltipPositioner, boolean drawBG) {
        if (!(ArmortipUtil.isTipItem(armorStack)))
            return;
        var vector2ic = tooltipPositioner.positionTooltip(guiGraphics.guiWidth(), guiGraphics.guiHeight(), mouseX, mouseY, ArmortipUtil.WIDTH, ArmortipUtil.HEIGHT);

        int x = vector2ic.x();
        int y = vector2ic.y();
        guiGraphics.pose().pushPose();

        if (drawBG)
            TooltipRenderUtil.renderTooltipBackground(guiGraphics, x, y, ArmortipUtil.WIDTH + ArmortipUtil.MARGIN * 2, ArmortipUtil.HEIGHT, 400);
        guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);

        renderEquipment(player, guiGraphics, armorStack, x, y);
        guiGraphics.pose().popPose();
    }

    private static void renderEquipment(Player player, GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        switch (itemStack.getItem()) {
            case Equipable equipable -> {
                var slot = equipable.getEquipmentSlot();
                switch (slot.getType()) {
                    case HAND -> renderPlayerEquipment(player, guiGraphics, itemStack, slot, x, y);
                    case HUMANOID_ARMOR -> renderPlayerArmor(player, guiGraphics, itemStack, slot, x, y);
                    case ANIMAL_ARMOR -> renderAnimal(player, guiGraphics, itemStack, x, y);
                }
            }
            case BlockItem blockItem when blockItem.getBlock() instanceof Equipable equipable -> {
                var slot = equipable.getEquipmentSlot();
                switch (slot.getType()) {
                    case HAND -> renderPlayerEquipment(player, guiGraphics, itemStack, slot, x, y);
                    case HUMANOID_ARMOR -> renderPlayerArmor(player, guiGraphics, itemStack, slot, x, y);
                    case ANIMAL_ARMOR -> renderAnimal(player, guiGraphics, itemStack, x, y);
                }
            }
            case SmithingTemplateItem ignored ->
                    renderArmorTrim(player, guiGraphics, itemStack, x, y);
            default -> {
            }
        }
    }

    private static void renderPlayerArmor(Player player, GuiGraphics guiGraphics, ItemStack itemStack, EquipmentSlot slot, int x, int y) {
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
            return;
        var inventory = player.getInventory().armor;
        int slotId = slot.getIndex();
        var originalArmor = inventory.get(slotId);
        inventory.set(slotId, itemStack);
        renderEntity(player, guiGraphics, x, y);
        inventory.set(slotId, originalArmor);
    }

    private static void renderPlayerEquipment(Player player, GuiGraphics guiGraphics, ItemStack itemStack, EquipmentSlot slot, int x, int y) {
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND)
            return;
        var inventory = slot == EquipmentSlot.MAINHAND ? player.getInventory().items : player.getInventory().offhand;
        ItemStack originalArmor = inventory.getFirst();
        inventory.set(0, itemStack);
        renderEntity(player, guiGraphics, x, y);
        inventory.set(0, originalArmor);
    }

    private static void renderArmorTrim(Player player, GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        var armor = player.getInventory().armor;
        var originalArmor = List.copyOf(armor);

        var wrapperLookup = player.level().registryAccess();
        var optMaterial = TrimMaterials.getFromIngredient(wrapperLookup, Items.DIAMOND.getDefaultInstance());
        var optPattern = TrimPatterns.getFromTemplate(wrapperLookup, itemStack);

        if (optMaterial.isEmpty() || optPattern.isEmpty()) return;

        for (int i = 0; i < armor.size(); i++) {
            var trimStack = armor.get(i).copy();
            if (trimStack.isEmpty()) {
                var armorStack = DEFAULT_ARMOR[i].getDefaultInstance();
                armorStack.set(DataComponents.TRIM, new ArmorTrim(optMaterial.get(), optPattern.get()));
                armor.set(i, armorStack);
            } else {
                trimStack.set(DataComponents.TRIM, new ArmorTrim(optMaterial.get(), optPattern.get()));
                armor.set(i, trimStack);
            }
        }
        renderEntity(player, guiGraphics, x, y);
        for (int i = 0; i < armor.size(); i++) {
            armor.set(i, originalArmor.get(i));
        }
    }

    private static void renderAnimal(Player player, GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        if (itemStack.getItem() instanceof AnimalArmorItem animalArmorItem) {
            var type = animalArmorItem.getBodyType();
            switch (type) {
                case EQUESTRIAN -> renderAnimal(player, EntityType.HORSE, guiGraphics, itemStack, x, y);
                case CANINE -> renderAnimal(player, EntityType.WOLF, guiGraphics, itemStack, x, y);
            }
        } else if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof WoolCarpetBlock) {
            renderAnimal(player, EntityType.LLAMA, guiGraphics, itemStack, x, y);
        }
    }

    private static void renderAnimal(Player player, EntityType<? extends Mob> animalType, GuiGraphics guiGraphics, ItemStack armorStack, int x, int y) {
        var animal = getCached(player.level(), animalType);
        if (animal == null)
            return;
        animal.setBodyArmorItem(armorStack);
        renderEntity(animal, guiGraphics, x, y);
        animal.setBodyArmorItem(ItemStack.EMPTY);
    }

    private static void renderEntity(LivingEntity entity, GuiGraphics guiGraphics, int x, int y) {
        float yBodyRot = entity.yBodyRot;
        float yaw = entity.getYRot();
        float pitch = entity.getXRot();
        float yHeadRot = entity.yHeadRot;
        float yHeadRotO = entity.yHeadRotO;

        float yRot = (float) Math.atan(80 * Math.cos(ArmortipUtil.ticks / 60.0F) / 40.0F);
        float xRot = (float) Math.atan(20 * Math.sin(2 * ArmortipUtil.ticks / 60.0F) / 40.0F);

        var size = ArmortipUtil.HEIGHT * 0.8F / Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (!(entity instanceof Player)) {
            size *= 0.8F;
        }

        Quaternionf quaternionf = new Quaternionf().rotateZ(Constants.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0F * Constants.DEG_TO_RAD);
        quaternionf.mul(quaternionf2);

        entity.yBodyRot = 200.0F + yRot * 10.0F;
        entity.setYRot(180.0F + yRot * 10.0F);
        entity.setXRot(-xRot * 10.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        InventoryScreen.renderEntityInInventory(guiGraphics, x + ArmortipUtil.MARGIN + ArmortipUtil.WIDTH / 2.0F, y + ArmortipUtil.HEIGHT, size, new Vector3f(0, 0, 0), quaternionf, quaternionf2, entity);

        entity.yBodyRot = yBodyRot;
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yHeadRot = yHeadRot;
        entity.yHeadRotO = yHeadRotO;
    }

    private static Mob getCached(Level level, EntityType<? extends Mob> type) {
        return CACHE.computeIfAbsent(type, t -> (Mob) t.create(level));
    }
}
