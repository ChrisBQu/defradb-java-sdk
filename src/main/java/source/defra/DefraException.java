package source.defra;

/**
 * Indicates that a DefraDB operation failed.
 */
public class DefraException extends Exception {
    /**
     * Creates an exception with the error message returned by DefraDB.
     *
     * @param message the DefraDB error message
     */
    public DefraException(String message) {
        super(message);
    }

    /**
     * Creates an exception with an error message and underlying cause.
     *
     * @param message the DefraDB error message
     * @param cause the underlying cause
     */
    public DefraException(String message, Throwable cause) {
        super(message, cause);
    }
}
