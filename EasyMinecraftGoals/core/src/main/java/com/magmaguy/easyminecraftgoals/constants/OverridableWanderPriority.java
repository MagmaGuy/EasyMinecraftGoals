package com.magmaguy.easyminecraftgoals.constants;

/*
 * The following enums store data about the priority level for goals & actions that will just barely not interfere with
 * default combat goals & actions. This is used to passively override the wander behavior and move entities to a specific
 * point while out of combat.
 */
public enum OverridableWanderPriority {
    BEE(1, false),
    BLAZE(5, false),
    CAVE_SPIDER(5, false),
    CREEPER(5, false),
    DROWNED(3, false),
    ELDER_GUARDIAN(5, false),
    ENDERMAN(3, false),
    EVOKER(7, false),
    GHAST(4, false),
    GOAT(2, true),
    GUARDIAN(5, false),
    HOGLIN(9, true),
    HUSK(3, false),
    ILLUSIONER(7, false),
    IRON_GOLEM(2, false),
    RABBIT(5, false),
    LLAMA(4, false),
    MAGMA_CUBE(3, false),
    PHANTOM(3, false),
    PIGLIN(9, true),
    PIGLIN_BRUTE(9, true),
    PILLAGER(4, false),
    POLAR_BEAR(2, false),
    RAVAGER(4, false),
    SHULKER(5, false),
    SILVERFISH(5, false),
    SKELETON(5, false),
    SLIME(3, false),
    SPIDER(5, false),
    STRAY(5, false),
    VEX(5, false),
    VINDICATOR(5, false),
    WITCH(3,false),
    WITHER_SKELETON(5,false),
    WOLF(6, false),
    ZOGLIN(9, true),
    ZOMBIE(3, false),
    ZOMBIFIED_PIGLIN(3, false),
    ENDERMITE(3, false),
    //todo: priorities below this point have not been verified yet because they are not yet used
    ALLAY(1, true ),
    AXOLOTL(1, true),
    CAMEL(1, true),
    CAT(1,false),
    CHICKEN(1,false),
    COD(1, false),
    COW(1,false),
    DONKEY(1,false),
    FOX(1,false),
    FROG(1,true),
    GLOW_SQUID(1,true),
    HORSE(1,false),
    MUSHROOM_COW(1,false),
    MULE(1,false),
    OCELOT(1,false),
    PARROT(1,false),
    PIG(1,false),
    PUFFERFISH(1,false),
    SALMON(1,false),
    SHEEP(1,false),
    SKELETON_HORSE(1,false),
    SNIFFER(1,true),
    SNOW_GOLEM(1,false),
    SQUID(1,false),
    STRIDER(1,false),
    TADPOLE(1,true),
    TROPICAL_FISH(1,false),
    TURTLE(1,false),
    VILLAGER(1,false),
    WANDERING_TRADER(1,false),
    DOLPHIN(1,false),
    PANDA(1, false),
    TRADER_LLAMA(1,false),
    WARDEN(1,true);

    public final int priority;
    public final boolean brain;

    /**
     * Initializer for the enum values
     *
     * @param priority Sets the priority of the action, it's 1 level above the attacks the mob has so it does not interfere with combat
     * @param brain    Sets if the entity relies on a brain for AI (new AI system for Minecraft), or if it uses Goals. Brains use Activities instead of goals.
     */
    OverridableWanderPriority(int priority, boolean brain) {
        this.priority = priority;
        this.brain = brain;
    }
}