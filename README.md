# WorldDownloaderForge
This is a port of a project made by Pokechu22 (mainainer) & other contributors, mainly uyjulian (litemod), dslake (old maintainer), nairol (old old maintainer) and countless others

This port was intended to run on Forge 1.8.9, but I ended up doing:  `1.9.4-liteloader` -> `1.9.4-forge` -> `1.8.9 forge`, so there's also a 1.9.4 branch available.
I'll maybe eventually port 1.12.2.  

# Credits
I am technically not allowed (for now) to redistribute this mod, as the license on both the `src/main/java/wdl/` & `src/main/java/com/uyjulian/LiteModWDL/` files clearly prohibit file redistribution without permission.  
However, this is already done and I've put some effort into it, and I didn't remove any original credit (except maybe the LiteModWDL mod file that stated uyjulian as the litemod author), so i'll take the gamble and say I'm good

# Compiling
(`./gradlew` -> `./gradlew.bat` on windows)
- `git clone https://github.com/Nixuge/WorldDownloaderForge`
- (optional) switch to the branch you need (1.8.9 by default)
- `./gradlew check`
- `./gradlew build`
Done, your mod should be in the `builds/libs/` folder

# Editing (tested on intellij & vscode)
(`./gradlew` -> `./gradlew.bat` on windows)
- `git clone https://github.com/Nixuge/WorldDownloaderForge`
- (optional) switch to the branch you need (1.8.9 by default)
- `./gradlew check`
- `./gradlew setupDecompWorkspace`
- `./gradlew genIntellijRuns`
Done, now you can edit the mod however you like and run `./gradlew runClient` to view your changes

# Important note: code quality
The code quality on this mod (at least at this point) is horrendous, as there's a bunch of version specific artifacts almost randomly left around.  
I had to manually (w ctrl+f) replace ~600 lines with errors to go from liteloader to forge, and another ~600 from 1.9.4 to 1.8.9 (+ manually editing features that weren't present in 1.8.9).  
### Don't except this to be bug free and/or perform well (for now).

# Known issues
- Entities won't save
- Tile Entities won't save