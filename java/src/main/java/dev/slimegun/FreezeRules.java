package dev.slimegun;

import java.util.ArrayList;
import java.util.List;

/** Engine-independent geometry and timing used by the actual cocoon code. */
public final class FreezeRules {
    public static final int FREEZE_TICKS = 160;
    public static final int SHOT_COOLDOWN = 20;
    public static final double RANGE = 28;
    public static final int MAX_BLOCKS = 64;
    public record Cell(int x, int y, int z) {}

    private FreezeRules() {}

    public static List<Cell> cells(double minX, double minY, double minZ,
                                   double maxX, double maxY, double maxZ) {
        // The upper edge is exclusive: a mob exactly two blocks high needs two
        // slime blocks, not a third block in the space immediately above it.
        int x0 = floor(minX), y0 = floor(minY), z0 = floor(minZ);
        int x1 = floor(maxX - 0.00001), y1 = floor(maxY - 0.00001), z1 = floor(maxZ - 0.00001);
        long count = (long) (x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1);
        if (count <= 0 || count > MAX_BLOCKS) return List.of();
        var result = new ArrayList<Cell>((int) count);
        for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++)
            result.add(new Cell(x, y, z));
        return result;
    }
    private static int floor(double value) { return (int) Math.floor(value); }
    public static long refreshedExpiry(long existing, long now) { return Math.max(existing, now + FREEZE_TICKS); }
    public static int remainingDelay(long expiry, long now) { return (int) Math.max(1, Math.min(FREEZE_TICKS, expiry - now)); }
}
