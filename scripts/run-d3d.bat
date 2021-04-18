@echo off

setlocal

set J2D_D3D_NO_HWCHECK=true

pushd %~dp0..

java -cp build\production\letter-game;lib\* -Dsun.java2d.d3d=true com.illcode.lettergame.LetterGame

popd

endlocal
