package com.github.tacowasa059.keybindinterval.server;

import com.github.tacowasa059.keybindinterval.KeybindInterval;
import com.github.tacowasa059.keybindinterval.common.YamlLoader;
import com.github.tacowasa059.keybindinterval.networks.NetworkHandler;
import com.github.tacowasa059.keybindinterval.networks.SyncYamlPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = KeybindInterval.MODID)
public class ServerEventListener {
    @SubscribeEvent
    public static void onServerStatingEvent(ServerStartingEvent event){
        MinecraftServer server = event.getServer();
        YamlLoader.loadYaml(server);
    }

    @SubscribeEvent
    public static void onServerJoinEvent(PlayerEvent.PlayerLoggedInEvent event){
        Player player = event.getEntity();
        if(player instanceof  ServerPlayer serverPlayer){
            SyncYamlPacket packet = new SyncYamlPacket(YamlLoader.get());
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
        }
    }
}
