package it.unibo.ds.core.codes;

/**
 * An interface modeling a {@link OneTimeCode} manager, which exposes functions
 * to generate, (in)validate and verify one-time-codes.
 * @param <C> the type of the context.
 */
public interface CodeManager<C> {

    /**
     * Generates a new {@link OneTimeCode} for the given election.
     * @param context the context of the transaction
     * @param electionId the election identifier
     * @param userId the user identifier
     * @return a new {@link OneTimeCode} instance for the given user and election.
     */
    OneTimeCode generateFor(C context, String electionId, String userId);

    /**
     * Generates a new {@link OneTimeCode}.
     * @param electionId the election identifier
     * @param userId the user identifier
     * @return a new {@link OneTimeCode} instance for the given user and election.
     */
    default OneTimeCode generateFor(final String electionId, final String userId) {
        return generateFor(null, electionId, userId);
    }

    /**
     * Check if the given code is still valid, i.e. has not been consumed yet for the given election.
     * @param context the context of the transaction
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     * @return true if the given code is still valid, false otherwise.
     */
    boolean isValid(C context, String electionId, String userId, OneTimeCode code);

    /**
     * Check if the given code is still valid, i.e. has not been consumed yet for the given election.
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     * @return true if the given code is still valid, false otherwise.
     */
    default boolean isValid(final String electionId, final String userId, final OneTimeCode code) {
        return isValid(null, electionId, userId, code);
    }

    /**
     * Invalidate the given code for the given election.
     * After calling this method the code can no longer be used.
     * @param context the context of the transaction
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     */
    void invalidate(C context, String electionId, String userId, OneTimeCode code);

    /**
     * Invalidate the given code for the given election.
     * After calling this method the code can no longer be used.
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     */
    default void invalidate(final String electionId, final String userId, final OneTimeCode code) {
        invalidate(null, electionId, userId, code);
    }

    /**
     * Verifies if the given code has been generated for the given user and election.
     * @param context the context of the transaction
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     * @return true if the given code is correct, false otherwise.
     */
    boolean verifyCodeOwner(C context, String electionId, String userId, OneTimeCode code);

    /**
     * Verifies if the given code has been generated for the given user and election.
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     * @return true if the given code is correct, false otherwise.
     */
    default boolean verifyCodeOwner(final String electionId, final String userId, final OneTimeCode code) {
        return verifyCodeOwner(null, electionId, userId, code);
    }
}
