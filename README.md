# Data Generation and Classification — Module Project

Java 8 / Eclipse project for the module **Conceptos Fundamentales de
Programación** (Politécnico Grancolombiano). Delivery type: design of a
documented Java program.

## Current status: Entrega 3 (Semanas 7-8) — Final version

The project has exactly two classes with a `main` method:

1. **`GenerateInfoFiles`** — generates the pseudo-random flat files:
   `products.txt`, `salesmen.txt` and several `sales_*.txt` files per
   salesman. It also creates one deliberately faulty file
   (`sales_invalid_sample.txt`) to demonstrate data validation. No user input.
2. **`main`** — reads those files and writes two reports. No user input.
   - `salesmen_report.csv`: `FirstNames LastNames;MoneyCollected`, sorted from
     highest to lowest money collected.
   - `products_report.csv`: `ProductName;PricePerUnit` of every sold product,
     sorted from most to least units sold.

Both programs print a success message or an error message.

## Extra features implemented

- **Several files per salesman:** sales are grouped by the document number
  inside each file, so any number of `sales_*.txt` files can belong to one
  salesman.
- **Serialized files:** products and salesmen are saved to `products.ser` /
  `salesmen.ser`; if the `.txt` files are missing, `main` loads the `.ser` ones.
- **Detection of wrong data:** malformed lines, unknown product ids, negative
  prices and non-positive quantities are reported as warnings and skipped.

## Project structure

```
src/
  GenerateInfoFiles.java   -> first entry point (data generation)
  main.java                -> second entry point (reports)
  Product.java             -> serializable model for a product
  Salesman.java            -> serializable model for a salesman
conslusion.txt             -> lessons learned, applications, difficulties
```

## How to run

Import as an existing Eclipse project (`File → Import → Existing Projects
into Workspace`), run `GenerateInfoFiles` first and then `main`, both as
**Java Application**. All files are created in the project's root folder.
