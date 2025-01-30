# Deutsche Survival Quest

"Deutsche Survival Quest" is created by java and is inspired by *The Maze Runner*. It is created by using libGDX framework.
The main Purpose is to collect the key and finding the exit to the next level. Game is successfully completed when all of the levels
are completed without loosing all of the lives. 

"Deutsche Survival Quest" depicts the story of an international student who has arrived to Germany for his studies. He
first lands to Stuttgart Airport, claims his luggage to get to his train. Our player needs to find his *Deutschlandticket* in order to get on
his train and safely arrive in Heilbronn. He needs to be careful, as there are strict ticket control personal in alert and also a lot of objects that are dangerous for his health.
After a long journey to Heilbronn, our player needs to relax, hence he visits the *Altstadt* to see the city and then a *Brauerei* have to have a couple of drinks. When our player finally catches his breath and rest a bit, he needs to go down town to
register himself in the Rathaus. However, he completely forgot that traffic and order in Germany is quite different from his hometown. Would it bee a smooth journey to navigate through the city?
When our player successfully navigates through the city and finds the Rathaus, he needs to register himself to seamlessly start his studies!!! 

## Table of Contents 

* Deutsche Survival Quest (introduction)
* Code Structure
* directory Structure
* 


### Code Structure
```
MazeRunnerGame
├── Base (Abstract Superclasses and General Game Objects)
│   ├── GameObject (Base class for all objects in the game)
│   ├── StaticObject (Extends GameObject, represents non-moving objects like keys, collectibles, traps, and portals)
│   └── Character (Extends GameObject, represents moving entities like Player and chasing enemies)
│
├── Game Objects (Specific Game Objects and Collectibles)
│   ├── Player (Extends Character, represents the player-controlled entity)
│   ├── ChasingEnemy (Extends Character, AI-driven enemy)
│   ├── Key (Extends Collectibles, used to unlock exits)
│   ├── Collectibles (Base class for items that can be collected)
│   ├── Portal (Teleports the player to the entrance)
│   └── Trap (Causes the player to lose a life upon collision)
│
├── Level (Tile System)
│   └── Tiles (Manages tile properties and interactions)
│
├── Rendering (Graphics and Effects)
│   ├── ElementRenderer (Responsible for rendering game objects)
│   ├── Panel (Manages UI components like pause and victory panels)
│   ├── SpeechBubble (Displays dialogues or reactions)
│   └── SpotlightEffect (Creates a visual effect for lighting)
│
├── Screens (Game Screens for Navigation)
│   ├── GameOverScreen (Shown after the player loses all their lives)
│   ├── GameScreen (Main gameplay screen)
│   ├── MenuScreen (Initial menu interface)
│   └── SelectLevelScreen (Allows level selection)
│
├── Tiles (Special Tile Types in the Maze)
│   ├── Tile (Base class for all tiles)
│   ├── Wall (Non-passable tile)
│   ├── Entrance (Starting point for the player)
│   ├── Exit (Goal for the player to reach)
│   └── SpeedBoost (Tile that increases player speed temporarily)
│
├── Utility (Helpers and Constants)
│   ├── Constants (Holds game-wide constants like screen size and tile size)
│   └── Position (Handles x, y coordinates and movement logic)

```

#### Directory Structure
```
src/de/tum/cit/fop/maze/
├── base/
│   ├── Character.java
│   ├── GameObject.java
│   └── StaticObject.java
│
├── game_objects/
│   ├── ChasingEnemy.java
│   ├── Collectibles.java
│   ├── Key.java
│   ├── Player.java
│   ├── Portal.java
│   └── Trap.java
│
├── level/
│   └── Tiles.java
│
├── rendering/
│   ├── ElementRenderer.java
│   ├── Panel.java
│   ├── SpeechBubble.java
│   └── SpotlightEffect.java
│
├── screens/
│   ├── GameOverScreen.java
│   ├── GameScreen.java
│   ├── MenuScreen.java
│   └── SelectLevelScreen.java
│
├── tiles/
│   ├── Entrance.java
│   ├── Exit.java
│   ├── SpeedBoost.java
│   ├── Tile.java
│   └── Wall.java
│
├── util/
│   ├── Constants.java
│   └── Position.java
│
└── MazeRunnerGame.java
```

## UML Diagram
`<put our UML-Diagramm here>`


## Instructions
**How to run the game**?
* For Windows/Linux:

* For Mac:
Run game on the upper right hand side should be clicked, and then edit configurations should be selected. Afterward,
*Build and Run* should be found on the newly opened window and *Modify Options*, which is aligned to the right side of 
*Build and Run* should be clicked. Then, *Add VM Options* should be selected and the text *-XstartOnFirstThread* should 
* be typed to the newly opened text field. When these steps are completed, the game ready to run. Have fun!



* WASD or Arrow Keys to move

* To Pause the game, *ESCAPE* key must be pressed and to resume, either the *ENTER* key or the *Resume* button on pause window should be pressed.

* enter or "resume" button on the pause screen to resume game

* m to mute

* Press *Shift* to speed up

## Game UI
maybe show some images of the gameplay here

## Features

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

* Stamina

**Collision**:
Deutsche Survival Quest offers pixel perfect collision with objects


**Collectables**:

* Lives: hearts and  health insurance card, provides the player additional lives when collected.

* Boost-ups: power potion (adds stamina), pretzel.

**Exit to Next Level with the Key**:
* Level 1: Player must claim his luggage and can earn bonus points on the way.

* Level 2: Player must get his Deutschlandticket to catch his train.

* Level 3: Player must find the key to enter his house

* Level 4: Player must find the key to exit bar.

* Level 5: Player must get his documents and the rathaus to register in the Rathaus.

* Level 6: Player must find the room for his appointment 

**Obstacles**:
* Traps: 

* Chasing enemies (dynamic object):

* Portal:

**Communication and Information**:
* Panels:
  * Introduction Panel: brief explanation of game/levels structure
  * Pause Panel: 
* Speech Bubble:
  *

**Screens**: 

**Spotlight Effect**

**Custom util class: Position**
Contributes converting the units


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
  

### Music
- Background Music by Bruno Belotti (Submitted by qubodup).
  Available at https://opengameart.org/content/cheerful-3-nel-giardino-dello-zar-polka-loop.
  Licence type: Attribution-ShareAlike 3.0 Unported  https://creativecommons.org/licenses/by-sa/3.0/.
- Pause Music 
  Available at https://opengameart.org/sites/default/files/A%20cup%20of%20tea_0.mp3
- Game Over Music by Cleyton R. Xavier
 


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


## Contact
- [berin.bueyuekboduk@tum.de](mailto:berin.bueyuekboduk@tum.de)
- [jenchien.hou@tum.de](mailto:jenchien.hou@tum.de)
- [go36yev@mytum.de](mailto:go36yev@mytum.de)



## Some draft.....
Deutsche Survival Quest (German Assimilation Game) (sorting trash/ fighting german neighbours/ a maze leading to the Rathaus and the documents are the boosters, enemy is the bahnhof enjoyers and the DB delays) - different cities as different levels (1. Munich, 2. Heilbronn 3.Berlin 4. Frankfurt) (random DB cancellation force the player to restart level)
Finding luggage at the airport while racing against time (before the train leaves) (

Key - documents that can be collected at the Burgeramt to unlock next level/ train ticket/ health insurance/


## Features

For our features, we incorporated both static and dynamic obstacles that are unique to each level. For instance, to fit the story
line, we have ticket controllers as our dynamic obstacles. Any collision with the obstacles would result in a decrease in health level,
that could ultimately lead to losing in the game if all 5 hearts are used up. 

Aside from that, we also have a tutorial page with an animated and interactive display on the rules of the game. This would allow user to 
quickly learn how to play the game. The spotlight effect we employed makes the tutorial easier to follow as it brings the attention of the 
user to the specific variable. 

The user also have control over the other interfaces in the game such as sounds and music, with the choice to adjust the volume based on their liking 
the slider, and an option to completely mute them. They are also able to choose which level they would want to skip to using the "Select Level" function 
in the menu and pause screen.

For our surprise element, we have a portal that teleports the user back to the starting point. In order for the user
to finish the game quick, he must avoid the portal. There are also traps every level such as glass bottles that would lower the health of the user. 




