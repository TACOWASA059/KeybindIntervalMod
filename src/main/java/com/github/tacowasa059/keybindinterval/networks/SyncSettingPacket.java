package com.github.tacowasa059.keybindinterval.networks;

import com.github.tacowasa059.keybindinterval.common.YamlLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSettingPacket {
    private final String path;
    private final String category;
    private final Integer value;

    public SyncSettingPacket(String path, String category, Integer value) {
        this.path = path;
        this.category = category;
        this.value = value;
    }

    public static void encode(SyncSettingPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.path);
        buf.writeUtf(packet.category);
        buf.writeInt(packet.value);
    }

    public static SyncSettingPacket decode(FriendlyByteBuf buf) {
        return new SyncSettingPacket(buf.readUtf(), buf.readUtf(), buf.readInt());
    }

    public static void handle(SyncSettingPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> Minecraft.getInstance().execute(() -> YamlLoader.addData(packet.path, packet.category, packet.value)));
        ctx.get().setPacketHandled(true);
    }
}
