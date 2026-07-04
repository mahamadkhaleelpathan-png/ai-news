@echo off
cd /d "C:\Users\Khaleel Pathan\Downloads\pace_tech-feast--main\pace_tech-feast--main"
echo %date% %time% Starting tunnel... >> tunnel_url.log
"%TEMP%\cloudflared.exe" tunnel --url http://localhost:5000 >> tunnel_url.log 2>&1
