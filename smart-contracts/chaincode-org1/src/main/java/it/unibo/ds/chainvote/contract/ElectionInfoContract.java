package it.unibo.ds.chainvote.contract;

import com.owlike.genson.Genson;
import it.unibo.ds.chainvote.presentation.GensonUtils;
import it.unibo.ds.core.assets.ElectionInfo;
import it.unibo.ds.core.factory.ElectionFactory;
import it.unibo.ds.core.utils.Choice;
import it.unibo.ds.core.utils.Utils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Contracts managing {@link ElectionInfo}.
 */
@Contract(
        name = "ElectionInfoContract",
        info = @Info(
                title = "Election Info Contract",
                description = "Contract used to manage election info"
        ),
        transactionSerializer = "it.unibo.ds.chaincode.transaction.TransactionSerializer"
)

@Default
public final class ElectionInfoContract implements ContractInterface {

    private final Genson genson = GensonUtils.create();

    private enum ElectionInfoTransferErrors {
        ELECTION_INFO_NOT_FOUND,
        ELECTION_INFO_ALREADY_EXISTS,
        ELECTION_INFO_INVALID_ARGUMENT
    }

    /**
     * Initialize Ledger.
     * @param ctx the {@link Context}.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
    }

    /**
     * Create a {@link ElectionInfo}.
     * @param ctx The {@link Context}.
     * @param goal The goal of the {@link ElectionInfo} to build.
     * @param votersNumber The number of voters that could cast a vote in the {@link ElectionInfo} to build.
     * @param startingDate The {@link LocalDateTime} representing the start of the possibility to cast a vote in the {@link ElectionInfo} to build.
     * @param endingDate The {@link LocalDateTime} representing the end of the possibility to cast a vote in the {@link ElectionInfo} to build.
     * @param choices The {@link List} of {@link Choice} that the {@link ElectionInfo} to build has.
     * @return The {@link String} representing the electionId of the {@link ElectionInfo} built.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String createElectionInfo(final Context ctx, String goal, Long votersNumber, LocalDateTime startingDate, LocalDateTime endingDate, List<Choice> choices) {
        ChaincodeStub stub = ctx.getStub();
        String electionId = Utils.calculateID(goal, startingDate, endingDate, choices);
        if (electionInfoExists(ctx, electionId)) {
            String errorMessage = String.format("Election info %s already exists", electionId);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, ElectionInfoTransferErrors.ELECTION_INFO_ALREADY_EXISTS.toString());
        }

        try {
            ElectionInfo electionInfo = ElectionFactory
                .buildElectionInfo(goal, votersNumber, startingDate, endingDate, choices);
            String sortedJson = genson.serialize(electionInfo);
            stub.putStringState(electionId, sortedJson);
            return electionId;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            throw new ChaincodeException(e.getMessage(), ElectionInfoTransferErrors.ELECTION_INFO_INVALID_ARGUMENT.toString());
        }
    }

    /**
     * Return the {@link ElectionInfo}.
     * @param ctx The {@link Context}.
     * @param electionId The electionId of the {@link ElectionInfo} to retrieve.
     * @return the {@link ElectionInfo}.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public ElectionInfo readElectionInfo(final Context ctx, String electionId) {
        System.out.println("[EIC] readElectionInfo");
        if (electionInfoExists(ctx, electionId)) {
            ChaincodeStub stub = ctx.getStub();
            String electionJSON = stub.getStringState(electionId);
            return genson.deserialize(electionJSON, ElectionInfo.class);
        } else {
            String errorMessage = String.format("Election info %s does not exist", electionId);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, ElectionInfoTransferErrors.ELECTION_INFO_NOT_FOUND.toString());
        }
    }

    /**
     * Delete an {@link ElectionInfo} from the ledger.
     * @param ctx The {@link Context}.
     * @param electionId The electionId of the {@link ElectionInfo} to delete.
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deleteAsset(final Context ctx, String electionId) {
        System.out.println("[EIC] deleteAsset");
        ChaincodeStub stub = ctx.getStub();
        if (!electionInfoExists(ctx, electionId)) {
            String errorMessage = String.format("Election info %s does not exist", electionId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, ElectionInfoTransferErrors.ELECTION_INFO_NOT_FOUND.toString());
        }
        stub.delState(electionId);
    }

    /**
     * Check if an {@link ElectionInfo} exists.
     * @param ctx The {@link Context}.
     * @param electionId The electionId of the {@link ElectionInfo} to check.
     * @return A boolean representing the {@link ElectionInfo} existance.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    private boolean electionInfoExists(final Context ctx, String electionId) {
        System.out.println("[EIC] electionInfoExists");

        ChaincodeStub stub = ctx.getStub();
        String electionInfoSerialized = stub.getStringState(electionId);
        return (electionInfoSerialized != null && !electionInfoSerialized.isEmpty());
    }

    /**
     * Return all the existing {@link ElectionInfo}s.
     * @param ctx The {@link Context}.
     * @return All the {@link ElectionInfo}s retrieved from the ledger as {@link String}.
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAllAssets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<ElectionInfo> queryResults = new ArrayList<ElectionInfo>();

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            ElectionInfo election = genson.deserialize(result.getStringValue(), ElectionInfo.class);
            queryResults.add(election);
        }

        return genson.serialize(queryResults);
    }
}