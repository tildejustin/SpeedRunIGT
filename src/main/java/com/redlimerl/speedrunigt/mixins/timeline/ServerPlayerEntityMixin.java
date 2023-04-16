package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class ServerPlayerEntityMixin {

    private ServerWorld beforeWorld = null;
    private Vec3d lastPortalPos = null;

    @Inject(method = "method_1984", at = @At("HEAD"))
    public void onChangeDimension(ServerPlayerEntity serverPlayerEntity, DimensionType dimensionType, CallbackInfo ci) {
        beforeWorld = serverPlayerEntity.getServerWorld();
        lastPortalPos = serverPlayerEntity.method_10787();
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;
    }

    @Inject(method = "method_1984", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;method_1986(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(ServerPlayerEntity serverPlayerEntity, DimensionType dimensionType, CallbackInfo ci) {
        DimensionType oldDimension = beforeWorld.dimension.method_11789();
        DimensionType newDimension = serverPlayerEntity.world.dimension.method_11789();

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE) {
            if (oldDimension == DimensionType.OVERWORLD && newDimension == DimensionType.THE_NETHER) {
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY) InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(DimensionType.THE_NETHER, serverPlayerEntity.method_10787().add(0, 0, 0), lastPortalPos.add(0, 0, 0));
            }

            if (oldDimension == DimensionType.THE_NETHER && newDimension == DimensionType.OVERWORLD) {
                if (this.isEnoughTravel(serverPlayerEntity)) {
                    int portalIndex = InGameTimerUtils.isBlindTraveled(lastPortalPos);
                    InGameTimer.getInstance().tryInsertNewTimeline("nether_travel");
                    if (portalIndex == 0) {
                        InGameTimer.getInstance().tryInsertNewTimeline("nether_travel_home");
                    } else {
                        InGameTimer.getInstance().tryInsertNewTimeline("nether_travel_blind");
                    }
                }
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY) InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(DimensionType.OVERWORLD, lastPortalPos.add(0, 0, 0), serverPlayerEntity.method_10787().add(0, 0, 0));
            }
        }
    }

    private boolean isEnoughTravel(ServerPlayerEntity serverPlayerEntity) {
        boolean eye = false, pearl = false, rod = false;
        for (ItemStack itemStack : serverPlayerEntity.inventory.field_15082) {
            if (itemStack != null) {
                if (itemStack.getItem() == Items.EYE_OF_ENDER) eye = true;
                if (itemStack.getItem() == Items.ENDER_PEARL) pearl = true;
                if (itemStack.getItem() == Items.BLAZE_POWDER || itemStack.getItem() == Items.BLAZE_ROD) rod = true;
            }
        }

        return eye || (pearl && rod);
    }
}
