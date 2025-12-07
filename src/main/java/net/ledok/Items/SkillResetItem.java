package net.ledok.Items;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class SkillResetItem extends Item {

    public SkillResetItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide && world instanceof ServerLevel serverLevel) {
            CommandSourceStack source = serverLevel.getServer().createCommandSourceStack();
            String playerName = user.getGameProfile().getName();
            String command = String.format("puffish_skills skills reset %s puffish_skills:minestar", playerName);

            serverLevel.getServer().getCommands().performPrefixedCommand(source, command);

            if (!user.getAbilities().instabuild) {
                user.getItemInHand(hand).shrink(1);
            }
            return InteractionResultHolder.success(user.getItemInHand(hand));
        }
        return InteractionResultHolder.pass(user.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.yggdrasil_ld.healing_potion.tooltip.reset_scroll")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, type);
    }
}
