package net.ledok.prime;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ledok.Yggdrasil_ld;
import net.ledok.prime.api.MinestarApi;
import net.ledok.prime.api.dto.PrimeStatusDTO;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.minecraft.server.network.ServerPlayerEntity;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrimeRoleHandler {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static MinestarApi minestarApi;
    private static LuckPerms luckPermsApi;

    public static void register() {
        if (!Yggdrasil_ld.CONFIG.prime_role_sync_enabled) {
            Yggdrasil_ld.LOGGER.info("[Prime Sync] Feature is disabled in the config.");
            return;
        }

        try {
            luckPermsApi = LuckPermsProvider.get();
        } catch (IllegalStateException ex) {
            Yggdrasil_ld.LOGGER.error("[Prime Sync] LuckPerms API not found. Disabling feature.");
            return;
        }

        minestarApi = MinestarApi.create();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            checkPrimeStatus(handler.getPlayer());
        });

        Yggdrasil_ld.LOGGER.info("[Prime Sync] Feature has been initialized successfully.");
    }

    private static void checkPrimeStatus(ServerPlayerEntity player) {
        Yggdrasil_ld.LOGGER.info("[Prime Sync] Checking prime status for player {} (UUID: {}).", player.getName().getString(), player.getUuid());

        executorService.submit(() -> {
            Call<PrimeStatusDTO> apiCall = minestarApi.getPrimeStatusByUuid(MinestarApi.formatUUID(player.getUuid()));
            try {
                Response<PrimeStatusDTO> response = apiCall.execute();

                if (!response.isSuccessful()) {
                    Yggdrasil_ld.LOGGER.warn("[Prime Sync] API call for {} was not successful. Response code: {}", player.getName().getString(), response.code());
                    return;
                }

                PrimeStatusDTO status = response.body();
                if (status == null) {
                    Yggdrasil_ld.LOGGER.warn("[Prime Sync] API response body for {} was null.", player.getName().getString());
                    return;
                }

                Yggdrasil_ld.LOGGER.info("[Prime Sync] API Response for {}: Status='{}'", player.getName().getString(), status.getStatus());

                User user = luckPermsApi.getUserManager().loadUser(player.getUuid()).join();
                if (user == null) {
                    Yggdrasil_ld.LOGGER.warn("[Prime Sync] Could not load LuckPerms user for {}.", player.getName().getString());
                    return;
                }

                boolean hasPrimeGroup = user.getNodes().stream()
                        .filter(n -> n instanceof InheritanceNode)
                        .map(n -> ((InheritanceNode) n).getGroupName())
                        .anyMatch(g -> g.equalsIgnoreCase("prime"));

                Yggdrasil_ld.LOGGER.info("[Prime Sync] LuckPerms check for {}: hasPrimeGroup={}", player.getName().getString(), hasPrimeGroup);

                if (status.isPrime()) {
                    if (!hasPrimeGroup) {
                        Yggdrasil_ld.LOGGER.info("[Prime Sync] API says user is Prime, but they don't have the role. Adding it now...");
                        user.data().add(InheritanceNode.builder("prime").build());
                        luckPermsApi.getUserManager().saveUser(user);
                        Yggdrasil_ld.LOGGER.info("[Prime Sync] Added 'Prime' role to {}.", player.getName().getString());
                    }
                } else {
                    if (hasPrimeGroup) {
                        Yggdrasil_ld.LOGGER.info("[Prime Sync] API says user is NOT Prime, but they have the role. Removing it now...");
                        user.data().remove(InheritanceNode.builder("prime").build());
                        luckPermsApi.getUserManager().saveUser(user);
                        Yggdrasil_ld.LOGGER.info("[Prime Sync] Removed 'Prime' role from {}.", player.getName().getString());
                    }
                }

            } catch (IOException e) {
                Yggdrasil_ld.LOGGER.error("[Prime Sync] An IOException occurred while checking prime status for {}.", player.getName().getString(), e);
            }
        });
    }
}

