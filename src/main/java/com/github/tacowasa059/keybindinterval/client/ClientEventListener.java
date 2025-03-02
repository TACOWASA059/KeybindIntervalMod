package com.github.tacowasa059.keybindinterval.client;

import com.github.tacowasa059.keybindinterval.KeybindInterval;
import com.github.tacowasa059.keybindinterval.common.YamlLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KeybindInterval.MODID, value = Dist.CLIENT)
public class ClientEventListener {
    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event){
        YamlLoader.reset();
        KeyInputListener.reset();
    }
}

