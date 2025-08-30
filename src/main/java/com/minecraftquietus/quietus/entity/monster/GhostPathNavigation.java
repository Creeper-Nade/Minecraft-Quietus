package com.minecraftquietus.quietus.entity.monster;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.*;

public class GhostPathNavigation extends FlyingPathNavigation {
    public GhostPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        // Return a dummy pathfinder that doesn't pathfind
        return new PathFinder(new NodeEvaluator() {
            @Override
            public Node getStart() { return null; }

            @Override
            public Target getTarget(double v, double v1, double v2) {
                return null;
            }
            @Override
            public int getNeighbors(Node[] neighbors, Node currentNode) { return 0; }

            @Override
            public PathType getPathTypeOfMob(PathfindingContext pathfindingContext, int i, int i1, int i2, Mob mob) {
                return null;
            }

            @Override
            public PathType getPathType(PathfindingContext pathfindingContext, int i, int i1, int i2) {
                return null;
            }
        }, maxVisitedNodes);
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speed) {
        mob.getMoveControl().setWantedPosition(x, y, z, speed);
        return true;
    }

    @Override
    public void tick() {
        // No pathfinding or collision checks
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }
}
