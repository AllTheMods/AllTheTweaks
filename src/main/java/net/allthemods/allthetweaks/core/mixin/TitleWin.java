package net.allthemods.allthetweaks.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.allthemods.allthetweaks.ATTConfig;
import net.allthemods.allthetweaks.proxy.BCCProxy;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value= Minecraft.class)
public class TitleWin {

    @ModifyReturnValue(method = "createTitle", at = @At("RETURN"))
    private static String allthetweaks$modifyTitleWin(String original) {
        if(ModList.get() == null){return original;}

        if(ModList.get().isLoaded("bcc")){
            return ATTConfig.packProfile().displayName() + " v" + BCCProxy.getVersion();
        }
        return original;
    }
}
