> [!NOTE]
> Updated to Cosmic Reach Alpha v0.5.5

# Laserstone Fixes
Laserstone Fixes aims to fix a few quirks and bugs in the current implementation of laserstone. It does not add new features!

## Features Summary
1. Fixes lasers clipping through blocks and entities

> ### 1. Laser Clipping Fix (second attempt)
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
>     this.updateConstraints(zone, this.targetPosition);
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

## Dependencies:
- Puzzle Loader ~~or Cosmic Quilt~~ (as of Cosmic Reach v0.4.17, this mod only supports Puzzle)
- Cosmic Reach Alpha v0.4.9 (for older versions of this mod) or newer. The last Cosmic Reach version that has been
verified to work with this mod is Alpha v0.5.5.

### Build dependencies
- Java >=17 for Cosmic Reach <v0.4.17 or Java >=24 for Cosmic Reach >=v0.4.17. The version must have a decimal
(ex. 24.0.1), otherwise you will get an IllegalStateException (specifically:
`throw new IllegalStateException("Unable to convert 'java.version' (" + jVersion + ") into a version number!");` from
quiltmc). As an example, version 21.0.0 will fail to parse and throw an error.

## How to Test Client & Server for Puzzle
- For the Client you can use the `./gradlew :runClient` task (add `--warning-mode all` for more useful outputs)
- For the Server  you can use the `./gradlew :runServer` task

