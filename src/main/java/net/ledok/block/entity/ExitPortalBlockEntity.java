package net.ledok.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExitPortalBlockEntity extends BlockEntity {

    private int lifetime;
    private BlockPos destination;

    public ExitPortalBlockEntity(BlockPos pos, BlockState state) {
        // --- Correctly reference the registered block entity type ---
        super(ModBlockEntities.EXIT_PORTAL_BLOCK_ENTITY, pos, state);
    }

    public void setDetails(int lifetime, BlockPos destination) {
        this.lifetime = lifetime;
        this.destination = destination;
        this.markDirty();
    }

    public static void tick(World world, BlockPos pos, BlockState state, ExitPortalBlockEntity be) {
        if(world.isClient()) return;

        be.lifetime--;
        if (be.lifetime <= 0) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    public BlockPos getDestination() {
        return this.destination;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("lifetime", lifetime);
        if(destination != null) {
            nbt.putInt("destX", destination.getX());
            nbt.putInt("destY", destination.getY());
            nbt.putInt("destZ", destination.getZ());
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        lifetime = nbt.getInt("lifetime");
        if(nbt.contains("destX")) {
            destination = new BlockPos(nbt.getInt("destX"), nbt.getInt("destY"), nbt.getInt("destZ"));
        }
    }
}

