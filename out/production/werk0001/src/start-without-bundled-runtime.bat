@echo off
cls
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
pause
