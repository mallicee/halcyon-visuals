@echo off
rem Запуск Minecraft 1.21.1 в режиме разработки Fabric с модом Halcyon (для проверки мода).
rem Нужен JDK 21: установите его или укажите путь в JAVA_HOME.
rem Первый запуск скачивает игру и ресурсы и может занять несколько минут.
if "%JAVA_HOME%"=="" (
  where java >nul 2>nul || (
    echo Не найден JDK 21. Установите Java 21 ^(например, Eclipse Temurin^) или задайте JAVA_HOME.
    pause
    exit /b 1
  )
)
cd /d "%~dp0"
call gradlew.bat --no-daemon runClient
pause
