@echo off
echo Creating WAR file structure...

:: Check if Tomcat is installed in the default location
if not exist "C:\Program Files\Apache Software Foundation\Tomcat 9.0\lib\servlet-api.jar" (
    echo Error: servlet-api.jar not found!
    echo Please make sure Tomcat 9.0 is installed in the default location
    echo or update the path in this script.
    pause
    exit /b 1
)

:: Create directories
if exist WEB-INF rmdir /s /q WEB-INF
mkdir WEB-INF
mkdir WEB-INF\classes
mkdir WEB-INF\classes\com
mkdir WEB-INF\classes\com\test

:: Copy files to their correct locations
copy /Y index.html .
copy /Y src\main\webapp\WEB-INF\web.xml WEB-INF\
copy /Y src\main\java\com\test\TestServlet.java WEB-INF\classes\com\test\

:: Compile the servlet for Java 8 compatibility
echo Compiling servlet...
javac -cp "C:\Program Files\Apache Software Foundation\Tomcat 9.0\lib\servlet-api.jar" -source 1.8 -target 1.8 WEB-INF\classes\com\test\TestServlet.java

if errorlevel 1 (
    echo Error compiling servlet!
    pause
    exit /b 1
)

:: Create the WAR file
echo Creating WAR file...
jar -cf TestApp.war index.html WEB-INF

if errorlevel 1 (
    echo Error creating WAR file!
    pause
    exit /b 1
)

echo Done! TestApp.war has been created.
echo You can now copy TestApp.war to your Tomcat webapps directory.
pause 