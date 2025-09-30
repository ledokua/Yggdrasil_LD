package net.ledok.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.ledok.Yggdrasil_ld;
import net.ledok.block.ModBlocks;
import net.ledok.screen.BossSpawnerData;
import net.ledok.screen.BossSpawnerScreenHandler;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BossSpawnerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BossSpawnerData> {
    public String mobId = "minecraft:zombie";
    public int respawnTime = 6000;
    public int portalActiveTime = 600;
    public String lootTableId = "minecraft:chests/simple_dungeon";
    public BlockPos exitPortalCoords = BlockPos.ORIGIN;
    public int triggerRadius = 10;
    public int battleRadius = 30;
    public int regeneration = 0;

    private boolean isBattleActive = false;
    private int respawnCooldown = 0;
    @Nullable private UUID activeBossUuid = null;
    @Nullable private RegistryKey<World> bossDimension = null;
    private int regenerationTickTimer = 0;

    public BossSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOSS_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("mobId", mobId);
        nbt.putInt("respawnTime", respawnTime);
        nbt.putInt("portalActiveTime", portalActiveTime);
        nbt.putString("lootTableId", lootTableId);
        nbt.putLong("exitPortalCoords", exitPortalCoords.asLong());
        nbt.putInt("triggerRadius", triggerRadius);
        nbt.putInt("battleRadius", battleRadius);
        nbt.putInt("regeneration", regeneration);
        nbt.putBoolean("isBattleActive", isBattleActive);
        nbt.putInt("respawnCooldown", respawnCooldown);
        if (activeBossUuid != null) {
            nbt.putUuid("activeBossUuid", activeBossUuid);
        }
        if (bossDimension != null) {
            nbt.putString("bossDimension", bossDimension.getValue().toString());
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        mobId = nbt.getString("mobId");
        respawnTime = nbt.getInt("respawnTime");
        portalActiveTime = nbt.getInt("portalActiveTime");
        lootTableId = nbt.getString("lootTableId");
        exitPortalCoords = BlockPos.fromLong(nbt.getLong("exitPortalCoords"));
        triggerRadius = nbt.getInt("triggerRadius");
        battleRadius = nbt.getInt("battleRadius");
        regeneration = nbt.getInt("regeneration");
        isBattleActive = nbt.getBoolean("isBattleActive");
        respawnCooldown = nbt.getInt("respawnCooldown");
        if (nbt.containsUuid("activeBossUuid")) {
            activeBossUuid = nbt.getUuid("activeBossUuid");
        } else {
            activeBossUuid = null;
        }
        if (nbt.contains("bossDimension")) {
            bossDimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(nbt.getString("bossDimension")));
        } else {
            bossDimension = null;
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, BossSpawnerBlockEntity be) {
        if (world.isClient()) return;
        ServerWorld serverWorld = (ServerWorld) world;

        if (be.isBattleActive && be.activeBossUuid != null) {
            MinecraftServer server = serverWorld.getServer();
            if (server != null && be.bossDimension != null) {
                ServerWorld bossWorld = server.getWorld(be.bossDimension);
                if (bossWorld == null || bossWorld.getEntity(be.activeBossUuid) == null) {
                    Yggdrasil_ld.LOGGER.warn("Boss spawner at {} found an invalid battle state on tick (boss missing). Resetting.", pos);
                    be.resetSpawner(serverWorld, state);
                    return;
                }
            }
        }

        if (be.isBattleActive) {
            be.monitorBattle(serverWorld, state);
        } else {
            if (be.respawnCooldown > 0) {
                be.respawnCooldown--;
            } else {
                be.checkForPlayers(serverWorld, state);
            }
        }
    }

    private void checkForPlayers(ServerWorld world, BlockState state) {
        Box triggerBox = new Box(pos).expand(triggerRadius);
        if (!world.getNonSpectatingEntities(PlayerEntity.class, triggerBox).isEmpty()) {
            startBattle(world, state);
        }
    }

    private void startBattle(ServerWorld world, BlockState state) {
        Yggdrasil_ld.LOGGER.info("Starting battle at spawner {}", pos);
        Optional<EntityType<?>> entityTypeOpt = Registries.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(this.mobId));
        if (entityTypeOpt.isEmpty()) {
            Yggdrasil_ld.LOGGER.warn("Invalid mob ID '{}' in spawner at {}.", this.mobId, pos);
            return;
        }

        Entity entity = entityTypeOpt.get().create(world);
        if (!(entity instanceof LivingEntity boss)) {
            Yggdrasil_ld.LOGGER.warn("Mob ID '{}' is not a LivingEntity.", this.mobId);
            return;
        }

        boss.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

        MinecraftServer server = world.getServer();
        if (server != null) {
            String bossUuidString = entity.getUuidAsString();
            server.getCommandManager().executeWithPrefix(server.getCommandSource(), "tag " + bossUuidString + " add yggdrasil_boss");
        }


        boss.setHealth(boss.getMaxHealth());

        world.spawnEntity(boss);
        this.activeBossUuid = boss.getUuid();
        this.bossDimension = world.getRegistryKey();
        this.isBattleActive = true;
        this.regenerationTickTimer = 0;
        this.markDirty();
        world.updateListeners(pos, state, state, 3);
        Yggdrasil_ld.LOGGER.info("Spawned boss {} with UUID {}.", this.mobId, this.activeBossUuid);
    }

    private void monitorBattle(ServerWorld world, BlockState state) {
        if (activeBossUuid == null) {
            handleBattleLoss(world, state, "Boss UUID was null.");
            return;
        }

        MinecraftServer server = world.getServer();
        if (server == null || this.bossDimension == null) {
            handleBattleLoss(world, state, "Server or boss dimension was null, cannot monitor battle.");
            return;
        }

        ServerWorld bossWorld = server.getWorld(this.bossDimension);
        if (bossWorld == null) {
            handleBattleLoss(world, state, "Boss world could not be found, battle lost.");
            return;
        }

        Entity boss = bossWorld.getEntity(activeBossUuid);
        if (boss == null || !boss.isAlive()) {
            handleBattleWin(world, state, boss);
            return;
        }

        if (this.regeneration > 0 && boss instanceof LivingEntity livingBoss) {
            this.regenerationTickTimer++;
            if (this.regenerationTickTimer >= 20) {
                livingBoss.heal((float) this.regeneration);
                this.regenerationTickTimer = 0;
            }
        }

        Box battleBox = new Box(pos).expand(battleRadius);
        if (world.getNonSpectatingEntities(PlayerEntity.class, battleBox).isEmpty()) {
            handleBattleLoss(world, state, "All players left the battle area.");
            boss.discard();
        }
    }

    private void handleBattleWin(ServerWorld world, BlockState state, @Nullable Entity defeatedBoss) {
        Yggdrasil_ld.LOGGER.info("Battle won at spawner {}. Spawning loot and portal.", pos);

        RegistryKey<LootTable> lootTableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.tryParse(this.lootTableId));
        MinecraftServer server = world.getServer();
        if (server != null) {
            Optional<LootTable> lootTableOpt = Optional.ofNullable(server.getReloadableRegistries().getLootTable(lootTableKey));

            if (lootTableOpt.isPresent()) {
                LootTable lootTable = lootTableOpt.get();

                LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                        .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos));

                if (defeatedBoss != null) {
                    builder.add(LootContextParameters.THIS_ENTITY, defeatedBoss);
                }

                PlayerEntity killer = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), battleRadius, false);
                if (killer != null) {
                    builder.add(LootContextParameters.ATTACKING_ENTITY, killer);
                    builder.add(LootContextParameters.LAST_DAMAGE_PLAYER, killer);
                    builder.add(LootContextParameters.DAMAGE_SOURCE, world.getDamageSources().playerAttack(killer));
                }

                List<ItemStack> lootItems = lootTable.generateLoot(builder.build(LootContextTypes.ENTITY));

                for(ItemStack stack : lootItems) {
                    ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, stack);
                    itemEntity.setToDefaultPickupDelay();
                    world.spawnEntity(itemEntity);
                }
            } else {
                Yggdrasil_ld.LOGGER.warn("Could not find loot table with ID '{}'", this.lootTableId);
            }
        }


        BlockPos portalPos = pos.up();
        world.setBlockState(portalPos, ModBlocks.EXIT_PORTAL_BLOCK.getDefaultState(), 3);
        if (world.getBlockEntity(portalPos) instanceof ExitPortalBlockEntity portal) {
            portal.setDetails(this.portalActiveTime, this.exitPortalCoords);
            Yggdrasil_ld.LOGGER.info("Spawned exit portal at {} for {} ticks.", portalPos, this.portalActiveTime);
        }

        resetSpawner(world, state);
    }

    private void handleBattleLoss(ServerWorld world, BlockState state, String reason) {
        Yggdrasil_ld.LOGGER.info("Battle lost at spawner {}: {}", pos, reason);
        resetSpawner(world, state);
    }

    private void resetSpawner(ServerWorld world, BlockState state) {
        this.isBattleActive = false;
        this.activeBossUuid = null;
        this.bossDimension = null;
        this.respawnCooldown = this.respawnTime;
        this.regenerationTickTimer = 0;
        this.markDirty();
        world.updateListeners(pos, state, state, 3);
    }

    @Nullable @Override public Packet<ClientPlayPacketListener> toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }
    @Override public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) { return createNbt(registryLookup); }
    @Override public Text getDisplayName() { return Text.literal("Boss Spawner Configuration"); }
    @Nullable @Override public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) { return new BossSpawnerScreenHandler(syncId, playerInventory, this); }
    @Override public BossSpawnerData getScreenOpeningData(ServerPlayerEntity player) { return new BossSpawnerData(this.pos); }
}

