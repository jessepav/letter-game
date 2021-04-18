@echo off

setlocal

pushd %~dp0..

java -cp build\production\letter-game;lib\* com.illcode.lettergame.LetterGame

popd

endlocal
