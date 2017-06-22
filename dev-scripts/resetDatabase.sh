#!/usr/bin/env bash
cd ..

echo "======"
echo "====== STEP 1/1: start to delete database"
echo "======"
../sbt "cmdtools/runMain com.ubirch.user.cmd.MongoDelete"
echo "======"
echo "====== STEP 1/1: finished deleting database"
echo "======"

echo "======"
echo "====== INFO: you still have to create the default contexts (see _Create Contexts_ section in README.md for details)"
echo "======"