@echo off
pushd %~dp0..
java -Xmx256M -cp build\production\Treehouse;lib\* -splash:assets/images/Treehouse-Splash.png ^
    com.worldofshots.treehouse.Treehouse
popd
