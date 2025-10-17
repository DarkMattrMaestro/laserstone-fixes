package com.darkmattrmaestro.laserstone_fixes.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.darkmattrmaestro.laserstone_fixes.Constants;
import finalforeach.cosmicreach.EntityPathfinder;
import finalforeach.cosmicreach.PathfindRequest;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.*;
import finalforeach.cosmicreach.entities.components.*;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.entities.projectiles.EntityProjectileLaser;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.items.containers.SlotContainer;
import finalforeach.cosmicreach.networking.packets.entities.EntityPositionPacket;
import finalforeach.cosmicreach.networking.packets.entities.HitEntityPacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.savelib.crbin.ICRBinSerializable;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.sounds.GameSoundBank;
import finalforeach.cosmicreach.util.ArrayUtils;
import finalforeach.cosmicreach.util.Axis;
import finalforeach.cosmicreach.util.GameTagList;
import finalforeach.cosmicreach.util.IGameTagged;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.EntityChunk;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(Entity.class)
public class EntityMixin implements ICRBinSerializable, IGameTagged, IDamageSource {
    @Shadow public static final transient Vector3 gravity = new Vector3(0.0F, -29.400002F, 0.0F);
    @Shadow protected static final transient Matrix4 tmpModelMatrix = new Matrix4();
    @Shadow protected static final transient Vector3 tmpRenderPos = new Vector3();
    @Shadow private static final BlockPosition tmpBlockPos1 = new BlockPosition((Chunk)null, 0, 0, 0);
    @Shadow private static final BlockPosition tmpBlockPos2 = new BlockPosition((Chunk)null, 0, 0, 0);
    @Shadow private static EntityPositionPacket positionPacket = new EntityPositionPacket();
    @CRBSerialized
    @Shadow public EntityUniqueId uniqueId;
    @CRBSerialized
    @Shadow public String entityTypeId;
    @CRBSerialized
    @Shadow protected float sightRange;
    @CRBSerialized
    @Shadow public float gravityModifier;
    @CRBSerialized
    @Shadow public boolean isOnGround;
    @CRBSerialized
    @Shadow public boolean collidedX;
    @CRBSerialized
    @Shadow public boolean collidedY;
    @CRBSerialized
    @Shadow public boolean collidedZ;
    @CRBSerialized
    @Shadow public float maxStepHeight;
    @CRBSerialized
    @Shadow public float fluidImmersionRatio;
    @CRBSerialized
    @Shadow public float fluidImmersionViscosity;
    @CRBSerialized
    @Shadow public float maxHitpoints;
    @CRBSerialized
    @Shadow public float hitpoints;
    @CRBSerialized
    @Shadow public Vector3 viewDirection;
    @CRBSerialized
    @Shadow public Vector3 position;
    @CRBSerialized
    @Shadow public Vector3 lastPosition;
    @CRBSerialized
    @Shadow public Vector3 viewPositionOffset;
    @CRBSerialized
    @Shadow private Vector3 acceleration;
    @CRBSerialized
    @Shadow public Vector3 velocity;
    @CRBSerialized
    @Shadow public Vector3 onceVelocity;
    @CRBSerialized
    @Shadow public BoundingBox localBoundingBox;
    @CRBSerialized
    @Shadow public float age;
    @CRBSerialized
    @Shadow private GameTagList tags;
    @Shadow private transient Array<IUpdateEntityComponent> updatingComponents;
    @Shadow private transient Array<IRenderEntityComponent> renderingComponents;
    @CRBSerialized
    @Shadow private SavedComponentArray savedComponents;
    @Shadow protected transient float footstepTimer;
    @Shadow public transient IEntityModelInstance modelInstance;
    @Shadow public transient Zone zone;
    @Shadow private transient float pendingDamage;
    @Shadow public transient BoundingBox globalBoundingBox;
    @Shadow protected transient BoundingBox tmpEntityBoundingBox;
    @Shadow private transient BoundingBox tmpEntityBoundingBox2;
    @Shadow protected transient BoundingBox tmpBlockBoundingBox;
    @Shadow private transient BoundingBox tmpBlockBoundingBox2;
    @Shadow protected transient float floorFriction;
    @Shadow public transient EntityChunk currentChunk;
    @Shadow protected transient Array<BoundingBox> tmpBlockBoundingBoxes;
    @Shadow protected transient long lastHitTick;
    @Shadow public transient Color modelLightColor;
    @Shadow protected transient Vector3 lastRenderPosition;
    @Shadow private transient Vector3 posDiff;
    @Shadow private transient Vector3 targetPosition;
    @Shadow protected transient float blockBouncinessY;
    @Shadow private transient EntityPositionPacket positionPacketTracker;
    @Shadow public IFallDamageComponent fallDamage;
    @Shadow protected boolean renderedLastFrame;
    @CRBSerialized
    @Shadow int lastClientUpdateTick;

    @Shadow
    public boolean isDead() {
        return this.hitpoints <= 0.0F;
    }

    @Shadow
    public boolean isInFluid() {
        return this.fluidImmersionRatio > 0.0F;
    }

    @Shadow
    public void onAttackInteraction(Player player, short inventorySlotNum) {}

    @Shadow
    public void onCollideWithBlock(Axis axis, BlockState block, Vector3 targetPosition, int bx, int by, int bz) {
    }

    @Shadow
    protected void onDeath() {}

    @Shadow
    protected void onHitpointChange() {}

    @Inject(
            method = "update",
            cancellable = true,
            at = @At("HEAD")
    )
    public void updateMixin(Zone zone, float deltaTime, CallbackInfo ci) {
        if (this.entityTypeId.equals("base:laser_projectile")) { Constants.LOGGER.warn("                                    ???Yes-Super1"); }
        this.hitpoints -= this.getPendingDamage();
        if (this.getPendingDamage() != 0.0F || this.hitpoints == 0.0F) {
            this.onHitpointChange();
        }

        this.setPendingDamage(0.0F);
        if (!this.isDead()) {
            if (this.viewDirection.isZero()) {
                this.viewDirection.set(0.0F, 0.0F, -1.0F);
            }

            this.tmpEntityBoundingBox.set(this.localBoundingBox);
            this.tmpEntityBoundingBox.min.add(this.position);
            this.tmpEntityBoundingBox.max.add(this.position);
            this.tmpEntityBoundingBox.update();
            int minBx = (int)Math.floor((double)this.tmpEntityBoundingBox.min.x);
            int minBy = (int)Math.floor((double)this.tmpEntityBoundingBox.min.y);
            int minBz = (int)Math.floor((double)this.tmpEntityBoundingBox.min.z);
            int maxBx = (int)Math.floor((double)this.tmpEntityBoundingBox.max.x);
            int maxBy = (int)Math.floor((double)this.tmpEntityBoundingBox.max.y);
            int maxBz = (int)Math.floor((double)this.tmpEntityBoundingBox.max.z);
            this.fluidImmersionRatio = 0.0F;
            float viscosity = 0.0F;
            boolean immersionFound = false;

            for(int bx = minBx; bx <= maxBx; ++bx) {
                for(int by = maxBy; by >= minBy; --by) {
                    for(int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj != null && blockAdj.isFluid) {
                            blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                            if (this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) {
                                float ratio = Math.max(this.fluidImmersionRatio, 1.0F - (this.tmpEntityBoundingBox.max.y - this.tmpBlockBoundingBox.max.y) / this.tmpEntityBoundingBox.getHeight());
                                if (!immersionFound) {
                                    this.fluidImmersionRatio = Math.min(ratio, 1.0F);
                                }

                                immersionFound = true;
                                viscosity = Math.max(viscosity, blockAdj.viscosity);
                            }
                        }
                    }
                }
            }

            this.fluidImmersionViscosity = viscosity;
            if (this.updatingComponents != null) {
                int s = this.updatingComponents.size;

                if (this.entityTypeId.equals("base:laser_projectile")) { Constants.LOGGER.warn("                                    ???Yes-Super2 {}", s); }

                for(int i = 0; i < s; ++i) {
                    IUpdateEntityComponent c = ((IUpdateEntityComponent[])this.updatingComponents.items)[i];
                    if (c != null) {
                        if (this.entityTypeId.equals("base:laser_projectile")) { Constants.LOGGER.warn("                                    ???Yes-Super3 {}", c); }
                        c.update(zone, (Entity) (Object) this, deltaTime);
                    }
                }
            }
            if (this.entityTypeId.equals("base:laser_projectile")) { Constants.LOGGER.warn("                                    ???Yes-Super4 {}", this.position); }
            this.updatePositions(zone, deltaTime);
            if (this.entityTypeId.equals("base:laser_projectile")) { Constants.LOGGER.warn("                                    ???Yes-Super5 {}", this.position); }
            this.age += deltaTime;
        }

        ci.cancel();
    }

    @Shadow
    public void updatePositions(Zone zone, float deltaTime) {}

    @Inject(
            method = "updatePositions",
            cancellable = true,
            at = @At("HEAD")
    )
    public void updatePositionsMixin(Zone zone, float deltaTime, CallbackInfo ci) {
        if (this.entityTypeId.equals("base:laser_projectile")) { Constants.LOGGER.warn("                                    ???Yes-Super-updatePositions-1"); }
        if (this.currentChunk != null) {
            if (this.entityTypeId.equals("base:laser_projectile")) { Constants.LOGGER.warn("                                    ???Yes-Super-updatePositions-2"); }
            this.blockBouncinessY = 0.0F;
            boolean wasOnGround = this.isOnGround;
            this.lastPosition.set(this.position);
            float ax = this.acceleration.x * deltaTime;
            float ay = this.acceleration.y * deltaTime;
            float az = this.acceleration.z * deltaTime;
            this.velocity.add(ax, ay, az);
            float oldVelocityY = this.velocity.y;
            if (this.isNoClip()) {
                this.floorFriction = 1.0F;
                EntityUtils.applyFriction(1.0F, this.velocity);
            } else {
                EntityUtils.applyFriction(this.floorFriction, this.velocity);
            }

            this.velocity.add(this.onceVelocity);
            float vx = this.velocity.x * deltaTime;
            float vy = this.velocity.y * deltaTime;
            float vz = this.velocity.z * deltaTime;
            this.posDiff.set(vx, vy, vz);
            this.targetPosition.set(this.position).add(this.posDiff);
            if (this.isNoClip()) {
                this.position.add(this.posDiff);
                this.velocity.sub(this.onceVelocity);
            } else {
                float d = this.targetPosition.dst(this.position);
                if (d < 1.0F) {
                    this.updateConstraints(zone, this.targetPosition);
                } else {
                    this.posDiff.set(this.targetPosition).sub(this.position).scl(1.0F / d);
                    this.targetPosition.set(this.position);
                    float floor = (float)Math.floor((double)d);

                    for(float l = 0.0F; l < floor; ++l) {
                        this.targetPosition.add(this.posDiff);
                        this.updateConstraints(zone, this.targetPosition);
                    }

                    if (d - floor > 0.0F) {
                        this.posDiff.scl(d - floor);
                        this.targetPosition.add(this.posDiff);
                        this.updateConstraints(zone, this.targetPosition);
                    }
                }

                if (this.isOnGround && !wasOnGround) {
                    float displacement = this.position.y - this.lastPosition.y;
                    double initialSquared = Math.pow((double)oldVelocityY, (double)2.0F);
                    float finalVelocity = (float)Math.sqrt(initialSquared + (double)(2.0F * this.acceleration.y * displacement));
                    if (Float.isNaN(finalVelocity)) {
                        finalVelocity = 0.0F;
                    }

                    float entityBounciness = this.getBounciness();
                    float bounceSign = Math.signum(this.blockBouncinessY);
                    if (bounceSign == 0.0F) {
                        bounceSign = 1.0F;
                    }

                    float bounceFactor = Math.max(Math.abs(this.blockBouncinessY), entityBounciness) * bounceSign;
                    this.velocity.y = finalVelocity * bounceFactor;
                    this.fallDamage.onLand((Entity) (Object) this, (finalVelocity + ay / 2.0F) * (1.0F - bounceFactor));
                } else {
                    this.velocity.sub(this.onceVelocity);
                }
            }

            this.getBoundingBox(this.globalBoundingBox);
            this.acceleration.setZero();
            this.onceVelocity.setZero();
            if (this.isOnGround) {
                if (wasOnGround) {
                    this.velocity.y = 0.0F;
                } else {
//                    this.playFootstepSound();
                }

                if (this.footstepTimer >= 0.9F) {
//                    this.playFootstepSound();
                }

                float dist = Vector2.dst2(this.lastPosition.x, this.lastPosition.z, this.position.x, this.position.z) / deltaTime;
                if (this.position.x - this.lastPosition.x != 0.0F || this.position.z - this.lastPosition.z != 0.0F) {
                    float factor = 1.0F;
                    if ((double)dist > 0.3) {
                        factor = 2.0F;
                    }

                    if ((double)dist < 0.1) {
                        factor = 0.5F;
                    }

                    if ((double)dist < 0.02) {
                        factor = 0.0F;
                    }

                    this.footstepTimer += deltaTime * factor;
                }
            }
        }

        EntityUtils.updateEntityChunk(zone, (Entity) (Object) this);
        this.sendPositionPacket();

        ci.cancel();
    }

    @Shadow
    protected void sendPositionPacket() {
        if (ServerSingletons.SERVER != null) {
            boolean shouldSendPacket = true;
            positionPacket.setEntity((Entity) (Object) this);
            if (shouldSendPacket) {
                ServerSingletons.SERVER.broadcast(this.zone, positionPacket);
                if (this.positionPacketTracker == null) {
                    this.positionPacketTracker = new EntityPositionPacket();
                }

                this.positionPacketTracker.setEntity((Entity) (Object) this);
            }

        }
    }

    @Shadow
    public float getBounciness() {
        return 0.0F;
    }

    @Shadow
    public void getBoundingBox(BoundingBox boundingBox) {
        boundingBox.set(this.localBoundingBox);
        boundingBox.min.add(this.position);
        boundingBox.max.add(this.position);
        boundingBox.update();
    }

    @Shadow
    public void updateConstraints(Zone zone, Vector3 targetPosition) {
        float floorFriction = 0.0F;
        this.tmpEntityBoundingBox.set(this.localBoundingBox);
        this.tmpEntityBoundingBox.min.add(this.position);
        this.tmpEntityBoundingBox.max.add(this.position);
        this.tmpEntityBoundingBox.min.y = this.localBoundingBox.min.y + targetPosition.y;
        this.tmpEntityBoundingBox.max.y = this.localBoundingBox.max.y + targetPosition.y;
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
        boolean isOnGround = false;
        float minPosY = targetPosition.y;
        float maxPosY = targetPosition.y;

        for(int bx = minBx; bx <= maxBx; ++bx) {
            for(int by = minBy; by <= maxBy; ++by) {
                for(int bz = minBz; bz <= maxBz; ++bz) {
                    BlockState blockAdj = zone.getBlockState(bx, by, bz);
                    if (blockAdj != null && !blockAdj.walkThrough) {
                        blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                        if (this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) {
                            blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz);
                            float oldY = this.tmpEntityBoundingBox.min.y;
                            Array.ArrayIterator var18 = this.tmpBlockBoundingBoxes.iterator();

                            while(var18.hasNext()) {
                                BoundingBox bb = (BoundingBox)var18.next();
                                if (bb.intersects(this.tmpEntityBoundingBox)) {
                                    this.velocity.y = 0.0F;
                                    this.onceVelocity.y = 0.0F;
                                    if (oldY <= bb.max.y && oldY >= bb.min.y) {
                                        minPosY = Math.max(minPosY, bb.max.y - this.localBoundingBox.min.y);
                                        maxPosY = Math.max(maxPosY, minPosY);
                                        if (!this.isOnGround) {
                                            this.footstepTimer = 0.45F;
                                        }

                                        isOnGround = true;
                                        floorFriction = Math.max(floorFriction, blockAdj.friction);
                                        this.blockBouncinessY = Math.max(this.blockBouncinessY, blockAdj.bounciness);
                                    } else {
                                        maxPosY = Math.min(maxPosY, bb.min.y - this.localBoundingBox.getHeight() - 0.01F);
                                        this.blockBouncinessY = Math.min(this.blockBouncinessY, -blockAdj.bounciness);
                                    }

                                    this.collidedY = true;
                                    this.onCollideWithBlock(Axis.Y, blockAdj, targetPosition, bx, by, bz);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isOnGround) {
            this.floorFriction = floorFriction;
        } else if (!this.isInFluid() && !this.isNoClip()) {
            this.floorFriction = 0.1F;
        } else {
            this.floorFriction = 1.0F;
        }

        targetPosition.y = MathUtils.clamp(targetPosition.y, minPosY, maxPosY);
        this.isOnGround = isOnGround;
        this.tmpEntityBoundingBox.min.x = this.localBoundingBox.min.x + targetPosition.x;
        this.tmpEntityBoundingBox.max.x = this.localBoundingBox.max.x + targetPosition.x;
        this.tmpEntityBoundingBox.min.y = this.localBoundingBox.min.y + targetPosition.y + 0.01F;
        this.tmpEntityBoundingBox.max.y = this.localBoundingBox.max.y + targetPosition.y;
        this.tmpEntityBoundingBox.update();
        minBx = (int)Math.floor((double)this.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor((double)this.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor((double)this.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor((double)this.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor((double)this.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor((double)this.tmpEntityBoundingBox.max.z);
        boolean constrainBySneaking = this.shouldConstrainBySneak(zone, this.tmpBlockBoundingBox, this.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        if (constrainBySneaking) {
            this.onceVelocity.x = 0.0F;
            this.velocity.x = 0.0F;
            targetPosition.x = this.position.x;
        }

        boolean steppedUpForAll = true;
        float desiredStepUp = targetPosition.y;
        if (!constrainBySneaking) {
            for(int bx = minBx; bx <= maxBx; ++bx) {
                for(int by = minBy; by <= maxBy; ++by) {
                    for(int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj != null && !blockAdj.walkThrough) {
                            blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                            if (this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) {
                                boolean didStepUp = false;
                                Array.ArrayIterator var21 = blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz).iterator();

                                while(var21.hasNext()) {
                                    BoundingBox bb = (BoundingBox)var21.next();
                                    if (bb.intersects(this.tmpEntityBoundingBox)) {
                                        if (!isOnGround || !(bb.max.y - this.tmpEntityBoundingBox.min.y <= this.maxStepHeight) || !(bb.max.y > this.tmpEntityBoundingBox.min.y)) {
                                            didStepUp = false;
                                            steppedUpForAll = false;
                                            break;
                                        }

                                        float currentDesiredStepUp = Math.max(desiredStepUp, bb.max.y - this.localBoundingBox.min.y);
                                        this.tmpEntityBoundingBox2.set(this.tmpEntityBoundingBox);
                                        this.tmpEntityBoundingBox2.min.y = currentDesiredStepUp;
                                        this.tmpEntityBoundingBox2.max.y = currentDesiredStepUp + this.localBoundingBox.getHeight();
                                        this.tmpEntityBoundingBox2.update();
                                        boolean canStepUp = true;

                                        label267:
                                        for(int bax = minBx; bax <= maxBx; ++bax) {
                                            for(int bay = by + 1; bay <= maxBy + 1; ++bay) {
                                                for(int baz = minBz; baz <= maxBz; ++baz) {
                                                    BlockState blockAbove = zone.getBlockState(bax, bay, baz);
                                                    if (blockAbove != null && !blockAbove.walkThrough) {
                                                        blockAbove.getBoundingBox(this.tmpBlockBoundingBox2, bax, bay, baz);
                                                        canStepUp &= !this.tmpBlockBoundingBox2.intersects(this.tmpEntityBoundingBox2);
                                                        if (!canStepUp) {
                                                            break label267;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (canStepUp) {
                                            desiredStepUp = currentDesiredStepUp;
                                            didStepUp = true;
                                        }
                                    }
                                }

                                if (!didStepUp) {
                                    var21 = blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz).iterator();

                                    while(var21.hasNext()) {
                                        BoundingBox bb = (BoundingBox)var21.next();
                                        if (bb.intersects(this.tmpEntityBoundingBox)) {
                                            float centX = this.tmpBlockBoundingBox.getCenterX();
                                            if (centX > targetPosition.x) {
                                                targetPosition.x = bb.min.x - this.tmpEntityBoundingBox.getWidth() / 2.0F - 0.01F;
                                            } else {
                                                targetPosition.x = bb.max.x + this.tmpEntityBoundingBox.getWidth() / 2.0F + 0.01F;
                                            }

                                            this.onCollideWithBlock(Axis.X, blockAdj, targetPosition, bx, by, bz);
                                            this.collidedX = true;
                                            this.onceVelocity.x = 0.0F;
                                            this.velocity.x = 0.0F;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (steppedUpForAll) {
            targetPosition.y = desiredStepUp;
        }

        this.tmpEntityBoundingBox.min.set(this.localBoundingBox.min).add(targetPosition.x, targetPosition.y + 0.01F, targetPosition.z);
        this.tmpEntityBoundingBox.max.set(this.localBoundingBox.max).add(targetPosition);
        this.tmpEntityBoundingBox.update();
        minBx = (int)Math.floor((double)this.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor((double)this.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor((double)this.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor((double)this.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor((double)this.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor((double)this.tmpEntityBoundingBox.max.z);
        constrainBySneaking = this.shouldConstrainBySneak(zone, this.tmpBlockBoundingBox, this.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        steppedUpForAll = true;
        desiredStepUp = targetPosition.y;
        if (constrainBySneaking) {
            this.onceVelocity.z = 0.0F;
            this.velocity.z = 0.0F;
            targetPosition.z = this.position.z;
        } else {
            for(int bx = minBx; bx <= maxBx; ++bx) {
                for(int by = minBy; by <= maxBy; ++by) {
                    for(int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj != null && !blockAdj.walkThrough) {
                            blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                            if (this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) {
                                boolean didStepUp = false;
                                Array.ArrayIterator var57 = blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz).iterator();

                                while(var57.hasNext()) {
                                    BoundingBox bb = (BoundingBox)var57.next();
                                    if (bb.intersects(this.tmpEntityBoundingBox)) {
                                        if (!isOnGround || !(bb.max.y - this.tmpEntityBoundingBox.min.y <= this.maxStepHeight) || !(bb.max.y > this.tmpEntityBoundingBox.min.y)) {
                                            didStepUp = false;
                                            steppedUpForAll = false;
                                            break;
                                        }

                                        float currentDesiredStepUp = Math.max(desiredStepUp, bb.max.y - this.localBoundingBox.min.y);
                                        this.tmpEntityBoundingBox2.set(this.tmpEntityBoundingBox);
                                        this.tmpEntityBoundingBox2.min.y = currentDesiredStepUp;
                                        this.tmpEntityBoundingBox2.max.y = currentDesiredStepUp + this.localBoundingBox.getHeight();
                                        this.tmpEntityBoundingBox2.update();
                                        boolean canStepUp = true;

                                        label200:
                                        for(int bax = minBx; bax <= maxBx; ++bax) {
                                            for(int bay = by + 1; bay <= maxBy + 1; ++bay) {
                                                for(int baz = minBz; baz <= maxBz; ++baz) {
                                                    BlockState blockAbove = zone.getBlockState(bax, bay, baz);
                                                    if (blockAbove != null && !blockAbove.walkThrough) {
                                                        blockAbove.getBoundingBox(this.tmpBlockBoundingBox2, bax, bay, baz);
                                                        canStepUp &= !this.tmpBlockBoundingBox2.intersects(this.tmpEntityBoundingBox2);
                                                        if (!canStepUp) {
                                                            break label200;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (canStepUp) {
                                            desiredStepUp = currentDesiredStepUp;
                                            didStepUp = true;
                                        }
                                    }
                                }

                                if (!didStepUp) {
                                    var57 = blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz).iterator();

                                    while(var57.hasNext()) {
                                        BoundingBox bb = (BoundingBox)var57.next();
                                        if (bb.intersects(this.tmpEntityBoundingBox)) {
                                            float centZ = this.tmpBlockBoundingBox.getCenterZ();
                                            if (centZ > targetPosition.z) {
                                                targetPosition.z = bb.min.z - this.tmpEntityBoundingBox.getDepth() / 2.0F - 0.01F;
                                            } else {
                                                targetPosition.z = bb.max.z + this.tmpEntityBoundingBox.getDepth() / 2.0F + 0.01F;
                                            }

                                            this.onCollideWithBlock(Axis.Z, blockAdj, targetPosition, bx, by, bz);
                                            this.collidedZ = true;
                                            this.onceVelocity.z = 0.0F;
                                            this.velocity.z = 0.0F;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (steppedUpForAll) {
            targetPosition.y = desiredStepUp;
        }

        this.position.set(targetPosition);
    }

    @Shadow
    protected boolean shouldConstrainBySneak(Zone zone, BoundingBox blockBoundingBox, BoundingBox entityBoundingBox, int minBx, int minBy, int minBz, int maxBx, int maxBz) {
        return false;
    }

    @Shadow
    public void read(CRBinDeserializer crBinDeserializer) {}

    @Shadow
    public void write(CRBinSerializer serial) {
        serial.autoWrite(this);
    }

    @Shadow
    public float getPendingDamage() {
        return this.pendingDamage;
    }

    @Shadow
    private void setPendingDamage(float pendingDamage) {
        this.pendingDamage = pendingDamage;
    }

    @Shadow
    public GameTagList getTags() {
        return this.tags;
    }

    @Shadow
    public boolean isNoClip() {
        return this.hasTag(CommonEntityTags.NOCLIP);
    }

    @Shadow
    public void initTagList() {
        this.tags = new GameTagList();
    }
}
