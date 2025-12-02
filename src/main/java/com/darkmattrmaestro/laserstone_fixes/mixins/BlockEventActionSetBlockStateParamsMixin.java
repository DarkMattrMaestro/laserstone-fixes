package com.darkmattrmaestro.laserstone_fixes.mixins;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.darkmattrmaestro.laserstone_fixes.Constants;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.BlockStateMissing;
import finalforeach.cosmicreach.gameevents.ActionId;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventArgs;
import finalforeach.cosmicreach.gameevents.blockevents.actions.BlockEventActionSetBlockStateParams;
import finalforeach.cosmicreach.gameevents.blockevents.actions.IBlockAction;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(BlockEventActionSetBlockStateParams.class)
@ActionId(
        id = "base:set_block_state_params"
)
public class BlockEventActionSetBlockStateParamsMixin {
    @Shadow
    private static final Pool<BlockPosition> POSITION_POOL = Pools.get(BlockPosition.class);
    @Shadow
    int xOff;
    @Shadow
    int yOff;
    @Shadow
    int zOff;
    @Shadow
    HashMap<String, String> params = new HashMap();

    @Inject(method = "act", at = @At(value = "HEAD"), cancellable = true)
    public void act(BlockEventArgs args, CallbackInfo ci) {
        Zone zone = args.zone;
        BlockPosition sourcePos = args.blockPos;
        BlockPosition bp = sourcePos.getOffsetBlockPos(POSITION_POOL, zone, this.xOff, this.yOff, this.zOff);
        BlockState blockState = bp.getBlockState();
        blockState = blockState.getVariantWithParams(this.params);
        if (bp != null) {
            if (blockState instanceof BlockStateMissing) {
                boolean var7 = false;
            }

            BlockSetter.get().replaceBlock(zone, blockState, bp);
        }

        POSITION_POOL.free(bp);

//        if (!"base:laser_emitter".equals(blockState.getBlockId())) {
            Constants.LOGGER.error("          + act bp: {}", bp);
//        }

        ci.cancel();
    }
}