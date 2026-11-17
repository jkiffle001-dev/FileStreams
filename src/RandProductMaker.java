import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * RandProductMaker is a Swing GUI that lets the user enter Product records
 * and saves each one as a fixed width record in a RandomAccessFile.
 *
 * The String fields are padded to fixed lengths by the Product class
 * (name 35, description 75, ID 6 chars) so every record is the same size.
 */
public class RandProductMaker extends JFrame
{
    /** The random access data file the records are written to. */
    public static final String DATA_FILE = "ProductData.dat";

    private final JTextField nameTF = new JTextField(Product.NAME_LENGTH);
    private final JTextField descriptionTF = new JTextField(40);
    private final JTextField idTF = new JTextField(Product.ID_LENGTH);
    private final JTextField costTF = new JTextField(10);
    private final JTextField recordCountTF = new JTextField(5);

    private final JButton addBtn = new JButton("Add Record");
    private final JButton clearBtn = new JButton("Clear");
    private final JButton quitBtn = new JButton("Quit");

    private int recordCount = 0;

    /**
     * Builds the form and wires up the buttons.
     */
    public RandProductMaker()
    {
        super("Random Product Maker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Enter Product Records", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        main.add(title, BorderLayout.NORTH);

        main.add(buildFormPanel(), BorderLayout.CENTER);
        main.add(buildButtonPanel(), BorderLayout.SOUTH);

        setContentPane(main);

        nameTF.setName("name");
        descriptionTF.setName("description");
        idTF.setName("id");
        costTF.setName("cost");
        recordCountTF.setName("count");
        addBtn.setName("add");

        addBtn.addActionListener(e -> addRecord());
        clearBtn.addActionListener(e -> clearFields());
        quitBtn.addActionListener(e -> quit());

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * @return the panel holding the labeled input fields and record count
     */
    private JPanel buildFormPanel()
    {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new TitledBorder("Product Data"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        addRow(form, gbc, 0, "Name (max " + Product.NAME_LENGTH + "):", nameTF);
        addRow(form, gbc, 1, "Description (max " + Product.DESCRIPTION_LENGTH + "):", descriptionTF);
        addRow(form, gbc, 2, "ID (max " + Product.ID_LENGTH + "):", idTF);
        addRow(form, gbc, 3, "Cost:", costTF);

        recordCountTF.setEditable(false);
        recordCountTF.setText("0");
        recordCountTF.setHorizontalAlignment(JTextField.CENTER);
        addRow(form, gbc, 4, "Records entered:", recordCountTF);
        return form;
    }

    /**
     * Adds one label + field row to the form.
     */
    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field)
    {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, gbc);
    }

    /**
     * @return the panel holding the Add, Clear and Quit buttons
     */
    private JPanel buildButtonPanel()
    {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttons.add(addBtn);
        buttons.add(clearBtn);
        buttons.add(quitBtn);
        return buttons;
    }

    /**
     * Validates the fields and, if they are all good, writes the record to
     * the random access file, bumps the record count and clears the form.
     */
    private void addRecord()
    {
        String name = nameTF.getText().trim();
        String description = descriptionTF.getText().trim();
        String id = idTF.getText().trim();
        String costText = costTF.getText().trim();

        String error = validateFields(name, description, id, costText);
        if (error != null)
        {
            JOptionPane.showMessageDialog(this, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Product product = new Product(id, name, description, Double.parseDouble(costText));

        try (RandomAccessFile raf = new RandomAccessFile(new File(DATA_FILE), "rw"))
        {
            raf.seek(raf.length()); // append the new record at the end of the file
            product.writeRandomRecord(raf);
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Could not write the record: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        recordCount++;
        recordCountTF.setText(String.valueOf(recordCount));
        clearFields();
    }

    /**
     * Checks that every field is filled in and fits its fixed length, and
     * that the cost is a non-negative number.
     *
     * @return an error message, or null when all of the fields are valid
     */
    private String validateFields(String name, String description, String id, String costText)
    {
        if (name.isEmpty() || description.isEmpty() || id.isEmpty() || costText.isEmpty())
        {
            return "Please fill in all of the fields before adding the record.";
        }
        if (name.length() > Product.NAME_LENGTH)
        {
            return "Name must be " + Product.NAME_LENGTH + " characters or less.";
        }
        if (description.length() > Product.DESCRIPTION_LENGTH)
        {
            return "Description must be " + Product.DESCRIPTION_LENGTH + " characters or less.";
        }
        if (id.length() > Product.ID_LENGTH)
        {
            return "ID must be " + Product.ID_LENGTH + " characters or less.";
        }
        try
        {
            double cost = Double.parseDouble(costText);
            if (cost < 0)
            {
                return "Cost cannot be negative.";
            }
        }
        catch (NumberFormatException ex)
        {
            return "Cost must be a number, for example 19.99";
        }
        return null;
    }

    /**
     * Clears the input fields and puts the cursor back in the name field.
     */
    private void clearFields()
    {
        nameTF.setText("");
        descriptionTF.setText("");
        idTF.setText("");
        costTF.setText("");
        nameTF.requestFocusInWindow();
    }

    /**
     * Confirms and exits the program.
     */
    private void quit()
    {
        int choice = JOptionPane.showConfirmDialog(this, "Quit Random Product Maker?", "Quit",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
    }

    /**
     * Starts the GUI.
     *
     * @param args not used
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new RandProductMaker().setVisible(true));
    }
}
