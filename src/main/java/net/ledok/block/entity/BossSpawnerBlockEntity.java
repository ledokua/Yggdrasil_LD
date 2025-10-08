package net.ledok.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.ledok.YggdrasilLdMod;
import net.ledok.block.ModBlocks;
import net.ledok.compat.PuffishSkillsCompat;
import net.ledok.screen.BossSpawnerData;
import net.ledok.screen.BossSpawnerScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BossSpawnerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BossSpawnerData> {

    // --- Configuration Fields ---
    public String mobId = "minecraft:zombie";
    public int respawnTime = 6000;
    public int portalActiveTime = 600;
    public String lootTableId = "minecraft:chests/simple_dungeon";
    public BlockPos exitPortalCoords = new BlockPos(0, 0, 0);
    public BlockPos enterPortalSpawnCoords = new BlockPos(0, 0, 0);
    public BlockPos enterPortalDestCoords = new BlockPos(0, 0, 0);
    public int triggerRadius = 16;
    public int battleRadius = 64;
    public int regeneration = 0;
    public int minPlayers = 2;
    public int skillExperiencePerWin = 100;

    // --- State Machine Fields ---
    private boolean isBattleActive = false;
    private int respawnCooldown = 0;
    private UUID activeBossUuid = null;
    private RegistryKey<World> bossDimension = null;
    private int regenerationTickTimer = 0;
    private int enterPortalRemovalTimer = -1;

    public BossSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOSS_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BossSpawnerBlockEntity be) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) return;

        if (be.isBattleActive) {
            be.handleActiveBattle(serverWorld);
        } else {
            be.handleIdleState(serverWorld, pos);
        }
    }

    private void handleIdleState(ServerWorld world, BlockPos pos) {
        if (respawnCooldown > 0) {
            respawnCooldown--;
            if (respawnCooldown == 0) {
                spawnEnterPortal(world);
            }
            return;
        }

        Box triggerBox = new Box(pos).expand(triggerRadius);
        List<ServerPlayerEntity> playersInTriggerZone = world.getEntitiesByClass(ServerPlayerEntity.class, triggerBox, p -> !p.isSpectator());

        if (playersInTriggerZone.size() >= this.minPlayers) {
            startBattle(world, pos, playersInTriggerZone.get(0));
        }
    }

    private void handleActiveBattle(ServerWorld world) {
        if (this.enterPortalRemovalTimer > 0) {
            this.enterPortalRemovalTimer--;
            if (this.enterPortalRemovalTimer == 0) {
                removeEnterPortal(world);
                YggdrasilLdMod.LOGGER.info("Enter portal at {} has timed out and was removed.", enterPortalSpawnCoords);
            }
        }

        if (activeBossUuid == null) {
            handleBattleLoss(world, "Boss UUID was null.");
            return;
        }
        MinecraftServer server = world.getServer();
        if (server == null) return;
        ServerWorld bossWorld = server.getWorld(bossDimension);
        if (bossWorld == null) {
            handleBattleLoss(world, "Boss world was null.");
            return;
        }
        Entity bossEntity = bossWorld.getEntity(activeBossUuid);
        if (bossEntity == null) {
            handleBattleLoss(world, "Boss entity disappeared.");
            return;
        }
        if (!bossEntity.isAlive()) {
            handleBattleWin(world, bossEntity);
            return;
        }
        Box battleBox = new Box(pos).expand(battleRadius);
        List<ServerPlayerEntity> playersInBattle = world.getEntitiesByClass(ServerPlayerEntity.class, battleBox, p -> !p.isSpectator());
        if (playersInBattle.isEmpty()) {
            bossEntity.discard();
            handleBattleLoss(world, "All players left the battle area.");
            return;
        }
        if (regeneration > 0 && bossEntity instanceof LivingEntity livingBoss) {
            regenerationTickTimer++;
            if (regenerationTickTimer >= 100) {
                livingBoss.heal((float) regeneration);
                regenerationTickTimer = 0;
            }
        }
    }

    private void startBattle(ServerWorld world, BlockPos spawnPos, ServerPlayerEntity triggeringPlayer) {
        this.enterPortalRemovalTimer = 1200;

        String fullMobId = this.mobId;
        if (!fullMobId.contains(":")) {
            fullMobId = "minecraft:" + fullMobId;
        }

        Optional<EntityType<?>> entityTypeOpt = Registries.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(fullMobId));

        // --- NEW: Get the proper display name for the announcement ---
        Text mobDisplayName;
        if (entityTypeOpt.isPresent()) {
            // This gets the translatable name like "Zombie" or "Wither"
            mobDisplayName = entityTypeOpt.get().getName();
        } else {
            // Fallback to the raw ID if the entity type is invalid
            mobDisplayName = Text.literal(this.mobId);
        }

        Text announcement = Text.translatable("message.yggdrasil_ld.raid_start", triggeringPlayer.getDisplayName(), mobDisplayName)
                .formatted(Formatting.GOLD);
        world.getServer().getPlayerManager().broadcast(announcement, false);

        if (entityTypeOpt.isEmpty()) {
            YggdrasilLdMod.LOGGER.error("Invalid mob ID in spawner at {}: {}", this.pos, this.mobId);
            this.respawnCooldown = this.respawnTime;
            return;
        }
        Entity boss = entityTypeOpt.get().create(world);
        if (boss == null) {
            YggdrasilLdMod.LOGGER.error("Failed to create entity from ID: {}", this.mobId);
            return;
        }
        boss.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY() + 1, spawnPos.getZ() + 0.5, 0, 0);
        world.spawnEntity(boss);
        this.isBattleActive = true;
        this.activeBossUuid = boss.getUuid();
        this.bossDimension = world.getRegistryKey();
        this.markDirty();
        YggdrasilLdMod.LOGGER.info("Battle started at spawner {} with boss {}", this.pos, this.mobId);
    }

    private void handleBattleWin(ServerWorld world, Entity defeatedBoss) {
        YggdrasilLdMod.LOGGER.info("Battle won at spawner {}", pos);

        // --- NEW: Award experience to players in the battle radius ---
        if (this.skillExperiencePerWin > 0) {
            Box battleBox = new Box(pos).expand(battleRadius);
            List<ServerPlayerEntity> playersInBattle = world.getEntitiesByClass(ServerPlayerEntity.class, battleBox, p -> !p.isSpectator());
            for (ServerPlayerEntity player : playersInBattle) {

                // Puffish Skills Experience (safely)
                if (this.skillExperiencePerWin > 0 && FabricLoader.getInstance().isModLoaded("puffish_skills")) {
                    PuffishSkillsCompat.addExperience(player, this.skillExperiencePerWin);
                }
            }
        }

        Identifier lootTableIdentifier = Identifier.tryParse(this.lootTableId);
        if (lootTableIdentifier != null) {
            RegistryKey<LootTable> lootTableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableIdentifier);
            LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(lootTableKey);

            LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                    .add(LootContextParameters.ORIGIN, pos.toCenterPos())
                    .add(LootContextParameters.THIS_ENTITY, defeatedBoss);

            List<ItemStack> loot = lootTable.generateLoot(builder.build(LootContextTypes.GIFT));
            for (ItemStack stack : loot) {
                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, stack));
            }
        }
        BlockPos portalPos = pos.up();
        world.setBlockState(portalPos, ModBlocks.EXIT_PORTAL_BLOCK.getDefaultState());
        if (world.getBlockEntity(portalPos) instanceof ExitPortalBlockEntity portal) {
            portal.setDetails(this.portalActiveTime, this.exitPortalCoords);
            YggdrasilLdMod.LOGGER.info("Spawned exit portal at {} for {} ticks.", portalPos, this.portalActiveTime);
        }
        resetSpawner(world);
    }

    private void handleBattleLoss(ServerWorld world, String reason) {
        YggdrasilLdMod.LOGGER.info("Battle lost at spawner {}: {}", pos, reason);
        removeEnterPortal(world);
        resetSpawner(world);
    }

    private void resetSpawner(ServerWorld world) {
        this.isBattleActive = false;
        this.activeBossUuid = null;
        this.bossDimension = null;
        this.respawnCooldown = this.respawnTime;
        this.regenerationTickTimer = 0;
        this.enterPortalRemovalTimer = -1;
        this.markDirty();
        world.updateListeners(pos, getCachedState(), getCachedState(), 3);

        if (this.respawnCooldown <= 0) {
            spawnEnterPortal(world);
        }
    }

    private void spawnEnterPortal(ServerWorld world) {
        if (enterPortalSpawnCoords == null || enterPortalDestCoords == null || enterPortalSpawnCoords.equals(new BlockPos(0, 0, 0))) {
            return;
        }
        world.setBlockState(enterPortalSpawnCoords, ModBlocks.ENTER_PORTAL_BLOCK.getDefaultState());
        if (world.getBlockEntity(enterPortalSpawnCoords) instanceof EnterPortalBlockEntity be) {
            be.setDestination(enterPortalDestCoords);
        }
    }

    private void removeEnterPortal(ServerWorld world) {
        if (enterPortalSpawnCoords != null && world.getBlockState(enterPortalSpawnCoords).isOf(ModBlocks.ENTER_PORTAL_BLOCK)) {
            world.setBlockState(enterPortalSpawnCoords, Blocks.AIR.getDefaultState());
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("MobId", mobId);
        nbt.putInt("RespawnTime", respawnTime);
        nbt.putInt("PortalActiveTime", portalActiveTime);
        nbt.putString("LootTableId", lootTableId);
        nbt.putLong("ExitPortalCoords", exitPortalCoords.asLong());
        if (enterPortalSpawnCoords != null) nbt.putLong("EnterPortalSpawn", enterPortalSpawnCoords.asLong());
        if (enterPortalDestCoords != null) nbt.putLong("EnterPortalDest", enterPortalDestCoords.asLong());
        nbt.putInt("TriggerRadius", triggerRadius);
        nbt.putInt("BattleRadius", battleRadius);
        nbt.putInt("Regeneration", regeneration);
        nbt.putInt("MinPlayers", minPlayers);
        nbt.putInt("SkillExperiencePerWin", skillExperiencePerWin);
        nbt.putBoolean("IsBattleActive", isBattleActive);
        nbt.putInt("RespawnCooldown", respawnCooldown);
        if (activeBossUuid != null) nbt.putUuid("ActiveBossUuid", activeBossUuid);
        if (bossDimension != null) nbt.putString("BossDimension", bossDimension.getValue().toString());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        mobId = nbt.getString("MobId");
        respawnTime = nbt.getInt("RespawnTime");
        portalActiveTime = nbt.getInt("PortalActiveTime");
        lootTableId = nbt.getString("LootTableId");
        exitPortalCoords = BlockPos.fromLong(nbt.getLong("ExitPortalCoords"));
        if (nbt.contains("EnterPortalSpawn"))
            enterPortalSpawnCoords = BlockPos.fromLong(nbt.getLong("EnterPortalSpawn"));
        if (nbt.contains("EnterPortalDest"))
            enterPortalDestCoords = BlockPos.fromLong(nbt.getLong("EnterPortalDest"));
        triggerRadius = nbt.getInt("TriggerRadius");
        battleRadius = nbt.getInt("BattleRadius");
        regeneration = nbt.getInt("Regeneration");
        minPlayers = nbt.contains("MinPlayers") ? nbt.getInt("MinPlayers") : 1;
        skillExperiencePerWin = nbt.getInt("SkillExperiencePerWin");
        isBattleActive = nbt.getBoolean("IsBattleActive");
        respawnCooldown = nbt.getInt("RespawnCooldown");
        if (nbt.containsUuid("ActiveBossUuid")) activeBossUuid = nbt.getUuid("ActiveBossUuid");
        if (nbt.contains("BossDimension"))
            bossDimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(nbt.getString("BossDimension")));
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Boss Spawner Configuration");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BossSpawnerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public BossSpawnerData getScreenOpeningData(ServerPlayerEntity player) {
        return new BossSpawnerData(this.pos);
    }
}

