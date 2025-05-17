package com.darkmattrmaestro.laserstone_fixes.utils;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Segment;
import com.darkmattrmaestro.laserstone_fixes.LaserstoneFixes;

public class CustomGameMath {
    public static BoundingBox expandAABB(BoundingBox bb, float radius) {
        return new BoundingBox(
                bb.min.sub(radius),
                bb.max.add(radius)
        );
    }
    /***
     * Ray-AABB intersection test using the slab method. See <a href="https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196">https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n196</a>
     *
     * @param segment
     * @param box
     * @return
     */
    public static boolean segmentAABBTest(Segment segment, BoundingBox box) {
        LaserstoneFixes.LOGGER.info("&&&&&&&&& segmentAABBTest Called!");

        float length = segment.len();
        Vector3 dir = new Vector3(
                (segment.b.x - segment.a.x) / length,
                (segment.b.y - segment.a.y) / length,
                (segment.b.z - segment.a.z) / length
        );
        Vector3 inv_dir = new Vector3(
                1 / dir.x,
                1 / dir.y,
                1 / dir.z
        );

        double tx1 = (box.min.x - segment.a.x)*inv_dir.x;
        double tx2 = (box.max.x - segment.a.x)*inv_dir.x;

        double tmin = Math.min(tx1, tx2);
        double tmax = Math.max(tx1, tx2);

        double ty1 = (box.min.y - segment.a.y)*inv_dir.y;
        double ty2 = (box.max.y - segment.a.y)*inv_dir.y;

        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));

        double tz1 = (box.min.z - segment.a.z)*inv_dir.z;
        double tz2 = (box.max.z - segment.a.z)*inv_dir.z;

        tmin = Math.max(tmin, Math.min(tz1, tz2));
        tmax = Math.min(tmax, Math.max(tz1, tz2));

        return tmax >= Math.max(0.0, tmin) && tmin < segment.len();
    }
}