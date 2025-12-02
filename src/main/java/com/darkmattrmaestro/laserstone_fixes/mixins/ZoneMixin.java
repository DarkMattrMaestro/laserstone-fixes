package com.darkmattrmaestro.laserstone_fixes.mixins;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;
import com.darkmattrmaestro.laserstone_fixes.Constants;
import finalforeach.cosmicreach.RandomTicks;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntity;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.commands.Command;
import finalforeach.cosmicreach.gameevents.blockevents.ScheduledBlockTrigger;
import finalforeach.cosmicreach.entities.*;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.networking.packets.CommandPacket;
import finalforeach.cosmicreach.rendering.IRenderable;
import finalforeach.cosmicreach.settings.DifficultySettings;
import finalforeach.cosmicreach.singletons.GameSingletonPlayers;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.util.ArrayUtils;
import finalforeach.cosmicreach.world.*;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Unique private boolean didPause = false; // TODO: Remove

    public void update(float deltaTime) {
        Constants.LOGGER.warn("  ---{}---", this.currentZoneTick);

        this.randomTicks.run();
        this.runScheduledTriggers();

        ArrayUtils.forEach((BlockEntity[])this.tickingBlockEntities.begin(), (be) -> be.onTick());
        this.tickingBlockEntities.end();

        AtomicInteger total = new AtomicInteger();
        ArrayUtils.forEach(this.getAllEntities().toArray(Entity.class), (Entity e) -> {
            if (!"base:laser_projectile".equals(e.entityTypeId)) {
                return;
            }
            total.getAndIncrement();
        });
        double lo = Math.log(total.get()) / Math.log(2);
//        if (lo % 1 != 0 && lo != Double.NEGATIVE_INFINITY && lo != Double.POSITIVE_INFINITY && !didPause) {
//            Command.triggerCommand(Chat.MAIN_CLIENT_CHAT, GameSingletons.client().getAccount(), new String[]{"tick", "freeze"});
//            didPause = true;
//            return;
//        }
        Constants.LOGGER.warn("Lasers: {}", total.get());

        // Iterate over copied array to avoid skipping over any entities.
        ArrayUtils.forEach(this.getAllEntities().toArray(Entity.class), (Entity e) -> {
            e.update((Zone) (Object) this, deltaTime);
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

        });
        this.hostileMobSpawner.tick((Zone) (Object) this);
        this.neutralMobSpawner.tick((Zone) (Object) this);

        ++this.currentZoneTick;
    }

    @Shadow
    public void dispose() {}

    @Shadow
    public void write(Json json) {}

    @Shadow
    public void read(Json json, JsonValue jsonValue) {}
}

