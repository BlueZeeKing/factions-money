package dev.blueish.factions.money.mixin;

import dev.blueish.factions.money.FactionsMoneyMod;
import io.icker.factions.api.persistents.Faction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Faction.class)
public class FactionMixin {
    @Inject(method = "calculateMaxPower", at = @At("RETURN"), cancellable = true, remap = false)
    public void calculateMaxPower(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + FactionsMoneyMod.getMoney((Faction)(Object)this));
    }

    @Inject(method = "getPower", at = @At("RETURN"), cancellable = true, remap = false)
    public void getPower(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + FactionsMoneyMod.getMoney((Faction)(Object)this));
    }
}
