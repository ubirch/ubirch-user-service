#!/bin/sh

./sbt modelRest/publishSigned
./sbt modelRest/sonatypeRelease

./sbt modelDb/publishSigned
./sbt modelDb/sonatypeRelease

./sbt modelDbTools/publishSigned
./sbt modelDbTools/sonatypeRelease
