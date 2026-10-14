import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Entry point (first of the two required "main" classes) for the module
 * project "Generacion y clasificacion de datos".
 *
 * Running this class generates the pseudo-random flat files that the
 * second program (class {@code main}) will later read and process:
 *
 * <ul>
 *   <li>{@code products.txt}       - list of available products.</li>
 *   <li>{@code salesmen.txt}       - list of registered salesmen.</li>
 *   <li>{@code sales_<id>.txt}     - one sales file per salesman.</li>
 * </ul>
 *
 * Design note: the assignment only specifies the required method
 * signatures for {@code createSalesMenFile}, {@code createProductsFile}
 * and {@code createSalesManInfoFile}. Since {@code createSalesMenFile}
 * does not receive a document type or a product catalog as parameters,
 * two design decisions were made and documented here for transparency:
 * <ol>
 *   <li>The document type used for every generated sales file is a
 *       fixed default ("CC"), because the method signature has no
 *       parameter for it.</li>
 *   <li>The random product ids written to each sales file are chosen
 *       in the range {@code [1, DEFAULT_PRODUCTS_COUNT]}, which matches
 *       the ids produced by {@code createProductsFile} when it is run
 *       with the same default count, so that references stay valid.</li>
 * </ol>
 *
 * None of the generation methods request information from the user,
 * as required by the assignment.
 */
public class GenerateInfoFiles {

    // ---------------------------------------------------------------
    // Configuration constants (no user input is requested, so the
    // amount of generated data is controlled from here).
    // ---------------------------------------------------------------

    /** Default number of salesmen to generate. */
    private static final int DEFAULT_SALESMEN_COUNT = 8;

    /** Default number of products to generate. */
    private static final int DEFAULT_PRODUCTS_COUNT = 12;

    /** Minimum number of sale lines generated per salesman file. */
    private static final int MIN_SALES_PER_FILE = 5;

    /** Maximum number of sale lines generated per salesman file. */
    private static final int MAX_SALES_PER_FILE = 15;

    /** Maximum number of sales files generated for a single salesman. */
    private static final int MAX_FILES_PER_SALESMAN = 3;

    /** When true, also creates a faulty sales file to demonstrate data validation. */
    private static final boolean GENERATE_INVALID_SAMPLE_FILE = true;

    /** Minimum quantity sold that can appear in a sale line. */
    private static final int MIN_QUANTITY_SOLD = 1;

    /** Maximum quantity sold that can appear in a sale line. */
    private static final int MAX_QUANTITY_SOLD = 20;

    /** Minimum price per unit used when generating a product. */
    private static final double MIN_PRICE = 5.0;

    /** Maximum price per unit used when generating a product. */
    private static final double MAX_PRICE = 2500.0;

    /** Default document type assigned to generated salesmen sales files. */
    private static final String DEFAULT_DOCUMENT_TYPE = "CC";

    /** Pool of realistic first names used to build coherent salesmen. */
    private static final String[] FIRST_NAMES = {
            "Carlos", "Maria", "Juan", "Laura", "Andres", "Camila", "Diego",
            "Valentina", "Santiago", "Sofia", "Felipe", "Daniela", "Julian",
            "Mariana", "Sebastian", "Paula", "Ricardo", "Natalia"
    };

    /** Pool of realistic last names used to build coherent salesmen. */
    private static final String[] LAST_NAMES = {
            "Gomez", "Rodriguez", "Martinez", "Lopez", "Garcia", "Perez",
            "Sanchez", "Ramirez", "Torres", "Diaz", "Vargas", "Castro",
            "Ortiz", "Rojas", "Moreno", "Suarez", "Jimenez", "Herrera"
    };

    /** Possible identification document types. */
    private static final String[] DOCUMENT_TYPES = {"CC", "CE", "TI"};

    /** Pool of realistic product base names used to build the catalog. */
    private static final String[] PRODUCT_NAMES = {
            "Laptop", "Wireless Mouse", "Mechanical Keyboard", "Monitor",
            "Headphones", "Webcam", "Printer", "Wi-Fi Router", "Tablet",
            "Smartphone", "USB-C Charger", "External Hard Drive",
            "Bluetooth Speaker", "Microphone", "Graphics Card"
    };

    /** Single Random instance reused by all generation methods. */
    private static final Random RANDOM = new Random();

    /**
     * Generates a pseudo-random sales file for a single salesman.
     *
     * The file follows the format required by the assignment: the first
     * line contains the salesman's document type and number, and every
     * following line contains a product id and the quantity sold of
     * that product, separated by semicolons.
     *
     * @param randomSalesCount number of sale lines to generate
     * @param name             full name of the salesman (used only for
     *                         the console log message, since the sales
     *                         file format defined by the assignment does
     *                         not include a name field)
     * @param id               document number of the salesman, also used
     *                         to build the output file name
     * @throws IOException if the file cannot be created or written
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
        createSalesMenFile(randomSalesCount, name, id, 1);
    }

    /**
     * Same as {@link #createSalesMenFile(int, String, long)}, but allows
     * generating several sales files for the same salesman. Part 1 is
     * written to {@code sales_<id>.txt}; any other part is written to
     * {@code sales_<id>_<part>.txt}. Every file repeats the salesman's
     * document type and number in its first line, which is how
     * the second program groups them.
     *
     * @param randomSalesCount number of sale lines to generate
     * @param name             full name of the salesman (log message only)
     * @param id               document number of the salesman
     * @param part             file number for this salesman (1, 2, 3...)
     * @throws IOException if the file cannot be created or written
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id, int part)
            throws IOException {
        String fileName = part <= 1 ? "sales_" + id + ".txt" : "sales_" + id + "_" + part + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // First line: document type and number of the salesman.
            writer.write(DEFAULT_DOCUMENT_TYPE + ";" + id);
            writer.newLine();

            // Following lines: one sale per line (productId;quantitySold;).
            for (int i = 0; i < randomSalesCount; i++) {
                int productId = 1 + RANDOM.nextInt(DEFAULT_PRODUCTS_COUNT);
                int quantitySold = MIN_QUANTITY_SOLD
                        + RANDOM.nextInt(MAX_QUANTITY_SOLD - MIN_QUANTITY_SOLD + 1);

                writer.write(productId + ";" + quantitySold + ";");
                writer.newLine();
            }
        }

        System.out.println("Sales file created for " + name + " (id " + id + "): " + fileName);
    }

    /**
     * Creates a deliberately faulty sales file ({@code sales_invalid_sample.txt})
     * so the validation of the second program can be demonstrated: it mixes
     * one valid line with a negative quantity, an unknown product id and a
     * malformed line. It is attributed to an existing salesman, so only the
     * valid line is counted in the reports.
     *
     * @param id document number of an existing salesman
     * @throws IOException if the file cannot be created or written
     */
    public static void createInvalidSalesSampleFile(long id) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sales_invalid_sample.txt"))) {
            writer.write(DEFAULT_DOCUMENT_TYPE + ";" + id);
            writer.newLine();
            writer.write("1;3;");          // valid line
            writer.newLine();
            writer.write("2;-5;");         // negative quantity
            writer.newLine();
            writer.write("9999;4;");       // product id that does not exist
            writer.newLine();
            writer.write("not-a-sale");    // malformed line
            writer.newLine();
        }
        System.out.println("Invalid sample file created: sales_invalid_sample.txt");
    }

    /**
     * Deletes the files produced by a previous run (sales, serialized and
     * report files), so stale data from old salesmen never mixes with the
     * newly generated data.
     */
    private static void deletePreviousGeneratedFiles() {
        File[] oldFiles = new File(".").listFiles((dir, fileName) ->
                (fileName.startsWith("sales_") && fileName.endsWith(".txt"))
                || fileName.endsWith(".ser")
                || fileName.equals("salesmen_report.csv")
                || fileName.equals("products_report.csv"));
        if (oldFiles != null) {
            for (File oldFile : oldFiles) {
                oldFile.delete();
            }
        }
    }

    /**
     * Generates a pseudo-random product catalog and writes it to
     * {@code products.txt}, one product per line, in the format
     * {@code IDProducto;NombreProducto;PrecioPorUnidad}.
     *
     * @param productsCount number of products to generate
     * @return the list of generated products, so callers (such as
     *         {@link #main(String[])}) can reuse the data without
     *         re-reading the file
     * @throws IOException if the file cannot be created or written
     */
    public static List<Product> createProductsFile(int productsCount) throws IOException {
        List<Product> products = new ArrayList<>();

        for (int productId = 1; productId <= productsCount; productId++) {
            String baseName = PRODUCT_NAMES[RANDOM.nextInt(PRODUCT_NAMES.length)];
            String modelSuffix = "M" + (100 + RANDOM.nextInt(900)); // e.g. "M427"
            String productName = baseName + " " + modelSuffix;

            double price = MIN_PRICE + RANDOM.nextDouble() * (MAX_PRICE - MIN_PRICE);
            price = Math.round(price * 100.0) / 100.0; // round to 2 decimals

            products.add(new Product(productId, productName, price));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("products.txt"))) {
            for (Product product : products) {
                writer.write(product.getId() + ";" + product.getName() + ";"
                        + String.format(Locale.US, "%.2f", product.getPricePerUnit()));
                writer.newLine();
            }
        }

        return products;
    }

    /**
     * Generates pseudo-random but coherent salesman records (real-looking
     * first and last names, valid document types, unique document
     * numbers) and writes them to {@code salesmen.txt}, one salesman per
     * line, in the format
     * {@code TipoDocumento;NumeroDocumento;NombresVendedor;ApellidosVendedor}.
     *
     * @param salesmanCount number of salesmen to generate
     * @return the list of generated salesmen, so callers (such as
     *         {@link #main(String[])}) can reuse the data without
     *         re-reading the file
     * @throws IOException if the file cannot be created or written
     */
    public static List<Salesman> createSalesManInfoFile(int salesmanCount) throws IOException {
        List<Salesman> salesmen = new ArrayList<>();
        java.util.Set<Long> usedDocumentNumbers = new java.util.HashSet<>();

        for (int i = 0; i < salesmanCount; i++) {
            String documentType = DOCUMENT_TYPES[RANDOM.nextInt(DOCUMENT_TYPES.length)];

            long documentNumber;
            do {
                documentNumber = 1_000_000_000L + RANDOM.nextInt(900_000_000);
            } while (!usedDocumentNumbers.add(documentNumber));

            String firstNames = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
            String lastNames = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];

            salesmen.add(new Salesman(documentType, documentNumber, firstNames, lastNames));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("salesmen.txt"))) {
            for (Salesman salesman : salesmen) {
                writer.write(salesman.getDocumentType() + ";" + salesman.getDocumentNumber() + ";"
                        + salesman.getFirstNames() + ";" + salesman.getLastNames());
                writer.newLine();
            }
        }

        return salesmen;
    }

    /**
     * Orchestrates the generation of all the input files required by the
     * second program of the project: the product catalog, the salesmen
     * directory, and one sales file per salesman. Prints a success or
     * error message at the end, as required by the assignment, and never
     * requests information from the user.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        try {
            deletePreviousGeneratedFiles();
            List<Product> products = createProductsFile(DEFAULT_PRODUCTS_COUNT);
            List<Salesman> salesmen = createSalesManInfoFile(DEFAULT_SALESMEN_COUNT);

            for (Salesman salesman : salesmen) {
                // Each salesman gets between 1 and MAX_FILES_PER_SALESMAN sales files.
                int filesCount = 1 + RANDOM.nextInt(MAX_FILES_PER_SALESMAN);
                for (int part = 1; part <= filesCount; part++) {
                    int salesCount = MIN_SALES_PER_FILE
                            + RANDOM.nextInt(MAX_SALES_PER_FILE - MIN_SALES_PER_FILE + 1);
                    createSalesMenFile(salesCount, salesman.getFullName(), salesman.getDocumentNumber(), part);
                }
            }

            if (GENERATE_INVALID_SAMPLE_FILE) {
                createInvalidSalesSampleFile(salesmen.get(0).getDocumentNumber());
            }

            System.out.println();
            System.out.println("SUCCESS: input files generated correctly.");
            System.out.println("  Products generated : " + products.size());
            System.out.println("  Salesmen generated  : " + salesmen.size());
        } catch (IOException e) {
            System.out.println("ERROR: could not generate the input files. Details: " + e.getMessage());
        }
    }
}
