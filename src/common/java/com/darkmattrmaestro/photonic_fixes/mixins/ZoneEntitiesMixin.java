package com.darkmattrmaestro.photonic_fixes.mixins;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;
import com.darkmattrmaestro.photonic_fixes.Constants;
import finalforeach.cosmicreach.entities.*;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.networking.packets.entities.DespawnEntityPacket;
import finalforeach.cosmicreach.networking.packets.entities.SpawnEntityPacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.settings.DifficultySettings;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.util.ArrayUtils;
import finalforeach.cosmicreach.util.Threads;
import finalforeach.cosmicreach.world.EntityChunk;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.world.ZoneEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ZoneEntities.class)
public class ZoneEntitiesMixin {
    @Shadow
    private SnapshotArray<GameEntity> allEntities = new SnapshotArray<>(GameEntity.class);
    @Shadow
    private ObjectMap<GameEntityUniqueId, GameEntity> knownEntities = new ObjectMap<>();
    @Shadow
    private Zone zone;

    @Shadow
    public void despawnEntity(GameEntity entity) {
        EntityChunk c = entity.currentChunk;
        if (c != null) {
            c.removeEntity(entity);
            entity.currentChunk = null;
        }

        this.removeEntity(entity);
    }

    @Shadow
    public void removeEntity(GameEntity e) {
        if (e != null) {
            EntityChunk c = e.currentChunk;
            if (c != null && c.getZone() == this.zone) {
                c.removeEntity(e);
                e.currentChunk = null;
            }

            if (GameSingletons.isHost() && ServerSingletons.SERVER != null) {
                ServerSingletons.SERVER.broadcast(this.zone, new DespawnEntityPacket(e));
            }

            this.knownEntities.remove(e.uniqueId);
            this.allEntities.removeValue(e, true);
            IEntityModelInstance d = e.getModelInstance();
            if (d instanceof Disposable) {
                Threads.runOnMainThread(() -> d.dispose());
            }

        }
    }

    @Inject(
            method = "updateEntities",
            cancellable = true,
            at = @At("HEAD")
    )
    public void updateEntities(float deltaTime, CallbackInfo ci) {
        Array<Player> players = this.zone.getPlayers();
        GameEntity[] items = (GameEntity[])this.allEntities.begin();
        Constants.LOGGER.warn("\nTick " + this.zone.getCurrentWorldTick() + ":");

        for(int i = 0, n = this.allEntities.size; i < n; ++i) {
            Constants.LOGGER.warn("\n" + i + " < " + n + " Items:");
            ArrayUtils.forEach(items, (GameEntity item) -> {
                Constants.LOGGER.info(item);
            });

            Constants.LOGGER.warn("\nOriginal Array:");
            ArrayUtils.forEach(this.allEntities.items, (GameEntity item) -> {
                Constants.LOGGER.info(item);
            });

            GameEntity e = items[i];

            Constants.LOGGER.warn("\nCurrent: " + e + "\n");

            if (e != null) {
                e.update(this.zone, deltaTime);
                if (e.isMob() && !e.hasTag(CommonEntityTags.NO_DESPAWN) && GameEntityUtils.getName(e) == null) {
                    boolean canDespawn = true;
                    float closestDistance = Float.MAX_VALUE;
                    boolean isPeaceful = DifficultySettings.IsPeaceful();

                    for(int j = 0; j < players.size; ++j) {
                        Player p = (Player)players.get(j);
                        if (p != null) {
                            Vector3 playerPos = p.getEntity().position;
                            closestDistance = Math.min(closestDistance, e.position.dst(playerPos));
                            if (closestDistance < 32.0F) {
                                canDespawn = false;
                                break;
                            }
                        }
                    }

                    boolean willDespawnFromPeaceful = isPeaceful && MobSpawner.HOSTILE_MOB_SPAWNER.hasMob(e);
                    if (canDespawn || willDespawnFromPeaceful) {
                        if (!(closestDistance > 128.0F) && !isPeaceful) {
                            if (e.age > 30.0F && closestDistance > 32.0F && MathUtils.randomBoolean(0.003125F)) {
                                this.despawnEntity(e);
                            }
                        } else {
                            this.despawnEntity(e);
                        }
                    }
                }
            }
        }

        this.allEntities.end();

        ci.cancel();
    }
}
