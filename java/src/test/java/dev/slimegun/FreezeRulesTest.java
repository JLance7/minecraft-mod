package dev.slimegun;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class FreezeRulesTest {
    @Test void humanoidFitsTwoBlocksWithoutAnExtraTopLayer() {
        var cells = FreezeRules.cells(.2, 1, .2, .8, 3, .8);
        assertEquals(2, cells.size());
        assertTrue(cells.contains(new FreezeRules.Cell(0, 1, 0)));
        assertTrue(cells.contains(new FreezeRules.Cell(0, 2, 0)));
    }
    @Test void negativeCoordinatesUseFloorNotTruncation() {
        assertEquals(java.util.List.of(new FreezeRules.Cell(-1, -2, -1)), FreezeRules.cells(-.8, -2, -.8, -.2, -1, -.2));
    }
    @Test void boundaryStraddlingMobGetsEveryIntersectedCellExactlyOnce() {
        var cells = FreezeRules.cells(-.3, 0, -.3, .3, 1.9, .3);
        assertEquals(8, cells.size()); assertEquals(cells.size(), new HashSet<>(cells).size());
    }
    @Test void enormousMobsCannotCreateUnboundedBlocks() { assertTrue(FreezeRules.cells(-10, 0, -10, 10, 10, 10).isEmpty()); }
    @Test void normalLargeMobFitsTheBound() { assertEquals(27, FreezeRules.cells(0, 0, 0, 3, 3, 3).size()); }
    @Test void refreshedDeadlineNeverShortensAnExistingCocoon() {
        assertEquals(260, FreezeRules.refreshedExpiry(200, 100));
        assertEquals(500, FreezeRules.refreshedExpiry(500, 100));
    }
    @Test void cleanupSchedulingAlwaysHasAPositiveBoundedDelay() {
        assertEquals(1, FreezeRules.remainingDelay(100, 101));
        assertEquals(60, FreezeRules.remainingDelay(160, 100));
        assertEquals(160, FreezeRules.remainingDelay(10000, 0));
    }
}
