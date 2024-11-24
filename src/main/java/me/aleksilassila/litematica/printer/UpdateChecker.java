package me.aleksilassila.litematica.printer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class UpdateChecker {
    public static final String version = "v" + PrinterReference.MOD_VERSION;

    // Try to get this to work at some point
//    static {
//        try (InputStream in = UpdateChecker.class.getResourceAsStream("/fabric.mod.json")) {
//            String jsonString = IOUtils.toString(in, StandardCharsets.UTF_8);
//            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
//            System.out.println("JSON object: " + json);
//            System.out.println("Raw json: " + jsonString);
//            System.out.println("File: " + new File(UpdateChecker.class.getResource("/fabric.mod.json").getFile()));
//            String version = json.get("version").getAsString();
//            System.out.println("Reading fabric.mod.json");
//            System.out.println("Parsed version: " + version);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @SuppressWarnings("deprecation")
    public static String getPrinterVersion() {
        try (InputStream inputStream = new URL("https://api.github.com/repos/aleksilassila/litematica-printer/tags").openStream(); Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
                JsonArray tags = new JsonParser().parse(scanner.next()).getAsJsonArray();
                return ((JsonObject) tags.get(0)).get("name").getAsString();
            }
        } catch (Exception exception) {
            System.out.println("Cannot look for updates: " + exception.getMessage());
        }

        return "";
    }
}
