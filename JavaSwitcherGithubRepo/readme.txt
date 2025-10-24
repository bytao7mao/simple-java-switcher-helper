Summary of How It Works
Step	What Happens
1. Start program	main() launches the GUI creation safely on the Swing thread.
2. Build GUI	A window with a dropdown and a “Switch” button appears.
3. Detect JDKs	The program scans C:\Program Files\Java and C:\Program Files (x86)\Java for folders containing bin\java.exe.
4. Choose version	User selects one JDK path from the dropdown.
5. Switch permanently	A PowerShell command updates JAVA_HOME and Path for the entire system.
6. Confirmation	A popup tells you to restart your terminals or system to apply the change.