package com.darkmattrmaestro.laserstone_fixes;

import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.annotations.IntegerRange;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.loader.api.config.v2.QuiltConfig;

import java.util.*;

public class LaserstoneFixesConfig extends ReflectiveConfig {
    public static final LaserstoneFixesConfig INSTANCE = QuiltConfig.create("laserstone_fixes", "laserstone_fixes", LaserstoneFixesConfig.class);

    public static final Map<String, List<String>> ConfigEnums = new HashMap<String, List<String>>() {{
        put("collisionOrderMethod", Arrays.asList("VANILLA (Directional)", "AXIS", "WEIGHTED"));
    }};

    @Comment("The laser collision block ordering method to use. Options: 'VANILLA', 'AXIS', 'WEIGHTED'")
    public final TrackedValue<Integer> collisionOrderMethod = this.value(0);
}
