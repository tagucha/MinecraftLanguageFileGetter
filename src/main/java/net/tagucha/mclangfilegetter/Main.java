package net.tagucha.mclangfilegetter;

import com.google.gson.Gson;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class Main {
    private static class IndexFile extends HashMap<String, HashMap<String, HashMap<String, String>>>{}

    public static void main(String[] args) {
        Gson gson = new Gson();
        JFrame frame = new JFrame("Minecraft Language File Getter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 310);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(null);

        String defaultMColder = System.getProperty("user.home") + "\\AppData\\Roaming\\.minecraft";
        if (System.getProperty("os.name").equals("Mac OS X")) defaultMColder = System.getProperty("user.home") + "/Library/Application Support/minecraft";
        JTextField mcDirName = add(panel, "Minecraft Folder", defaultMColder, 30);
        JTextField verDirName = add(panel, "Minecraft Version", "1.19", 80);
        JTextField langName = add(panel, "Lang", "ja_jp", 130);
        JTextField outputDirName = add(panel, "Output Folder", System.getProperty("user.dir"), 180);

        JButton button = new JButton("Get");
        button.addActionListener(e -> {
            try {
                File mcDir = new File(mcDirName.getText());
                if (!mcDir.exists()) throw new Exception("Minecraft Folder was not found.");
                if (!mcDir.isDirectory()) throw new Exception("Minecraft Folder was not found.");
                File index = new File(mcDir, "assets/indexes/" + verDirName.getText() + ".json");
                if (!index.exists()) throw new Exception("The version was not found.");
                if (!index.isFile()) throw new Exception("The version was not found.");
                IndexFile indexFile = gson.fromJson(new FileReader(index), IndexFile.class);
                String uuid = indexFile.getOrDefault("objects", new HashMap<>())
                        .getOrDefault("minecraft/lang/" + langName.getText() + ".json", new HashMap<>())
                        .get("hash");
                if (uuid == null) throw new Exception("The version was not found.");
                File file = new File(mcDir, "assets/objects/" + uuid.substring(0, 2) + "/" + uuid);
                if (!file.exists()) throw new Exception("The language was not found.");
                if (!file.isFile()) throw new Exception("The language was not found.");
                File output = new File(outputDirName.getText(), langName.getText() + ".json");
                output.getParentFile().mkdirs();
                output.createNewFile();
                BufferedReader br = new BufferedReader(new FileReader(file));
                String arg = br.lines().collect(Collectors.joining());
                JSONObject object = new JSONObject(arg);
                BufferedWriter bw = new BufferedWriter(new FileWriter(output));
                PriorityQueue<String> queue = new PriorityQueue<>(object.keySet());
                String key = queue.remove();
                bw.write(String.format("{\n  \"%s\": \"%s\"", key, object.getString(key)
                        .replace("\\", "\\\\")
                        .replace("\n", "\\n")
                ));
                while (!queue.isEmpty()) {
                    key = queue.remove();
                    bw.write(String.format(",\n  \"%s\": \"%s\"", key, object.getString(key)
                            .replace("\\", "\\\\")
                            .replace("\n", "\\n")
                    ));
                }
                bw.write("\n}\n");
                bw.close();
                JOptionPane.showMessageDialog(null, "Done!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }

        });
        button.setBounds(300, 230, 40, 20);
        panel.add(button);

        frame.setVisible(true);

        System.out.println(frame.isActive());
    }

    public static JTextField add(JPanel panel, String title, String txt, int y) {
        JLabel label = new JLabel(title + ":");
        label.setBounds(20, y, 130, 20);
        panel.add(label);
        JTextField text = new JTextField(txt);
        text.setBounds(160, y, 460, 20);
        panel.add(text);
        return text;
    }
}
