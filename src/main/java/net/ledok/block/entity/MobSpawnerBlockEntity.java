package net.ledok.block.entity;

import com.mojang.brigadier.ParseResults;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.ledok.YggdrasilLdMod;
import net.ledok.screen.MobSpawnerData;
import net.ledok.screen.MobSpawnerScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MobSpawnerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<MobSpawnerData> {

    // --- Configuration Fields ---
    public String mobId = "minecraft:zombie";
    public int respawnTime = 1200;
    public String lootTableId = "";
    public int triggerRadius = 16;
    public int battleRadius = 64;
    public int regeneration = 0;
    public int skillExperiencePerWin = 0;
    public int mobCount = 1;
    public int mobSpread = 5;
    public double mobHealth = 20.0;
    public double mobAttackDamage = 3.0;

    // --- State Machine Fields ---
    private boolean isBattleActive = false;
    private int respawnCooldown = 0;
    private final List<UUID> activeMobUuids = new ArrayList<>();
    private final Map<UUID, Float> playerDamageDealt = new HashMap<>();
    private int regenerationTickTimer = 0;


    public MobSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOB_SPAWNER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, MobSpawnerBlockEntity be) {
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
            return;
        }

        Box triggerBox = new Box(pos).expand(triggerRadius);
        List<ServerPlayerEntity> playersInTriggerZone = world.getEntitiesByClass(ServerPlayerEntity.class, triggerBox, p -> !p.isSpectator());

        if (!playersInTriggerZone.isEmpty()) {
            startBattle(world, pos);
        }
    }

    private void handleActiveBattle(ServerWorld world) {
        activeMobUuids.removeIf(uuid -> world.getEntity(uuid) == null || !world.getEntity(uuid).isAlive());

        if (activeMobUuids.isEmpty()) {
            handleBattleWin(world);
            return;
        }

        Box battleBox = new Box(pos).expand(battleRadius);
        List<ServerPlayerEntity> playersInBattle = world.getEntitiesByClass(ServerPlayerEntity.class, battleBox, p -> !p.isSpectator());
        if (playersInBattle.isEmpty()) {
            handleBattleLoss(world, "All players left the battle area.");
            return;
        }

        for (UUID mobUuid : new ArrayList<>(activeMobUuids)) {
            Entity mob = world.getEntity(mobUuid);
            if (mob != null && !battleBox.intersects(mob.getBoundingBox())) {
                handleBattleLoss(world, "A mob left the battle zone.");
                return; // Exit immediately
            }
        }

        if (regeneration > 0) {
            regenerationTickTimer++;
            if (regenerationTickTimer >= 100) { // Every 5 seconds
                for (UUID mobUuid : activeMobUuids) {
                    Entity mob = world.getEntity(mobUuid);
                    if (mob instanceof LivingEntity livingMob) {
                        livingMob.heal((float) regeneration);
                    }
                }
                regenerationTickTimer = 0;
            }
        }
    }


    private void startBattle(ServerWorld world, BlockPos spawnCenter) {
        Optional<EntityType<?>> entityTypeOpt = Registries.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(mobId));
        if (entityTypeOpt.isEmpty()) {
            YggdrasilLdMod.LOGGER.error("Invalid mob ID in arena spawner at {}: {}", this.pos, this.mobId);
            return;
        }

        isBattleActive = true;
        playerDamageDealt.clear();

        for (int i = 0; i < mobCount; i++) {
            Entity mob = entityTypeOpt.get().create(world);
            if (mob instanceof LivingEntity livingMob) {
                // Apply custom stats
                Objects.requireNonNull(livingMob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(this.mobHealth);
                livingMob.heal((float) this.mobHealth);
                if (livingMob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
                    Objects.requireNonNull(livingMob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(this.mobAttackDamage);
                }


                double x = spawnCenter.getX() + 0.5 + (world.random.nextDouble() - 0.5) * mobSpread * 2;
                double y = spawnCenter.getY() + 1;
                double z = spawnCenter.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * mobSpread * 2;
                livingMob.refreshPositionAndAngles(x, y, z, world.random.nextFloat() * 360.0F, 0.0F);
                world.spawnEntity(livingMob);
                activeMobUuids.add(livingMob.getUuid());
            }
        }
        YggdrasilLdMod.LOGGER.info("Mob Spawner started at {} with {} of {}", this.pos, this.mobCount, this.mobId);
        markDirty();
    }

    private void handleBattleWin(ServerWorld world) {
        YggdrasilLdMod.LOGGER.info("Mob Spawner won at {}", pos);

        // --- Drop Loot ---
        if (lootTableId != null && !lootTableId.isEmpty()) {
            Identifier lootTableIdentifier = Identifier.tryParse(lootTableId);
            if (lootTableIdentifier != null) {
                RegistryKey<LootTable> lootTableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableIdentifier);
                LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(lootTableKey);

                ServerPlayerEntity contextPlayer = null;
                if (!playerDamageDealt.isEmpty()) {
                    UUID playerUuid = playerDamageDealt.keySet().iterator().next();
                    contextPlayer = world.getServer().getPlayerManager().getPlayer(playerUuid);
                }

                if (contextPlayer == null) {
                    Box battleBox = new Box(pos).expand(battleRadius);
                    List<ServerPlayerEntity> playersInBattle = world.getEntitiesByClass(ServerPlayerEntity.class, battleBox, p -> !p.isSpectator());
                    if (!playersInBattle.isEmpty()) {
                        contextPlayer = playersInBattle.get(0);
                    }
                }

                if (contextPlayer != null) {
                    LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                            .add(LootContextParameters.ORIGIN, pos.toCenterPos())
                            .add(LootContextParameters.THIS_ENTITY, contextPlayer);

                    List<ItemStack> loot = lootTable.generateLoot(builder.build(LootContextTypes.GIFT));
                    for (ItemStack stack : loot) {
                        world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, stack));
                    }
                } else {
                    YggdrasilLdMod.LOGGER.warn("Mob Spawner at {} won, but no player could be found to generate loot.", pos);
                }
            }
        }

        // --- Grant Skill Experience ---
        if (skillExperiencePerWin > 0) {
            MinecraftServer server = world.getServer();
            if (server != null) {
                ServerCommandSource source = server.getCommandSource();
                CommandManager commandManager = server.getCommandManager();

                playerDamageDealt.forEach((playerUuid, damage) -> {
                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
                    if (player != null) {
                        String command = String.format("puffish_skills experience add %s %s %d",
                                player.getName().getString(), YggdrasilLdMod.CONFIG.puffish_skills_tree_id, skillExperiencePerWin);
                        ParseResults<ServerCommandSource> parseResults = commandManager.getDispatcher().parse(command, source.withSilent());
                        commandManager.execute(parseResults, command);
                    }
                });
            }
        }

        resetSpawner(world, true);
    }

    private void handleBattleLoss(ServerWorld world, String reason) {
        YggdrasilLdMod.LOGGER.info("Mob Spawner lost at {}: {}", pos, reason);
        for (UUID mobUuid : activeMobUuids) {
            Entity mob = world.getEntity(mobUuid);
            if (mob != null && mob.isAlive()) {
                mob.discard();
            }
        }
        resetSpawner(world, false);
    }

    private void resetSpawner(ServerWorld world, boolean wasWin) {
        isBattleActive = false;
        activeMobUuids.clear();
        playerDamageDealt.clear();
        regenerationTickTimer = 0;
        if (wasWin) {
            respawnCooldown = respawnTime;
        } else {
            respawnCooldown = 0; // Immediate reset
        }
        markDirty();
        world.updateListeners(pos, getCachedState(), getCachedState(), 3);
    }

    public void onMobDamaged(DamageSource source, float amount) {
        if (isBattleActive && source.getAttacker() instanceof PlayerEntity) {
            UUID playerUuid = source.getAttacker().getUuid();
            playerDamageDealt.merge(playerUuid, amount, Float::sum);
        }
    }


    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("MobId", mobId);
        nbt.putInt("RespawnTime", respawnTime);
        nbt.putString("LootTableId", lootTableId);
        nbt.putInt("TriggerRadius", triggerRadius);
        nbt.putInt("BattleRadius", battleRadius);
        nbt.putInt("Regeneration", regeneration);
        nbt.putInt("SkillExperiencePerWin", skillExperiencePerWin);
        nbt.putInt("MobCount", mobCount);
        nbt.putInt("MobSpread", mobSpread);
        nbt.putDouble("MobHealth", mobHealth);
        nbt.putDouble("MobAttackDamage", mobAttackDamage);
        nbt.putBoolean("IsBattleActive", isBattleActive);
        nbt.putInt("RespawnCooldown", respawnCooldown);

        NbtList mobUuids = new NbtList();
        for (UUID uuid : activeMobUuids) {
            NbtCompound uuidNbt = new NbtCompound();
            uuidNbt.putUuid("uuid", uuid);
            mobUuids.add(uuidNbt);
        }
        nbt.put("ActiveMobs", mobUuids);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        mobId = nbt.getString("MobId");
        respawnTime = nbt.getInt("RespawnTime");
        lootTableId = nbt.getString("LootTableId");
        triggerRadius = nbt.getInt("TriggerRadius");
        battleRadius = nbt.getInt("BattleRadius");
        regeneration = nbt.getInt("Regeneration");
        skillExperiencePerWin = nbt.getInt("SkillExperiencePerWin");
        mobCount = nbt.contains("MobCount") ? nbt.getInt("MobCount") : 1;
        mobSpread = nbt.contains("MobSpread") ? nbt.getInt("MobSpread") : 5;
        mobHealth = nbt.contains("MobHealth") ? nbt.getDouble("MobHealth") : 20.0;
        mobAttackDamage = nbt.contains("MobAttackDamage") ? nbt.getDouble("MobAttackDamage") : 3.0;
        isBattleActive = nbt.getBoolean("IsBattleActive");
        respawnCooldown = nbt.getInt("RespawnCooldown");

        activeMobUuids.clear();
        NbtList mobUuids = nbt.getList("ActiveMobs", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < mobUuids.size(); i++) {
            NbtCompound uuidNbt = mobUuids.getCompound(i);
            activeMobUuids.add(uuidNbt.getUuid("uuid"));
        }
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
        return Text.literal("Mob Spawner Configuration");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new MobSpawnerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public MobSpawnerData getScreenOpeningData(ServerPlayerEntity player) {
        return new MobSpawnerData(this.pos);
    }
}

