package it.unibo.ds.chainvote;

import com.owlike.genson.Genson;
import it.unibo.ds.chainvote.utils.Choice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ChoiceSerializationTest {

    private final Genson genson = GensonUtils.defaultBuilder().create();
    private static final String CHOICE_TO_CAST = "123prova";
    private static final Choice CHOICE = new Choice(CHOICE_TO_CAST);

    @Test
    void testDeserialization() {
        final var deserialized = genson.deserialize(genson.serialize(CHOICE), Choice.class);
        assertEquals(CHOICE, deserialized);
    }
}
