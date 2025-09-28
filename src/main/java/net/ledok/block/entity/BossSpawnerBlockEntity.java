package net.ledok.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.ledok.Yggdrasil_ld;
import net.ledok.screen.BossSpawnerData;
import net.ledok.screen.BossSpawnerScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BossSpawnerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BossSpawnerData> {

    // --- Configurable Data Fields ---
    public String mobId = "minecraft:zombie";
    public int respawnTime = 100;
    public int bossLevel = 10;
    public int portalActiveTime = 60;
    public String lootTableId = "minecraft:chests/simple_dungeon";
    public BlockPos exitPortalCoords = new BlockPos(0, 100, 0);
    public int triggerRadius = 2;
    public int battleRadius = 4;
    public int regeneration = 10;

    // --- Internal state tracking fields ---
    private boolean isBattleActive = false;
    @Nullable private UUID activeBossUuid = null;
    private int respawnCooldown = 0;
    private int tickCounter = 0;

    public BossSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOSS_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    // --- Saving and Loading Data (NBT) ---
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("mobId", mobId);
        nbt.putInt("respawnTime", respawnTime);
        nbt.putInt("bossLevel", bossLevel);
        nbt.putInt("portalActiveTime", portalActiveTime);
        nbt.putString("lootTableId", lootTableId);
        nbt.putInt("exitPortalX", exitPortalCoords.getX());
        nbt.putInt("exitPortalY", exitPortalCoords.getY());
        nbt.putInt("exitPortalZ", exitPortalCoords.getZ());
        nbt.putInt("triggerRadius", triggerRadius);
        nbt.putInt("battleRadius", battleRadius);
        nbt.putInt("regeneration", regeneration);
        nbt.putBoolean("isBattleActive", isBattleActive);
        if(activeBossUuid != null) {
            nbt.putUuid("activeBossUuid", activeBossUuid);
        }
        nbt.putInt("respawnCooldown", respawnCooldown);
        nbt.putInt("tickCounter", tickCounter);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        mobId = nbt.getString("mobId");
        respawnTime = nbt.getInt("respawnTime");
        bossLevel = nbt.getInt("bossLevel");
        portalActiveTime = nbt.getInt("portalActiveTime");
        lootTableId = nbt.getString("lootTableId");
        exitPortalCoords = new BlockPos(nbt.getInt("exitPortalX"), nbt.getInt("exitPortalY"), nbt.getInt("exitPortalZ"));
        triggerRadius = nbt.getInt("triggerRadius");
        battleRadius = nbt.getInt("battleRadius");
        regeneration = nbt.getInt("regeneration");
        isBattleActive = nbt.getBoolean("isBattleActive");
        if(nbt.contains("activeBossUuid")) {
            activeBossUuid = nbt.getUuid("activeBossUuid");
        }
        respawnCooldown = nbt.getInt("respawnCooldown");
        tickCounter = nbt.getInt("tickCounter");
    }

    // --- Ticking Logic ---
    public static void tick(World world, BlockPos pos, BlockState state, BossSpawnerBlockEntity be) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // --- BATTLE MANAGEMENT ---
        if (be.isBattleActive) {
            be.manageActiveBattle(serverWorld, state);
            return; // If a battle is active, we don't need to do anything else.
        }

        // --- COOLDOWN & TRIGGER LOGIC ---
        if (be.respawnCooldown > 0) {
            be.respawnCooldown--;
            return;
        }

        be.tickCounter++;
        if (be.tickCounter < 100) {
            return;
        }
        be.tickCounter = 0;

        Box triggerArea = new Box(pos).expand(be.triggerRadius);
        List<PlayerEntity> playersInTriggerArea = world.getEntitiesByClass(PlayerEntity.class, triggerArea, (player) -> true);

        if (!playersInTriggerArea.isEmpty()) {
            be.spawnBoss(serverWorld, state);
        }
    }

    // --- NEW: Method to manage an ongoing battle ---
    private void manageActiveBattle(ServerWorld world, BlockState state) {
        if (activeBossUuid == null) {
            // Should not happen, but a good failsafe.
            handleBattleLoss(world, state, "Boss UUID was null.");
            return;
        }

        Entity boss = world.getEntity(activeBossUuid);

        // --- WIN CONDITION: Boss is dead ---
        if (boss == null || !boss.isAlive()) {
            handleBattleWin(world, state);
            return;
        }

        // --- LOSS CONDITION 1: Boss leaves the battle radius ---
        if (!boss.getBoundingBox().intersects(new Box(pos).expand(battleRadius))) {
            boss.discard(); // Despawn the boss
            handleBattleLoss(world, state, "Boss left the battle radius.");
            return;
        }

        // --- LOSS CONDITION 2: All players leave the battle radius ---
        Box battleArea = new Box(pos).expand(battleRadius);
        List<PlayerEntity> playersInBattle = world.getEntitiesByClass(PlayerEntity.class, battleArea, p -> true);
        if (playersInBattle.isEmpty()) {
            boss.discard(); // Despawn the boss
            handleBattleLoss(world, state, "All players left the battle radius.");
        }
    }

    private void spawnBoss(ServerWorld world, BlockState state) {
        Optional<EntityType<?>> entityTypeOpt = Registries.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(this.mobId));

        if (entityTypeOpt.isEmpty()) {
            Yggdrasil_ld.LOGGER.warn("Boss Spawner at {} has an invalid mob ID: {}", this.pos, this.mobId);
            return;
        }

        Entity bossEntity = entityTypeOpt.get().create(world);
        if (!(bossEntity instanceof LivingEntity boss)) {
            Yggdrasil_ld.LOGGER.error("Failed to create a living entity for mob ID: {}", this.mobId);
            return;
        }

        boss.refreshPositionAndAngles(this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, 0, 0);
        world.spawnEntity(boss);
        Yggdrasil_ld.LOGGER.info("Boss {} spawned at {}", this.mobId, this.pos);

        this.isBattleActive = true;
        this.activeBossUuid = boss.getUuid();
        this.markDirty();
        world.updateListeners(pos, state, state, 3);
    }

    // --- NEW: Methods to handle the outcome of a battle ---
    private void handleBattleWin(ServerWorld world, BlockState state) {
        Yggdrasil_ld.LOGGER.info("Battle won at spawner {}", pos);
        // TODO: Spawn loot chest
        // TODO: Spawn exit portal

        resetSpawner(world, state);
    }

    private void handleBattleLoss(ServerWorld world, BlockState state, String reason) {
        Yggdrasil_ld.LOGGER.info("Battle lost at spawner {}: {}", pos, reason);
        // No loot or portal on loss.

        resetSpawner(world, state);
    }

    // --- NEW: Helper to reset the spawner after a battle ends ---
    private void resetSpawner(ServerWorld world, BlockState state) {
        this.isBattleActive = false;
        this.activeBossUuid = null;
        this.respawnCooldown = this.respawnTime; // Start the cooldown
        this.markDirty();
        world.updateListeners(pos, state, state, 3);
    }

    @Nullable @Override public Packet<ClientPlayPacketListener> toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }
    @Override public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) { return createNbt(registryLookup); }
    @Override public Text getDisplayName() { return Text.literal("Boss Spawner Configuration"); }
    @Nullable @Override public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) { return new BossSpawnerScreenHandler(syncId, playerInventory, this); }
    @Override public BossSpawnerData getScreenOpeningData(ServerPlayerEntity player) { return new BossSpawnerData(this.pos); }
}

