import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Second entry point (class named {@code main}, as required by the
 * assignment) of the module project "Generacion y clasificacion de datos".
 *
 * It reads the flat files produced by {@link GenerateInfoFiles}, processes
 * every sales file found in the project folder and writes two reports:
 * <ol>
 *   <li>{@code salesmen_report.csv}: every salesman followed by the money
 *       collected, sorted from highest to lowest.</li>
 *   <li>{@code products_report.csv}: every product that was sold, with its
 *       price, sorted from the most to the least units sold.</li>
 * </ol>
 *
 * Extra features implemented (all three optional items of the assignment):
 * <ul>
 *   <li><b>Several sales files per salesman:</b> sales are grouped by the
 *       document number found inside each file, not by file name, so any
 *       number of {@code sales_*.txt} files can belong to one salesman.</li>
 *   <li><b>Serialized files:</b> the products and salesmen loaded are saved
 *       as {@code products.ser} / {@code salesmen.ser}; if the text files
 *       are missing, the program loads the serialized ones instead.</li>
 *   <li><b>Detection of wrong data:</b> malformed lines, unknown product ids,
 *       negative prices and non-positive quantities are reported with a
 *       warning and skipped, without stopping the program.</li>
 * </ul>
 *
 * The program never requests information from the user.
 */
public class main {

    private static final String PRODUCTS_FILE = "products.txt";
    private static final String SALESMEN_FILE = "salesmen.txt";
    private static final String PRODUCTS_SERIALIZED_FILE = "products.ser";
    private static final String SALESMEN_SERIALIZED_FILE = "salesmen.ser";
    private static final String SALES_FILE_PREFIX = "sales_";
    private static final String SALES_FILE_EXTENSION = ".txt";

    private static final String SALESMEN_REPORT_FILE = "salesmen_report.csv";
    private static final String PRODUCTS_REPORT_FILE = "products_report.csv";

    /**
     * Counters used to summarize how many files were processed and how many
     * problems were detected while reading the input data.
     */
    private static class ValidationSummary {
        private int processedFiles = 0;
        private int skippedFiles = 0;
        private int invalidLines = 0;

        private void warn(String message) {
            System.out.println("WARNING: " + message);
        }
    }

    /**
     * Runs the whole classification process: loads reference data, processes
     * all sales files and writes both reports. Prints a success message, or
     * an error message if something goes wrong.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        try {
            System.out.println("Starting data classification process...");
            ValidationSummary summary = new ValidationSummary();

            // 1. Load reference data (text files, or serialized files as fallback).
            Map<Integer, Product> productsMap = loadProducts(summary);
            Map<Long, Salesman> salesmenMap = loadSalesmen(summary);

            // 2. Save the loaded data as serialized (binary) files.
            writeSerialized(new ArrayList<Product>(productsMap.values()), PRODUCTS_SERIALIZED_FILE);
            writeSerialized(new ArrayList<Salesman>(salesmenMap.values()), SALESMEN_SERIALIZED_FILE);

            // 3. Initialize accumulators.
            Map<Long, Double> salesmanMoney = new HashMap<>();
            for (Long documentNumber : salesmenMap.keySet()) {
                salesmanMoney.put(documentNumber, 0.0);
            }
            Map<Integer, Integer> productQuantity = new HashMap<>();
            for (Integer productId : productsMap.keySet()) {
                productQuantity.put(productId, 0);
            }

            // 4. Process every sales file in the project folder.
            File[] salesFiles = new File(".").listFiles(
                    (dir, name) -> name.startsWith(SALES_FILE_PREFIX) && name.endsWith(SALES_FILE_EXTENSION));
            if (salesFiles == null || salesFiles.length == 0) {
                throw new IOException("No sales files (" + SALES_FILE_PREFIX + "*" + SALES_FILE_EXTENSION
                        + ") were found. Run GenerateInfoFiles first.");
            }
            Arrays.sort(salesFiles); // deterministic processing order
            for (File salesFile : salesFiles) {
                processSalesFile(salesFile, salesmenMap, productsMap, salesmanMoney, productQuantity, summary);
            }

            // 5. Write the sorted reports.
            writeSalesmenReport(salesmenMap, salesmanMoney, SALESMEN_REPORT_FILE);
            writeProductsReport(productsMap, productQuantity, PRODUCTS_REPORT_FILE);

            System.out.println();
            System.out.println("SUCCESS: report files generated correctly.");
            System.out.println("  - Salesmen report : " + SALESMEN_REPORT_FILE);
            System.out.println("  - Products report : " + PRODUCTS_REPORT_FILE);
            System.out.println("  Sales files processed : " + summary.processedFiles);
            System.out.println("  Sales files skipped   : " + summary.skippedFiles);
            System.out.println("  Invalid lines ignored : " + summary.invalidLines);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("ERROR: could not process the data. Details: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Loading of reference data
    // ------------------------------------------------------------------

    /**
     * Loads the product catalog from {@code products.txt}; if that file does
     * not exist, it falls back to {@code products.ser}.
     *
     * @param summary counters updated when invalid lines are found
     * @return products indexed by id
     * @throws IOException            if no catalog could be loaded
     * @throws ClassNotFoundException if the serialized file is corrupt
     */
    private static Map<Integer, Product> loadProducts(ValidationSummary summary)
            throws IOException, ClassNotFoundException {
        Map<Integer, Product> products = new LinkedHashMap<>();

        if (new File(PRODUCTS_FILE).exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(PRODUCTS_FILE))) {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split(";");
                    try {
                        if (parts.length < 3) {
                            throw new IllegalArgumentException("expected 3 fields");
                        }
                        int id = Integer.parseInt(parts[0].trim());
                        double price = Double.parseDouble(parts[2].trim());
                        if (price < 0) {
                            throw new IllegalArgumentException("negative price");
                        }
                        products.put(id, new Product(id, parts[1].trim(), price));
                    } catch (IllegalArgumentException e) {
                        summary.invalidLines++;
                        summary.warn(PRODUCTS_FILE + " line " + lineNumber + " ignored (" + e.getMessage() + ")");
                    }
                }
            }
        } else if (new File(PRODUCTS_SERIALIZED_FILE).exists()) {
            for (Product product : readSerialized(PRODUCTS_SERIALIZED_FILE, Product.class)) {
                products.put(product.getId(), product);
            }
        }

        if (products.isEmpty()) {
            throw new IOException("No products could be loaded. Check " + PRODUCTS_FILE);
        }
        return products;
    }

    /**
     * Loads the salesmen directory from {@code salesmen.txt}; if that file does
     * not exist, it falls back to {@code salesmen.ser}.
     *
     * @param summary counters updated when invalid lines are found
     * @return salesmen indexed by document number
     * @throws IOException            if no salesmen could be loaded
     * @throws ClassNotFoundException if the serialized file is corrupt
     */
    private static Map<Long, Salesman> loadSalesmen(ValidationSummary summary)
            throws IOException, ClassNotFoundException {
        Map<Long, Salesman> salesmen = new LinkedHashMap<>();

        if (new File(SALESMEN_FILE).exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(SALESMEN_FILE))) {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split(";");
                    try {
                        if (parts.length < 4) {
                            throw new IllegalArgumentException("expected 4 fields");
                        }
                        long documentNumber = Long.parseLong(parts[1].trim());
                        salesmen.put(documentNumber, new Salesman(parts[0].trim(), documentNumber,
                                parts[2].trim(), parts[3].trim()));
                    } catch (IllegalArgumentException e) {
                        summary.invalidLines++;
                        summary.warn(SALESMEN_FILE + " line " + lineNumber + " ignored (" + e.getMessage() + ")");
                    }
                }
            }
        } else if (new File(SALESMEN_SERIALIZED_FILE).exists()) {
            for (Salesman salesman : readSerialized(SALESMEN_SERIALIZED_FILE, Salesman.class)) {
                salesmen.put(salesman.getDocumentNumber(), salesman);
            }
        }

        if (salesmen.isEmpty()) {
            throw new IOException("No salesmen could be loaded. Check " + SALESMEN_FILE);
        }
        return salesmen;
    }

    // ------------------------------------------------------------------
    // Serialization helpers
    // ------------------------------------------------------------------

    /**
     * Writes a list of serializable objects to a binary file.
     *
     * @param data     objects to save
     * @param fileName destination file
     * @throws IOException if the file cannot be written
     */
    private static <T> void writeSerialized(ArrayList<T> data, String fileName) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(data);
        }
    }

    /**
     * Reads a list of objects previously saved with {@link #writeSerialized}.
     *
     * @param fileName source file
     * @param type     expected element type
     * @return the objects stored in the file
     * @throws IOException            if the file cannot be read
     * @throws ClassNotFoundException if the file content is not valid
     */
    private static <T> List<T> readSerialized(String fileName, Class<T> type)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            List<?> raw = (List<?>) in.readObject();
            List<T> result = new ArrayList<>();
            for (Object item : raw) {
                result.add(type.cast(item));
            }
            return result;
        }
    }

    // ------------------------------------------------------------------
    // Sales processing
    // ------------------------------------------------------------------

    /**
     * Reads one sales file and adds its valid sales to the accumulators.
     * The owner of the file is identified by the document number in its
     * first line, so several files can belong to the same salesman.
     * Wrong lines are reported and skipped; a file with an unreadable
     * header or an unknown salesman is skipped entirely.
     */
    private static void processSalesFile(File file, Map<Long, Salesman> salesmenMap,
            Map<Integer, Product> productsMap, Map<Long, Double> salesmanMoney,
            Map<Integer, Integer> productQuantity, ValidationSummary summary) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            long documentNumber;
            try {
                String[] headerParts = header == null ? new String[0] : header.split(";");
                if (headerParts.length < 2) {
                    throw new IllegalArgumentException("expected 'DocumentType;DocumentNumber'");
                }
                documentNumber = Long.parseLong(headerParts[1].trim());
            } catch (IllegalArgumentException e) {
                summary.skippedFiles++;
                summary.warn(file.getName() + " skipped: invalid header (" + e.getMessage() + ")");
                return;
            }

            if (!salesmenMap.containsKey(documentNumber)) {
                summary.skippedFiles++;
                summary.warn(file.getName() + " skipped: salesman " + documentNumber
                        + " is not listed in " + SALESMEN_FILE);
                return;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    String[] parts = line.split(";");
                    if (parts.length < 2) {
                        throw new IllegalArgumentException("expected 'ProductId;Quantity;'");
                    }
                    int productId = Integer.parseInt(parts[0].trim());
                    int quantity = Integer.parseInt(parts[1].trim());

                    Product product = productsMap.get(productId);
                    if (product == null) {
                        throw new IllegalArgumentException("unknown product id " + productId);
                    }
                    if (quantity <= 0) {
                        throw new IllegalArgumentException("quantity must be positive, found " + quantity);
                    }

                    salesmanMoney.put(documentNumber,
                            salesmanMoney.get(documentNumber) + quantity * product.getPricePerUnit());
                    productQuantity.put(productId, productQuantity.get(productId) + quantity);
                } catch (IllegalArgumentException e) {
                    summary.invalidLines++;
                    summary.warn(file.getName() + " line " + lineNumber + " ignored (" + e.getMessage() + ")");
                }
            }
            summary.processedFiles++;
        }
    }

    // ------------------------------------------------------------------
    // Reports
    // ------------------------------------------------------------------

    /**
     * Writes the salesmen report: one salesman per line, in the format
     * {@code FirstNames LastNames;MoneyCollected}, sorted from the highest
     * to the lowest amount of money collected.
     */
    private static void writeSalesmenReport(Map<Long, Salesman> salesmenMap,
            Map<Long, Double> salesmanMoney, String fileName) throws IOException {

        List<Salesman> salesmen = new ArrayList<>(salesmenMap.values());
        salesmen.sort(Comparator
                .comparing((Salesman s) -> salesmanMoney.get(s.getDocumentNumber())).reversed()
                .thenComparing(Salesman::getFullName));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Salesman salesman : salesmen) {
                writer.write(salesman.getFullName() + ";"
                        + String.format(Locale.US, "%.2f", salesmanMoney.get(salesman.getDocumentNumber())));
                writer.newLine();
            }
        }
    }

    /**
     * Writes the products report: one sold product per line, in the format
     * {@code ProductName;PricePerUnit}, sorted from the most to the least
     * units sold. Products with no sales are not included.
     */
    private static void writeProductsReport(Map<Integer, Product> productsMap,
            Map<Integer, Integer> productQuantity, String fileName) throws IOException {

        List<Product> soldProducts = new ArrayList<>();
        for (Product product : productsMap.values()) {
            if (productQuantity.get(product.getId()) > 0) {
                soldProducts.add(product);
            }
        }
        soldProducts.sort(Comparator
                .comparing((Product p) -> productQuantity.get(p.getId())).reversed()
                .thenComparing(Product::getName));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Product product : soldProducts) {
                writer.write(product.getName() + ";"
                        + String.format(Locale.US, "%.2f", product.getPricePerUnit()));
                writer.newLine();
            }
        }
    }
}
