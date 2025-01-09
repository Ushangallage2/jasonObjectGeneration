package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static JTextArea outputArea;
    private static String generatedJson; // Store the generated JSON in a class variable

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setLookAndFeel();
            JFrame frame = createMainFrame();
            frame.setVisible(true);
        });
    }

    private static JFrame createMainFrame() {
        JFrame frame = new JFrame("JSON Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel inputPanel = createInputPanel(frame);
        JPanel outputPanel = createOutputPanel();

        tabbedPane.addTab("Input", inputPanel);
        tabbedPane.addTab("Output", outputPanel);

        frame.add(tabbedPane);
        return frame;
    }

    private static JPanel createInputPanel(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JComboBox<String> templateComboBox = new JComboBox<>();
        JTextField terminalField = new JTextField();
        JTextField locationField = new JTextField();
        JComboBox<String> flagComboBox = new JComboBox<>(new String[]{"S", "F"});
        JTextField numberField = new JTextField();

        // Set smaller sizes for input fields
        terminalField.setPreferredSize(new Dimension(200, 25));
        locationField.setPreferredSize(new Dimension(200, 25));
        flagComboBox.setPreferredSize(new Dimension(200, 25));
        numberField.setPreferredSize(new Dimension(200, 25));
        templateComboBox.setPreferredSize(new Dimension(200, 25));

        // Load templates dynamically
        loadTemplates(templateComboBox);

        panel.add(createTemplatePanel(templateComboBox));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createLabeledField("Terminal:", terminalField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createLabeledField("Location:", locationField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createLabeledDropdown("Flag (S/F):", flagComboBox));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createLabeledField("Number of Objects:", numberField));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createGenerateButton(templateComboBox, terminalField, locationField, flagComboBox, numberField, frame));
        panel.add(createSaveButton(frame)); // Add the Save button

        return panel;
    }

    private static JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        outputArea = new JTextArea(); // Reference the output area here
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), "Generated JSON"));

        // Create a JScrollPane for output area
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static JPanel createTemplatePanel(JComboBox<String> templateComboBox) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Select Template:");

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadTemplates(templateComboBox));

        panel.add(label);
        panel.add(templateComboBox);
        panel.add(refreshButton);

        return panel;
    }

    private static JPanel createLabeledField(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        textField.setPreferredSize(new Dimension(200, 25));
        panel.add(label);
        panel.add(textField);
        return panel;
    }

    private static JPanel createLabeledDropdown(String labelText, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        panel.add(label);
        panel.add(comboBox);
        return panel;
    }

    private static JButton createGenerateButton(JComboBox<String> templateComboBox, JTextField terminalField, JTextField locationField, JComboBox<String> flagComboBox, JTextField numberField, JFrame frame) {
        JButton generateButton = new JButton("Generate JSON");
        generateButton.setBackground(Color.BLUE);
        generateButton.setForeground(Color.BLACK);
        generateButton.setFocusPainted(false);
        generateButton.setPreferredSize(new Dimension(100, 30)); // Set same size for uniformity

        generateButton.addActionListener(e -> {
            generatedJson = generateJson(templateComboBox, terminalField, locationField, flagComboBox, numberField, outputArea, frame);
            // Show JSON in the output area
            outputArea.setText(generatedJson);
        });

        return generateButton;
    }

    private static JButton createSaveButton(JFrame frame) {
        JButton saveButton = new JButton("Save JSON");
        saveButton.setBackground(Color.GREEN);
        saveButton.setForeground(Color.BLACK);
        saveButton.setFocusPainted(false);
        saveButton.setPreferredSize(new Dimension(400, 30)); // Set same size for uniformity

        saveButton.addActionListener(e -> {
            if (generatedJson == null || generatedJson.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No JSON generated to save.", "Save Error", JOptionPane.ERROR_MESSAGE);
            } else {
                saveJson(generatedJson, frame);
            }
        });

        return saveButton;
    }

    private static String generateJson(JComboBox<String> templateComboBox, JTextField terminalField, JTextField locationField, JComboBox<String> flagComboBox, JTextField numberField, JTextArea outputArea, JFrame frame) {
        String terminal = terminalField.getText().trim();
        String location = locationField.getText().trim();
        String flag = (String) flagComboBox.getSelectedItem();
        int numberOfObjects;

        if (terminal.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return "";
        }

        try {
            numberOfObjects = Integer.parseInt(numberField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid number for objects.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return "";
        }

        if (templateComboBox.getSelectedItem() == null || !templateComboBox.isEnabled()) {
            JOptionPane.showMessageDialog(frame, "Please select a valid template.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return "";
        }

        String chosenTemplateName = (String) templateComboBox.getSelectedItem();
        File chosenTemplate = new File("templates/" + chosenTemplateName);
        ArrayNode jsonArray = objectMapper.createArrayNode();

        try {
            JsonNode templateJson = objectMapper.readTree(chosenTemplate);

            for (int i = 1; i <= numberOfObjects; i++) {
                ObjectNode jsonObject = (ObjectNode) templateJson.deepCopy();
                String invoiceSuffix = flag.equals("S") ? String.format("%07d", i) : "0000001";

                String newSyncKey = "INVOICE#" + terminal + "S" + invoiceSuffix;
                ObjectNode detailNode = (ObjectNode) jsonObject.path("DetailList").get(0);
                detailNode.put("SyncKey", newSyncKey);

                String valueWithoutPrefix = newSyncKey.replace("INVOICE#", "");
                ArrayNode inventoryList = (ArrayNode) detailNode.path("SyncValue").path("inventorylist");
                if (inventoryList.size() > 0) {
                    ObjectNode inventoryItem = (ObjectNode) inventoryList.get(0);
                    inventoryItem.put("transactionnumber", valueWithoutPrefix);
                }

                ObjectNode invoiceNode = (ObjectNode) detailNode.path("SyncValue").path("invoice");
                invoiceNode.put("invoicenumber", valueWithoutPrefix);

                jsonObject.put("Terminal", terminal);
                jsonObject.put("Location", location);

                jsonArray.add(jsonObject);
            }

            // Return the generated JSON as a string
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonArray);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error generating JSON: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return "";
        }
    }

    private static void saveJson(String jsonToSave, JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save JSON File");
        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter fileWriter = new FileWriter(fileToSave)) {
                fileWriter.write(jsonToSave);
                JOptionPane.showMessageDialog(frame, "JSON saved successfully to: " + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error saving JSON: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void loadTemplates(JComboBox<String> templateComboBox) {
        templateComboBox.removeAllItems();
        File templatesDir = new File("templates");
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();
            templateComboBox.addItem("No templates found. Add templates and restart.");
            templateComboBox.setEnabled(false);
        } else {
            File[] templateFiles = templatesDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (templateFiles != null && templateFiles.length > 0) {
                for (File file : templateFiles) {
                    templateComboBox.addItem(file.getName());
                }
                templateComboBox.setEnabled(true);
            } else {
                templateComboBox.addItem("No templates found. Add templates and restart.");
                templateComboBox.setEnabled(false);
            }
        }
    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}