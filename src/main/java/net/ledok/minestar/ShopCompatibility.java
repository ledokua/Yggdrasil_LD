package net.ledok.minestar;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.ChatFormatting;
import ua.com.minestar.model.ShopProduct;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ledok.YggdrasilLdMod.LOGGER;
import static ua.com.minestar.Minestar.minestar;

public class ShopCompatibility {

    /**
     * Notifies a player on join if they have pending products to claim.
     * @param player The player who just joined the server.
     */
    public static void notifyOnJoin(ServerPlayer player) {
        minestar.getUserPendingShopProductsByProfileUuidAndServerId(player.getUUID())
                .onSuccess(products -> {
                    if (!products.isEmpty()) {
                        Component message = Component.translatable("message.yggdrasil_ld.shop.unclaimed_items", products.size())
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.translatable("message.yggdrasil_ld.shop.claim_button")
                                        .withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE)
                                        .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shop-receive")))
                                )
                                .append(Component.translatable("message.yggdrasil_ld.shop.claim_suffix").withStyle(ChatFormatting.GOLD));

                        player.sendSystemMessage(message);
                    }
                })
                .onFailure(cause -> LOGGER.error("[Shop Sync] Failed to check for pending products on join for {}.", player.getName().getString(), cause));
    }

    public static void claimPurchases(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.yggdrasil_ld.shop.checking").withStyle(ChatFormatting.GRAY));
        LOGGER.info("[Shop Sync] Checking for pending products for player {}.", player.getName().getString());

        minestar.getUserPendingShopProductsByProfileUuidAndServerId(player.getUUID())
                .onSuccess(products -> {
                    if (products.isEmpty()) {
                        player.sendSystemMessage(Component.translatable("message.yggdrasil_ld.shop.no_items").withStyle(ChatFormatting.YELLOW));
                        LOGGER.info("[Shop Sync] No pending products found for {}.", player.getName().getString());
                        return;
                    }
                    player.sendSystemMessage(Component.translatable("message.yggdrasil_ld.shop.found_items", products.size()).withStyle(ChatFormatting.GREEN));
                    LOGGER.info("[Shop Sync] Found {} pending products for {}.", products.size(), player.getName().getString());
                    processAndConfirmDelivery(player, products);
                })
                .onFailure(cause -> {
                    player.sendSystemMessage(Component.translatable("message.yggdrasil_ld.shop.error").withStyle(ChatFormatting.RED));
                    LOGGER.error("[Shop Sync] Failed to get pending products for {}.", player.getName().getString(), cause);
                });
    }

    private static void processAndConfirmDelivery(ServerPlayer player, List<ShopProduct> products) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        Set<Long> deliveredProductIds = new HashSet<>();

        for (ShopProduct product : products) {
            boolean allCommandsSuccessful = true;
            for (int i = 0; i < product.quantity(); i++) {
                for (String commandTemplate : product.commands()) {
                    try {
                        String command = commandTemplate.replace("{player}", player.getName().getString());
                        CommandSourceStack source = server.createCommandSourceStack();
                        Commands commands = server.getCommands();
                        commands.performPrefixedCommand(source, command);

                        LOGGER.info("[Shop Sync] Executed command for product ID {} (Item {} of {}): {}", product.id(), i + 1, product.quantity(), command);
                    } catch (Exception e) {
                        allCommandsSuccessful = false;
                        LOGGER.error("[Shop Sync] Failed to execute a command for product ID {}.", product.id(), e);
                        break;
                    }
                }
                if (!allCommandsSuccessful) {
                    break;
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
