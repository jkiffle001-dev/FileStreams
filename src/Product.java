import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Objects;

/**
 * Product models a product record with an ID, name, description and cost.
 * The ID never changes once the object is created, so it has no setter.
 *
 * For the Random Access file every record must be the same size in bytes,
 * so the three String fields are padded with spaces to fixed lengths:
 * name = 35 chars, description = 75 chars, ID = 6 chars.
 * Each char is written as 2 bytes and the cost as an 8 byte double, so one
 * record is (35 + 75 + 6) * 2 + 8 = 240 bytes.
 *
 * Product also implements Serializable so it can be written with an
 * ObjectOutputStream.
 */
public class Product implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** Fixed length of the name field in characters. */
    public static final int NAME_LENGTH = 35;
    /** Fixed length of the description field in characters. */
    public static final int DESCRIPTION_LENGTH = 75;
    /** Fixed length of the ID field in characters. */
    public static final int ID_LENGTH = 6;
    /** Size of one fixed width record in bytes (chars are 2 bytes, double is 8). */
    public static final int RECORD_SIZE = (NAME_LENGTH + DESCRIPTION_LENGTH + ID_LENGTH) * 2 + Double.BYTES;

    private String name;
    private String description;
    private final String ID; // should never change
    private double cost;

    /**
     * Full constructor that takes all of the fields.
     *
     * @param ID the unique ID for the product (never changes)
     * @param name the product name
     * @param description a short description of the product
     * @param cost the cost of the product
     */
    public Product(String ID, String name, String description, double cost)
    {
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.cost = cost;
    }

    /**
     * Overloaded constructor for a product without a description.
     * The description is set to an empty String.
     *
     * @param ID the unique ID for the product (never changes)
     * @param name the product name
     * @param cost the cost of the product
     */
    public Product(String ID, String name, double cost)
    {
        this(ID, name, "", cost);
    }

    /**
     * @return the product name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the new product name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the new description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the ID (there is no setter because the ID never changes)
     */
    public String getID()
    {
        return ID;
    }

    /**
     * @return the cost
     */
    public double getCost()
    {
        return cost;
    }

    /**
     * @param cost the new cost
     */
    public void setCost(double cost)
    {
        this.cost = cost;
    }

    // ---------------------------------------------------------------
    // Fixed length / Random Access utility methods
    // ---------------------------------------------------------------

    /**
     * Pads (or cuts) a String with spaces so it is exactly the given length.
     *
     * @param value the String to pad, null is treated as an empty String
     * @param length the exact number of characters wanted
     * @return the String padded with trailing spaces or truncated to length
     */
    public static String padToLength(String value, int length)
    {
        if (value == null)
        {
            value = "";
        }
        if (value.length() > length)
        {
            return value.substring(0, length);
        }
        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < length)
        {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * @return the name padded with spaces to NAME_LENGTH characters
     */
    public String getPaddedName()
    {
        return padToLength(name, NAME_LENGTH);
    }

    /**
     * @return the description padded with spaces to DESCRIPTION_LENGTH characters
     */
    public String getPaddedDescription()
    {
        return padToLength(description, DESCRIPTION_LENGTH);
    }

    /**
     * @return the ID padded with spaces to ID_LENGTH characters
     */
    public String getPaddedID()
    {
        return padToLength(ID, ID_LENGTH);
    }

    /**
     * Creates the fixed width text form of the record (without the cost),
     * useful for display and for checking the record format.
     *
     * @return ID + name + description, each padded to its fixed length
     */
    public String toRandomRecordString()
    {
        return getPaddedID() + getPaddedName() + getPaddedDescription();
    }

    /**
     * Writes this product as one fixed width record at the file's current
     * position: padded name, padded description, padded ID, then cost.
     *
     * @param raf an open RandomAccessFile positioned where the record goes
     * @throws IOException if the write fails
     */
    public void writeRandomRecord(RandomAccessFile raf) throws IOException
    {
        raf.writeChars(getPaddedName());
        raf.writeChars(getPaddedDescription());
        raf.writeChars(getPaddedID());
        raf.writeDouble(cost);
    }

    /**
     * Reads one fixed width record from the file's current position and
     * trims the padding off the String fields.
     *
     * @param raf an open RandomAccessFile positioned at the start of a record
     * @return the Product stored in that record
     * @throws IOException if the read fails
     */
    public static Product readRandomRecord(RandomAccessFile raf) throws IOException
    {
        String name = readFixedString(raf, NAME_LENGTH).trim();
        String description = readFixedString(raf, DESCRIPTION_LENGTH).trim();
        String id = readFixedString(raf, ID_LENGTH).trim();
        double cost = raf.readDouble();
        return new Product(id, name, description, cost);
    }

    /**
     * Reads a fixed number of chars from a RandomAccessFile.
     *
     * @param raf the open file
     * @param length the number of chars to read
     * @return the chars read as a String (still padded)
     * @throws IOException if the read fails
     */
    private static String readFixedString(RandomAccessFile raf, int length) throws IOException
    {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
        {
            sb.append(raf.readChar());
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // Lab 01 formatting methods
    // ---------------------------------------------------------------

    /**
     * Creates a comma separated value record for writing to a text file.
     *
     * @return the CSV record: ID, name, description, cost
     */
    public String toCSV()
    {
        return ID + ", " + name + ", " + description + ", " + cost;
    }

    /**
     * Creates a JSON representation of the product.
     *
     * @return the product as a JSON object String
     */
    public String toJSON()
    {
        final char DQ = '"';
        return "{" + DQ + "ID" + DQ + ":" + DQ + ID + DQ + ","
                + DQ + "name" + DQ + ":" + DQ + name + DQ + ","
                + DQ + "description" + DQ + ":" + DQ + description + DQ + ","
                + DQ + "cost" + DQ + ":" + cost + "}";
    }

    /**
     * Creates an XML representation of the product.
     *
     * @return the product as an XML element String
     */
    public String toXML()
    {
        return "<Product>"
                + "<ID>" + ID + "</ID>"
                + "<name>" + name + "</name>"
                + "<description>" + description + "</description>"
                + "<cost>" + cost + "</cost>"
                + "</Product>";
    }

    /**
     * @return a readable String with all of the fields
     */
    @Override
    public String toString()
    {
        return "Product{" +
                "ID='" + ID + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cost=" + cost +
                '}';
    }

    /**
     * Two Product objects are equal when all of their fields match.
     *
     * @param o the object to compare with
     * @return true if the objects hold the same data
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Double.compare(product.cost, cost) == 0
                && Objects.equals(ID, product.ID)
                && Objects.equals(name, product.name)
                && Objects.equals(description, product.description);
    }

    /**
     * @return a hash code consistent with equals()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(ID, name, description, cost);
    }
}
