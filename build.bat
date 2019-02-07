@echo off
call :install_tree %1

:install_tree 
    for /R %~1 %%f in (*-SNAPSHOT.jar) do (
        call :install %%f
    )
EXIT /B 0

:install
set jarFile=%~1
set pomFile=%jarFile:jar=pom%
set "cmd=mvn install:install-file -DpomFile=%pomFile% -Dfile=%jarFile% -Dpackaging=jar"
set sourcesFile=%jarFile:.jar=-sources.jar%
if exist %sourcesFile% (
   set "cmd=%cmd% -Dsources=%sourcesFile%"
)
call %cmd%
EXIT /B 0

