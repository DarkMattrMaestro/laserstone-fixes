package com.darkmattrmaestro.laserstone_fixes.mixins;

import com.darkmattrmaestro.laserstone_fixes.configs.LaserstoneFixesSettings;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityLaserProjectile;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.util.Axis;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import finalforeach.cosmicreach.util.GameMath;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Segment;

@Mixin(EntityLaserProjectile.class)
public class EntityLaserProjectileMixin extends Entity {
    @Shadow public float maxAge;
    @Shadow private transient Segment displacementSegment;
    @Shadow private boolean leftSource;
    @Shadow private EntityUniqueId sourceEntityId;
    @Shadow private float strength;
    @Shadow private float radius;
    @Shadow public int sourceBlockX;
    @Shadow public int sourceBlockY;
    @Shadow public int sourceBlockZ;

    public EntityLaserProjectileMixin(String entityTypeId) {
        super(entityTypeId);
    }

////    @Inject(
////            method = "update",
////            cancellable = true,
////            at = @At("HEAD")
//////            at = @At(
//////                    value = "INVOKE",
//////                    target = "Lfinalforeach/cosmicreach/entities/EntityLaserProjectile;onDeath()V", //"Lfinalforeach/cosmicreach/util/GameMath;distanceSegmentBoundingBox(Lcom/badlogic/gdx/math/collision/Segment;com/badlogic/gdx/math/collision/BoundingBox;)F"
//////                    shift = At.Shift.BY,
//////                    by = -8
//////            )
////    )
////    private void updateProxy(Zone zone, float deltaTime, CallbackInfo ci) {
//////        LaserstoneFixes.LOGGER.info("!-!!! distanceSegmentBoundingBoxProxy Called!");
////
////        super.update(zone, deltaTime);
////        if (!(this.age > this.maxAge) && !this.isDead()) {
////            this.displacementSegment.a.set(this.lastPosition);
////            this.displacementSegment.b.set(this.position);
////            this.forEachEntityInNearbyChunks((e) -> {
////                if (e != this) {
////                    LaserstoneFixes.LOGGER.info("");
//////                    LaserstoneFixes.LOGGER.info("%%%% {}", e.entityTypeId);
////                    if (!this.leftSource && e.uniqueId.equals(this.sourceEntityId)) {
//////                        LaserstoneFixes.LOGGER.info("   - Not left source");
////                        return;
////                    }
////
////                    if (e.hasTag(CommonEntityTags.PROJECTILE_IMMUNE)) {
//////                        LaserstoneFixes.LOGGER.info("   - Target immune");
////                        return;
////                    }
////
//////                    if (GameMath.distanceSegmentBoundingBox(this.displacementSegment, e.globalBoundingBox) < this.radius) {
////                    BoundingBox expandedBox = CustomGameMath.expandAABB(e.globalBoundingBox, this.radius);
////                    if (CustomGameMath.segmentAABBTest(this.displacementSegment, expandedBox)) { // TODO: Add this.radius back in
//////                        LaserstoneFixes.LOGGER.info("   - Collision!");
////                        e.hit(this, this.strength);
////                        this.die(zone);
////                    }
////
//////                    LaserstoneFixes.LOGGER.info("   - No collision");
////                }
////
////            });
////        } else {
////            this.onDeath();
////        }
////
//////        LaserstoneFixes.LOGGER.info("DONE!!!");
////
////        ci.cancel();
////    }
//
    @Unique
    private boolean checkBlock(int bx, int by, int bz, Vector3 targetPosition) {
        if (bx != this.sourceBlockX || by != this.sourceBlockY || bz != this.sourceBlockZ) {
            BlockState blockAdj = zone.getBlockState(bx, by, bz);
            if (blockAdj != null && !blockAdj.walkThrough && blockAdj.stopsLasers) {
                blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                if (this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) {
                    blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz);
                    Array.ArrayIterator<BoundingBox> var13 = this.tmpBlockBoundingBoxes.iterator();

                    while(var13.hasNext()) {
                        BoundingBox bb = (BoundingBox)var13.next();
                        if (bb.intersects(this.tmpEntityBoundingBox)) {
                            float dist = GameMath.distanceBoundingBoxPoint(bb, this.lastPosition);
                            float len = this.lastPosition.dst(targetPosition);
                            if (len == 0.0F || dist == 0.0F) {
                                return true;
                            }

                            if (dist > this.radius) {
                                dist -= this.radius;
                            }

                            float ratio = dist / len;
                            float oldTargetX = targetPosition.x;
                            float oldTargetY = targetPosition.y;
                            float oldTargetZ = targetPosition.z;
                            targetPosition.x = this.lastPosition.x + ratio * (targetPosition.x - this.lastPosition.x);
                            targetPosition.y = this.lastPosition.y + ratio * (targetPosition.y - this.lastPosition.y);
                            targetPosition.z = this.lastPosition.z + ratio * (targetPosition.z - this.lastPosition.z);
                            this.onCollideWithBlock((Axis)null, blockAdj, targetPosition, bx, by, bz);
                            if (this.isDead()) {
                                this.tmpEntityBoundingBox.set(this.localBoundingBox);
                                this.tmpEntityBoundingBox.min.add(targetPosition);
                                this.tmpEntityBoundingBox.max.add(targetPosition);
                                this.tmpEntityBoundingBox.update();
                                return true;
                            }

                            targetPosition.set(oldTargetX, oldTargetY, oldTargetZ);
                            break;
                        }
                    }
                }
            }
        }

        return false;
    }

    ////////////////////////////////////////////////////
    ////////////// updateConstraintsProxy //////////////

    @Unique
    private void updateConstraintsProxyAXIS(Zone zone, Vector3 targetPosition) {
        this.tmpEntityBoundingBox.set(this.localBoundingBox);
        this.tmpEntityBoundingBox.min.add(targetPosition);
        this.tmpEntityBoundingBox.max.add(targetPosition);
        this.tmpEntityBoundingBox.update();
        this.collidedX = false;
        this.collidedY = false;
        this.collidedZ = false;
        int minBx = (int)Math.floor((double)this.tmpEntityBoundingBox.min.x);
        int minBy = (int)Math.floor((double)this.tmpEntityBoundingBox.min.y);
        int minBz = (int)Math.floor((double)this.tmpEntityBoundingBox.min.z);
        int maxBx = (int)Math.floor((double)this.tmpEntityBoundingBox.max.x);
        int maxBy = (int)Math.floor((double)this.tmpEntityBoundingBox.max.y);
        int maxBz = (int)Math.floor((double)this.tmpEntityBoundingBox.max.z);

        // Axis-Aligned Directions (is axis positive)
        boolean isPosX = targetPosition.x > this.lastPosition.x;
        boolean isPosY = targetPosition.y > this.lastPosition.y;
        boolean isPosZ = targetPosition.z > this.lastPosition.z;

        label76:
        for(int bx = isPosX ? minBx : maxBx; bx >= minBx && bx <= maxBx; bx += isPosX ? 1 : -1) {
            for(int by = isPosY ? minBy : maxBy; by >= minBy && by <= maxBy; by += isPosY ? 1 : -1) {
                for(int bz = isPosZ ? minBz : maxBz; bz >= minBz && bz <= maxBz; bz += isPosZ ? 1 : -1) {
                    if (checkBlock(bx, by, bz, targetPosition)) {
                        break label76;
                    }
                }
            }
        }

        this.updateRefraction(targetPosition);
        this.position.set(targetPosition);
    }

    /**
     * Iterate through possible block collisions while
     * following the laser's path as closely as possible
     */
    @Unique
    private void updateConstraintsProxyWEIGHTED(Zone zone, Vector3 targetPosition) {
        // TODO: Verify functionality

        this.tmpEntityBoundingBox.set(this.localBoundingBox);
        this.tmpEntityBoundingBox.min.add(targetPosition);
        this.tmpEntityBoundingBox.max.add(targetPosition);
        this.tmpEntityBoundingBox.update();
        this.collidedX = false;
        this.collidedY = false;
        this.collidedZ = false;
        int minBx = (int)Math.floor((double)this.tmpEntityBoundingBox.min.x);
        int minBy = (int)Math.floor((double)this.tmpEntityBoundingBox.min.y);
        int minBz = (int)Math.floor((double)this.tmpEntityBoundingBox.min.z);
        int maxBx = (int)Math.floor((double)this.tmpEntityBoundingBox.max.x);
        int maxBy = (int)Math.floor((double)this.tmpEntityBoundingBox.max.y);
        int maxBz = (int)Math.floor((double)this.tmpEntityBoundingBox.max.z);
 
        int dx = (int)Math.ceil(targetPosition.x - this.lastPosition.x);
        int dy = (int)Math.ceil(targetPosition.y - this.lastPosition.y);
        int dz = (int)Math.ceil(targetPosition.z - this.lastPosition.z);
 
        int steps = dx + dy + dz;
 
        int x = 0; int y = 0; int z = 0;
        for (int i = 0; i < steps; i++) {
            // TODO: precompute inverse deltas
            float ratioX = x / (float)dx;
            float ratioY = y / (float)dy;
            float ratioZ = z / (float)dz;
 
            int difX = 0; int difY = 0; int difZ = 0;
 
            // Prioritize the fastest axis in case of percentage tie
            if (ratioX == ratioY && ratioY == ratioZ) {
                if (dx > dy && dx > dz) { x++; difX = 1; }
                else if (dy > dz) { y++; difY = 1; }
                else { z++; difZ = 1; }
            }
            else if (ratioX == ratioY) {
                if (dx > dy) { x++; difX = 1; }
                else { y++; difY = 1; }
            }
            else if (ratioY == ratioZ) {
                if (dy > dz) { y++; difY = 1; }
                else { z++; difZ = 1; }
            }
            else if (ratioZ == ratioX) { // TODO: Check if alteration broke it
                if (dz > dx) { z++; difZ = 1; }
                else { x++; difX = 1; }
            }
 
            // Prioritize smallest percentage
            else if (ratioX < ratioY && ratioX < ratioZ) { x++; difX = 1; }
            else if (ratioY < ratioZ) { y++; difY = 1; }
            else { z++; difZ = 1; }
 
            if (difX == 1) {
                labelLayerCheckYZ:
                for (int iy = 0; iy <= y; iy++) {
                    for (int iz = 0; iz <= z; iz++) {
                        if (checkBlock(x, iy, iz, targetPosition)) {
                            break labelLayerCheckYZ;
                        }
                    }
                }
            }
 
            if (difY == 1) {
                labelLayerCheckXZ:
                for (int ix = 0; ix <= x; ix++) {
                    for (int iz = 0; iz <= z; iz++) {
                        if (checkBlock(ix, y, iz, targetPosition)) {
                            break labelLayerCheckXZ;
                        }
                    }
                }
            }
 
            if (difZ == 1) {
                labelLayerCheckXY:
                for (int ix = 0; ix <= x; ix++) {
                    for (int iy = 0; iy <= y; iy++) {
                        if (checkBlock(ix, iy, z, targetPosition)) {
                            break labelLayerCheckXY;
                        }
                    }
                }
            }
        }

        this.updateRefraction(targetPosition);
        this.position.set(targetPosition);
    }

    @Inject(
            method = "updateConstraints",
            cancellable = true,
            at = @At("HEAD")
    )
    private void updateConstraintsProxy(Zone zone, Vector3 targetPosition, CallbackInfo ci) {
        if (LaserstoneFixesSettings.collisionOrderMethod == 1) {
            updateConstraintsProxyAXIS(zone, targetPosition);
            ci.cancel();
            return;
        }
        
        else if (LaserstoneFixesSettings.collisionOrderMethod == 2) {
            updateConstraintsProxyWEIGHTED(zone, targetPosition);
            ci.cancel();
            return;
        }

        // If collisionOrderMethod is 0, pass to vanilla implementation
    }

    ////////////// updateConstraintsProxy //////////////
    ////////////////////////////////////////////////////

    @Shadow protected void updateRefraction(Vector3 targetPosition) {}
}