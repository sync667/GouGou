GouGou Server
======

Server for GouGou.


#Starting server

##Windows
1. As a prerequisite, install Java 7 or above if you don't have it already (from Java.com).
2. Download the latest build 
3. Place the file in a new directory dedicated to GouGouServer.
4. Create a new startup script (start.bat) to launch the the JAR:
```
@echo off

java -Xms512M -Xmx1536M -XX:MaxPermSize=128M -jar GouGouServer.jar
pause
```
5. Double click the batch file.

##Linux
1. As a prerequisite, install Java 7 or above if you don't have it already [Ubuntu https://help.ubuntu.com/community/Java, CentOS http://stackoverflow.com/a/20901970].
2. Download the latest build 
3. Place the file in a new directory dedicated to GouGouServer.
4. Create a new startup script (start.sh) to launch the the JAR:
```
#!/bin/sh

java -Xms512M -Xmx1536M -XX:MaxPermSize=128M -jar GouGouServer.jar
```
5. Run your start up script:
```
chmod +x start.sh
./start.sh
```
