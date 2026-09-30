package com.thevortex.allthetweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.platform.SablePlotPlatform;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot", remap = false)
public class MixinServerLevelPlot
{
    // Sable's plot load() reads each chunk's NeoForge attachments from that chunk's own tag, while
    // save() hands the plot's root tag to writeChunkAttachments. Every chunk then overwrites the same
    // root entry and load() never finds it, so data attachments on sub-level chunks are lost on every
    // reload. Writing them into the chunk tag puts them where load() reads them.
    // https://github.com/ryanhcode/sable/issues/1601
    @WrapOperation(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/sable/platform/SablePlotPlatform;writeChunkAttachments(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/chunk/LevelChunk;)V"
            ),
            require = 0
    )
    private void allthetweaks$writeAttachmentsToChunkTag(SablePlotPlatform platform, CompoundTag plotTag, RegistryAccess registryAccess, LevelChunk chunk, Operation<Void> original, @Local(name = "chunkTag") CompoundTag chunkTag) {
        original.call(platform, chunkTag, registryAccess, chunk);
    }
}
