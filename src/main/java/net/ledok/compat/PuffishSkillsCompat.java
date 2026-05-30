package net.ledok.compat;

import net.ledok.YggdrasilLdMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Experience;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PuffishSkillsCompat {

    public static List<String> getConfiguredSkillTreeIds() {
        return YggdrasilLdMod.CONFIG.getPuffishSkillsTreeIds();
    }

    public static Optional<String> getPrimarySkillTreeId() {
        return getConfiguredSkillTreeIds().stream().findFirst();
    }

    public static String getXpSkillTreeId() {
        return YggdrasilLdMod.CONFIG.getPuffishSkillsXpTreeId();
    }

    public static Stream<Category> getConfiguredSkillTrees() {
        return getConfiguredSkillTreeIds().stream()
                .map(ResourceLocation::tryParse)
                .filter(resourceLocation -> resourceLocation != null)
                .map(SkillsAPI::getCategory)
                .flatMap(Optional::stream);
    }

    /**
     * Gets a player's total skill points from the configured skill trees.
     * @param player The player to check.
     * @return The player's total skill points, or 0 if no trees are found.
     */
    public static int getPlayerLevel(Player player) {
        if(!(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        return getConfiguredSkillTrees()
                .mapToInt(tree -> tree.getPointsTotal(serverPlayer))
                .sum();
    }

    /**
     * Adds experience to the configured skill tree for a specific player.
     * @param player The player to grant experience to.
     * @param amount The amount of experience to add.
     */
    public static void addExperience(ServerPlayer player, int amount) {
        Optional.of(getXpSkillTreeId())
                .map(ResourceLocation::tryParse)
                .flatMap(SkillsAPI::getCategory)
                .ifPresent(category -> {
            Optional<Experience> experienceHandler = category.getExperience();
            experienceHandler.ifPresent(exp -> exp.addTotal(player, amount));
        });
    }
}
