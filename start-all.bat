@echo off
echo ===========================================
echo   Hmall Starter - Do NOT close this window!
echo ===========================================
set JAVA="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin\java.exe"
set OPTS=--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED

echo Starting Gateway (8080)...
start "Gateway" %JAVA% %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\hm-gateway\target\hm-gateway-1.0.0.jar
echo Gateway launched.

timeout /t 3 >nul
echo Starting Item (8081)...
start "Item" %JAVA% %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\item-service\target\item-service.jar
echo Item launched.

timeout /t 3 >nul
echo Starting User (8083)...
start "User" %JAVA% %OPTS% -jar C:\bc1\cq\sky-take-out\hmall\user-service\target\user-service.jar
echo User launched.

echo Starting Frontend (3000)...
start "Frontend" node C:\bc1\cq\sky-take-out\hmall\serve-frontend.js
echo Frontend launched.

echo.
echo ===========================================
echo Waiting 30s for services to start...
timeout /t 30 >nul

echo Testing...
curl -s http://localhost:8080/items/search?key=test >nul && echo [OK] Gateway || echo [FAIL] Gateway
curl -s http://localhost:8081/items/search?key=test >nul && echo [OK] Item || echo [FAIL] Item  
curl -s http://localhost:3000 >nul && echo [OK] Frontend || echo [FAIL] Frontend

echo.
echo ===========================================
echo   Done! Open http://localhost:3000
echo   Login: Jack / 123
echo ===========================================
pause
