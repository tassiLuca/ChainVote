package it.unibo.ds.chainvote;

import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import it.unibo.ds.chainvote.assets.Election;
import it.unibo.ds.chainvote.assets.ElectionInfo;
import it.unibo.ds.chainvote.utils.Choice;
import org.hyperledger.fabric.contract.execution.SerializerInterface;
import org.hyperledger.fabric.contract.metadata.TypeSchema;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * A hyperledger custom serializer for calling smart contract transactions
 * with custom data types.
 */
public abstract class GensonTransactionsSerializer implements SerializerInterface {

    private final Genson genson;
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


    public GensonTransactionsSerializer(final Genson genson) {
        this.genson = genson;
    }

    /**
     * Convert the {@link Object} value in a {@link Response} and serialize it in bytes.
     * @param value the {@link Object} to serialize.
     * @param ts the {@link TypeSchema} of the value.
     * @return bytes representing the {@link Response} of the value serialized.
     */
    @Override
    public byte[] toBuffer(final Object value, final TypeSchema ts) {
        return genson.serialize(new Response<>(value)).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Deserialize the object previously serialized in bytes.
     * @param buffer the byte buffer from the wire.
     * @param ts the TypeSchema representing the type.
     * @return the {@link Object} deserialized.
     */
    @Override
    public Object fromBuffer(final byte[] buffer, final TypeSchema ts) {
        String value = new String(buffer, StandardCharsets.UTF_8);
        String type = ts.get("schema").toString();
        String key;
        if (ts.getRef() == null) {
            key = ts.getType();
        } else {
            key = ts.getRef().split("/")[type.split("/").length - 1];
        }
        switch (key) {
            case "List":
                return genson.deserialize(value, new GenericType<List<Choice>>() { });
            case "Map":
                return genson.deserialize(value, new GenericType<Map<String, Long>>() { });
            case "integer":
                return Long.valueOf(value);
            case "Election":
                return genson.deserialize(value, Election.class);
            case "ElectionInfo":
                return genson.deserialize(value, ElectionInfo.class);
            case "LocalDateTime":
                return LocalDateTime.parse(value, FORMATTER);
            case "Choice":
                return genson.deserialize(value, Choice.class);
        }
        return value;
    }
}
