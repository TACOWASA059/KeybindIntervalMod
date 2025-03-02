package com.github.tacowasa059.keybindinterval;

import com.github.tacowasa059.keybindinterval.networks.NetworkHandler;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(KeybindInterval.MODID)
public class KeybindInterval {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "keybindinterval";

    public KeybindInterval() {
        NetworkHandler.register();
    }
}
