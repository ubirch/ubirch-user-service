#!/usr/bin/env bash

cd ..

echo "======"
echo "====== STEP 1/1: init admin users"
echo "======"
./sbt "cmdtools/runMain com.ubirch.user.cmd.InitUsers"
echo "======"
echo "====== STEP 1/1: finished to init admin users"
echo "======"
