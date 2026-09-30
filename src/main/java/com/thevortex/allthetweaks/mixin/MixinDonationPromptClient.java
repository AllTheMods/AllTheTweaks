package com.thevortex.allthetweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.fishguy129.apokinetics.client.DonationPromptClient", remap = false)
public abstract class MixinDonationPromptClient
{
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 0)
    private static void allthetweaks$suppressDonationPrompt(CallbackInfo ci) {
        ci.cancel();
    }
}
