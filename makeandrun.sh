#!/bin/sh

# Kinda shitty fix since for some reason runClient doesn't want to include the mod
# - build task
# - cp the mod from build/libs to run/mods
# - remove the .mixin folder from run/ (if not, runClient doesn't start at all)
# - runClient task


#./gradlew build
#rm run/mods/*
rm -r run/.mixin.out
#mv build/libs/* run/mods/
./gradlew runClient