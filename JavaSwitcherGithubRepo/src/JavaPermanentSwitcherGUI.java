// Importing necessary Swing and AWT classes for GUI components
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JavaPermanentSwitcherGUI
 * ------------------------
 * A simple GUI program that allows users to switch their system's active Java version permanently.
 * It works by detecting installed JDKs and letting the user set one as the default
 * (via JAVA_HOME and Path environment variables).
 *
 * Compatible with older Java versions (no lambdas or streams).
 */
public class JavaPermanentSwitcherGUI {

    // JComboBox: Dropdown menu to list all detected JDKs
    private static JComboBox<String> jdkComboBox;

    // List to hold all detected JDK directories
    private static List<File> jdks;

    public static void main(String[] args) {
        // Run GUI creation code on the Event Dispatch Thread
        // This is best practice for Swing applications — prevents threading issues.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(); // Build and display the window
            }
        });
    }

    /**
     * Builds and displays the main window (GUI) for the Java Switcher.
     */
    private static void createAndShowGUI() {
        // Create the main application window
        JFrame frame = new JFrame("Java Permanent Switcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit program on close
        frame.setSize(600, 250); // Window dimensions (width, height)
        frame.setLayout(new BorderLayout(10, 10)); // Layout manager with padding

        // Detect installed JDKs and prepare their paths for display
        jdks = detectJDKs(); // Detects all Java installations
        String[] jdkNames = new String[jdks.size()];
        for (int i = 0; i < jdks.size(); i++) {
            jdkNames[i] = jdks.get(i).getAbsolutePath(); // Convert File → String
        }

        // Create dropdown with the detected JDK paths
        jdkComboBox = new JComboBox<String>(jdkNames);
        frame.add(jdkComboBox, BorderLayout.CENTER); // Add dropdown to the center of the window

        // Create button to apply the selected Java version
        JButton applyButton = new JButton("Switch Permanently");

        // Add button listener — executes when user clicks "Switch Permanently"
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switchJava(); // Perform the switch logic
            }
        });

        // Add the button at the bottom of the window
        frame.add(applyButton, BorderLayout.SOUTH);

        // Center window on the screen
        frame.setLocationRelativeTo(null);

        // Make the window visible
        frame.setVisible(true);
    }

    /**
     * Scans common installation directories for Java JDKs.
     *
     * @return a List of File objects, each representing a detected JDK directory.
     */
    private static List<File> detectJDKs() {
        List<File> list = new ArrayList<File>();

        // Common JDK installation directories on Windows
        File[] roots = new File[]{
                new File("C:\\Program Files\\Java"),
                new File("C:\\Program Files (x86)\\Java")
        };

        // Loop through both possible locations
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];

            // Check if directory exists (to avoid null errors)
            if (root.exists() && root.isDirectory()) {
                File[] children = root.listFiles(); // List subfolders (e.g., jdk-17, jre1.8, etc.)
                if (children != null) {
                    for (int j = 0; j < children.length; j++) {
                        File f = children[j];

                        // Check if "bin/java.exe" exists inside the folder (proves it's a real JDK)
                        File javaExe = new File(f, "bin\\java.exe");
                        if (javaExe.exists()) {
                            list.add(f); // Add folder to the JDK list
                        }
                    }
                }
            }
        }

        return list; // Return detected JDKs
    }

    /**
     * Switches the system's Java version permanently by updating JAVA_HOME and Path
     * using a PowerShell command executed via Runtime.
     */
    private static void switchJava() {
        // Get which JDK the user selected
        int selectedIndex = jdkComboBox.getSelectedIndex();

        // If no JDK is selected, do nothing
        if (selectedIndex < 0 || selectedIndex >= jdks.size()) {
            return;
        }

        // Get full path to selected JDK
        String selectedJDK = jdks.get(selectedIndex).getAbsolutePath();

        // Find PowerShell executable path (system variable)
        String psPath = System.getenv("WINDIR") + "\\System32\\WindowsPowerShell\\v1.0\\powershell.exe";

        // Build PowerShell command to:
        // 1. Set JAVA_HOME to the chosen JDK
        // 2. Add its bin folder to system PATH
        String command = "\"" + psPath + "\" " +
                "-Command " +
                "\"[Environment]::SetEnvironmentVariable('JAVA_HOME', '" + selectedJDK + "', 'Machine'); " +
                "[Environment]::SetEnvironmentVariable('Path', '" + selectedJDK + "\\bin;' + " +
                "([Environment]::GetEnvironmentVariable('Path','Machine')), 'Machine')\"";

        try {
            // Execute PowerShell command
            Runtime.getRuntime().exec(command);

            // Inform user of success
            JOptionPane.showMessageDialog(null,
                    "JAVA_HOME and Path updated permanently.\n" +
                            "Please restart PowerShell, CMD, or your PC to apply changes.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            // If the process fails (e.g., no admin rights)
            JOptionPane.showMessageDialog(null,
                    "Failed to switch Java. Make sure to run this program as Administrator.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
