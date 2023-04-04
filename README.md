EasyMinecraftGoals aims to implement an easy interface to inject preset AI goals into Minecraft Entities using the Spigot API, NMS code with Mojang remapping and a minimal amount of reflections.

Special thanks go to mfnalex for guidance on multi-module project structure.

Minecraft AI is split between legacy and current AI. The main difference between the two lies in the fact that one uses a GoalManager while the other one uses a Brain. Further, not all entities extend PathfindingEntity (Spigot's Creature class). Entity types also have individually hardcoded goal priorities, making AI modifications time-consuming to implement.

The current primary use of this API is to act as an easy interface to add specific goals for EliteMobs, with plans to naturally extend the scope to something useful to all.

Note: the word "AI", "Goal" and "Objective" are used mostly interchangeably in this project, as they usually describe the same thing.

# Supported Minecraft versions
EasyMinecraftGoals supports every Minecraft version between 1.17 R1 and 1.19 R3. It uses Java 17.

# Understanding the file structure
This multi-module project is split into the following parts:

- EasyMinecraftGoals-manager: Used to set up the various modules
- EasyMinecraftGoals-core: This module decides which adapter will be used, depending on the version. It is the module other plugins will interface with.
- EasyMinecraftGoals-dist: This module only exists to define how the projects get compiled.
- EasyMinecraftGoals_v1_XY_RZ: These modules set the implementation of the various behaviors based on the Minecraft version. Since the project uses Mojang remappings, the classes are very similar before compilation, but once reobfuscated after compilation they are not guaranteed to call the same method names.

# Using the API

## Maven/Gradle repository

A maven repository for EasyMinecraftGoals is hosted via Sonatype.

Maven:
```xml
<dependency>
    <groupId>com.magmaguy</groupId>
    <artifactId>EasyMinecraftGoals</artifactId>
    <version>1.0.0</version>
</dependency>
```

Gradle:
```kotlin
repositories {
    maven { url = 'https://s01.oss.sonatype.org/content/repositories/releases/' }
}
dependencies {
    implementation group : 'com.magmaguy', name: 'EasyMinecraftGoals-dist', version: '1.0.0';
}
```

## Registering on startup

Make sure to register EasyMinecraftGoals on enable:

```java
    @Override
    public void onEnable() {
        NMSManager.initializeAdapter(this);
    }
```

## Adding AI

### Access

EasyMinecraftGoals come with preset goals which can be customized. Access is done through

```java
NMSManager.getAdapter();
```

The adapter contains all the methods developers may want to use to assign AI.

### Builder pattern

Because AI can have several parameters, a builder pattern is used to assign AI, and it is necessary to run `#register();` to fully register the AI.

### Example

Let's take a look at a sample implementation from EliteMobs:

```java
public static void addSoftLeashAI(RegionalBossEntity regionalBossEntity) {
        if (regionalBossEntity.getLivingEntity() instanceof Creature)
            NMSManager.getAdapter().returnToPoint(
                            regionalBossEntity.getLivingEntity(),
                            regionalBossEntity.getSpawnLocation(),
                            regionalBossEntity.getCustomBossesConfigFields().getLeashRadius() / 2D,
                            20 * 5)
                    .setSpeed(1.2f)
                    .setStopReturnDistance(1)
                    .setGoalRefreshCooldownTicks(20 * 3)
                    .setHardObjective(false)
                    .setTeleportOnFail(true)
                    .register();
    }

    public static void addHardLeashAI(RegionalBossEntity regionalBossEntity) {
        NMSManager.getAdapter().returnToPoint(
                        regionalBossEntity.getLivingEntity(),
                        regionalBossEntity.getSpawnLocation(),
                        regionalBossEntity.getCustomBossesConfigFields().getLeashRadius(),
                        20 * 5)
                .setSpeed(2f)
                .setStopReturnDistance(0)
                .setGoalRefreshCooldownTicks(20 * 3)
                .setHardObjective(true)
                .setTeleportOnFail(true)
                .register();
    }
```

In this implementation, both a soft and a hard leash are set for a given LivingEntity.

# API features

Currently, the following AI can be assigned to living entities:

## WanderBackToPoint

Makes a Creature go back to a point when it goes beyond a certain distance from it. Mechanically meant to be used like a leash, as observed in MMORPG and ARPG games.

Can be used as a "soft" or "hard" leash. 
- Soft means it is injected into normal roaming behavior, and won't override combat movement, guaranteeing that the living entity will remain in the general vicinity of the point while out of combat.
- Hard means it will override combat behavior, making the entity run back or teleport back to the point no matter what it is doing at the time.

This AI also fires the following events:

- WanderBackToPointStartEvent
- WanderBackToPointEndEvent

Entities will prefer to run back, but can be set to teleport in case no pathfinding solution is found.
The start and end event will fire sequentially on the same tick if the entity teleports.

Note: Only entities extending the Spigot Creature (NMS PathfinderMob) class can be set to navigate back. All other entities will teleport back if teleport is set, or alternatively not do anything.


## More coming soon!