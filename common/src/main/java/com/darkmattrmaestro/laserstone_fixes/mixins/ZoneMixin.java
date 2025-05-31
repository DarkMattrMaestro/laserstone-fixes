package com.darkmattrmaestro.laserstone_fixes.mixins;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;
import com.darkmattrmaestro.laserstone_fixes.LaserstoneFixes;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.blockentities.BlockEntityCreator;
import finalforeach.cosmicreach.blockentities.BlockEntityLaserEmitter;
import finalforeach.cosmicreach.blockentities.IBlockEntity;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityLaserProjectile;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.containers.SlotContainer;
import finalforeach.cosmicreach.networking.packets.entities.SpawnEntityPacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.savelib.crbin.CRBSerialized;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.EntityChunk;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Zone.class)
public class ZoneMixin implements Json.Serializable, Disposable {
    @Shadow private ObjectMap<EntityUniqueId, Entity> knownEntities;
    @Shadow private Array<Entity> allEntities;

    @Shadow
    public void write(Json json) {}

    @Shadow
    public void read(Json json, JsonValue jsonData) {}

    @Shadow
    public void dispose() {}

    @Shadow
    public void despawnEntity(Entity entity) {}

    @Inject(
            method = "addEntity",
            cancellable = true,
            at = @At("HEAD")
    )
    public void addEntityMixin(Entity entity, CallbackInfo ci) {
        entity.zone = (Zone) (Object) this;
        entity.resetRenderPosition();
        if (this.knownEntities.containsKey(entity.uniqueId)) {
            this.despawnEntity(entity);
        }

        this.knownEntities.put(entity.uniqueId, entity);
        if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
            ServerSingletons.SERVER.broadcast((Zone) (Object) this, new SpawnEntityPacket(entity));
        }

        this.allEntities.add(entity);

        ci.cancel();
    }
}
