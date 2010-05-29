#!/bin/sh

cd ~/Desktop
rm -rf pac
mkdir pac
cd pac
curl -O http://lamp.epfl.ch/~rytz/pac/pacman_2.8.0.RC2-1.0.jar
curl -O http://lamp.epfl.ch/~rytz/pac/scala-library.jar
curl -O http://lamp.epfl.ch/~rytz/pac/scala-compiler.jar
curl -O http://lamp.epfl.ch/~rytz/pac/scala-swing-2.8.0.RC2.jar
java -cp pacman_2.8.0.RC2-1.0.jar:scala-library.jar:scala-compiler.jar:scala-swing-2.8.0.RC2.jar epfl.pacman.Main
