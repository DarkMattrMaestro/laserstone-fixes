package com.darkmattrmaestro.laserstone_fixes;

import com.darkmattrmaestro.laserstone_fixes.configs.LaserstoneFixesSettings;

import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.annotations.Processor;
import org.quiltmc.config.api.annotations.SerializedName;
import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.annotations.IntegerRange;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.loader.api.config.v2.QuiltConfig;

import java.util.*;

//@Processor("processConfig")
public class LaserstoneFixesConfig extends ReflectiveConfig {
    public static final LaserstoneFixesConfig INSTANCE = QuiltConfig.create("laserstone_fixes", "laserstone_fixes", LaserstoneFixesConfig.class);

//    public void processConfig(Config.Builder builder) {
//        System.out.println("Loading config!");
//        // builder.format("json5");
//        builder.callback(config -> System.out.println("Updated!"));
//    }

    @Processor("processCollisionOrderMethod")
    @SerializedName("collision_order_method")
    @Comment("The laser collision block ordering method to use. Options: 'VANILLA' (0), 'AXIS' (1), 'WEIGHTED' (2)")
    public final TrackedValue<Integer> collisionOrderMethod = this.value(1);
    public void processCollisionOrderMethod(TrackedValue.Builder<Integer> builder) {
        builder.callback(value -> LaserstoneFixesSettings.collisionOrderMethod = collisionOrderMethod.value());
    }
}
