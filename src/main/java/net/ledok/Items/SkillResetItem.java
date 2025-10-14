package net.ledok.Items;

import com.mojang.brigadier.ParseResults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class SkillResetItem extends Item {

    public SkillResetItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            MinecraftServer server = world.getServer();
            if (server == null) {
                return TypedActionResult.fail(user.getStackInHand(hand));
            }

            ServerCommandSource source = server.getCommandSource();

            String playerName = user.getName().getString();
            String command = String.format("puffish_skills skills reset %s puffish_skills:minestar", playerName);

            // ---------------------

            CommandManager commandManager = server.getCommandManager();
            ParseResults<ServerCommandSource> parseResults = commandManager.getDispatcher().parse(command, source.withSilent());
            commandManager.execute(parseResults, command);

            if (!user.getAbilities().creativeMode) {
                user.getStackInHand(hand).decrement(1);
            }

            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal(""));

        tooltip.add(Text.translatable("item.yggdrasil_ld.healing_potion.tooltip.reset_scroll")
                .formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}