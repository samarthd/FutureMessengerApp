package cs371m.hermes.futuremessenger.support;

/**
 * Exception thrown when a scheduled message is missing.
 */
public class EntityMissingException extends RuntimeException {
    public EntityMissingException(String message) {
        super(message);
    }
}
