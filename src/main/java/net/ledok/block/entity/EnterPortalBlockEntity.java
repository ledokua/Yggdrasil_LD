package net.ledok.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class EnterPortalBlockEntity extends BlockEntity {

    private BlockPos destination;

    public EnterPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENTER_PORTAL_BLOCK_ENTITY, pos, state);
    }

    public void setDestination(BlockPos destination) {
        this.destination = destination;
        this.markDirty();
    }

    public BlockPos getDestination() {
        return this.destination;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if(destination != null) {
            nbt.putLong("destination", destination.asLong());
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if(nbt.contains("destination")) {
            destination = BlockPos.fromLong(nbt.getLong("destination"));
        }
    }
}

