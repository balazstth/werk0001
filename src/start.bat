@echo off
cls
set JAVA_HOME=jdk-14+36-jre-hotspot
set GROOVY_HOME=groovy-3.0.2
set PATH=%JAVA_HOME%\bin;%GROOVY_HOME%\bin;%PATH%
echo ===========================================================================
echo EDITOR
echo ===========================================================================
echo Engine.JAVA_HOME  : %JAVA_HOME%
echo Engine.GROOVY_HOME: %GROOVY_HOME%
echo Engine.PATH:
echo %PATH%
echo ---------------------------------------------------------------------------
echo Engine.Java runtime:
java -version
echo ---------------------------------------------------------------------------
echo Engine.Run: Editor.groovy
echo Loading and running...
groovy Editor
echo ---------------------------------------------------------------------------
echo Done.
echo ===========================================================================
