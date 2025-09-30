package net.ledok.compat;

import net.ledok.Yggdrasil_ld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.Category;
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
        Optional<Category> skillTree = SkillsAPI.getCategory(Identifier.tryParse(Yggdrasil_ld.CONFIG.puffish_skills_tree_id));
        return skillTree.map(tree -> tree.getPointsTotal(serverPlayer)).orElse(0);
    }
}

