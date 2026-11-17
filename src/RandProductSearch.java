import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * RandProductSearch is a Swing GUI that asks the user for a partial product
 * name and displays every Product in the random access file whose name
 * contains that text (case insensitive).
 *
 * Because every record is a fixed RECORD_SIZE bytes, the program finds
 * record i by seeking to i * RECORD_SIZE.
 */
public class RandProductSearch extends JFrame
{
    private final JTextField searchTF = new JTextField(25);
    private final JTextArea resultsTA = new JTextArea(14, 80);
    private final JLabel statusLbl = new JLabel("Enter part of a product name and click Search.");

    private final JButton searchBtn = new JButton("Search");
    private final JButton showAllBtn = new JButton("Show All");
    private final JButton quitBtn = new JButton("Quit");

    /**
     * Builds the search form and wires up the buttons.
     */
    public RandProductSearch()
    {
        super("Random Product Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setBorder(new TitledBorder("Search " + RandProductMaker.DATA_FILE));
        top.add(new JLabel("Partial product name:"));
        top.add(searchTF);
        top.add(searchBtn);
        top.add(showAllBtn);
        main.add(top, BorderLayout.NORTH);

        resultsTA.setEditable(false);
        resultsTA.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroller = new JScrollPane(resultsTA);
        scroller.setBorder(new TitledBorder("Matching Products"));
        main.add(scroller, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(statusLbl, BorderLayout.CENTER);
        bottom.add(quitBtn, BorderLayout.EAST);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);

        searchTF.setName("search");
        searchBtn.setName("searchBtn");

        searchBtn.addActionListener(e -> search(searchTF.getText().trim()));
        searchTF.addActionListener(e -> search(searchTF.getText().trim()));
        showAllBtn.addActionListener(e -> search(""));
        quitBtn.addActionListener(e -> System.exit(0));

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Reads every fixed width record from the random access file.
     *
     * @return all of the Products in the file, in file order
     * @throws IOException if the file can't be read
     */
    private ArrayList<Product> readAllProducts() throws IOException
    {
        ArrayList<Product> products = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(new File(RandProductMaker.DATA_FILE), "r"))
        {
            long recordTotal = raf.length() / Product.RECORD_SIZE;
            for (long i = 0; i < recordTotal; i++)
            {
                raf.seek(i * Product.RECORD_SIZE); // jump straight to record i
                products.add(Product.readRandomRecord(raf));
            }
        }
        return products;
    }

    /**
     * Displays every product whose name contains the search text.
     * An empty search text matches every product.
     *
     * @param partialName the text to look for in product names
     */
    private void search(String partialName)
    {
        ArrayList<Product> products;
        try
        {
            products = readAllProducts();
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this,
                    "Could not read " + RandProductMaker.DATA_FILE + ". Run RandProductMaker first.\n" + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String target = partialName.toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s%-20s%-36s%10s%n", "ID", "Name", "Description", "Cost"));
        sb.append("=".repeat(74)).append('\n');

        int matches = 0;
        for (Product p : products)
        {
            if (p.getName().toLowerCase().contains(target))
            {
                sb.append(String.format("%-8s%-20s%-36s%10.2f%n",
                        p.getID(), p.getName(), p.getDescription(), p.getCost()));
                matches++;
            }
        }
        if (matches == 0)
        {
            sb.append("No products found with a name containing \"").append(partialName).append("\"\n");
        }

        resultsTA.setText(sb.toString());
        resultsTA.setCaretPosition(0);
        statusLbl.setText(matches + " of " + products.size() + " records matched \"" + partialName + "\"");
    }

    /**
     * Starts the GUI.
     *
     * @param args not used
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new RandProductSearch().setVisible(true));
    }
}
