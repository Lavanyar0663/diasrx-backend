@echo off
echo Starting MySQL in debug mode... > mysql_debug_output.txt
"C:\xampp\mysql\bin\mysqld.exe" --console >> mysql_debug_output.txt 2>&1
echo MySQL exited with code: %ERRORLEVEL% >> mysql_debug_output.txt
