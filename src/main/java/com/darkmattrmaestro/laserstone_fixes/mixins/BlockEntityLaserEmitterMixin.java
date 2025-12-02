package com.darkmattrmaestro.laserstone_fixes.mixins;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.darkmattrmaestro.laserstone_fixes.Constants;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntity;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntityCreator;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntityLaserEmitter;
import finalforeach.cosmicreach.blocks.blockentities.IBlockEntity;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.projectiles.EntityProjectileLaser;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.gameevents.blockevents.LaserBlockEventArgs;
import finalforeach.cosmicreach.items.containers.SlotContainer;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.util.Axis;
import finalforeach.cosmicreach.util.constants.Direction;
import finalforeach.cosmicreach.world.Zone;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.badlogic.gdx.math.MathUtils;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.projectiles.EntityProjectileLaser;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.containers.SlotContainer;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.util.constants.Direction;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(BlockEntityLaserEmitter.class)
public class BlockEntityLaserEmitterMixin extends BlockEntity implements IBlockEntity {
    @Shadow public static final String BLOCK_ENTITY_ID = "base:laser_emitter";
    @Shadow public static final String SIGNAL_SHOOT_PROJECTILE = "shootProjectile";
    @CRBSerialized
    @Shadow boolean willShoot;
    @CRBSerialized
    @Shadow public Color laserColor;
    @Shadow int shootTick;

//    public void onInteract(Player player, Zone zone) {
//        super.onInteract(player, zone);
//        if (GameSingletons.isHost) {
//            this.shootProjectile();
//        }
//    }

//    public void onTick() {
//        super.onTick();
//        if (this.willShoot) {
//            if (this.getZone().currentZoneTick - this.shootTick == 0) {
//                return;
//            }
//
//            Constants.LOGGER.warn("CREATED LASER ENTITY");
//            this.shootProjectileNow();
//            this.willShoot = false;
//        }
//
//        this.setTicking(false);
//    }

//    @Shadow
//    public void shootProjectile() {
//        BlockState blockState = this.getBlockState();
//        if (blockState != null) {
//            String type = blockState.getParam("type");
//            if (type != null) {
//                switch (type) {
//                    case "single" -> {
//                        Direction direction = blockState.getParamDirection("direction");
//                        Constants.LOGGER.warn("    setTicking " + this.getZone().currentZoneTick + " Single " + direction.toString());
//                    }
//                    case "split" -> {
//                        String direction = blockState.getParam("axis");
//                        float xDir = 0.0F;
//                        float yDir = 0.0F;
//                        float zDir = 0.0F;
//                        switch (direction) {
//                            case "Z" -> zDir = 1.0F;
//                            case "X" -> xDir = 1.0F;
//                            case "Y" -> yDir = 1.0F;
//                            default -> throw new IllegalArgumentException("Unexpected value: " + direction);
//                        }
//                        Constants.LOGGER.warn("    setTicking " + this.getZone().currentZoneTick + " Split " + xDir + " " + yDir + " " + zDir);
//                    }
//                }
//
//            }
//        }
//
//        this.shootTick = this.getZone().currentZoneTick;
//        this.willShoot = true;
//        this.setTicking(true);
//    }
//
//    @Shadow
//    public void shootProjectileNow() {}
//
////    @Shadow
//    private void shootProjectileNowSplit(BlockState blockState) {
//        String direction = blockState.getParam("axis");
//        float xDir = 0.0F;
//        float yDir = 0.0F;
//        float zDir = 0.0F;
//        switch (direction) {
//            case "Z" -> zDir = 1.0F;
//            case "X" -> xDir = 1.0F;
//            case "Y" -> yDir = 1.0F;
//            default -> throw new IllegalArgumentException("Unexpected value: " + direction);
//        }
//
//        this.shootProjectileNowInDirection(xDir, yDir, zDir, true);
////        this.setGlobalPosition(this.getGlobalX(), this.getGlobalY(), this.getGlobalZ() + 1);
////        this.shootProjectileNowInDirection(xDir, yDir, zDir, false);
////        this.setGlobalPosition(this.getGlobalX(), this.getGlobalY(), this.getGlobalZ() + 1);
////        this.shootProjectileNowInDirection(xDir, yDir, zDir, false);
////        this.setGlobalPosition(this.getGlobalX(), this.getGlobalY(), this.getGlobalZ() + 1);
////        this.shootProjectileNowInDirection(xDir, yDir, zDir, false);
////        this.setGlobalPosition(this.getGlobalX(), this.getGlobalY(), this.getGlobalZ() + 1);
////        this.shootProjectileNowInDirection(xDir, yDir, zDir, false);
////        this.setGlobalPosition(this.getGlobalX(), this.getGlobalY(), this.getGlobalZ() + 1);
////        this.shootProjectileNowInDirection(xDir, yDir, zDir, false);
////        this.setGlobalPosition(this.getGlobalX(), this.getGlobalY(), this.getGlobalZ() - 5);
//    }

    @Inject(method = "shootProjectileNowSplit", at = @At(value = "HEAD"))
    private void shootProjectileNowSplitHead(BlockState blockState, CallbackInfo ci) {
        Constants.LOGGER.warn("Shooting split from {} {} {}", this.getGlobalX(), this.getGlobalY(), this.getGlobalZ());
    }

    @Inject(method = "shootProjectileNowSplit", at = @At(value = "TAIL"))
    private void shootProjectileNowSplitTail(BlockState blockState, CallbackInfo ci) {
        Constants.LOGGER.warn("   Shoot split from {} {} {}", this.getGlobalX(), this.getGlobalY(), this.getGlobalZ());
    }
//
//    @Shadow
//    private void shootProjectileNowSingle(BlockState blockState) {
//        Direction direction = blockState.getParamDirection("direction");
//        this.shootProjectileNowInDirection((float)direction.getXOffset(), (float)direction.getYOffset(), (float)direction.getZOffset(), true);
//    }
//

    @Inject(method = "shootProjectileNowInDirection", at = @At(value = "HEAD"), cancellable = true)
    private void shootProjectileNowInDirection(float xDir, float yDir, float zDir, boolean playSound, CallbackInfo ci) {
        EntityProjectileLaser projectile = new EntityProjectileLaser(this);
        projectile.setPosition((float)this.getGlobalX() + 0.5F, (float)this.getGlobalY() + 0.5F, (float)this.getGlobalZ() + 0.5F);
        projectile.position.add(xDir / 2.0F, yDir / 2.0F, zDir / 2.0F);
        projectile.velocity.set(xDir, yDir, zDir).nor().scl(60.0F);
        projectile.setLaserColor(this.laserColor);
        this.getZone().addEntity(projectile);
        projectile.updateConstraints(this.getZone(), projectile.getPosition());
        if (playSound) {
            EntityProjectileLaser.gameSound.playGlobalSound3D(this.getZone(), projectile.getPosition(), 1.0F, MathUtils.random(0.95F, 1.05F));
        }

        Constants.LOGGER.warn("   -   projectile {} - v: {}", projectile.position, projectile.velocity);

        ci.cancel();
    }

//    @Shadow
//    private void shootProjectileNowInDirection(float xDir, float yDir, float zDir, boolean playSound) {
//        Constants.LOGGER.warn(this.getZone().currentZoneTick + " | " + (xDir == 0 ? "= " : xDir > 0 ? "+ " : "- ") + (yDir == 0 ? "= " : yDir > 0 ? "+ " : "- ") + (zDir == 0 ? "=" : zDir > 0 ? "+" : "-"));
//        EntityProjectileLaser projectile = new EntityProjectileLaser(this);
//        projectile.setPosition((float)this.getGlobalX() + 0.5F, (float)this.getGlobalY() + 0.5F, (float)this.getGlobalZ() + 0.5F);
//        projectile.position.add(xDir / 2.0F, yDir / 2.0F, zDir / 2.0F);
//        projectile.velocity.set(xDir, yDir, zDir).nor().scl(60.0F);
//        projectile.setLaserColor(this.laserColor);
//        this.getZone().addEntity(projectile);
//        projectile.updateConstraints(this.getZone(), projectile.getPosition());
//        if (playSound) {
//            EntityProjectileLaser.gameSound.playGlobalSound3D(this.getZone(), projectile.getPosition(), 1.0F, MathUtils.random(0.95F, 1.05F));
//        }
//
//        Constants.LOGGER.warn("Shot from {} {} {}", this.getGlobalX(), this.getGlobalY(), this.getGlobalZ());
//
//    }

    @Shadow
    public String getBlockEntityId() {
        return "base:laser_emitter";
    }
}

