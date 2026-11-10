@echo off
rem ============================================================
rem  start-backend.cmd — «кнопка» запуска БЭКЕНДА
rem
rem  Что делает:
rem   1) переходит в папку проекта cafe-backend (%~dp0 = папка, где лежит этот файл);
rem   2) настраивает JAVA_HOME на твой JDK 25 (без этого Maven не найдёт Java);
rem   3) запускает Maven: spring-boot:run  (собирает и стартует сервер).
rem
rem  Запуск:  двойной клик по файлу  ИЛИ  в терминале набрать:  start-backend
rem
rem  Когда увидишь "Started CafeBackendApplication" — бэкенд работает
rem  на http://localhost:8080
rem ============================================================
cd /d "%~dp0cafe-backend"
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call "%~dp0_tools\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
pause