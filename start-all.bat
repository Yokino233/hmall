@echo off
title Hmall

echo ============================================
echo  Hmall - Starting 6 services + frontend...
echo ============================================

cd /d C:\bc1\cq\hmall

set OPTS=--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED
set JAVA=C:\PROGRA~1\ECLIPS~1\jdk-17.0.19.10-hotspot\bin\java.exe

echo [1/7] Gateway   8080 && start "GW" "%JAVA%" %OPTS% -jar hm-gateway\target\hm-gateway-1.0.0.jar && timeout /t 6 /nobreak >/dev/null
echo [2/7] Item      8081 && start "IT" "%JAVA%" %OPTS% -jar item-service\target\item-service.jar  && timeout /t 5 /nobreak >/dev/null
echo [3/7] User      8083 && start "US" "%JAVA%" %OPTS% -jar user-service\target\user-service.jar  && timeout /t 2 /nobreak >/dev/null
echo [4/7] Cart      8082 && start "CT" "%JAVA%" %OPTS% -jar cart-service\target\cart-service.jar  && timeout /t 2 /nobreak >/dev/null
echo [5/7] Trade     8084 && start "TR" "%JAVA%" %OPTS% -jar trade-service\target\trade-service.jar  && timeout /t 2 /nobreak >/dev/null
echo [6/7] Pay       8085 && start "PY" "%JAVA%" %OPTS% -jar pay-service\target\pay-service.jar    && timeout /t 2 /nobreak >/dev/null
echo [7/7] Frontend  3000 && start "FE" node serve-frontend.js

echo.
echo ============================================
echo   Wait 45s, then http://localhost:3000
echo   Login: Jack / 123
echo ============================================
pause
