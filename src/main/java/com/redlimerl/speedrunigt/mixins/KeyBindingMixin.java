package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.util.collection.IntObjectStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Shadow @Final private static IntObjectStorage<KeyBinding> KEY_MAP;

    @Inject(method = "setKeyPressed", at = @At("TAIL"))
    private static void onPress(int keyCode, boolean pressed, CallbackInfo ci) {
        KeyBinding keyBinding = KEY_MAP.get(keyCode);
        if(keyBinding!=null && pressed){
            InGameTimer timer = InGameTimer.getInstance();
            if (Objects.equals(keyBinding.getCategory(), "key.categories.inventory")
                    || (Objects.equals(keyBinding.getCategory(), "key.categories.gameplay"))) {
                if (InGameTimerUtils.canUnpauseTimer(false)) {
                    timer.setPause(false, "pressed key");
                }
                timer.updateFirstInput();
            }
            if (keyBinding == SpeedRunIGT.timerResetKeyBinding) {
                if (timer.getCategory() == RunCategories.CUSTOM && timer.isResettable()) {
                    InGameTimer.reset();
                }
            }
            if (keyBinding == SpeedRunIGT.timerStopKeyBinding) {
                if (timer.getCategory() == RunCategories.CUSTOM && timer.isStarted()) {
                    InGameTimer.complete();
                }
            }
        }
    }
}
