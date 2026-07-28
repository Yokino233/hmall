@echo off
title Hmall - All Services
set JAVA="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\java"
set OPTS=--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED

echo Starting all services...

start "Gateway" cmd /c "%JAVA% %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\hm-gateway\target\hm-gateway-1.0.0.jar && pause"
timeout /t 3 >nul

start "Item" cmd /c "%JAVA% %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\item-service\target\item-service.jar && pause"
timeout /t 3 >nul

start "Cart" cmd /c "%JAVA% %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\cart-service\target\cart-service.jar && pause"
timeout /t 3 >nul

start "User" cmd /c "%JAVA% %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\user-service\target\user-service.jar && pause"
timeout /t 3 >nul

start "Trade" cmd /c "%JAVA% %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\trade-service\target\trade-service-v2.jar && pause"
timeout /t 3 >nul

start "Pay" cmd /c "%JAVA% %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\pay-service\target\pay-service-v2.jar && pause"
timeout /t 3 >nul

start "Frontend" cmd /c "node C:\bc1\cq\sky-take-out\hmall\serve-frontend.js && pause"

echo.
echo All 7 launched. Each in its own window.
echo If a window closes instantly, that service failed.
echo Wait 40 seconds, then open http://localhost:3000
echo Login: Jack / 123
pause
