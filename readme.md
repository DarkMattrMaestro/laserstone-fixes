# Laserstone Fixes
Laserstone Fixes aims to fix a few quirks and bugs in the current implementation of laserstone.

## Features Summary
1. Fixes lasers clipping through blocks

> ### 1. Laser Clipping Fix
> The current implementation (as of Cosmic Reach Alpha-0.4.9) of the laser entity, when preparing to move, checks for block collisions from the furthest negative point in its potential path to the most positive point. See the vanilla implementation:
> ```Java
> for(int bx = minBx; bx <= maxBx; ++bx) {
>   for(int by = minBy; by <= maxBy; ++by) {
>     for(int bz = minBz; bz <= maxBz; ++bz) {
>       /* Check Collision */
>       // ...
>     }
>   }
> }
> ```
>
> This mod's fix varies the search order depending on the direction the laser is travelling. See the new implementation:
> ```Java
> boolean isPosX = targetPosition.x > this.lastPosition.x;
> boolean isPosY = targetPosition.y > this.lastPosition.y;
> boolean isPosZ = targetPosition.z > this.lastPosition.z;
>
> for(int bx = isPosX ? minBx : maxBx; bx >= minBx && bx <= maxBx; bx += isPosX ? 1 : -1) {
>   for(int by = isPosY ? minBy : maxBy; by >= minBy && by <= maxBy; by += isPosY ? 1 : -1) {
>     for(int bz = isPosZ ? minBz : maxBz; bz >= minBz && bz <= maxBz; bz += isPosZ ? 1 : -1) {
>       /* Check Collision */
>       // ...
>     }
>   }
> }
> ```

## Dependencies:
- Puzzle Loader or Cosmic Quilt
- Cosmic Reach Alpha v0.4.9 or newer

### Build dependencies
- Java >=17. The version must have a decimal (ex. 24.0.1), otherwise you will get an IllegalStateException (specifically: `throw new IllegalStateException("Unable to convert 'java.version' (" + jVersion + ") into a version number!");` from quiltmc). As an example, version 21.0.0 will fail to parse and throw an error.

## How to Test Client & Server for Puzzle
- go to the Puzzle Subproject
- For the Client you can use the `./gradlew :puzzle:runClient` task
- For the Server  you can use the `./gradlew :puzzle:runServer` task

## How to Test Client & Server for Quilt
- go to the Quilt Subproject
- For the Client you can use the `./gradlew :quilt:runClient` task
- For the Server  you can use the `./gradlew :quilt:runServer` task

