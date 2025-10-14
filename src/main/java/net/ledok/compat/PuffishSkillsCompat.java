package net.ledok.compat;

import net.ledok.YggdrasilLdMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Experience;
import net.puffish.skillsmod.api.SkillsAPI;

import java.util.Optional;

public class PuffishSkillsCompat {

    /**
     * Gets a player's total skill points from the configured skill tree.
     * @param player The player to check.
     * @return The player's total skill points, or 0 if the tree is not found.
     */
    public static int getPlayerLevel(PlayerEntity player) {
        if(!(player instanceof ServerPlayerEntity serverPlayer)) {
            return 0;
        }
        // Use the skill tree ID from the config file.
        Optional<Category> skillTree = SkillsAPI.getCategory(Identifier.tryParse(YggdrasilLdMod.CONFIG.puffish_skills_tree_id));
        return skillTree.map(tree -> tree.getPointsTotal(serverPlayer)).orElse(0);
    }

    /**
     * Adds experience to the configured skill tree for a specific player.
     * @param player The player to grant experience to.
     * @param amount The amount of experience to add.
     */
    public static void addExperience(ServerPlayerEntity player, int amount) {
        // Find the skill tree using the ID from your config file.
        Optional<Category> skillTree = SkillsAPI.getCategory(Identifier.tryParse(YggdrasilLdMod.CONFIG.puffish_skills_tree_id));

        // If the category exists, get its experience handler and add the experience.
        skillTree.ifPresent(category -> {
            Optional<Experience> experienceHandler = category.getExperience();
            experienceHandler.ifPresent(exp -> exp.addTotal(player, amount));
        });
    }
}

