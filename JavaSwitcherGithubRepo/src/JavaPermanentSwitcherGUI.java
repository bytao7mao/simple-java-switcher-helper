import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class JavaPermanentSwitcherGUI {
    private static JComboBox<String> jdkComboBox;
    private static List<File> jdks;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JavaPermanentSwitcherGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Java Permanent Switcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 250);
        frame.setLayout(new BorderLayout(10, 10));

        jdks = detectJDKs();
        String[] jdkNames = jdks.stream().map(File::getAbsolutePath).toArray(String[]::new);
        jdkComboBox = new JComboBox<>(jdkNames);
        frame.add(jdkComboBox, BorderLayout.CENTER);

        JButton applyButton = new JButton("Switch Permanently");
        applyButton.addActionListener(e -> switchJava());
        frame.add(applyButton, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static List<File> detectJDKs() {
        List<File> list = new ArrayList<>();
        File[] roots = {
                new File("C:\\Program Files\\Java"),
                new File("C:\\Program Files (x86)\\Java")
        };

        for (File root : roots) {
            if (root.exists() && root.isDirectory()) {
                File[] children = root.listFiles();
                if (children != null) {
                    for (File f : children) {
                        if (new File(f, "bin\\java.exe").exists()) {
                            list.add(f);
                        }
                    }
                }
            }
        }
        return list;
    }


    private static void switchJava() {
        int selectedIndex = jdkComboBox.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= jdks.size()) return;

        String selectedJDK = jdks.get(selectedIndex).getAbsolutePath();
        String psPath = System.getenv("WINDIR") + "\\System32\\WindowsPowerShell\\v1.0\\powershell.exe";

        // Build PowerShell command to permanently set JAVA_HOME and Path
        String command = String.join(" ",
                "\"" + psPath + "\"",
                "-Command",
                "\"[Environment]::SetEnvironmentVariable('JAVA_HOME', '" + selectedJDK + "', 'Machine');",
                "[Environment]::SetEnvironmentVariable('Path', '" + selectedJDK + "\\bin;' + ([Environment]::GetEnvironmentVariable('Path','Machine')), 'Machine')\""
        );

        try {
            Runtime.getRuntime().exec(command);
            JOptionPane.showMessageDialog(null,
                    "JAVA_HOME and Path updated permanently.\nPlease restart PowerShell, CMD, or your PC to apply changes.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Failed to switch Java. Make sure to run this program as Administrator.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
