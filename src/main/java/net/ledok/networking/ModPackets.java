package net.ledok.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.ledok.Yggdrasil_ld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

public class ModPackets {

    // Клас, що представляє наш пакет даних
    public record ReputationSyncPayload(UUID playerUuid, int reputation) implements CustomPayload {
        // ID нашого пакету
        public static final CustomPayload.Id<ReputationSyncPayload> ID = new CustomPayload.Id<>(Yggdrasil_ld.REPUTATION_SYNC_ID);
        // "Кодек" для запису та читання даних. Це новий стандарт Fabric.
        public static final PacketCodec<PacketByteBuf, ReputationSyncPayload> CODEC = PacketCodec.of(ReputationSyncPayload::write, ReputationSyncPayload::new);

        // Конструктор для читання даних з буферу
        public ReputationSyncPayload(PacketByteBuf buf) {
            this(buf.readUuid(), buf.readInt());
        }

        // Метод для запису даних у буфер (не потребує @Override)
        public void write(PacketByteBuf buf) {
            buf.writeUuid(playerUuid);
            buf.writeInt(reputation);
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    // Метод для реєстрації пакетів, що йдуть з сервера на клієнт (S2C)
    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(ReputationSyncPayload.ID, ReputationSyncPayload.CODEC);
    }
}

