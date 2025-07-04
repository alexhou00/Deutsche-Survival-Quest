package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.game_objects.BFSChasingEnemy;
import de.tum.cit.fop.maze.game_objects.ChasingEnemy;
import de.tum.cit.fop.maze.util.Position;
import de.tum.cit.fop.maze.game_objects.Trap;
import de.tum.cit.fop.maze.tiles.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static de.tum.cit.fop.maze.MazeRunnerGame.createDirectionalAnimations;
import static de.tum.cit.fop.maze.tiles.TileType.*;
import static de.tum.cit.fop.maze.util.Constants.*;
import static de.tum.cit.fop.maze.util.Position.PositionUnit.*;

/** this is like a TilesManager or ".properties" File Reader,
 * or more specifically, a LevelManager
 * It manages tile (and also other objects for the level) creation for each level
 */
public class LevelManager {
    public TiledMapTileLayer layer;
    private Position keyTilePosition;
    private final boolean cameraAngled = false;
    public Array<Trap> traps;
    public Array<ChasingEnemy> chasingEnemies;
    private final Map<Integer, Map<String, Animation<TextureRegion>>> enemiesAnimations; // Map of "enemy Animations", and an "enemy Animations" is a map of "enemy Animation"

    /** entrance tile, coordinates of the tile can be accessed through this */
    public Entrance entrance;

    /** exit tile, coordinates of the tile can be accessed through this */
    public Array<Exit> exits;

    private TextureRegion[] tileset;
    private Tile[][] tileOnMap;
    ObjectMap<String, String> mapProperties;

    int maxTilesOnCell;

    private static final Set<Integer> SPEED_BOOST = TileType.SPEED_BOOST.getAll();
    public static final Set<Integer> EXIT = TileType.EXIT.getAll();
    private TileType[][] tileEnumOnMap;
    private final MazeRunnerGame game;

    /**
     * Constructor: initializes the LevelManager object with default values.
     */
    public LevelManager(MazeRunnerGame game) {
        keyTilePosition = null;
        entrance = null; //getEntrance();
        exits = new Array<>();
        traps = new Array<>();
        chasingEnemies = new Array<>();
        enemiesAnimations = new HashMap<>();
        maxTilesOnCell = 0;
        this.game = game;
        mapProperties = new ObjectMap<>();
    }

    public Entrance getEntrance() {
       return entrance;
    }

    /**
     * Loads a tiled map from the specified map and tile sheet files.
     * Note that only this method is public, which means only this method
     * should be accessed outside of this class when we want to load a tiled map <br><br>
     * STEPS: <br>
     * <li> First, we load the tile sheet and store it in `tileset` </li>
     * <li> Second, we parse the .properties file and process every tile (store it in mapData) </li>
     * <li> Third, we place the tiles on the map by creating a new instance of the tile (so that each tile can have their own position)  </li>
     *
     * @param mapFilePath       Path to the map properties file.
     * @param tileSheetPath     Path to the tile sheet image.
     * @return The created {@link TiledMap} object.
     */
    public TiledMap loadTiledMap(String mapFilePath, String tileSheetPath, String ObstacleSheetPath) {
        // To completely load the tiled map,
        // FIRST,
        // Load the tile sheet
        tileset = loadTileSheet(tileSheetPath, ObstacleSheetPath);

        // SECOND,
        // Parse ".properties" file. The position of the key will also be handled here.
        ObjectMap<String, Array<Integer>> mapData = parsePropertiesFile(mapFilePath);

        // THIRD,
        // Put the tiles on the map. And if the tile is a trap/enemy, create a trap/enemy.
        return createTiledMap(mapData, horizontalTilesCount, verticalTilesCount);
    }


    /** Loads tile images and obstacle images from the specified file paths
     * and organizes them into an array of Tile objects.
     */
    private TextureRegion[] loadTileSheet(String tileSheetPath, String ObstacleSheetPath) {
        var tileSheet = new Texture(tileSheetPath);//represents the main tile sheet image.
        var obstacleSheet = new Texture(ObstacleSheetPath);//represents the main tile sheet image.
        int tileCols = tileSheet.getWidth() / TILE_SIZE;
        int tileRows = tileSheet.getHeight() / TILE_SIZE;

        // `tileset` is the tileset
        TextureRegion[] tileset = new TextureRegion[tileCols * tileRows];

        // Create the tileset to reference back to the tile type based on the tile sheet
        //This is the core logic of the method. It iterates through each position (grid cell)
        // in the tile sheet and determines how to handle the tile at that position
        for (int y = 0; y < tileRows; y++) {
            for (int x = 0; x < tileCols; x++) {
                int index = y * tileCols + x;
                TextureRegion tileRegion;
                // Load the TextureRegions from the sheets:
                if (TRAP.getAll().contains(index)) {
                    int startX = (index == TRAP.getId()) ? 0: TRAP_SIZE * (index - TRAP.getSecond() + 1);
                    tileRegion = new TextureRegion(obstacleSheet, startX, 0, TRAP_SIZE, TRAP_SIZE);
                }

                else if (ENEMY.getAll().contains(index)) {
                    int startY = TRAP_SIZE + getEnemyIndex(index) * ENEMY_SIZE; //index == ENEMY_FIRST) ? 0: 16 * (index - ENEMY_SECOND + 1);
                    tileRegion = new TextureRegion(obstacleSheet, 0, startY, ENEMY_SIZE, ENEMY_SIZE);
                    int enemyIndex = getEnemyIndex(index);
                    enemiesAnimations.put(enemyIndex,
                            createDirectionalAnimations(obstacleSheet, true, 0.1f,
                                    TRAP_SIZE + ENEMY_SIZE * (enemyIndex), ENEMY_SIZE, ENEMY_SIZE, 3));
                }
                else /*(!TRAPS.contains(index) && !CHASING_ENEMIES.contains(index))*/ { // DEFAULT
                    tileRegion = new TextureRegion(tileSheet, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                tileset[index] = tileRegion; // createTile(index, tileRegion, false, 0,0);
            }
        }
        return tileset;
    }

    /** Creates a new tile based on its type (Wall, Entrance, Exit, or generic Tile),
     * at the first time it is used, isPositionKnown is false, and we are just using it
     * to create Tile for the tile set.
     * <p>
     * The second time, we create new tiles, and we place it in the `tileOnMap` array,
     * which tells the program that we're putting it on the map
     * we create new tiles so that we won't be accessing the same tile from the array
     * if we still use the {@code Tile tile = tileset[tileValue];}; <br>
     * All types of existing tiles should be managed here,
     * and this method updates its position on the tileOnMap array
     *
     * @param index the tileIndex or the tileValue on the tileset;
     * @param tileRegion the textureRegion of the tile
     * isPositionKnown true if creating tiles for the tileset, which the positions of the tiles are indeed unknown;
     *                        false if creating completely new instances of tiles
     * @param x the tile X position on the map if isPositionKnown
     * @param y the tile Y position on the map if isPositionKnown
     *
     * @return a Tile object, it can either be a Wall, Entrance, Exit or a generic Tile
     */
    private Tile createTile(int index, TextureRegion tileRegion, int x, int y) {
        if (WALL.getAll().contains(index)){
            Tile tile = new Wall(tileRegion);
            tile.getProperties().put("type", "Wall");

                tileOnMap[x][y] = tile;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
                tileEnumOnMap[x][y] = TileType.WALL;

            return tile;
        }

        else if (index == ENTRANCE.getId()){
            entrance = new Entrance(tileRegion); // we create our Entrance instance here
            entrance.getProperties().put("type", "Entrance");
                entrance.setTilePosition(new Position(x, y, TILES));
                tileOnMap[x][y] = entrance;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
                tileEnumOnMap[x][y] = ENTRANCE;

            return entrance;
        }
        else if (EXIT.contains(index)){
            Exit exit = new Exit(tileRegion);
            exit.getProperties().put("type", "Exit");

                exit.setTilePosition(new Position(x, y, TILES));
                exits.add(exit);
                tileOnMap[x][y] = exit;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
                tileEnumOnMap[x][y] = TileType.EXIT;

            return exit;
        }
        else if (TRAP.getAll().contains(index)){
            Tile tile = new Tile(tileRegion);
            tile.getProperties().put("type", "Trap");

                tileOnMap[x][y] = tile;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
                tileEnumOnMap[x][y] = TileType.TRAP;

            return tile;
        }
        else if (ENEMY.getAll().contains(index)){
            Tile tile = new Tile(tileRegion);
            tile.getProperties().put("type", "Enemy");

                tileOnMap[x][y] = tile;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
                tileEnumOnMap[x][y] = TileType.ENEMY;

            return tile;
        }

        else if (SPEED_BOOST.contains(index)){
            Tile tile = new SpeedBoost(tileRegion);
            tile.getProperties().put("type", "Speed Boost");

                tileOnMap[x][y] = tile;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
                tileEnumOnMap[x][y] = TileType.SPEED_BOOST;

            return tile;
        }
        else {
            Tile tile = new Tile(tileRegion);
            tile.getProperties().put("type", "");

                tileOnMap[x][y] = tile;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
                if (index < EXTRA.getId())
                    tileEnumOnMap[x][y] = GROUND;
                else // if index too large, it is considered to be special like a train
                    tileEnumOnMap[x][y] = EXTRA;

            return tile;
        }
    }

    /**
     * Parses the properties file for the tile map.
     *
     * @param filePath Path to the properties file.
     * @return An {@link ObjectMap} containing map data.
     */
    private ObjectMap<String, Array<Integer>> parsePropertiesFile(String filePath) {
        ObjectMap<String, Array<Integer>> mapData = new ObjectMap<>();
        horizontalTilesCount = 1;
        verticalTilesCount = 1; // reset when new level starts

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("=")) continue; // skip invalid lines

                String[] parts = line.split("="); // split into key-value, parts[0] is position (String) and parts[1] is the tileType or tileValue
                if (parts.length != 2) continue; // ignore malformed lines

                String key = parts[0].trim();
                String value = parts[1].trim();

                if (key.matches("\\d+, *\\d+")){ // the key is matching the pattern of the coordinate format
                    String[] tileTypes = value.split(","); // Handle multiple tile types (could contain the key)

                    for (String tileType : tileTypes) {
                        //Gdx.app.log("Parse", "Tile Parsed: " + position);
                        putTileDataInMapData(key, tileType, mapData); // key is the positionStr
                    }

                    // Update the Map Size as we read the positions and find the largest value
                    Position position = stringToPosition(key, TILES);
                    if (horizontalTilesCount < position.getTileX() + 1) {
                        horizontalTilesCount = position.getTileX()+1;
                        //Gdx.app.debug("MapParser", "horizontal tiles count updated to " + horizontalTilesCount);
                    }
                    if (verticalTilesCount < position.getTileY() + 1) verticalTilesCount = position.getTileY() + 1;
                }
                else{
                    mapProperties.put(key, value);
                }
            }

        } catch (IOException e) {
            Gdx.app.error("TileMapParser", String.valueOf(e));
        }

        return mapData;
    }

    /**
     * Put the tiles on the {@link TiledMap} based on the given map data and dimensions.
     * And if the tile is a trap/enemy, create a trap/enemy.
     *
     * @param mapData an object map (like the mapping or the dict, not the drawing map) containing tile data.
     * @param mapWidthInTiles the width of the map in tiles.
     * @param mapHeightInTiles the height of the map in tiles.
     * @return the created {@link TiledMap}.
     */
    private TiledMap createTiledMap(ObjectMap<String, Array<Integer>> mapData, int mapWidthInTiles, int mapHeightInTiles) {
        // Create a TiledMap
        TiledMap map = new TiledMap();

        tileOnMap = new Tile[mapWidthInTiles][mapHeightInTiles]; // stores the tile in that cell that is on the most upper layer
        tileEnumOnMap = new TileType[mapWidthInTiles][mapHeightInTiles];

        // iterate every layer, since there could be two of them
        // (the first one is the ground, and the second is some additional stuff on it)
        for (int layerI = 0; layerI < maxTilesOnCell; layerI++){

            layer = new TiledMapTileLayer(mapWidthInTiles, mapHeightInTiles, TILE_SIZE, TILE_SIZE); // put our width/height here

            // Populate the layer with tiles
            try{
                for (String key : mapData.keys()) {
                    if (mapData.get(key).size <= layerI)
                        continue;
                    int tileValue = mapData.get(key).get(layerI);
                    int tileIndex = tileValue;
                    Position position = stringToPosition(key, TILES);
                    int x = position.getTileX();
                    int y = position.getTileY();

                    if (tileValue == KEY.getId()){
                        keyTilePosition = new Position(x, y, TILES);
                    }
                    else if (TRAP.getAll().contains(tileValue)){ // a trap
                        TextureRegion tileRegion = tileset[tileValue];

                        Position trapPosition = new Position(x, y, TILES).convertTo(PIXELS);
                        float worldX = trapPosition.getX();
                        float worldY = trapPosition.getY();
                        // a new instance of a trap is created here
                        traps.add(new Trap(tileRegion, worldX, worldY,
                                TILE_SIZE, TILE_SIZE, TILE_SIZE, TILE_SIZE,
                                TILE_SCREEN_SIZE * 0.8f, TILE_SCREEN_SIZE * 0.8f, 1));
                        tileEnumOnMap[x][y] = TileType.TRAP;  // fixing the problem that somehow hearts are spawning on traps, it's actually because createTile() is not called so that tileEnumOnMap isn't updated
                    }

                    else if (ENEMY.getAll().contains(tileValue)){//an enemy or a chasing enemy i myself don't know it yet
                        TextureRegion tileRegion = tileset[tileValue];

                        Position chasingEnemyPosition = new Position(x, y, TILES);
                        int worldX = chasingEnemyPosition.getTileX();
                        int worldY = chasingEnemyPosition.getTileY();
                        int enemyIndex = getEnemyIndex(tileValue);
                        String nonBFSEnemy = getProperties("nonBFSEnemyTypes");
                        if (!nonBFSEnemy.isEmpty() && Integer.parseInt(nonBFSEnemy) == getEnemyIndex(tileValue) + 1){
                            // create normal enemy if in the properties file, it says "non BFS"
                            chasingEnemies.add(new ChasingEnemy(tileRegion, worldX, worldY,
                                    TILE_SIZE, TILE_SIZE, 10, 16, 64, 64,
                                    3, this, game, enemyIndex));
                        }
                        else { // BFS enemy
                            chasingEnemies.add(new BFSChasingEnemy(tileRegion, worldX, worldY,
                                    TILE_SIZE, TILE_SIZE, 10, 16, 64, 64,
                                    3, this, game, enemyIndex));
                        }
                    }
                    else { // if it is neither a trap nor a key, which is the default one

                        // There would be a random chance to change the ground tile
                        if (tileIndex == GROUND.getId()) {
                            double random = Math.random(); // generates random number between 0.0 and 1.0
                            if (random <= 0.005 * 4) { // 0.5% chance each for four of our ground tile variant
                                tileIndex = 7 + (int) (Math.floor(random * 200)); // 1/0.5 is 200%, tileIndex can therefore be 7~10
                            }
                        }

                        TextureRegion tileRegion = tileset[tileIndex]; // We get the texture through this tile

                        // create a new tile based on its type so that we won't be accessing the same tile from the array
                        // also set its position on the map
                        Tile tile = createTile(tileValue, tileRegion, x, y); // it is still tileValue instead of tileIndex here, so the functionalities will not be aff

                        // deal with LibGDX own library
                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tile);
                        layer.setCell(x, y, cell);

                        }

                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Gdx.app.error("LevelManager", "Error loading tiles: ", e);
            }

            map.getLayers().add(layer);

        }

        Gdx.app.log("LevelManager", "Tiled Map loaded");
        //Gdx.app.log("LevelManager", "entrance position: " + entrance.getTilePosition());
        return map;
    }

    /**
     * While parsing the .properties file,
     * Processes an individual tile to update the map data.
     *
     * @param position   The position key of the tile. (Format: "x,y")
     * @param tileType   The tile value or type but as a string.
     * @param mapData    The mapData to update.
     */
    private void putTileDataInMapData(String position, String tileType, ObjectMap<String, Array<Integer>> mapData) {
        try {
            int tileValue = Integer.parseInt(tileType);

            // originally it was an Integer instead of a List<Integer>,
            // but now we can have more than one layer,
            // so we need a list to store the tiles in this specific cell at this position
            Array<Integer> list;
            if (mapData.get(position) == null){ // puts an empty list to the Map if empty
                list = new Array<>();
                mapData.put(position, list);
            }
            list = mapData.get(position);
            list.add(tileValue);
            if (list.size > maxTilesOnCell) maxTilesOnCell = list.size; // set maxTilesOnCell to fine the maximum # of tiles that any grid has
            mapData.put(position, list);
            //Gdx.app.log("mapData", "Put to mapData: " + position + " " + mapData.get(position));


        } catch (NumberFormatException e) {
            Gdx.app.error("TileMapParser", "Invalid tile value: " + tileType + " at position " + position, e);
        }
    }

    /**
     * Converts a string representation of coordinates "x,y" into a {@link Position}.
     *
     * @param string the string in the format "x,y".
     * @param unit   the unit of the position, either {@link Position.PositionUnit#TILES} or {@link Position.PositionUnit#PIXELS}.
     * @return a {@link Position} object representing the parsed coordinates.
     */
    private Position stringToPosition(String string, Position.PositionUnit unit) {
        String[] parts = string.split(",");
        float x = (Float.parseFloat(parts[0]) % 1 == 0) ? Integer.parseInt(parts[0]) : Float.parseFloat(parts[0]);
        float y = (Float.parseFloat(parts[1]) % 1 == 0) ? Integer.parseInt(parts[1]) : Float.parseFloat(parts[1]);
        if (unit == TILES){
            return new Position(x, y, TILES);
        }
        else { // unit is pixels
            return new Position(x, y, PIXELS);
        }
    }

    public TextureRegion[] getTileset() {
        return tileset;
    }

    /**
     * Retrieves the position of the key tile in the map.
     * <p>
     * If the position of the key tile has not been set yet, this method checks the map's properties to
     * retrieve the key position from the property "keyPosition". If the key position is found, it converts
     * the position from tile coordinates to pixel coordinates. If no key position is found, it returns a default
     * position (0,0) in tile coordinates.
     * </p>
     *
     * @return The position of the key tile in pixel coordinates, or a default position (0, 0) if not set.
     */
    public Position getKeyTilePosition() {
        if (keyTilePosition == null) {
            if (mapProperties.get("keyPosition") != null){
                String pos =  mapProperties.get("keyPosition");
                return stringToPosition(pos, TILES).convertTo(PIXELS);
            }
            else{
                return new Position(0, 0, TILES); // by default (0,0)
            }
        }
        return keyTilePosition;
    }

    /**
     * Retrieves the tile at the specified position on the map.
     * <p>
     * Given the x and y coordinates, this method returns the tile at that position on the map.
     * The coordinates are assumed to be within the bounds of the map.
     * </p>
     *
     * @param x The x-coordinate of the tile on the map.
     * @param y The y-coordinate of the tile on the map.
     * @return The tile at the specified position on the map.
     */
    public Tile getTileOnMap(int x, int y) {
        return tileOnMap[x][y];
    }

    /**
     * Returns the 2D array of {@link TileType} representing the map layout.
     *
     * @return a 2D array of tile types on the map.
     */
    public TileType[][] getTileEnumOnMap() {
        return tileEnumOnMap;
    }

    /**
     * Retrieves the {@link TileType} at the specified coordinates on the map.
     * Equivalent to {@link LevelManager#getTileEnumOnMap() getTileEnumOnMap()[x][y]}
     *
     * @param x the x-coordinate of the tile.
     * @param y the y-coordinate of the tile.
     * @return the tile type at the specified coordinates.
     */
    public TileType getTileEnumOnMap(int x, int y) {
        return tileEnumOnMap[x][y];
    }


    /**
     * Finds the nearest {@link Exit} to the given player coordinates.
     *
     * @param playerX the x-coordinate of the player.
     * @param playerY the y-coordinate of the player.
     * @return the nearest exit to the player's position.
     * @throws IllegalStateException if no exits are available on the map.
     */
    public Exit getNearestExit(float playerX, float playerY){
        // find the nearest exit to (x,y) to this.exits
        if (exits == null || exits.isEmpty()) {
            throw new IllegalStateException("No exits available on the map.");
        }

        Exit nearestExit = null;
        double minDistance = Double.MAX_VALUE;

        for (Exit exit : iterate(exits)) {
            Position exitPosition = exit.getTilePosition().convertTo(PIXELS);
            float exitX = exitPosition.getX();
            float exitY = exitPosition.getY();

            // Calculate the Euclidean distance
            double distance = Math.sqrt(Math.pow(exitX - playerX, 2) + Math.pow(exitY - playerY, 2));

            if (distance < minDistance) {
                minDistance = distance;
                nearestExit = exit;
            }
        }

        return nearestExit;
    }

    /**
     * Checks if the current map's perspective requires the camera to be slightly angled
     * rather than completely 2D top-down view.
     *
     * @return {@code true} if the camera is angled, {@code false} otherwise.
     */
    public boolean isCameraAngled() {
        return isProperties("angled");
    }

    /**
     * Retrieves the animations for the enemy at the specified index.
     * <p>
     * Given the index of the enemy, this method returns the corresponding set of animations
     * for that enemy. The animations are stored in a map where the keys represent the direction
     * of the animations (e.g., "up", "down", "left", "right"), and the values are the
     * corresponding animation frames.
     * </p>
     *
     * @param index The index of the enemy whose animations are to be retrieved.
     * @return A map of animations for the specified enemy. The map's keys are direction strings
     *         ("up", "down", "left", "right") and the values are the corresponding animations.
     */
    public Map<String, Animation<TextureRegion>> getEnemyAnimations(Integer index) {
        return enemiesAnimations.get(index);
    }

    /**
     * Returns the index of the enemy based on the tile value.
     * <p>
     * The method calculates the enemy's index based on the given tile value. If the tile value
     * corresponds to the default (first type of) enemy, the index is 0. Otherwise, the index is
     * calculated based on the difference between the tile value and a predefined value associated
     * with the enemy types, adjusted accordingly.
     * </p>
     *
     * @param tileValue The value of the tile, which determines which enemy is present.
     * @return The index of the enemy based on the tile value. If the tile value matches the default
     *         enemy type, the index is 0; otherwise, it calculates the index relative to other enemy types.
     */
    public int getEnemyIndex(int tileValue){
        // starts from 0
        // if it's the default (first type of) enemy; the index is 0, else the index continues from tileset[150]
        return (tileValue == ENEMY.getId()) ? 0 : ((tileValue - ENEMY.getSecond()) + 1);
    }

    /**
     * Retrieves the value associated with the given key from the map of properties.
     * <p>
     * This method checks if the specified key exists in the properties map and returns its associated
     * value. If the key does not exist, an empty string is returned.
     * </p>
     *
     * @param key The key whose associated value is to be retrieved.
     * @return The value associated with the specified key, or an empty string if the key does not exist.
     */
    public String getProperties(String key){
        if (mapProperties.get(key) != null){
            return mapProperties.get(key);
        }
        else return "";
    }

    /**
     * Checks if the value associated with the given key is "true" in the map of properties.
     * <p>
     * This method retrieves the value associated with the specified key from the properties map
     * and checks if the value is equal to the string "true". If the key does not exist or the
     * value is not "true", the method returns false.
     * </p>
     *
     * @param key The key whose associated value is to be checked.
     * @return {@code true} if the value associated with the key is "true", otherwise {@code false}.
     */
    public boolean isProperties(String key){
        if (mapProperties.get(key) != null){
            return Objects.equals(mapProperties.get(key), "true");
        }
        else return false;
    }
}
