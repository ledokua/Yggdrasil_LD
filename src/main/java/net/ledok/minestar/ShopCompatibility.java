package net.ledok.minestar;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ua.com.minestar.model.ShopProduct;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ledok.YggdrasilLdMod.CONFIG;
import static net.ledok.YggdrasilLdMod.LOGGER;
import static ua.com.minestar.Minestar.minestar;

public class ShopCompatibility {

    private static final int SERVER_ID = CONFIG.server_id;

    /**
     * Notifies a player on join if they have pending products to claim.
     * @param player The player who just joined the server.
     */
    public static void notifyOnJoin(ServerPlayerEntity player) {
        minestar.getUserPendingShopProductsByProfileUuidAndServerId(player.getUuid(), SERVER_ID)
                .onSuccess(products -> {
                    if (!products.isEmpty()) {
                        Text message = Text.translatable("message.yggdrasil_ld.shop.unclaimed_items", products.size())
                                .formatted(Formatting.GOLD)
                                .append(Text.translatable("message.yggdrasil_ld.shop.claim_button")
                                        .formatted(Formatting.AQUA, Formatting.UNDERLINE)
                                        .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shop-receive")))
                                )
                                .append(Text.translatable("message.yggdrasil_ld.shop.claim_suffix").formatted(Formatting.GOLD));

                        player.sendMessage(message, false);
                    }
                })
                .onFailure(cause -> LOGGER.error("[Shop Sync] Failed to check for pending products on join for {}.", player.getName().getString(), cause));
    }

    public static void claimPurchases(ServerPlayerEntity player) {
        player.sendMessage(Text.translatable("message.yggdrasil_ld.shop.checking").formatted(Formatting.GRAY));
        LOGGER.info("[Shop Sync] Checking for pending products for player {}.", player.getName().getString());

        minestar.getUserPendingShopProductsByProfileUuidAndServerId(player.getUuid(), SERVER_ID)
                .onSuccess(products -> {
                    if (products.isEmpty()) {
                        player.sendMessage(Text.translatable("message.yggdrasil_ld.shop.no_items").formatted(Formatting.YELLOW));
                        LOGGER.info("[Shop Sync] No pending products found for {}.", player.getName().getString());
                        return;
                    }
                    player.sendMessage(Text.translatable("message.yggdrasil_ld.shop.found_items", products.size()).formatted(Formatting.GREEN));
                    LOGGER.info("[Shop Sync] Found {} pending products for {}.", products.size(), player.getName().getString());
                    processAndConfirmDelivery(player, products);
                })
                .onFailure(cause -> {
                    player.sendMessage(Text.translatable("message.yggdrasil_ld.shop.error").formatted(Formatting.RED));
                    LOGGER.error("[Shop Sync] Failed to get pending products for {}.", player.getName().getString(), cause);
                });
    }

    private static void processAndConfirmDelivery(ServerPlayerEntity player, List<ShopProduct> products) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        Set<Long> deliveredProductIds = new HashSet<>();

        for (ShopProduct product : products) {
            boolean allCommandsSuccessful = true;
            // Execute commands based on the purchased quantity
            for (int i = 0; i < product.quantity(); i++) {
                for (String commandTemplate : product.commands()) {
                    try {
                        String command = commandTemplate.replace("{player}", player.getName().getString());
                        ServerCommandSource source = server.getCommandSource();
                        CommandManager commandManager = server.getCommandManager();
                        commandManager.execute(commandManager.getDispatcher().parse(command, source), command);
                        LOGGER.info("[Shop Sync] Executed command for product ID {} (Item {} of {}): {}", product.id(), i + 1, product.quantity(), command);
                    } catch (Exception e) {
                        allCommandsSuccessful = false;
                        LOGGER.error("[Shop Sync] Failed to execute a command for product ID {}.", product.id(), e);
                        break; // Stop processing commands for this product if one fails
                    }
                }
                if (!allCommandsSuccessful) {
                    break; // Stop processing quantities for this product
                }
            }

            if (allCommandsSuccessful) {
                deliveredProductIds.add(product.id());
            }
        }

        if (!deliveredProductIds.isEmpty()) {
            LOGGER.info("[Shop Sync] Confirming delivery of {} products for {}.", deliveredProductIds.size(), player.getName().getString());
            minestar.deleteUserPendingShopProductsByIds(deliveredProductIds)
                    .onSuccess(unit -> {
                        String ids = deliveredProductIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
                        LOGGER.info("[Shop Sync] Successfully confirmed delivery of product IDs [{}] for {}.", ids, player.getName().getString());
                    })
                    .onFailure(cause -> LOGGER.error("[Shop Sync] Failed to confirm product delivery for {}.", player.getName().getString(), cause));
        }
    }
}

