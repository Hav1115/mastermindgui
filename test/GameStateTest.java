/* ECE422C Mastermind Multiplayer Lab
 * Unit Tests
 * 
 * This file contains unit tests for the GameState class.
 * Writing good tests is a crucial software engineering skill!
 * 
 * LEARNING OBJECTIVES:
 * - Writing comprehensive unit tests
 * - Testing edge cases
 * - Test-driven development principles
 * 
 * ESTIMATED TIME: 2-3 hours
 * 
 * To run tests: ./run-tests.sh
 */

public class GameStateTest {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   GameState Unit Tests");
        System.out.println("========================================\n");

        runAllTests();

        System.out.println("\n========================================");
        System.out.println("   Test Results");
        System.out.println("========================================");
        System.out.println("Tests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + testsFailed);
        System.out.println("Total Tests:  " + (testsPassed + testsFailed));
        
        if (testsFailed == 0) {
            System.out.println("\n✓ All tests passed!");
        } else {
            System.out.println("\n✗ Some tests failed.");
            System.exit(1);
        }
    }

    private static void runAllTests() {
        testAllCorrect();
        testNoMatches();
        testSomeCorrectPosition();
        testSomeCorrectColor();
        testMixedMatches();
        testDuplicateColorsInGuess();
        testDuplicateColorsInCode();
        testComplexDuplicates();
        testAllSameColor();
        testEdgeCases();
    }

    /**
     * TODO 1: Test Perfect Match (15 minutes)
     * 
     * Test when guess exactly matches the secret code.
     * 
     * Steps:
     * 1. Create a GameState with secretCode "BGRP"
     * 2. Evaluate guess "BGRP"
     * 3. Assert result[0] (black pegs) == 4
     * 4. Assert result[1] (white pegs) == 0
     * 
     * This tests the winning condition!
     */
    private static void testAllCorrect() {
        System.out.println("Test: All positions correct (perfect match)");
        
        // TODO: Create GameState with a known code
        
        // TODO: Evaluate matching guess
        
        // TODO: Assert 4 black pegs, 0 white pegs
        
        // TODO: Call assertResult with your values
        GameState currentState = new GameState("BGRP");
         int[] result = currentState.evaluateGuess("BGRP");
        assertResult("Perfect Match", result, result[0], result[1]);
    }

    /**
     * TODO 2: Test No Matches (15 minutes)
     * 
     * Test when guess has no colors in common with secret code.
     * 
     * Steps:
     * 1. Create GameState with secretCode "BGRP"
     * 2. Evaluate guess "YYOO" (no matching colors)
     * 3. Assert result[0] == 0 (no black pegs)
     * 4. Assert result[1] == 0 (no white pegs)
     */
    private static void testNoMatches() {
        System.out.println("Test: No color matches");
        
        GameState currentState = new GameState("BGRP");
        int[] result = currentState.evaluateGuess("YYOO");
        assertResult("No Matches", result, result[0], result[1]);
        
    }

    /**
     * TODO 3: Test Correct Positions Only (20 minutes)
     * 
     * Test when some colors are in correct positions.
     * 
     * Steps:
     * 1. Create GameState with secretCode "BGRP"
     * 2. Evaluate guess "BYRY" (B and R are correct positions)
     * 3. Assert result[0] == 2 (two black pegs)
     * 4. Assert result[1] == 0 (no white pegs)
     * 
     * This tests exact position matching without any color-only matches.
     */
    private static void testSomeCorrectPosition() {
        System.out.println("Test: Some correct positions, no wrong positions");
        
        // TODO: Test partial correct positions
        GameState currentState = new GameState("BGRP");
        int[] result = currentState.evaluateGuess("BYRY");
        assertResult("Correct Positions Only", result, result[0], result[1]);
    }

    /**
     * TODO 4: Test Correct Colors, Wrong Positions (20 minutes)
     * 
     * Test when colors match but are all in wrong positions.
     * 
     * Steps:
     * 1. Create GameState with secretCode "BGRP"
     * 2. Evaluate guess "PRBG" (all colors present, all wrong positions)
     * 3. Assert result[0] == 0 (no black pegs)
     * 4. Assert result[1] == 4 (four white pegs)
     * 
     * This is a tricky case - all colors right but positions wrong!
     */
    private static void testSomeCorrectColor() {
        System.out.println("Test: Correct colors in wrong positions");
        GameState currentState = new GameState("BGRP");
        int[] result = currentState.evaluateGuess("PRBG");
        assertResult("Test Correct Colors, Wrong Positions", result, result[0], result[1]);
        
    }

    /**
     * TODO 5: Test Mixed Results (25 minutes)
     * 
     * Test when there are both correct positions and correct colors.
     * 
     * Steps:
     * 1. Create GameState with secretCode "BGRP"
     * 2. Evaluate guess "BGYO" where:
     *    - B is correct position (black peg)
     *    - G is correct position (black peg)
     *    - Y doesn't match anything
     *    - O doesn't match anything
     * 3. Assert result[0] == 2 (two black pegs)
     * 4. Assert result[1] == 0 (no white pegs)
     * 
     * Then test another case:
     * 1. Use secretCode "BGRP"
     * 2. Evaluate guess "RBYO" where:
     *    - R is in code but wrong position (white peg)
     *    - B is in code but wrong position (white peg)
     *    - Y doesn't match
     *    - O doesn't match
     * 3. Assert result[0] == 0
     * 4. Assert result[1] == 2
     */
    private static void testMixedMatches() {
        System.out.println("Test: Mixed correct positions and colors");
        
        // TODO: Test mix of black and white pegs
        GameState currentState = new GameState("BGRP");
        int[] result = currentState.evaluateGuess("BGYO");
        assertResult("Test: Mixed correct positions and colors", result, result[0], result[1]);
        result = currentState.evaluateGuess("RBYO");
        assertResult("Test: Mixed correct positions and colors", result, result[0], result[1]);
    }

    /**
     * TODO 6: Test Duplicate Colors in Guess (30 minutes)
     * 
     * Important edge case: What if guess has duplicate colors?
     * 
     * Example:
     * - Secret: "BGRP"
     * - Guess:  "BBBB"
     * - Only the first B should match (1 black peg)
     * - The other B's don't match anything (0 white pegs for them)
     * 
     * Steps:
     * 1. Create GameState with secretCode "BGRP"
     * 2. Evaluate guess "BBBB"
     * 3. Assert result[0] == 1 (only one B matches)
     * 4. Assert result[1] == 0 (other B's don't count)
     * 
     * Test another case:
     * 1. Use secretCode "BGRP"
     * 2. Evaluate guess "GGGG"
     * 3. Should get 1 black, 0 white
     */
    private static void testDuplicateColorsInGuess() {
        System.out.println("Test: Duplicate colors in guess");
        
        // TODO: Test when guess has repeated colors
        GameState currentState = new GameState("BGRP");
        int[] result = currentState.evaluateGuess("BBBB");
        assertResult("Test: Duplicate Colors in Guess", result, result[0], result[1]);
        result = currentState.evaluateGuess("GGGG");
        assertResult("Test: Duplicate Colors in Guess", result, result[0], result[1]);
    }

    /**
     * TODO 7: Test Duplicate Colors in Secret Code (30 minutes)
     * 
     * Another edge case: What if the secret code has duplicates?
     * 
     * Example:
     * - Secret: "BBGR"
     * - Guess:  "BOPY"
     * - First B matches (1 black peg)
     * - Second B in code doesn't match anything in guess
     * - O doesn't match, P doesn't match, Y doesn't match
     * - Result: 1 black, 0 white
     * 
     * Steps:
     * 1. Create GameState with secretCode "BBGR"
     * 2. Evaluate guess "BOPY"
     * 3. Assert result[0] == 1
     * 4. Assert result[1] == 0
     * 
     * Test another case with white pegs:
     * 1. Use secretCode "BBGR"
     * 2. Evaluate guess "OPBY"
     * 3. Should get 0 black (no exact matches), 2 white (B and G in wrong positions)
     */
    private static void testDuplicateColorsInCode() {
        System.out.println("Test: Duplicate colors in secret code");
        
        GameState currentState = new GameState("BBGR");
        int[] result = currentState.evaluateGuess("BOPY");
        assertResult("Test: Duplicate Colors in Secret Code", result, result[0], result[1]);
        result = currentState.evaluateGuess("BBBB");
        assertResult("Test: Duplicate Colors in Secret Code", result, result[0], result[1]);
        
    }

    /**
     * TODO 8: Test Complex Duplicate Scenario (30 minutes)
     * 
     * Most complex case: Duplicates in both guess and code!
     * 
     * Example:
     * - Secret: "BBRR"
     * - Guess:  "RBBR"
     * - Position 0: R vs B - wrong (check for white later)
     * - Position 1: B vs B - correct! (1 black)
     * - Position 2: B vs R - wrong (check for white later)
     * - Position 3: R vs R - correct! (1 black)
     * - Now check colors in wrong positions:
     *   * R at position 0 matches B at position 0 in code? No, that's matched
     *   * R at position 0 matches R at position 2 in code? Yes! (1 white)
     *   * B at position 2 matches B at position 1 in code? No, already matched
     *   * B at position 2 matches B at position 0 in code? Yes! (1 white)
     * - Wait, that's not right! Each code position can only be matched once
     * - Correct answer: 2 black (positions 1 and 3), 2 white (the unmatched B and R)
     * 
     * Steps:
     * 1. Create GameState with "BBRR"
     * 2. Evaluate guess "RBBR"
     * 3. Figure out the correct answer by hand first!
     * 4. Assert the result matches your calculation
     */
    private static void testComplexDuplicates() {
        System.out.println("Test: Complex duplicate scenario");
        
        // TODO: Test the most complex case with duplicates in both
        // Hint: Work out the answer by hand first!
        GameState currentState = new GameState("BBRR");
        int[] result = currentState.evaluateGuess("RBBR");
        assertResult("Test: Complex duplicate scenario", result, result[0], result[1]);
    }

    /**
     * TODO 9: Test All Same Color (15 minutes)
     * 
     * Edge case: What if everything is the same color?
     * 
     * Steps:
     * 1. Create GameState with "BBBB"
     * 2. Evaluate guess "BBBB"
     * 3. Assert result[0] == 4, result[1] == 0 (perfect match)
     * 
     * Also test:
     * 1. Use "BBBB" as secret
     * 2. Evaluate "GGGG" as guess
     * 3. Assert result[0] == 0, result[1] == 0 (no matches)
     */
    private static void testAllSameColor() {
        System.out.println("Test: All same color");
        
        // TODO: Test monochrome cases
        GameState currentState = new GameState("BBBB");
        int[] result = currentState.evaluateGuess("BBBB");
        assertResult("Test: Duplicate Colors in Secret Code", result, result[0], result[1]);
        result = currentState.evaluateGuess("GGGG");
        assertResult("Test: Duplicate Colors in Secret Code", result, result[0], result[1]);
    }

    /**
     * TODO 10: Test Additional Edge Cases (20 minutes)
     * 
     * Think of other edge cases to test:
     * - What if the code is "RGBP" and guess is "PRGB"? (all colors, rotated)
     * - What about "BYRG" vs "RGYB"? (some matches)
     * - Test with all 6 available colors
     * 
     * Add at least 2 more test cases you think are important!
     */
    private static void testEdgeCases() {
        System.out.println("Test: Additional edge cases");
        
        // TODO: Add your own creative test cases!

        //Tests 0 Black and 4 White
        GameState currentState = new GameState("RGBP");
        int[] result = currentState.evaluateGuess("PRGB");
        assertResult("Test: Additional edge cases", result, result[0], result[1]);
        
        //Tests INnverse (0B 4W)
        currentState = new GameState("BBOO");
        result = currentState.evaluateGuess("OOBB");
        assertResult("Test: Additional edge cases", result, result[0], result[1]);

        //Tests Inverse and Black and White again (1B 3W)
        currentState = new GameState("RYGB");
        result = currentState.evaluateGuess("GYBR");
        assertResult("Test: Additional edge cases", result, result[0], result[1]);

        //Tests Duplicates on both sides (2B 2W)
        currentState = new GameState("BBRR");
        result = currentState.evaluateGuess("RBBR");
        assertResult("Test: Additional edge cases", result, result[0], result[1]);

        //Tests Extra Duplicates (1B 2W)
         currentState = new GameState("BBGR");
        result = currentState.evaluateGuess("GBBB");
        assertResult("Test: Additional edge cases", result, result[0], result[1]);
    }

    /**
     * Helper method to assert test results
     */
    private static void assertResult(String testName, int[] result, int expectedBlack, int expectedWhite) {
        if (result[0] == expectedBlack && result[1] == expectedWhite) {
            System.out.println("  ✓ PASS: " + testName);
            System.out.println("    Expected: " + expectedBlack + " black, " + expectedWhite + " white");
            System.out.println("    Got:      " + result[0] + " black, " + result[1] + " white\n");
            testsPassed++;
        } else {
            System.out.println("  ✗ FAIL: " + testName);
            System.out.println("    Expected: " + expectedBlack + " black, " + expectedWhite + " white");
            System.out.println("    Got:      " + result[0] + " black, " + result[1] + " white\n");
            testsFailed++;
        }
    }
}
