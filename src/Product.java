import java.io.Serializable;

/**
 * Simple data holder that represents a product available for sale.
 *
 * Instances of this class are immutable: once a Product is created,
 * its id, name and price cannot change. The class is {@link Serializable}
 * so that product catalogs can be saved to and loaded from binary
 * ({@code .ser}) files.
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final double pricePerUnit;

    /**
     * Creates a new product.
     *
     * @param id           unique identifier of the product
     * @param name         display name of the product
     * @param pricePerUnit price charged for a single unit of this product
     */
    public Product(int id, String name, double pricePerUnit) {
        this.id = id;
        this.name = name;
        this.pricePerUnit = pricePerUnit;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }
}
