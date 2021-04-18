@echo off

setlocal

pushd %~dp0..

java -cp build\production\letter-game;lib\* -Dsun.java2d.opengl=true com.illcode.lettergame.LetterGame

popd

endlocal
