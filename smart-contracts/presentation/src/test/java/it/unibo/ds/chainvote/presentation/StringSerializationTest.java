package it.unibo.ds.chainvote.presentation;

import com.owlike.genson.Genson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringSerializationTest {

    private final Genson genson = GensonUtils.create();
    private static final String TO_TEST = "prova";

    private String getExpected() {
        return "{value:prova}";
    }

    @Test
    void testSerialization() {
        final var serialized = genson.serialize(TO_TEST);
        assertEquals(getExpected(), serialized.replace("\"", ""));
    }

    @Test
    void testDeserialization() {
        final var deserialized = genson.deserialize(genson.serialize(TO_TEST), String.class);
        assertEquals(TO_TEST, deserialized);
    }
}
