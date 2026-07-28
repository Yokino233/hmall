@echo off
title Hmall Starter

set JAVA=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\java.exe
set OPTS=--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED
set BASE=C:\bc1\cq\sky-take-out\hmall

echo ===========================================
echo   Hmall - 7 services launching...
echo ===========================================

echo Gateway... && start "GW" cmd /c ""%JAVA%" %OPTS% -jar "%BASE%\hm-gateway\target\hm-gateway-1.0.0.jar" && pause"
timeout /t 4 /nobreak >nul

echo Item... && start "IT" cmd /c ""%JAVA%" %OPTS% -jar "%BASE%\item-service\target\item-service.jar" && pause"
timeout /t 3 /nobreak >nul

echo Cart... && start "CT" cmd /c ""%JAVA%" %OPTS% -jar "%BASE%\cart-service\target\cart-service.jar" && pause"
timeout /t 3 /nobreak >nul

echo User... && start "US" cmd /c ""%JAVA%" %OPTS% -jar "%BASE%\user-service\target\user-service.jar" && pause"
timeout /t 3 /nobreak >nul

echo Trade... && start "TR" cmd /c ""%JAVA%" %OPTS% -jar "%BASE%\trade-service\target\trade-service.jar" && pause"
timeout /t 3 /nobreak >nul

echo Pay... && start "PY" cmd /c ""%JAVA%" %OPTS% -jar "%BASE%\pay-service\target\pay-service.jar" && pause"
timeout /t 3 /nobreak >nul

echo Frontend... && start "FE" cmd /c "node "%BASE%\serve-frontend.js" && pause"

echo.
echo ===========================================
echo   All 7 launched. Wait 40s.
echo   Open: http://localhost:3000
echo   Login: Jack / 123
echo ===========================================
pause
