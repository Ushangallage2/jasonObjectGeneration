package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ObjectMapper objectMapper = new ObjectMapper();
        boolean continueGenerating = true;
        int fileCounter = 1;

        while (continueGenerating) {
            // List all available templates in the templates directory
            File templatesDir = new File("templates");
            File[] templateFiles = templatesDir.listFiles((dir, name) -> name.endsWith(".json"));

            if (templateFiles == null || templateFiles.length == 0) {
                System.out.println("No templates found in the 'templates' directory.");
                return;
            }

            // Display the available templates
            System.out.println("Available templates:");
            for (int i = 0; i < templateFiles.length; i++) {
                System.out.println((i + 1) + ". " + templateFiles[i].getName());
            }

            // Ask the user to select a template
            int templateChoice;
            do {
                System.out.println("Enter the number of the template you want to use:");
                while (!scanner.hasNextInt()) {
                    System.out.println("Please enter a valid number:");
                    scanner.next();
                }
                templateChoice = scanner.nextInt();
            } while (templateChoice < 1 || templateChoice > templateFiles.length);

            File chosenTemplate = templateFiles[templateChoice - 1];
            scanner.nextLine(); // Consume the newline

            // Get user input for Terminal and Location values
            System.out.println("Enter Terminal value:");
            String terminal = scanner.nextLine().trim();

            System.out.println("Enter Location value:");
            String location = scanner.nextLine().trim();

            String flag;
            do {
                System.out.println("Enter S for Sales or F for Fixed:");
                flag = scanner.nextLine().toUpperCase().trim();
            } while (!flag.equals("S") && !flag.equals("F"));

            int numberOfObjects;
            do {
                System.out.println("Enter the number of JSON objects to generate:");
                while (!scanner.hasNextInt()) {
                    System.out.println("Please enter a valid number:");
                    scanner.next();
                }
                numberOfObjects = scanner.nextInt();
            } while (numberOfObjects <= 0);

            try {
                JsonNode templateJson = objectMapper.readTree(chosenTemplate);
                FileWriter fileWriter = new FileWriter("output" + fileCounter + ".json");
                ArrayNode jsonArray = objectMapper.createArrayNode();

                for (int i = 1; i <= numberOfObjects; i++) {
                    ObjectNode jsonObject = (ObjectNode) templateJson.deepCopy();
                    String invoiceSuffix = flag.equals("S")
                            ? String.format("%07d", i)
                            : "0000001";

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

                fileWriter.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonArray));
                fileWriter.close();
                System.out.println("JSON objects generated successfully in 'output" + fileCounter + ".json'");
                fileCounter++;

            } catch (IOException e) {
                e.printStackTrace();
            }

            // Ask the user if they want to generate another file
            System.out.println("Do you want to generate another file? (yes/no)");
            String userResponse = scanner.next().toLowerCase().trim();
            continueGenerating = userResponse.equals("yes");
            scanner.nextLine(); // Consume the newline
        }

        scanner.close();
        System.out.println("Program finished.");
    }
}
