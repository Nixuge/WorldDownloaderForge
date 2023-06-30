# WorldDownloaderForge
This is a port of a project made by Pokechu22 (mainainer) & other contributors, mainly uyjulian (litemod), dslake (old maintainer), nairol (old old maintainer) and countless others

This port was intended to run on Forge 1.8.9, but I ended up doing:  `1.9.4-liteloader` -> `1.9.4-forge` -> `1.8.9 forge`, so there's also a 1.9.4 branch available.
I'll maybe eventually port 1.12.2.  

# Credits/License
Since this mod is a fork, there are 2 licenses in this repo:
- LICENSE is the original license and the one that's present in most files. **As the less permissive license, it's the one you have to respect if you want to redistribute this mod in its entirity**
- LICENSE (Nixuge) is the license used for the files I added.

You can find which license a file is under by looking at its header. If nothing specified, it's under LICENSE (Nixuge). Otherwise, it's under LICENSE.
  
I've got permission from Pokechu to make this fork
![Permission-fork](https://user-images.githubusercontent.com/33488576/227806844-d7843197-e7bc-4699-aba1-8bc62803da31.png)

And for the modrinth upload
![Permission-modrinth](https://github.com/Nixuge/WorldDownloaderForge/assets/33488576/22911b83-63f1-4655-9711-5623d15f42e1)

# Compiling
(`./gradlew` -> `./gradlew.bat` on windows)
- `git clone https://github.com/Nixuge/WorldDownloaderForge`
- (optional) switch to the branch you need (1.8.9 by default)
- `./gradlew check`
- `./gradlew build`


Done, your mod should
 be in the `builds/libs/` folder

# Editing (tested on intellij & vscode)
(`./gradlew` -> `./gradlew.bat` on windows)
- `git clone https://github.com/Nixuge/WorldDownloaderForge`
- (optional) switch to the branch you need (1.8.9 by default)
- `./gradlew check`
- `./gradlew setupDecompWorkspace`
- `./gradlew genIntellijRuns`

Done, now you can edit the mod however you like and run `./gradlew runClient` to view your changes

# Important note: code quality
The code quality on this mod (at least at this point) isn't the best, as there's a bunch of version specific artifacts randomly left around.  
I had to manually (w ctrl+f) replace ~600 lines with errors to go from liteloader to forge, and another ~600 from 1.9.4 to 1.8.9 (+ manually editing features that weren't present in 1.8.9).  
### Don't except this to be bug free for now. It's definitively usable (more than the liteloader version if you have the same usage as me), but not perfect yet.

# Known issues
- Villagers' data isn't saved at all
- NPCs aren't saved, don't know yet if it's possible to fix but will see later
- Some crashes when clicking on some buttons (about->copy info button)
- Outer chunks from previous maps are still present when saving over a folder,
- - TODO: remove the folder's chunks (& eventually other data) to fix that
- Localization isn't working properly
- "Crashlog" on launch, see next known issue
- Crashlog maker crashes (lol)
- Updater broken, need to make it use Nixuge/WorldDownloaderForge properly

# Todo
- Better UI (If possible notifications-based like some clients)
- More settings (eg. freeze entities)
- Better name parsing (to implement)
- Some kind of way to script the name of the map to save
- Some kind of script on map save
- Command Support (would be nice w Notifications)
- As this version of the mod will mostly be used for server archive, make the Saved Chunks map display chunks currently being saved instead of already saved ones instead of having it display already saved chunks from Singleplayer
- IF I keep the old UI system, have a "dropdown list" for choice buttons instead of it just rolling choices one by one when you click on the button
- Add back tests
- See src/main/java/wdl/functions/README.md
- Fix issues with liquidbounce's buttons
- Def redo the menu system
