package com.thevortex.allthetweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.simibubi.create.foundation.collision.ContinuousOBBCollider", remap = false)
public class MixinContinuousOBBCollider
{
    // The separation manifold records an axis only when the tested distance is non-zero, so a
    // contraption block sitting exactly on an entity's centre leaves both axes null and crashes
    // the tick loop. The paired separation is then still Double.MAX_VALUE, so a zero axis cancels
    // that pair's push for the tick, and the next tick the entity has moved enough to find a real
    // axis.
    @ModifyExpressionValue(
            method = "collideMany",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lcom/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold;axis:Lnet/minecraft/world/phys/Vec3;"
            ),
            require = 0
    )
    private static Vec3 allthetweaks$guardAxis(Vec3 axis) {
        return axis == null ? Vec3.ZERO : axis;
    }

    @ModifyExpressionValue(
            method = "collideMany",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lcom/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold;normalAxis:Lnet/minecraft/world/phys/Vec3;"
            ),
            require = 0
    )
    private static Vec3 allthetweaks$guardNormalAxis(Vec3 normalAxis) {
        return normalAxis == null ? Vec3.ZERO : normalAxis;
    }
}
