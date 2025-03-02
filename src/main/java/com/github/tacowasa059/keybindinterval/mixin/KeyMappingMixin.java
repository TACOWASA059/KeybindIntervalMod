package com.github.tacowasa059.keybindinterval.mixin;

import com.github.tacowasa059.keybindinterval.interfaces.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements KeyMappingAccessor {
    @Shadow
    protected abstract void release();
    @Override
    public void keybindInterval$release() {
        release();
    }
}
