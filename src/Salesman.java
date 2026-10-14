import java.io.Serializable;

/**
 * Simple data holder that represents a salesman.
 *
 * Instances of this class are immutable: once a Salesman is created,
 * its identification data and name cannot change. The class is
 * {@link Serializable} so that salesmen directories can be saved to and
 * loaded from binary ({@code .ser}) files.
 */
public class Salesman implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String documentType;
    private final long documentNumber;
    private final String firstNames;
    private final String lastNames;

    /**
     * Creates a new salesman.
     *
     * @param documentType   type of identification document (e.g. "CC", "CE", "TI")
     * @param documentNumber identification document number
     * @param firstNames     given name(s) of the salesman
     * @param lastNames      family name(s) of the salesman
     */
    public Salesman(String documentType, long documentNumber, String firstNames, String lastNames) {
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
    }

    public String getDocumentType() {
        return documentType;
    }

    public long getDocumentNumber() {
        return documentNumber;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public String getLastNames() {
        return lastNames;
    }

    /**
     * Convenience method that concatenates first and last names,
     * mainly used for reports and log messages.
     *
     * @return the full name of the salesman
     */
    public String getFullName() {
        return firstNames + " " + lastNames;
    }
}
