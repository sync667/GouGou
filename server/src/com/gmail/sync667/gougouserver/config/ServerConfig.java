package com.gmail.sync667.gougouserver.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UnknownFormatConversionException;

import com.gmail.sync667.gougouserver.GouGouServer;

public class ServerConfig extends Config {

    private static Map<Object, Object> entrys = new HashMap<>();
    private final File serverConfig;

    public ServerConfig() {
        super(path);
        path = Paths.get(GouGouServer.getServer().getClass().getProtectionDomain().getCodeSource().getLocation()
                .getPath()
                + File.separator + "serverConfig.txt");

        serverConfig = path.toFile();

        if (!serverConfig.exists()) {
            set();
            saveConfig();
            GouGouServer.getConsole().infoC("Created ServerConfig file!");
        } else {
            reloadConfig();
            checkConfig();
        }

    }

    private void saveConfig() {
        serverConfig.delete();
        try {
            serverConfig.createNewFile();
        } catch (IOException e2) {
            GouGouServer.getConsole().infoC("Failed to create serverConfig! Stopping...");
            GouGouServer.getServer().stop();
        }
        try {
            serverConfig.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {

            for (Entry<Object, Object> entry : entrys.entrySet()) {
                writer.write(entry.getKey().toString() + ": " + entry.getValue().toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void reloadConfig() {
        try (Scanner scanner = new Scanner(path, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains(":")) {
                    String[] strings = line.split(":");
                    entrys.put(strings[0], strings[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void set() {
        if (!entrys.containsKey("ServerMOTD")) {
            entrys.put("ServerMOTD", "Default Server");
        }
        if (!entrys.containsKey("ServerIP")) {
            entrys.put("ServerIP", "127.0.0.1");
        }
        if (!entrys.containsKey("ServerPort")) {
            entrys.put("ServerPort", 1332);
        }
        if (!entrys.containsKey("ServerSlots")) {
            entrys.put("ServerSlots", 10);
        }
        if (!entrys.containsKey("ServerDebug")) {
            entrys.put("ServerDebug", true);
        }
    }

    private boolean checkConfig() {
        boolean error = false;
        List<String> errors = new ArrayList<String>();

        if (entrys.containsKey("ServerIP")) {
            String[] ip = entrys.get("ServerIP").toString().split("\\.");
            try {
                InetAddress.getByAddress(new byte[] { Integer.valueOf(ip[0]).byteValue(),
                        Integer.valueOf(ip[0]).byteValue(), Integer.valueOf(ip[0]).byteValue(),
                        Integer.valueOf(ip[0]).byteValue() });
            } catch (NumberFormatException e) {
                error = true;
                errors.add("Wrong IP Address format!");
                entrys.remove("ServerIP");
                entrys.put("ServerIP", "127.0.0.1");
            } catch (UnknownHostException e) {
                error = true;
                errors.add("Wrong IP Address!");
                entrys.remove("ServerIP");
                entrys.put("ServerIP", "127.0.0.1");
            }
        }
        if (entrys.containsKey("ServerPort")) {
            try {
                Integer.valueOf((String) entrys.get("ServerPort"));
            } catch (NumberFormatException e) {
                error = true;
                errors.add("Wrong Port format!");
                entrys.remove("ServerPort");
                entrys.put("ServerPort", 1332);
            }
        }
        if (entrys.containsKey("ServerSlots")) {
            try {
                Integer.valueOf((String) entrys.get("ServerSlots"));
            } catch (NumberFormatException e) {
                error = true;
                errors.add("Wrong Slots format!");
                entrys.remove("ServerSlots");
                entrys.put("ServerSlots", 10);
            }
        }
        if (entrys.containsKey("ServerDebug")) {
            try {
                Boolean.valueOf((String) entrys.get("ServerDebug"));
            } catch (UnknownFormatConversionException e) {
                error = true;
                errors.add("Wrong Debug format!");
                entrys.remove("ServerDebug");
                entrys.put("ServerDebug", true);
            }
        }

        saveConfig();

        if (error) {
            GouGouServer.getConsole().infoC("Error will parsing server config: ");
            for (String error2 : errors) {
                GouGouServer.getConsole().infoC(error2);
            }

            return false;
        }

        return true;
    }

    public Object getEntry(String key) {
        return entrys.get(key);
    }
}
