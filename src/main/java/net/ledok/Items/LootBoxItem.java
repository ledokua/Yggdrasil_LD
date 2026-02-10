package net.ledok.Items;

import net.ledok.registry.LootBoxDefinition;
import net.ledok.registry.LootBoxRegistry;
import net.ledok.registry.ModDataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public class LootBoxItem extends Item {

    public LootBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        String id = stack.get(ModDataComponents.LOOT_BOX_ID);
        if (id != null) {
            LootBoxDefinition def = LootBoxRegistry.getDefinition(id);
            if (def != null) {
                return Component.literal(def.name());
            }
        }
        return super.getName(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        String id = stack.get(ModDataComponents.LOOT_BOX_ID);
        if (id != null) {
            LootBoxDefinition def = LootBoxRegistry.getDefinition(id);
            if (def != null) {
                return def.glow();
            }
        }
        return super.isFoil(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (world.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        String id = stack.get(ModDataComponents.LOOT_BOX_ID);
        if (id == null) {
            user.displayClientMessage(Component.literal("Loot box is invalid: Missing ID."), true);
            return InteractionResultHolder.fail(stack);
        }

        LootBoxDefinition def = LootBoxRegistry.getDefinition(id);
        if (def == null) {
             user.displayClientMessage(Component.literal("Unknown loot box ID: " + id), true);
             return InteractionResultHolder.fail(stack);
        }

        ResourceLocation lootTableIdRL = ResourceLocation.tryParse(def.lootTableId());
        if (lootTableIdRL == null) {
            user.displayClientMessage(Component.literal("Invalid loot table ID: " + def.lootTableId()), true);
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel serverLevel = (ServerLevel) world;
        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableIdRL);
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableKey);

        if (lootTable == LootTable.EMPTY) {
            user.displayClientMessage(Component.literal("Loot table is empty or invalid: " + def.lootTableId()), true);
            return InteractionResultHolder.fail(stack);
        }

        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, user)
                .withParameter(LootContextParams.ORIGIN, user.position())
                .create(LootContextParamSets.GIFT);

        List<ItemStack> loot = lootTable.getRandomItems(lootParams);

        if (loot.isEmpty()) {
            user.displayClientMessage(Component.literal("Loot box was empty!"), true);
        } else {
            for (ItemStack itemStack : loot) {
                if (!user.getInventory().add(itemStack)) {
                    user.drop(itemStack, false);
                }
            }
        }

        stack.consume(1, user);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        String id = stack.get(ModDataComponents.LOOT_BOX_ID);
        if (id != null) {
            LootBoxDefinition def = LootBoxRegistry.getDefinition(id);
            if (def != null) {
                tooltip.add(Component.translatable("tooltip.yggdrasil_ld.loot_box.contains").withStyle(net.minecraft.ChatFormatting.GRAY));
                tooltip.add(Component.literal(" " + def.lootTableId()).withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
            }
        }
    }
}
