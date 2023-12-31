package it.unibo.ds.chainvote.codes;

/**
 * An interface modeling a {@link OneTimeCode} manager, which exposes functions
 * to generate, (in)validate and verify one-time-codes.
 * @param <C> the type of the context.
 */
public interface CodesManager<C> {

    /**
     * Generates a new {@link OneTimeCode} for the given election.
     * @param context the context of the transaction
     * @param electionId the election identifier
     * @param userId the user identifier
     * @return a new {@link OneTimeCode} instance for the given user and election.
     * @throws AlreadyGeneratedCodeException if a code for the given election and user has already been generated.
     */
    OneTimeCode generateCodeFor(C context, String electionId, String userId) throws AlreadyGeneratedCodeException;

    /**
     * Generates a new {@link OneTimeCode} for the given election.
     * @param context the context of the transaction
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param arg a string argument to be used in the generation phase
     * @return a new {@link OneTimeCode} instance for the given user and election.
     * @throws AlreadyGeneratedCodeException if a code for the given election and user has already been generated.
     */
    OneTimeCode generateCodeFor(C context, String electionId, String userId, String arg)
        throws AlreadyGeneratedCodeException;

    /**
     * Generates a new {@link OneTimeCode}.
     * @param electionId the election identifier
     * @param userId the user identifier
     * @return a new {@link OneTimeCode} instance for the given user and election.
     * @throws AlreadyGeneratedCodeException if a code for the given election and user has already been generated.
     */
    default OneTimeCode generateCodeFor(final String electionId, final String userId) throws AlreadyGeneratedCodeException {
        return generateCodeFor((C) null, electionId, userId);
    }

    /**
     * Generates a new {@link OneTimeCode} for the given election.
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param arg a string argument to be used in the generation phase
     * @return a new {@link OneTimeCode} instance for the given user and election.
     * @throws AlreadyGeneratedCodeException if a code for the given election and user has already been generated.
     */
    default OneTimeCode generateCodeFor(final String electionId, final String userId, String arg) throws AlreadyGeneratedCodeException {
        return generateCodeFor(null, electionId, userId, arg);
    }

    /**
     * Check if the given code is still valid, i.e. has not been consumed yet for the given election.
     * @param context the context of the transaction
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     * @return true if the given code is still valid, false otherwise.
     */
    boolean isValid(C context, String electionId, String userId, String code);

    /**
     * Check if the given code is still valid, i.e. has not been consumed yet for the given election.
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     * @return true if the given code is still valid, false otherwise.
     */
    default boolean isValid(final String electionId, final String userId, final String code) {
        return isValid(null, electionId, userId, code);
    }

    /**
     * Invalidate the given code for the given election.
     * After calling this method the code can no longer be used.
     * @param context the context of the transaction
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     * @throws IncorrectCodeException if the given code is not correct for the given election and user.
     * @throws InvalidCodeException if the given code has already been invalidated.
     */
    void invalidate(C context, String electionId, String userId, String code)
        throws IncorrectCodeException, InvalidCodeException;

    /**
     * Invalidate the given code for the given election.
     * After calling this method the code can no longer be used.
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     * @throws IncorrectCodeException if the given code is not correct for the given election and user.
     * @throws InvalidCodeException if the given code has already been invalidated.
     */
    default void invalidate(
        final String electionId,
        final String userId,
        final String code
    ) throws IncorrectCodeException, InvalidCodeException {
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
    boolean verifyCodeOwner(C context, String electionId, String userId, String code);

    /**
     * Verifies if the given code has been generated for the given user and election.
     * @param electionId the election identifier
     * @param userId the user identifier
     * @param code the code to be validated
     * @return true if the given code is correct, false otherwise.
     */
    default boolean verifyCodeOwner(final String electionId, final String userId, final String code) {
        return verifyCodeOwner(null, electionId, userId, code);
    }
}
