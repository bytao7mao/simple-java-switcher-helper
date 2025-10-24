Option 1: Run only this script (recommended)
powershell -ExecutionPolicy Bypass -File "C:\Users\tao\Desktop\SCRIPTING\java_switch.ps1"

Option 2: Change the policy for your user
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser

Option 3: Unblock just this file
Unblock-File "C:\Users\tao\Desktop\SCRIPTING\java_switch.ps1"


Option 4:
Recommended combo:
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
Unblock-File "C:\Users\tao\Desktop\SCRIPTING\java_switch.ps1"
Then just run:
.\java_switch.ps1