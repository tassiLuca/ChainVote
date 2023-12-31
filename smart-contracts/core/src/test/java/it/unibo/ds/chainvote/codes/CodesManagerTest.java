package it.unibo.ds.chainvote.codes;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodesManagerTest {

    private static final String ELECTION_ID = "test-election";
    private static final String USER_ID = "mrossi";
    private final CodesManager<Void> localManager = new CodesManagerImpl<>(new CodesRepository<>() {

        private final Map<String, Map<String, OneTimeCode>> codes = new HashMap<>();

        @Override
        public Optional<OneTimeCode> get(final Void context, final String electionId, final String userId) {
            return Optional.ofNullable(codes.getOrDefault(electionId, Map.of()).get(userId));
        }

        @Override
        public void put(final Void context, final String electionId, final String userId, final OneTimeCode code) {
            codes.putIfAbsent(electionId, new HashMap<>());
            codes.get(electionId).put(userId, code);
        }

        @Override
        public void replace(final Void context, final String electionId, final String userId, final OneTimeCode code) {
            codes.get(electionId).replace(userId, code);
        }
    }, new SecureRandomGenerator());

    @Test
    void testGenerate() {
        final var code = assertDoesNotThrow(() -> localManager.generateCodeFor(ELECTION_ID, USER_ID));
        assertNotNull(code);
    }

    @Test
    void testGenerateMultipleTimes() {
        assertDoesNotThrow(() -> localManager.generateCodeFor(ELECTION_ID, USER_ID));
        assertThrows(AlreadyGeneratedCodeException.class, () -> localManager.generateCodeFor(ELECTION_ID, USER_ID));
    }

    @Test
    void testCodeValidity() {
        final OneTimeCode code = assertDoesNotThrow(() -> localManager.generateCodeFor(ELECTION_ID, USER_ID));
        assertTrue(localManager.isValid(ELECTION_ID, USER_ID, code.getCode()));
    }

    @Test
    void testUnknownCodeValidity() {
        assertFalse(localManager.isValid(ELECTION_ID, USER_ID, "0"));
    }

    @Test
    void testCodeInvalidation() {
        final OneTimeCode code = assertDoesNotThrow(() -> localManager.generateCodeFor(ELECTION_ID, USER_ID));
        assertDoesNotThrow(() -> localManager.invalidate(ELECTION_ID, USER_ID, code.getCode()));
        assertFalse(localManager.isValid(ELECTION_ID, USER_ID, code.getCode()));
    }

    @Test
    void testCodeInvalidationMultipleTimes() {
        final OneTimeCode code = assertDoesNotThrow(() -> localManager.generateCodeFor(ELECTION_ID, USER_ID));
        assertDoesNotThrow(() -> localManager.invalidate(ELECTION_ID, USER_ID, code.getCode()));
        assertThrows(InvalidCodeException.class, () -> localManager.invalidate(ELECTION_ID, USER_ID, code.getCode()));
    }

    @Test
    void testAttemptInvalidationOnUnknownCode() {
        assertThrows(IncorrectCodeException.class, () -> localManager.invalidate(ELECTION_ID, USER_ID, "0"));
    }

    @Test
    void testVerifyCodeOwner() throws AlreadyGeneratedCodeException {
        final OneTimeCode code = localManager.generateCodeFor(ELECTION_ID, USER_ID);
        assertTrue(localManager.verifyCodeOwner(ELECTION_ID, USER_ID, code.getCode()));
    }
}
