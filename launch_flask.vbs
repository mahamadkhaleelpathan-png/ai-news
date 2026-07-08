Set WshShell = CreateObject("WScript.Shell")
WshShell.Run chr(34) & "PROJECT_ROOT\.venv\Scripts\python.exe" & chr(34) & " " & chr(34) & "PROJECT_ROOT\app.py" & chr(34), 0, False
