package net.ledok.block.entity;

import net.ledok.YggdrasilLdMod;
import net.ledok.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PhaseBlockEntity extends BlockEntity {
    private String groupId = "";

    public PhaseBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.PHASE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            YggdrasilLdMod.PHASE_BLOCK_MANAGER.register(this);
        }
    }

    @Override
    public void setRemoved() {
        if (this.level != null && !this.level.isClientSide()) {
            YggdrasilLdMod.PHASE_BLOCK_MANAGER.unregister(this);
        }
        super.setRemoved();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.groupId = tag.getString("groupId");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("groupId", this.groupId);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
        setChanged();
    }
}
