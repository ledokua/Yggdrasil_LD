package net.ledok.reputation;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReputationState extends PersistentState {

    // Структура для зберігання репутації: UUID гравця -> його репутація
    private final Map<UUID, Integer> playerReputations = new HashMap<>();

    /**
     * Цей метод записує всі дані з нашої Map у NBT-тег,
     * який потім Minecraft збереже у файл.
     */
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound reputationsNbt = new NbtCompound();
        playerReputations.forEach((uuid, reputation) -> {
            reputationsNbt.putInt(uuid.toString(), reputation);
        });
        nbt.put("reputations", reputationsNbt);
        return nbt;
    }

    /**
     * Цей статичний метод читає дані з NBT-тегу, коли світ завантажується,
     * і створює новий екземпляр нашого класу, наповнений цими даними.
     * -- ЗМІНЕНО: Додано другий параметр для сумісності з новим API --
     */
    public static ReputationState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        ReputationState state = new ReputationState();
        NbtCompound reputationsNbt = tag.getCompound("reputations");
        for (String key : reputationsNbt.getKeys()) {
            state.playerReputations.put(UUID.fromString(key), reputationsNbt.getInt(key));
        }
        return state;
    }

    /**
     * Спеціальний об'єкт, який реєструє наш клас у системі збереження Minecraft.
     * Він вказує, як створювати новий стан і як завантажувати існуючий.
     */
    public static final Type<ReputationState> Type = new Type<>(
            ReputationState::new,         // Функція для створення нового, порожнього стану
            ReputationState::createFromNbt, // Функція для завантаження стану з NBT (тепер працює)
            null                          // DataFixer, для модів зазвичай не потрібен
    );

    /**
     * Зручний метод для отримання нашого стану зі світу.
     * Він або завантажить існуючий стан, або створить новий, якщо його ще немає.
     */
    public static ReputationState getServerState(ServerWorld world) {
        PersistentStateManager persistentStateManager = world.getPersistentStateManager();
        // "yggdrasil_reputation" - це унікальне ім'я нашого файлу збереження
        return persistentStateManager.getOrCreate(Type, "yggdrasil_reputation");
    }

    // --- Публічні методи для керування репутацією ---

    public int getReputation(UUID playerUuid) {
        return playerReputations.getOrDefault(playerUuid, 0);
    }

    public void setReputation(UUID playerUuid, int amount) {
        // Обмежуємо репутацію в межах від -100000 до 100000
        int cappedAmount = Math.max(-100000, Math.min(1000000, amount));
        playerReputations.put(playerUuid, cappedAmount);

        // ВАЖЛИВО: Повідомляємо Minecraft, що дані змінилися і їх потрібно зберегти на диск.
        markDirty();
    }
}

