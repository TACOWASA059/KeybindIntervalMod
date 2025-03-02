package com.github.tacowasa059.keybindinterval.networks;

import com.github.tacowasa059.keybindinterval.common.YamlLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncYamlPacket {
    private final Map<String, Map<String, Integer>> yamlData;

    public SyncYamlPacket(Map<String, Map<String, Integer>> yamlData) {
        this.yamlData = yamlData;
    }

    public SyncYamlPacket(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        this.yamlData = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buffer.readUtf();
            int subSize = buffer.readVarInt();
            Map<String, Integer> subMap = new HashMap<>();
            for (int j = 0; j < subSize; j++) {
                String subKey = buffer.readUtf();
                Integer value = buffer.readInt();
                subMap.put(subKey, value);
            }
            this.yamlData.put(key, subMap);
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(yamlData.size());
        for (Map.Entry<String, Map<String, Integer>> entry : yamlData.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeVarInt(entry.getValue().size());
            for (Map.Entry<String, Integer> subEntry : entry.getValue().entrySet()) {
                buffer.writeUtf(subEntry.getKey());
                buffer.writeInt(subEntry.getValue());
            }
        }
    }

    public static SyncYamlPacket decode(FriendlyByteBuf buffer) {
        return new SyncYamlPacket(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> YamlLoader.setData(yamlData));
        context.setPacketHandled(true);
    }
}
