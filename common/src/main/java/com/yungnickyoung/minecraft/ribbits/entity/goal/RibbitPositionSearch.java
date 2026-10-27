package com.yungnickyoung.minecraft.ribbits.entity.goal;

import net.minecraft.core.BlockPos;
import java.util.Optional;
import java.util.function.Predicate;

/** Replacement for the removed BlockPos.findClosestMatch API. */
final class RibbitPositionSearch {
    private RibbitPositionSearch() {}

    static Optional<BlockPos> findClosest(BlockPos center, int horizontalRange, int verticalRange, Predicate<BlockPos> condition) {
        BlockPos closest = null;
        long bestDistance = Long.MAX_VALUE;
        for (BlockPos candidate : BlockPos.betweenClosed(
                center.getX() - horizontalRange, center.getY() - verticalRange, center.getZ() - horizontalRange,
                center.getX() + horizontalRange, center.getY() + verticalRange, center.getZ() + horizontalRange)) {
            long dx = (long)candidate.getX() - center.getX();
            long dy = (long)candidate.getY() - center.getY();
            long dz = (long)candidate.getZ() - center.getZ();
            long dist = dx * dx + dy * dy + dz * dz;
            if (dist < bestDistance && condition.test(candidate)) {
                bestDistance = dist;
                closest = candidate.immutable();
            }
        }
        return Optional.ofNullable(closest);
    }
}
