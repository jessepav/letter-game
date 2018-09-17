@echo off
pushd %~dp0..
del lettergame-src.7z
"c:\Program Files\7-Zip\7z.exe" a lettergame-src.7z @scripts\srclist.txt -bb ^
    -xr!.*.marks -xr!.svn -xr!*.png -xr!*.ttf -xr!*.ogg -xr!*.wav
popd
