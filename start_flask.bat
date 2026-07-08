@echo off
cd /d "PROJECT_ROOT"
start /B "" ".venv\Scripts\python.exe" app.py > flask_out.log 2>&1
