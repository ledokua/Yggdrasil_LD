package net.ledok.placeholder;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.ledok.Yggdrasil_ld;
import net.ledok.reputation.ReputationManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class ReputationPlaceholders {
    public static void register() {
        // Реєструємо плейсхолдери для топ гравців
        // ВИПРАВЛЕНО: Використовуємо Identifier.of() замість new Identifier()
        Placeholders.register(Identifier.of(Yggdrasil_ld.MOD_ID, "top"), (ctx, arg) -> {
            if (arg != null) {
                try {
                    int rank = Integer.parseInt(arg) - 1; // Користувачі вводять 1, 2, 3..., а нам потрібні індекси 0, 1, 2...
                    if (rank >= 0) {
                        return getRankingPlaceholder(ctx, rank, false); // false = не за зростанням (топ позитивних)
                    }
                } catch (NumberFormatException ignored) {}
            }
            return PlaceholderResult.invalid("Invalid rank! Use format like 'top_1'");
        });

        // Реєструємо плейсхолдери для "дна" гравців
        // ВИПРАВЛЕНО: Використовуємо Identifier.of() замість new Identifier()
        Placeholders.register(Identifier.of(Yggdrasil_ld.MOD_ID, "bottom"), (ctx, arg) -> {
            if (arg != null) {
                try {
                    int rank = Integer.parseInt(arg) - 1;
                    if (rank >= 0) {
                        return getRankingPlaceholder(ctx, rank, true); // true = за зростанням (топ негативних)
                    }
                } catch (NumberFormatException ignored) {}
            }
            return PlaceholderResult.invalid("Invalid rank! Use format like 'bottom_1'");
        });
    }

    private static PlaceholderResult getRankingPlaceholder(PlaceholderContext ctx, int rank, boolean ascending) {
        MinecraftServer server = ctx.server();
        if (server == null) return PlaceholderResult.invalid("Server not available");

        List<ReputationManager.ReputationEntry> ranking = ReputationManager.getRankings(server, ascending);

        if (rank < ranking.size()) {
            ReputationManager.ReputationEntry entry = ranking.get(rank);
            // Плейсхолдер повертає ім'я гравця та його репутацію
            return PlaceholderResult.value(Text.literal(entry.name + " (").append(
                    Text.literal(String.valueOf(entry.reputation)).formatted(entry.reputation >= 0 ? Formatting.GREEN : Formatting.RED)
            ).append(Text.literal(")")));
        }

        return PlaceholderResult.value(Text.literal("-")); // Повертаємо прочерк, якщо гравець на цьому місці відсутній
    }
}

