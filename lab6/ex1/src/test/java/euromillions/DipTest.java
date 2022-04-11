/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package euromillions;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.*;

/**
 * @author ico0
 */
public class DipTest {

    private Dip instance;


    public DipTest() {
    }

    @BeforeEach
    public void setUp() {
        instance = new Dip(new int[]{10, 20, 30, 40, 50}, new int[]{1, 2});
    }

    @AfterEach
    public void tearDown() {
        instance = null;
    }


    @Test
    void testConstructorFromBadArrays() {
        assertThrows(IllegalArgumentException.class, () -> new Dip(new int[]{1,3,5,4,4,4}, new int[]{9,6}));
        assertThrows(IllegalArgumentException.class, () -> new Dip(new int[]{1,3,5}, new int[]{9,6}));
        assertThrows(IllegalArgumentException.class, () -> new Dip(new int[]{6,3,2,1,0}, new int[]{7}));
        assertThrows(IllegalArgumentException.class, () -> new Dip(new int[]{6,3,2,1,0}, new int[]{7,9,8}));
        assertThrows(IllegalArgumentException.class, () -> new Dip(new int[]{}, new int[]{}));
    }

    @Test
    void testFormat() {
        String result = instance.format();
        assertEquals("N[ 10 20 30 40 50] S[  1  2]", result, "format as string: formatted string not as expected. ");
    }

    @Test
    void testStarRange() {
        /**
         * Using the constants
         */
        assertDoesNotThrow(() -> new Dip(new int[]{1, 2, 3, 4, 5}, new int[]{1, 2}));
        assertDoesNotThrow(() -> new Dip(new int[]{1, 2, 3, 4, 5}, new int[]{3, 4}));
        assertDoesNotThrow(() -> new Dip(new int[]{1, 2, 3, 4, 5}, new int[]{5, 6}));
        assertDoesNotThrow(() -> new Dip(new int[]{1, 2, 3, 4, 5}, new int[]{7, 8}));
        assertDoesNotThrow(() -> new Dip(new int[]{1, 2, 3, 4, 5}, new int[]{9, 10}));
        assertDoesNotThrow(() -> new Dip(new int[]{1, 2, 3, 4, 5}, new int[]{11, 12}));
        assertThrows(IllegalArgumentException.class, () -> new Dip(new int[]{1, 2, 3, 4, 5}, new int[]{12, 13}));

        // Using the generateRandomDip method (bad...)
        Set<Integer> possibleStars = new HashSet<>();
        final int maxIterations = 100000;
        int i;
        for (i = 0; i < maxIterations && possibleStars.size() < 12; i++)
            Dip.generateRandomDip().getStarsColl().forEach(possibleStars::add);

        // Check that the loop has not ended because of too many iterations
        assertNotEquals(i, maxIterations);
        assertEquals(possibleStars.size(), 12);

        boolean validRange = true;
        for (Integer possibleStar : possibleStars)
            if (possibleStar < 1 || possibleStar > 12) {
                validRange = false;
                break;
            }
        
        assertTrue(validRange);
    }

}
