package com.eidng3lz.mininginplace.mixins.client;

import com.eidng3lz.mininginplace.Config;
import com.eidng3lz.mininginplace.MiningInPlaceClient;
import com.eidng3lz.mininginplace.util.BlockGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void cancelBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ClientLevel world = minecraft.level;
        BlockState blockState = world.getBlockState(pos);
        LocalPlayer player = minecraft.player;
        BlockGroup blockGroup = new BlockGroup();
        if (MiningInPlaceClient.keyState) {
            blockGroup.add(blockState.getBlock());
        } else {
            @SuppressWarnings("unchecked") List<BlockGroup> chainedBlocksGroups = (List<BlockGroup>) MiningInPlaceClient.serverConfigs.get(Config.ServerConfigs.CHAINED_BLOCKS_GROUPS);
            for (BlockGroup configBlockGroup : chainedBlocksGroups) {
                if (configBlockGroup.contains(blockState.getBlock())) {
                    blockGroup.addBlockGroup(configBlockGroup);
                }
            }
        }
        if (
                !blockGroup.isEmpty()
                        && (player.isShiftKeyDown() == Config.inventControl)
                        && !(Config.disableAtCreative && player.isCreative())
        ) {
            cir.setReturnValue(false);
        }
    }
}
