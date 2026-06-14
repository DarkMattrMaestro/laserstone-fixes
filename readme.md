> [!NOTE]
> Updated to Cosmic Reach Alpha v0.5.21

# Photonic Fixes (renamed from Laserstone Fixes)
Photonic Fixes aims to fix a few quirks and bugs in the current implementation of photonics in CR. It does not add new features!

## Features Summary
1. ~~[Fixes lasers clipping through blocks and entities](#1-Laser-Clipping-Fix)~~
2. [Fixes the pseudo-random laser timing offsets](#2-Random-Laser-Offset-Fix)

> ### 1. Laser Clipping Fix
> > [!NOTE]
> > This fix is temporarily disabled for the current version of the mod (Photonic Fixes 0.1.7) as it will require a
> > substantial rewrite.
> 
> The current implementation of laser entities (as of Cosmic Reach alpha 0.5.5) has two primary quirks that can lead to
> lasers clipping through blocks or through entities.
> 
> **1. Discrete Steps**
> 
> As is common in quite a few games, CR calculates moving entities' collisions in steps (and substeps). For collisions
> with other entities, the laser entity checks if its target position will intersect with any nearby entity every tick.
> For collisions with blocks, the laser entity forms substeps such that it only moves by one unit per substep. There is
> a notable disconnect between the two collision types, wherein the laser can often easily skip over an entity while it
> would not for a block of a similar bounding box.
> 
> Blocks with small hitboxes tend to be skipped over even with the generated substeps. As such, I opted for the slower
> but more exact ray collision approach ([see here for the implementation used](https://gamedev.stackexchange.com/a/18459/197454)).
> It will reduce the need for substeps, and also provide a distance to the collision which can be used in the following
> step.
> 
> **2. Collision-Type Priority**
> 
> The laser entity's update function, which runs once per tick, first calls its parent's update function then checks
> for collisions with entities.
> <details>
> <summary>See code</summary>
> 
> ```java
> public void update(Zone zone, float deltaTime) {
>     boolean wasAlive = !this.isDead();
>     super.update(zone, deltaTime);
>     if (this.age > this.maxAge || this.isDead()) {
>         this.onDeath();
>         if (!wasAlive) {
>             return;
>         }
>     }
> 
>     this.displacementSegment.a.set(this.lastPosition);
>     this.displacementSegment.b.set(this.position);
>     this.forEachEntityInNearbyChunks((e) -> {
>         if (e != this) {
>             if (!this.leftSource && e.uniqueId.equals(this.sourceEntityId)) {
>                 return;
>             }
> 
>             if (e.hasTag(CommonEntityTags.PROJECTILE_IMMUNE)) {
>                 return;
>             }
> 
>             if (GameMath.distanceSegmentBoundingBox(this.displacementSegment, e.globalBoundingBox) < this.radius) {
>                 e.hit(this, this.strength);
>                 this.die(zone);
>             }
>         }
> 
>     });
> }
> ```
> </details>
>
> The parent's (that is, `Entity`) update function does a few checks, then attempts to update the position of the entity
> by checking block collisions for a given number of substeps.
> <details>
> <summary>See code</summary>
>
> ```java
> float d = this.targetPosition.dst(this.position);
> if (d < 1.0F) {
>     this.updateConstraints(zone, this.targetPosition); // <- this line checks block collisions
> } else {
>     this.posDiff.set(this.targetPosition).sub(this.position).scl(1.0F / d);
>     this.targetPosition.set(this.position);
>     float floor = (float)Math.floor((double)d);
> 
>     for(float l = 0.0F; l < floor; ++l) {
>         this.targetPosition.add(this.posDiff);
>         this.updateConstraints(zone, this.targetPosition); // <- this line checks block collisions
>     }
> 
>     if (d - floor > 0.0F) {
>         this.posDiff.scl(d - floor);
>         this.targetPosition.add(this.posDiff);
>         this.updateConstraints(zone, this.targetPosition); // <- this line checks block collisions
>     }
> }
> ```
> </details>
> 
> Since the super call is performed before the entity collision checks, blocks always have priority even when they would
> otherwise be considered "behind" an entity. To fix this likely unintended behaviour, the fix determines the entity or
> block that has a lesser ray collision distance (using the ray collision algorithm mentioned in the previous step). As
> a result, the entity collision check is moved into the `updateConstraints` method.
> 
> See the `updateConstraintsProxyNearest` method in this repository for further details on the exact workings of this
> fix.

> ### 2. Random Laser Offset Fix
> The fix for quantum entanglement (and erratic update orders) in CR0.5.12 was unfixed in a later update (I'm unsure
> which one). CR0.5.12 created a temporary copy of the array, which properly fixed the issue. The current version
> (CR0.5.21), however, uses a `SnapshotArray` which would work as well, except that the loop iterates until it reaches
> the end of the original array. This skips updating the last entities of the snapshot array when any entity is
> despawned/deleted.
> 
> This new bug is less predictable than its former counterpart as it skips the last entities in the list of ticking
> entities (which can easily vary) rather than skipping the entity immediately following the deleted entity.
>
> In the `updateEntities` method of the `ZoneEntities` class, replacing
> ```java
> for(int i = 0; i < Math.min(this.allEntities.size, items.length); ++i) {
> ```
> with
> ```java
> for(int i = 0, n = this.allEntities.size; i < n; ++i) {
> ```
> fixes the quantum entanglement bug again. This code also more closely matches the
> [docs](https://libgdx.com/wiki/utils/collections#snapshotarray-code). Note that `n` is set to `this.allEntities.size`,
> not to `items.length`, as the latter seems to give larger-than-expected values.
> 
> I have not noticed any side effects from this fix. This is so long as the snapshot array and its backing array are not
> mixed up (e.g. the index in one to access an item in the other, as indexing can differ between the two).
> 
> > <details>
> > <summary>Before 0.5.12-alpha</summary>
> > 
> > In the `update` method of the `Zone` class, the array of ticking entities is iterated via a for-each loop. However,
> > elements of the array can be removed while iterating, mostly due to self-deletion upon collision. Elements of the
> > array are shifted left and the iterator, in turn, ends up skipping some entities, and leaving their updating to the
> > next tick.
> > 
> > Here is the original section which causes pseudo-random offsets (offsets are not entirely random):
> > <details>
> > <summary>See code</summary>
> > 
> > ```java
> > ArrayUtils.forEach(this.getAllEntities(), (e) -> {
> >     // ...
> > }
> > ```
> > </details>
> > 
> > The fix creates a copy of the array so that entities in the array can't be removed while iterating. Note that
> > `ArrayUtils.forEach()` already checks for null values in the array, so deleted objects are not a worry.
> > <details>
> > <summary>See code</summary>
> > 
> > ```java
> > ArrayUtils.forEach(this.getAllEntities().toArray(Entity.class), (Entity e) -> {
> >     // ...
> > }
> > ```
> > </details>
> </details>

## Dependencies:
- Puzzle Loader ~~or Cosmic Quilt~~ (as of Cosmic Reach v0.4.17, this mod only supports Puzzle)
- Cosmic Reach Alpha v0.4.9 (for older versions of this mod) or newer. The last Cosmic Reach version that has been
verified to work with this mod is Alpha v0.5.21.

### Build dependencies
- Java >=17 for Cosmic Reach <v0.4.17 or Java >=24 for Cosmic Reach >=v0.4.17. The version must have a decimal
(ex. 24.0.1), otherwise you will get an IllegalStateException (specifically:
`throw new IllegalStateException("Unable to convert 'java.version' (" + jVersion + ") into a version number!");` from
quiltmc). As an example, version 21.0.0 will fail to parse and throw an error.

## How to Test Client & Server for Puzzle
Use `gradle cleanOldJigsawLocal` and `gradle cleanOldJigsawGlobal` to remove outdated Jigsaw directories from the local
and global environments. Then, run `gradle transformJars` to update the game jars.
- For the Client, run `./gradlew runModdedClient --warning-mode all` (keep `--warning-mode all` for more useful outputs)
- For the Server, run `./gradlew runModdedServer --warning-mode all`

