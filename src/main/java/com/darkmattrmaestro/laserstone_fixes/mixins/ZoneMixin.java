package com.darkmattrmaestro.laserstone_fixes.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;
import com.darkmattrmaestro.laserstone_fixes.Constants;
import finalforeach.cosmicreach.RandomTicks;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntity;
import finalforeach.cosmicreach.entities.projectiles.EntityProjectileLaser;
import finalforeach.cosmicreach.gameevents.blockevents.ScheduledBlockTrigger;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.*;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.lighting.LightPropagator;
import finalforeach.cosmicreach.networking.packets.entities.DespawnEntityPacket;
import finalforeach.cosmicreach.networking.packets.entities.SpawnEntityPacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.rendering.IRenderable;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.savelib.blocks.IBlockDataFactory;
import finalforeach.cosmicreach.settings.DifficultySettings;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.util.ArrayUtils;
import finalforeach.cosmicreach.util.Point3DMap;
import finalforeach.cosmicreach.util.logging.Logger;
import finalforeach.cosmicreach.world.*;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Mixin(Zone.class)
public class ZoneMixin implements Json.Serializable, Disposable {
    @Shadow public PriorityQueue<ScheduledBlockTrigger> eventQueue;
    @Shadow public SnapshotArray<BlockEntity> tickingBlockEntities;
    @Shadow public Array<IRenderable> allRenderableBlockEntities;
    @Shadow public int currentZoneTick;
    @Shadow private transient Region[] regionValues;
    @Shadow private ObjectMap<EntityUniqueId, Entity> knownEntities;
    @Shadow private Array<Entity> allEntities;
    @Shadow private Array<Player> players;
    @Shadow public Vector3 spawnPoint;
    @Shadow public String zoneId;
    @Shadow public ZoneGenerator zoneGenerator;
    @Shadow public float respawnHeight;
    @Shadow private String skyId;
    @Shadow private transient World world;
    @Shadow private transient MobSpawner hostileMobSpawner;
    @Shadow private transient MobSpawner neutralMobSpawner;
    @Shadow private RandomTicks randomTicks;

    @Shadow
    public void runScheduledTriggers() {}

    @Shadow
    public void despawnEntity(Entity entity) {}

    @Shadow
    public Array<Entity> getAllEntities() { return null; }

    public void update(float deltaTime) {
        Constants.LOGGER.info("-tick {}-", this.currentZoneTick);
        this.randomTicks.run();
        this.runScheduledTriggers();

        Constants.LOGGER.warn("Update Tick ran ({}) {}", this.currentZoneTick, Arrays.toString(this.tickingBlockEntities.items));

        ArrayUtils.forEach((BlockEntity[])this.tickingBlockEntities.begin(), (be) -> be.onTick());
        this.tickingBlockEntities.end();

        ArrayUtils.forEach(this.getAllEntities(), (e) -> {
            if (e.getClass() != EntityProjectileLaser.class) { return; }
            Constants.LOGGER.warn("                                    ---------- {} {}", e, e.position);
        });

//        ArrayUtils.forEach(this.getAllEntities(), (e) -> {
        Array<Entity> foundEntities = new Array<Entity>(this.getAllEntities());
        int n = 0;
        for (int j=0; j<foundEntities.size; j++) {
            Array<Entity> updatedFoundEntities = new Array<Entity>(this.getAllEntities());
            if (!foundEntities.equals(updatedFoundEntities)) {
                foundEntities = updatedFoundEntities;
                j--;
            }
            if (foundEntities.get(j).getClass() == EntityProjectileLaser.class) {
                Constants.LOGGER.info("    size: {}", foundEntities.size);
                for (int k=0; k<getAllEntities().size; k++) {
                    if (getAllEntities().get(k).getClass() == EntityProjectileLaser.class) {
                        Constants.LOGGER.info("                                            {}", getAllEntities().get(k));
                    }
                }
                Constants.LOGGER.info("");
            }
            Entity e = getAllEntities().get(j);
            n++;

            if (e.getClass() == EntityProjectileLaser.class) { Constants.LOGGER.warn("                                    ???------- {} {}   after {}", e, e.position, n); n = 0; }
            e.update((Zone) (Object) this, deltaTime);
            if (e.getClass() == EntityProjectileLaser.class) { Constants.LOGGER.warn("                                    ???Yes"); }
            if (e.isMob() && !e.hasTag(CommonEntityTags.NO_DESPAWN)) {
                boolean canDespawn = true;
                float closestDistance = Float.MAX_VALUE;
                boolean isPeaceful = DifficultySettings.IsPeaceful();

                for(int i = 0; i < this.players.size; ++i) {
                    Player p = (Player)this.players.get(i);
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
                    if (closestDistance > 128.0F || isPeaceful) {
                        this.despawnEntity(e);
                        return;
                    }

                    if (e.age > 30.0F && closestDistance > 32.0F && MathUtils.randomBoolean(0.003125F)) {
                        this.despawnEntity(e);
                        return;
                    }
                }
            }

        }//);
        this.hostileMobSpawner.tick((Zone) (Object) this);
        this.neutralMobSpawner.tick((Zone) (Object) this);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}

        ++this.currentZoneTick;
    }

    @Shadow
    public void dispose() {}

    @Shadow
    public void write(Json json) {}

    @Shadow
    public void read(Json json, JsonValue jsonValue) {}
}

