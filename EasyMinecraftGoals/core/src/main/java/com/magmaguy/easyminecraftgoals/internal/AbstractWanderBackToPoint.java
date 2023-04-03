package com.magmaguy.easyminecraftgoals.internal;

public interface AbstractWanderBackToPoint {
    /**
     * Sets the speed used to return. Defaults to the current movement speed of the entity
     *
     * @param speed Speed to use
     * @return Instance for builder pattern
     */
    AbstractWanderBackToPoint setSpeed(float speed);

    /**
     * Sets the distance considered to be close enough to stop the wander behavior
     *
     * @param distance Distance, in blocks
     * @return Instance for builder pattern
     */
    AbstractWanderBackToPoint setStopReturnDistance(int distance);

    /**
     * Sets the cooldown of this goal. That is the interval between scans that verify if the behavior should happen.
     * Longer cooldowns are better for performance.
     *
     * @param ticks Cooldown between goal scans, in ticks.
     * @return Instance for builder pattern
     */
    AbstractWanderBackToPoint setGoalRefreshCooldownTicks(int ticks);

    /**
     * Sets if the priority of this objective is above all other goals. If set to true, this will have a higher probability
     * than any other goal. Otherwise, the priority will be on the level of a random wander.
     * If set to hard, once a goal starts it can not be cancelled by anything else.
     *
     * @param hardObjective If the objective should override all other objectives
     * @return Instance for builder pattern
     */
    AbstractWanderBackToPoint setHardObjective(boolean hardObjective);

    /**
     * Sets if the entity will instantly teleport when it fails to find a valid path back to the point defined.
     * Failure is defined by either timing the maximum duration out or pathfinding being unable to find a valid path for
     * the entity.
     *
     * @param teleportOnFail If it should teleport
     * @return Instance for builder pattern
     */
    AbstractWanderBackToPoint setTeleportOnFail(boolean teleportOnFail);

    /**
     * Sets if the cooldown should start when the register runs, preventing it from potentially instantly running.
     *
     * @param startWithCooldown
     * @return Instance for builder pattern
     */
    AbstractWanderBackToPoint setStartWithCooldown(boolean startWithCooldown);

    /**
     * Updates the scan cooldown. Mostly meant for internal use.
     */
    void updateCooldown();

    /**
     * Registers the goal on to the mob. Make sure that the initial settings are set before calling it!
     * This is the end of the builder pattern.
     */
    void register();

    /**
     * Unregisters the goal on the mob, effectively removing the behavior. Does not work for the new mobs that use the
     * brain AI system as of writing this.
     */
    void unregister();
}