@echo off
title Hmall Starter
set JAVA=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\java
set OPTS=--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED

echo Starting services...
echo ========================================

echo Gateway...
start "GW" "%JAVA%" %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\hm-gateway\target\hm-gateway-1.0.0.jar
ping -n 5 127.0.0.1 >nul

echo Item...
start "Item" "%JAVA%" %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\item-service\target\item-service.jar
ping -n 4 127.0.0.1 >nul

echo Cart...
start "Cart" "%JAVA%" %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\cart-service\target\cart-service.jar
ping -n 4 127.0.0.1 >nul

echo User...
start "User" "%JAVA%" %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\user-service\target\user-service.jar
ping -n 4 127.0.0.1 >nul

echo Trade...
start "Trade" "%JAVA%" %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\trade-service\target\trade-service.jar
ping -n 4 127.0.0.1 >nul

echo Pay...
start "Pay" "%JAVA%" %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\pay-service\target\pay-service.jar
ping -n 4 127.0.0.1 >nul

echo Frontend...
start "FE" node C:\bc1\cq\sky-take-out\hmall\serve-frontend.js

echo ========================================
echo All launched, waiting 35 seconds...
ping -n 35 127.0.0.1 >nul

echo.
echo Testing...
curl -s -o NUL http://localhost:8080/ && echo [PASS] Gateway || echo [FAIL] Gateway
curl -s -o NUL http://localhost:8081/ && echo [PASS] Item || echo [FAIL] Item
curl -s -o NUL http://localhost:3000/ && echo [PASS] Frontend || echo [FAIL] Frontend

echo.
echo ========================================
echo Open http://localhost:3000
echo Login: Jack / 123
echo ========================================
pause
