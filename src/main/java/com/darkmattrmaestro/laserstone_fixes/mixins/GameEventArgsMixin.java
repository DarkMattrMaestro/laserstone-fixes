package com.darkmattrmaestro.laserstone_fixes.mixins;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;
import com.darkmattrmaestro.laserstone_fixes.Constants;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gameevents.GameEventArgs;
import finalforeach.cosmicreach.gameevents.GameEventTrigger;
import finalforeach.cosmicreach.gameevents.ScheduledTrigger;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.world.BlockSetter;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(GameEventArgs.class)
public class GameEventArgsMixin<ARGS extends finalforeach.cosmicreach.gameevents.GameEventArgs<ARGS, ST, T>, ST extends ScheduledTrigger<?>, T extends GameEventTrigger> {
    @Shadow
    private NetworkIdentity srcIdentity;
    @Shadow
    public Player srcPlayer;
    @Shadow
    public Zone zone;
    @Shadow
    protected Queue<ST> queuedTriggers;

//    @Shadow
//    public void set(ARGS other) {
//        this.srcIdentity = other.getSrcIdentity();
//        this.srcPlayer = other.srcPlayer;
//        this.zone = other.zone;
//    }

//    @Shadow
//    public NetworkIdentity getSrcIdentity() {
//        return this.srcIdentity;
//    }

//    @Shadow
//    public void setSrcIdentity(NetworkIdentity srcIdentity) {
//        this.srcIdentity = srcIdentity;
//        if (srcIdentity != null) {
//            this.srcPlayer = srcIdentity.getPlayer();
//        }
//
//    }

//    @Shadow
//    public void addQueuedTrigger(ST trigger) {
//        if (this.queuedTriggers == null) {
//            this.queuedTriggers = new Queue();
//        }
//
//        this.queuedTriggers.addLast(trigger);
//    }

    @Inject(method = "runScheduledTriggers", cancellable = true, at = @At("HEAD"))
    public void runScheduledTriggers(CallbackInfo ci) {
        Constants.LOGGER.warn("runScheduledTriggers {}", this.queuedTriggers);
        if (this.queuedTriggers != null) {
            while(!this.queuedTriggers.isEmpty()) {
                ST t = (ST)(this.queuedTriggers.removeFirst());
                t.run();
            }

            this.queuedTriggers = null;
        }

        ci.cancel();
    }

//    @Shadow
//    public void shareQueue(ARGS otherArgs) {
//        if (this.queuedTriggers == null) {
//            this.createNewQueue();
//        }
//
//        otherArgs.queuedTriggers = this.queuedTriggers;
//    }

//    @Shadow
//    public void createNewQueue() {
//        this.queuedTriggers = new Queue();
//    }

    @Inject(method = "run", cancellable = true, at = @At("HEAD"))
    public void run(T[] triggers, CallbackInfo ci) {
        Constants.LOGGER.warn("    *# GameEventArgs.run {}", Arrays.toString(triggers));

        for(int i = 0; i < triggers.length; ++i) {
            triggers[i].act((GameEventArgs<ARGS, ST, T>) (Object) this);
        }

        ci.cancel();
    }
}
