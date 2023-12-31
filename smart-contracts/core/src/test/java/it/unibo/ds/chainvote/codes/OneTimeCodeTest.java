package it.unibo.ds.chainvote.codes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OneTimeCodeTest {

    private static final String GENERATED_CODE = "123";
    private OneTimeCode code;

    @BeforeEach
    void setup() {
        code = new OneTimeCodeImpl(GENERATED_CODE);
        assertFalse(code.consumed());
    }

    @Test
    void testConsume() {
        assertDoesNotThrow(code::consume);
        assertTrue(code.consumed());
    }

    @Test
    void testConsumeMultipleTimes() {
        assertDoesNotThrow(code::consume);
        assertThrows(InvalidCodeException.class, () -> code.consume());
    }

    @Test
    void testEquality() {
        final var code2 = new OneTimeCodeImpl(GENERATED_CODE);
        assertDoesNotThrow(code2::consume);
        assertEquals(code2, code);
    }
}
