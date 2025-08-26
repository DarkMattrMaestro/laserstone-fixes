package com.darkmattrmaestro.laserstone_fixes.mixins;

import com.badlogic.gdx.math.MathUtils;
import com.darkmattrmaestro.laserstone_fixes.LaserstoneFixes;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blockentities.BlockEntityCreator;
import finalforeach.cosmicreach.blockentities.BlockEntityLaserEmitter;
import finalforeach.cosmicreach.blockentities.IBlockEntity;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.EntityLaserProjectile;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.containers.SlotContainer;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.darkmattrmaestro.laserstone_fixes.Constants;

import java.util.ArrayList;
import java.util.List;

@Mixin(BlockEntityLaserEmitter.class)
public class BlockEntityLaserEmitterMixin extends BlockEntity implements IBlockEntity {
    @Shadow public static final String BLOCK_ENTITY_ID = "base:laser_emitter";
    @Shadow public static final String SIGNAL_SHOOT_PROJECTILE = "shootProjectile";
    @Shadow @CRBSerialized
    boolean willShoot;
    @Shadow int shootTick;

    @Shadow
    public void onRemove() {
        super.onRemove();
        if (GameSingletons.isHost) {
        }

    }

    @Shadow
    public void onInteract(Player player, Zone zone) {
        super.onInteract(player, zone);
        if (GameSingletons.isHost) {
            this.shootProjectile();
        }

    }

    @Inject(
            method = "onTick",
            cancellable = true,
            at = @At("HEAD")
    )
    public void onTickMixin(CallbackInfo ci) {
        super.onTick();
        if (this.willShoot) {
            if (this.getZone().currentZoneTick - this.shootTick == 0) {
                return;
            }

            this.shootProjectileNow();
            this.willShoot = false;
        }

        this.setTicking(false);

        ci.cancel();
    }

    @Shadow
    public void shootProjectile() {
        this.shootTick = this.getZone().currentZoneTick;
        this.willShoot = true;
        this.setTicking(true);
    }

    @Shadow
    public void shootProjectileNow() {
        BlockState blockState = this.getBlockState();
        if (blockState != null) {
            String type = blockState.getParam("type");
            if (type != null) {
                switch (type) {
                    case "single" -> this.shootProjectileNowSingle(blockState);
                    case "split" -> this.shootProjectileNowSplit(blockState, null);
                }

            }
        }
    }

    @Inject(
            method = "shootProjectileNowSplit",
            cancellable = true,
            at = @At("HEAD")
    )
    private void shootProjectileNowSplit(BlockState blockState, CallbackInfo ci) {
        String direction = blockState.getParam("axis");
        float xDir = 0.0F;
        float yDir = 0.0F;
        float zDir = 0.0F;
        switch (direction) {
            case "Z" -> zDir = 1.0F;
            case "X" -> xDir = 1.0F;
            case "Y" -> yDir = 1.0F;
            default -> throw new IllegalArgumentException("Unexpected value: " + direction);
        }

        laserList.clear();

        this.shootProjectileNowInDirectionFixed(xDir, yDir, zDir, true);
        this.shootProjectileNowInDirectionFixed(-xDir, -yDir, -zDir, false);

        // Temporary
        Constants.LOGGER.info("laserList Size: {}", laserList.size());
        for (int i = 0; i < laserList.size(); i++) {
            EntityLaserProjectile projectile = laserList.get(laserList.size() - 1 - i);

            this.getZone().addEntity(projectile);
            projectile.updateConstraints(this.getZone(), projectile.getPosition());
        }

        ci.cancel();
    }

    @Inject(
            method = "shootProjectileNowSingle",
            cancellable = true,
            at = @At("HEAD")
    )
    private void shootProjectileNowSingleMixin(BlockState blockState, CallbackInfo ci) {
        String direction = blockState.getParam("direction");
        float xDir = 0.0F;
        float yDir = 0.0F;
        float zDir = 0.0F;
        switch (direction) {
            case "NegZ" -> zDir = -1.0F;
            case "PosX" -> xDir = 1.0F;
            case "PosZ" -> zDir = 1.0F;
            case "NegX" -> xDir = -1.0F;
            case "NegY" -> yDir = -1.0F;
            case "PosY" -> yDir = 1.0F;
            default -> throw new IllegalArgumentException("Unexpected value: " + direction);
        }

        laserList.clear();

        this.shootProjectileNowInDirection(xDir, yDir, zDir, true);

        // Temporary
        Constants.LOGGER.info("laserList Size: {}", laserList.size());
        for (int i = 0; i < laserList.size(); i++) {
            EntityLaserProjectile projectile = laserList.get(i);

            this.getZone().addEntity(projectile);
            projectile.updateConstraints(this.getZone(), projectile.getPosition());
        }

        ci.cancel();
    }

    /* ----------------------------- */
    /* shootProjectileNowInDirection */

    @Shadow
    private void shootProjectileNowInDirection(float xDir, float yDir, float zDir, boolean playSound) {}

    @Shadow
    private void shootProjectileNowSingle(BlockState blockState) {}

    @Unique List<EntityLaserProjectile> laserList = new ArrayList<EntityLaserProjectile>();

    @Inject(
            method = "shootProjectileNowInDirection",
            cancellable = true,
            at = @At("HEAD")
    )
    private void shootProjectileNowInDirectionMixin(float xDir, float yDir, float zDir, boolean playSound, CallbackInfo ci) {
        Constants.LOGGER.info("Called: shootProjectileNowInDirection Mixin");
        shootProjectileNowInDirectionFixed(xDir, yDir, zDir, playSound);

        ci.cancel();
    }

    @Unique
    private void shootProjectileNowInDirectionFixed(float xDir, float yDir, float zDir, boolean playSound) {
        EntityLaserProjectile projectile = new EntityLaserProjectile(this);
        projectile.setPosition((float)this.getGlobalX() + 0.5F, (float)this.getGlobalY() + 0.5F, (float)this.getGlobalZ() + 0.5F);
        projectile.position.add(xDir / 2.0F, yDir / 2.0F, zDir / 2.0F);
        projectile.velocity.set(xDir, yDir, zDir).nor().scl(60.0F);
//        this.getZone().addEntity(projectile);
//        projectile.updateConstraints(this.getZone(), projectile.getPosition());

        // Temporary
        laserList.add(projectile);

        if (playSound) {
            EntityLaserProjectile.gameSound.playGlobalSound3D(this.getZone(), projectile.getPosition(), 1.0F, MathUtils.random(0.95F, 1.05F));
        }

    }

    @Shadow
    public void read(CRBinDeserializer crbs) {
        super.read(crbs);
    }

    @Shadow
    public void write(CRBinSerializer crbs) {
        super.write(crbs);
    }

    @Shadow
    public static void register() {
        BlockEntityCreator.registerBlockEntityCreator("base:laser_emitter", (blockState, zone, x, y, z) -> {
            Block block = blockState.getBlock();
            int numSlots = getBlockEntityParamInt(block, "numSlots", 1);
            SlotContainer slotContainer = new SlotContainer(numSlots);
            return new finalforeach.cosmicreach.blockentities.BlockEntityLaserEmitter(zone, x, y, z, slotContainer);
        });
    }

    @Shadow
    public String getBlockEntityId() {
        return "base:laser_emitter";
    }
}
