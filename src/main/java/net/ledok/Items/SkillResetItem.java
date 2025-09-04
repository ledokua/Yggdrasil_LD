package net.ledok.Items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SkillResetItem extends Item {

    public SkillResetItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Код виконується тільки на сервері, щоб уникнути помилок
        if (!world.isClient) {
            // Отримуємо "джерело" команди від гравця
            ServerCommandSource source = user.getCommandSource();
            // Ваша команда
            String command = "puffish_skills skills reset @s puffish_skills:minestar";

            // Виконуємо команду
            world.getServer().getCommandManager().execute(source.withSilent(), command);

            // Забираємо 1 предмет, якщо гравець не в креативі
            if (!user.getAbilities().creativeMode) {
                user.getStackInHand(hand).decrement(1);
            }

            // Повертаємо успішний результат
            return TypedActionResult.success(user.getStackInHand(hand));
        }

        // Якщо це клієнт, нічого не робимо
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}