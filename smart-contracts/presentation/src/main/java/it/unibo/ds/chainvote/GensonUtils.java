package it.unibo.ds.chainvote;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import it.unibo.ds.chainvote.assets.Ballot;
import it.unibo.ds.chainvote.assets.BallotImpl;
import it.unibo.ds.chainvote.assets.Election;
import it.unibo.ds.chainvote.assets.ElectionImpl;
import it.unibo.ds.chainvote.assets.ElectionInfo;
import it.unibo.ds.chainvote.assets.ElectionInfoImpl;
import it.unibo.ds.chainvote.codes.OneTimeCode;
import it.unibo.ds.chainvote.codes.OneTimeCodeImpl;
import it.unibo.ds.chainvote.converters.*;
import it.unibo.ds.chainvote.utils.Choice;

/**
 * Utility class for Genson (de)serialization stuffs.
 */
public final class GensonUtils {

    private GensonUtils() { }

    /**
     * @return a new {@link Genson} instance, already configured.
     */
    public static GensonBuilder defaultBuilder() {
        return new GensonBuilder()
            .useRuntimeType(false)
            .withConverter(new OneTimeCodeConverter(), OneTimeCodeImpl.class)
            .withConverter(new OneTimeCodeConverter(), OneTimeCode.class)
            .withConverter(new BallotConverter(), Ballot.class)
            .withConverter(new BallotConverter(), BallotImpl.class)
            .withConverter(new ElectionConverter(), Election.class)
            .withConverter(new ElectionConverter(), ElectionImpl.class)
            .withConverter(new ElectionInfoConverter(), ElectionInfo.class)
            .withConverter(new ElectionInfoConverter(), ElectionInfoImpl.class)
            .withConverter(new ChoiceConverter(), Choice.class);
            // .withSerializer(new ElectionToReadConverter(), ElectionFacade.class)
            // .withSerializer(new ElectionToReadConverter(), ElectionFacadeImpl.class)
            // .withSerializer(new ElectionWithResultsToReadConverter(), ElectionCompleteFacade.class)
            // .withSerializer(new ElectionWithResultsToReadConverter(), ElectionCompleteFacadeImpl.class)
            // .create();
    }
}
