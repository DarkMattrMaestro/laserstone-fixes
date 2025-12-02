package com.darkmattrmaestro.laserstone_fixes.mixins;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Queue;
import com.darkmattrmaestro.laserstone_fixes.Constants;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntity;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntityLaserEmitter;
import finalforeach.cosmicreach.lighting.BlockLightPropagator;
import finalforeach.cosmicreach.lighting.SkyLightPropagator;
import finalforeach.cosmicreach.networking.packets.blockentities.BlockEntityDataPacket;
import finalforeach.cosmicreach.networking.packets.blocks.BlockReplacePacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.util.constants.Direction;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(BlockSetter.class)
public class BlockSetterMixin {
    @Shadow
    SkyLightPropagator skylightProp = new SkyLightPropagator();
    @Shadow
    BlockLightPropagator blockLightProp = new BlockLightPropagator();
    @Shadow
    Queue<BlockPosition> tmpQueue = new Queue();

    @Inject(method = "replaceBlock(Lfinalforeach/cosmicreach/world/Zone;Lfinalforeach/cosmicreach/blocks/BlockState;Lfinalforeach/cosmicreach/blocks/BlockPosition;)V", at = @At(value = "HEAD"), cancellable = true)
    public void replaceBlock(Zone zone, BlockState targetBlockState, BlockPosition blockPos, CallbackInfo ci) {
        BlockState oldBlockState = blockPos.getBlockState();
        if (targetBlockState != oldBlockState) {
            blockPos.setBlockState(targetBlockState);
            this.adjustLightsAfterReplace(zone, oldBlockState, targetBlockState, blockPos, this.tmpQueue);
            if (targetBlockState.getModel() != oldBlockState.getModel() && GameSingletons.isClient) {
                blockPos.flagTouchingChunksForRemeshing(zone, false);
                GameSingletons.meshGenThread.requestImmediateResorting();
            }
        }

        if (!"base:laser_emitter".equals(targetBlockState.getBlockId())){
            Constants.LOGGER.error("         - Replaced {} with {} at {}", oldBlockState, blockPos.getBlockState(), blockPos);
        }

        if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
            ServerSingletons.SERVER.broadcast(zone, new BlockReplacePacket(zone, targetBlockState, blockPos));
        }

        ci.cancel();
    }

    @Shadow
    private void adjustLightsAfterReplace(Zone zone, BlockState oldBlockState, BlockState targetBlockState, BlockPosition blockPos, Queue<BlockPosition> tmpQueue) {

    }
}
