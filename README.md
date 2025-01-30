# Deutsche Survival Quest

"Deutsche Survival Quest" is created by java and is inspired by *The Maze Runner*. It is created by using libGDX framework.
The main goal of the game is to collect the key and finding the exit to enter the next level. The game is successfully completed when all of the levels
are completed without loosing all of the lives. 

"Deutsche Survival Quest" depicts the story of an international student who has arrived to Germany for his studies. He
first lands to Stuttgart Airport, claims his luggage to get to his train. Our player needs to find his *Deutschlandticket* in order to get on
his train and safely arrive in Heilbronn. He needs to be careful, as there are strict ticket control personal in alert and also a lot of objects that are dangerous for his health.
After a long journey to Heilbronn, our player needs to relax, hence he visits the *Altstadt* to see the city and get his house keys.When he is already there, he decides to head to a *Brauerei* have to have a couple of drinks.
But would the player keep an eye on his new keys? 

When our player finally catches his breath and rest a bit, he needs to go down town to register himself in the Rathaus. 
However, he completely forgot that traffic and order in Germany is quite different from his hometown. Would it bee a smooth journey to navigate through the city?
When our player successfully navigates through the city and finds the Rathaus, he needs to register himself to finally start his studies!!! 

## Table of Contents 

* Code Structure
* Directory Structure
* UML Diagram
* Instructions
* Features
* Credits


### Code Structure
```
MazeRunnerGame
├── Base (Abstract Superclasses and General Game Objects)
│   ├── GameObject (Base class for all objects in the game)
│   ├── StaticObject (Extends GameObject, represents non-moving objects like keys, collectibles, traps, and portals)
│   └── Character (Extends GameObject, represents moving entities like Player and chasing enemies)
│
├── Game Objects (Specific Game Objects and Collectibles)
│   ├── BFSChasingEnemy (Specialized path finding Breadth-First Search algorithm that extends Chasing Enemy)
│   ├── ChasingEnemy (Extends Character, AI-driven enemy)
│   ├── Collectibles (Base class for items that can be collected)
│   ├── Key (Extends Collectibles, used to unlock exits)
│   ├── Player (Extends Character, represents the player-controlled entity)
│   ├── Portal (Teleports the player to the entrance)
│   └── Trap (Causes the player to lose a life upon collision)
│
├── Level (Tile System)
│   └── LevelManager (Manages tile and level properties and interactions)
│
├── Rendering (Graphics and Effects)
│   ├── ElementRenderer (Responsible for rendering game objects)
│   ├── Panel (Manages UI components like pause and victory panels)
│   ├── ResizeableTable (Manages the ratios of the content within the Ppanels (on tables))
│   ├── SpeechBubble (Displays dialogues or reactions of characters)
│   └── SpotlightEffect (Creates a visual effect for lighting and adds shadows on everything else)
│
├── Screens (Game Screens for Navigation)
│   ├── GameOverScreen (Shown after the player loses all their lives)
│   ├── GameScreen (Main gameplay screen)
│   ├── MenuScreen (Initial menu interface)
│   ├── OptionsScreen (Allows audio adjustments)
│   ├── SelectLevelScreen (Allows level selection)
│   └── VictoryScreen (Displays if the game is succsessfully completed)
│
├── Tiles (Special Tile Types in the Maze)
│   ├── Entrance (Starting point for the player)
│   ├── Exit (Goal for the player to reach)
│   ├── SpeedBoost (Tile that increases player speed temporarily)
│   ├── Tile (Base class for all tiles)
│   ├── TileType (Enum that holds all the tile types)
│   └── Wall (Non-passable tile)
│
├── Utility (Helpers and Constants)
│   ├── Constants (Holds game-wide constants like screen size and tile size)
│   └── Position (Handles x, y coordinates and movement logic)
│
├── MazeRunnerGame (Includes all of the imported media)
│
└── SoundManager (Manages Sounds)

```

## UML Diagram
`<put our UML-Diagramm here>`


## Instructions
**How to run the game**?
* For Windows/Linux:
Run game on the upper right hand side should be clicked, and then edit configurations should be selected. Afterward,
*Build and Run* should be found, then  *-XstartOnFirstThread* should be removed from VM options.


* For Mac:
Run game on the upper right hand side should be clicked, and then edit configurations should be selected. Afterward,
*Build and Run* should be found on the newly opened window and *Modify Options*, which is aligned to the right side of 
*Build and Run* should be clicked. Then, *Add VM Options* should be selected and the text *-XstartOnFirstThread* should 
* be typed to the newly opened text field. When these steps are completed, the game ready to run. Have fun!


**How to Play**:

* Select "How to Play" button to have a tutorial session, where the game mechanics are introduced.

* Select "Start Game" button  continue with Level 1.

* Select "Options" button to adjust the game audio.

* Select "Exit Game" button to quit the game.

* Character Movement: Press "WASD" or Arrow Keys to move the character.

* Press *Shift* to speed up.

* To Pause the game, *ESCAPE* key must be pressed and to resume, the *ENTER*, and *SPACE* keys, or the *Resume* button on pause window should be pressed.
* When game is "paused", *ESCAPE* key is pressed, an additional in-game menu, which has the similar functionality to the main menu.

## Features

### Overview
- For our features, we incorporated both static and dynamic obstacles that are unique to each level. For instance, to fit the story
line, we have ticket controllers as our dynamic obstacles. Any collision with the obstacles would result in a decrease in health level,
that could ultimately lead to losing in the game if all 5 hearts are used up.

- Aside from that, we also have a tutorial page with an animated and interactive display on the rules of the game. This would allow user to
quickly learn how to play the game. The spotlight effect we employed makes the tutorial easier to follow as it brings the attention of the
user to the specific variable.

- The user also have control over the other interfaces in the game such as sounds and music, with the choice to adjust the volume based on their liking
the slider, and an option to completely mute them. They are also able to choose which level they would want to skip to using the "Select Level" function
in the menu and pause screen.

- For our surprise element, we have a portal that teleports the user back to the starting point. In order for the user
to finish the game quick, he must avoid the portal. There are also traps every level such as glass bottles that would lower the health of the user.


###  Detailed

**View**:
* View Point :Deutsche Survival Quest is a 2D game with a third person view.
* clamp zooming

**HUD**:
- Number of hearts displays remaining lives,
- On the top right, it indicates amount of coins collected and whether key has been collected
- speech bubbles from our dynamic enemies 

**Point System**:
In Deutsche Survival Quest, there are total of *5* coins that are randomly generated on each map.
The player gets a score form A to F in the end of each successfully completed level, based on amount of coins collected. 
Whether they progress on to the next level, however, is independent on the points they earn but rather dependent on the
the collection of keys and finding the exit.


**Player's Movement**:
* Our player's can move smoothly towards four directions and diagonally

* It can also speed up but only for a limited amount of time when the shift 
button is pressed

* Stamina: Our player can run 10 seconds, which can be tracked by the stamina wheel. The amount of time doubles with 
collecting boost-up potion

**Collision**:
Deutsche Survival Quest offers pixel perfect collision with objects


**Collectables**:

* Lives: *hearts*, *pretzels*, and  *health insurance card*, provides the player additional lives when collected.

* Boost-ups: power potion (adds stamina).

* Points: amount of *coins* collected determines the score.

**Exit to Next Level with the Key**:

* Deutsche Survival Quest offers unique key designs at each level, which align with the map's theme and conceptually ties
levels that come after each other.

* Level 1: Player must claim his luggage and can earn bonus points on the way.

* Level 2: Player must get his Deutschlandticket to catch his train.

* Level 3: Player must claim his house keys

* Level 4: Player must find the key to exit bar.

* Level 5: Player must get his documents and the rathaus to register in the Rathaus.

* Level 6: Player must find the room for his appointment 

**Obstacles**:
* Traps: damages the player by 1 hear

* Very *intelligent* Chasing enemies (dynamic object): They detect the player when the player enters the detection radius, and starts
chasing player until the player either gets out from the detections radius or the cooldown period is reached. Damages the player
by one heart.

* Portal: When collision between the player and portal occurs, the player is sent back to the location of the entrance. 

**Communication and Information**:

* Panels:

  * Introduction Panel: brief explanation of game/levels structure
  * Pause Panel: Allows to go back to the main menu, 
  * Options Panel: Allows to adjust the game's music and sound effects 
  * Victory Panel: Displays if the level is successfully completed and the score of the player.

* Speech Bubble:

  * Chasing enemies interact with the player through the speech bubbles. It either shows when the player is within the
  detection radius of the chasing enemy, or displays level specific chasing enemy text

**Screens**: 

* Game Screen: holds the game logic 
* Menu Screen: is the main menu of the game. It allows to access the tutorial, start game, select levels, options, and 
quit game
* Select Level Screen : allows to play a specific level, with the selection of "Back" button, it allows return to
the screen or panel it was accessed.
* Game Over Screen: only gets displayed when the player losses all his lives.
* Victory Screen: only gets displayed when the game is successfully completed, when Level 6 key is collected and 
exit is reached


**Spotlight Effect**:

* Deutsche Survival Quest uses spotlight effect during the tutorial to introduce the game features.

**Custom util class: Position**:
Contributes converting the units

**Sound Manager Class**:
Allows to better handle sound, as they do not have the same methods with the Music class.

### Tutorial

* Tutorial can be accessed through *How to Play* on the main menu screen. The tutorial is a smaller map based on 
the Level 1 map. Here, the main game features and instructions are introduced. The tutorial is also interactive,
enabling users to play the game but with more guidance.


## Credits
### Tilesets
The tile sets of Deutsche Survival Quest has been created by modifying the following:

- Tileset 1: **Airport Game Sprites** by Konig Games. 
  Available at: https://akonig513.itch.io/free-airport-sprite-pack. 
 Used with permission under the "name your own price" model.
- Tileset 2: **Train Station Tileset** by Ekat99. <br>
  This tileset was created as part of an art challenge and is publicly available for non-commercial projects. 
  Available at: [DeviantArt](https://www.deviantart.com/ekat99/art/Train-Station-876100255).
- Tileset 4: **Bar Tileset** by aveontrainer <br>
  This tileset is publicly available, with ongoing updates provided by the author.
  Available at: [DeviantArt](https://www.deviantart.com/aveontrainer/art/Bar-Tileset-829643285)
- Tileset 5 : **City Tileset** by KabisCube. <br>
  '3x3 Minimal' Sprite sheet created by KabisCube under Public Domain license.
  Available at: https://opengameart.org/content/3x3-minimal.
  License Type: CC0 1.0 Universal https://creativecommons.org/publicdomain/zero/1.0/.
  Key: created by djvstock available at <a href="https://www.vecteezy.com/vector-art/10968271-paper-sheet-pixel">paper sheet pixel Vectors by Vecteezy</a>. 

### Keys
- Luggage (Level 1) created by Bekzod Shoyakubov.
  Available at https://www.vecteezy.com/vector-art/5146448-suitcase-pixel-art.
- Document (Level 5) created by djvstock (Diana Johanna Velasquez).
  Available at https://www.vecteezy.com/vector-art/10968271-paper-sheet-pixel.

### Obstacles 
- Trash can created by djvstock (Diana Johanna Velasquez).
  Available at https://www.vecteezy.com/vector-art/10968225-trash-can-pixel.
- Trash bag created by amandalamsyah (Kwee Amanda Alamsyah).
  Available at https://www.vecteezy.com/vector-art/39841555-black-plastic-trash-bag-or-garbage-junk-container-with-red-rubber-ties-pixel-bit-retro-game-styled-vector-illustration-drawing-simple-flat-cartoon-drawing.
- Beer bottle create by collaborapix (Collaborapix Studio).
  Available at https://www.vecteezy.com/vector-art/32858114-pixel-art-illustration-beer-bottle-pixelated-beer-beer-bottle-icon-pixelated-for-the-pixel-art-game-and-icon-for-website-and-video-game-old-school-retro.


### Backgrounds:
* Game Over Background created by Dionysus.
  Available at https://www.stockvault.net/photo/284807/dark-red-stains-on-crimson-background-watercolor-effect.
* Houses tiles from Victory Background by Risa Athene

### Music
- Background Music by Bruno Belotti (Submitted by qubodup).
  Available at https://opengameart.org/content/cheerful-3-nel-giardino-dello-zar-polka-loop.
  Licence type: Attribution-ShareAlike 3.0 Unported  https://creativecommons.org/licenses/by-sa/3.0/.
- Pause Music 
  Available at https://opengameart.org/sites/default/files/A%20cup%20of%20tea_0.mp3
- Game Over Music by Cleyton R. Xavier
- Victory Music by spuispuin
  Available at https://opengameart.org/content/won-orchestral-winning-jingle.
 

### Sound Effects
- Sound Effect by <a href="https://pixabay.com/users/freesound_community-46691455/?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=14658">freesound_community</a> from <a href="https://pixabay.com/sound-effects//?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=14658">Pixabay</a>
- Sound Effect by <a href="https://pixabay.com/users/666herohero-25759907/?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=131479">666HeroHero</a> from <a href="https://pixabay.com/sound-effects//?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=131479">Pixabay</a>
- Sound Effect by <a href="https://pixabay.com/users/hasin2004-46173687/?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=247449">Hasin Amanda</a> from <a href="https://pixabay.com/sound-effects//?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=247449">Pixabay</a>
- Sound Effect Teleport by Ogrebane.
  Available at: https://opengameart.org/content/teleport-spell.
  licence type: CC0 1.0 Universal https://creativecommons.org/publicdomain/zero/1.0/.
- Sound Effect Warning by Jerimee.
  Available at: https://opengameart.org/content/warning-noise.
  license type: Attribution 3.0 Unported https://creativecommons.org/licenses/by/3.0/.
- Sound Effect Damage by VoiceBosch.
  Available at https://opengameart.org/content/damage-sounds-male-audio-pack.
  License type: Attribution-ShareAlike 4.0 International https://creativecommons.org/licenses/by-sa/4.0/.
- sound Effect Key by ViRiX (David McKee), (Submitted by Anonymous).
  Available at https://opengameart.org/content/ui-accept-or-forward.
  License type: Attribution 3.0 Unported https://creativecommons.org/licenses/by/3.0/.
- Sound Effect Running
- Sound Effect Victory by Matthew Pablo.
  Available at https://opengameart.org/content/lively-meadow-victory-fanfare-and-song.
  License type: Attribution 3.0 Unported https://creativecommons.org/licenses/by/3.0/.

### Fonts
- **Mainz Fraktur** by _Peter Wiegel_<br>
  A Fraktur-style font released in 2010 under the Open Font License (OFL).
  Available at: https://www.fontasy.de/font-937-mainzerfraktur.php?lang=de<br>
  License: Open Font License (OFL)

## Contact
- [berin.bueyuekboduk@tum.de](mailto:berin.bueyuekboduk@tum.de)
- [jenchien.hou@tum.de](mailto:jenchien.hou@tum.de)
- [go36yev@mytum.de](mailto:go36yev@mytum.de)
