package de.tum.cit.fop.maze.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import de.tum.cit.fop.maze.game_objects.ChasingEnemy;
import de.tum.cit.fop.maze.util.Position;
import de.tum.cit.fop.maze.game_objects.Trap;
import de.tum.cit.fop.maze.tiles.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.tum.cit.fop.maze.util.Constants.*;
import static de.tum.cit.fop.maze.util.Position.PositionUnit.*;

/** this is like a TilesManager or ".properties" File Reader,
 * or more specifically, a LevelManager
 * It manages tile (and also other objects for the level) creation for each level
 */
public class Tiles {
    public TiledMapTileLayer layer;

    private Position keyTilePosition;
    public Array<Trap> traps;

    public Array<ChasingEnemy> chasingEnemies;

    /** entrance tile, coordinates of the tile can be accessed through this */
    public Entrance entrance;
    /** exit tile, coordinates of the tile can be accessed through this */
    public Array<Exit> exits;

    private Tile[] tileset;
    private Tile[][] tileOnMap;

    int maxTilesOnCell;

    // Create an immutable Set of integers representing wall
    // IntStream.concat(IntStream.rangeClosed(10, 29),IntStream.rangeClosed(64, 66)) in case i want to concat two sections in the future
    private static final Set<Integer> WALLS = IntStream.concat(IntStream.concat(
                    IntStream.of(0),
                    IntStream.rangeClosed(20, 79)),
                    IntStream.rangeClosed(100, 149))
            .boxed()
            .collect(Collectors.toSet());
    private static final int TRAPS_FIRST = 3;
    private static final int TRAPS_SECOND = 80;

    private static final int ENEMY_FIRST = 4;
    private static final int ENEMY_SECOND = 150;


    private static final Set<Integer> TRAPS = IntStream.concat(IntStream.of(TRAPS_FIRST),IntStream.rangeClosed(TRAPS_SECOND, 89))
            .boxed()
            .collect(Collectors.toSet());

    private static final Set<Integer> CHASING_ENEMIES = IntStream.concat(
            IntStream.of(ENEMY_FIRST),
            IntStream.rangeClosed(ENEMY_SECOND, 159))
            .boxed()//Converts the primitive int values in the stream into their wrapper class, Integer
            //[1, 2, 3, 10, 11, ...] becomes [Integer(1), Integer(2), ...]
            .collect(Collectors.toSet());//Collects the Integer values from the stream and stores them in a Set


    private static final Set<Integer> SPEED_BOOST = IntStream.rangeClosed(90, 99)
            .boxed()
            .collect(Collectors.toSet());
    public static final int KEY = 5;
    public static final int ENTRANCE = 1;
    public static final int BASIC_GROUND = 10;
    public static final Set<Integer> EXIT = IntStream.concat(IntStream.of(2), IntStream.rangeClosed(12, 14))
            .boxed()
            .collect(Collectors.toSet());

    /**
     * Constructor: initializes the Tiles object with default values.
     */
    public Tiles() {
        keyTilePosition = new Position(0, 0, TILES);
        entrance = null;
        exits = new Array<>();

        traps = new Array<>();
        chasingEnemies = new Array<>();
        maxTilesOnCell = 0;


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


    // Loads tile images and obstacle images from the specified file paths and organizes them into an array of Tile objects.
    private Tile[] loadTileSheet(String tileSheetPath, String ObstacleSheetPath) {
        var tileSheet = new Texture(tileSheetPath);//represents the main tile sheet image.
        var obstacleSheet = new Texture(ObstacleSheetPath);//represents the main tile sheet image.
        var enemySheet = new Texture(Gdx.files.internal("mob_guy.png"));
        //Calculates how many tiles (tileCols and tileRows) can fit horizontally and vertically in the tile sheet, assuming each tile has a fixed size (TILE_SIZE).
        int tileCols = tileSheet.getWidth() / TILE_SIZE;
        int tileRows = tileSheet.getHeight() / TILE_SIZE;

        // `tileset` is the tileset
        Tile[] tileset = new Tile[tileCols * tileRows];

        // Create the tileset to reference back to the tile type based on the tile sheet
        //This is the core logic of the method. It iterates through each position (grid cell)
        // in the tile sheet and determines how to handle the tile at that position
        for (int y = 0; y < tileRows; y++) {
            for (int x = 0; x < tileCols; x++) {
                int index = y * tileCols + x;

                TextureRegion tileRegion = null;

                //TextureRegion tileRegion;
                // Load the TextureRegions from the sheets:
                if (TRAPS.contains(index)) {
                    int startX = (index == TRAPS_FIRST) ? 0: 32 * (index - TRAPS_SECOND + 1);
                    tileRegion = new TextureRegion(obstacleSheet, startX, 0, 32, 32);
                }
                else if (CHASING_ENEMIES.contains(index)) {
                    int startX = (index == ENEMY_FIRST) ? 0: 16 * (index - ENEMY_SECOND + 1);
                    tileRegion = new TextureRegion(enemySheet, startX, 0, 16, 16);
                }
                else /*(!TRAPS.contains(index) && !CHASING_ENEMIES.contains(index))*/ { // DEFAULT
                    tileRegion = new TextureRegion(tileSheet, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                tileset[index] = createTile(index, tileRegion, false, 0,0);
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
     * @param isPositionKnown true if creating tiles for the tileset, which the positions of the tiles are indeed unknown;
     *                        false if creating completely new instances of tiles
     * @param x the tile X position on the map if isPositionKnown
     * @param y the tile Y position on the map if isPositionKnown
     *
     * @return a Tile object, it can either be a Wall, Entrance, Exit or a generic Tile
     */
    private Tile createTile(int index, TextureRegion tileRegion, boolean isPositionKnown, int x, int y) {
        if (WALLS.contains(index)){
            Tile tile = new Wall(tileRegion);
            tile.getProperties().put("type", "Wall");

            if (isPositionKnown){
                tileOnMap[x][y] = tile;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
            }

            return tile;
        }
        else if (index == ENTRANCE){
            entrance = new Entrance(tileRegion); // we create our Entrance instance here
            entrance.getProperties().put("type", "Entrance");

            if (isPositionKnown) {
                entrance.setTilePosition(new Position(x, y, TILES));
                tileOnMap[x][y] = entrance;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
            }

            return entrance;
        }
        else if (EXIT.contains(index)){
            Exit exit = new Exit(tileRegion);
            exit.getProperties().put("type", "Exit");

            if (isPositionKnown){
                exit.setTilePosition(new Position(x, y, TILES));
                exits.add(exit);
                tileOnMap[x][y] = exit;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
            }


            return exit;
        }
        else if (TRAPS.contains(index)){
            Tile tile = new Tile(tileRegion);
            tile.getProperties().put("type", "Trap");

            if (isPositionKnown){
                tileOnMap[x][y] = tile;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
            }

            return tile;
        }

        else if (CHASING_ENEMIES.contains(index)){
            Tile tile = new Tile(tileRegion);
            tile.getProperties().put("type", "Enemy");
            if (isPositionKnown){
                tileOnMap[x][y] = tile;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
            }
            return tile;
        }

        else if (SPEED_BOOST.contains(index)){
            Tile tile = new SpeedBoost(tileRegion);
            tile.getProperties().put("type", "Speed Boost");

            if (isPositionKnown){
                tileOnMap[x][y] = tile;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
            }

            return tile;
        }
        else {
            Tile tile = new Tile(tileRegion);
            tile.getProperties().put("type", "");

            if (isPositionKnown){
                tileOnMap[x][y] = tile;
                tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
            }

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

                if (!Objects.equals(parts[0], "keyPosition")){ // null-safe equal, the key is NOT "keyPosition"
                    String positionStr = parts[0];
                    String[] tileTypes = parts[1].split(","); // Handle multiple tile types (could contain the key)

                    for (String tileType : tileTypes) {
                        //Gdx.app.log("Parse", "Tile Parsed: " + position);
                        putTileDataInMapData(positionStr, tileType, mapData);
                    }

                    // Update the Map Size as we read the positions and find the largest value
                    Position position = stringToPosition(positionStr, TILES);
                    if (horizontalTilesCount < position.getTileX() + 1) {
                        horizontalTilesCount = position.getTileX()+1;
                        //Gdx.app.debug("MapParser", "horizontal tiles count updated to " + horizontalTilesCount);
                    }
                    if (verticalTilesCount < position.getTileY() + 1) verticalTilesCount = position.getTileY() + 1;
                }
                else{ // the key in the key-value is "keyPosition" which allows us to place the key on float positions
                    keyTilePosition = stringToPosition(parts[1], TILES).convertTo(PIXELS);
                }



            }
        } catch (IOException e) {
            Gdx.app.error("TileMapParser", String.valueOf(e));
        }

        return mapData;
    }

    private TiledMap createTiledMap(ObjectMap<String, Array<Integer>> mapData, int mapWidthInTiles, int mapHeightInTiles) {
        // Create a TiledMap
        TiledMap map = new TiledMap();

        tileOnMap = new Tile[mapWidthInTiles][mapHeightInTiles]; // stores the tile in that cell that is on the most upper layer

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

                    if (tileValue == KEY){
                        keyTilePosition = new Position(x, y, TILES);
                    }
                    else if (TRAPS.contains(tileValue)){ // a trap
                        Tile tile = tileset[tileValue];

                        Position trapPosition = new Position(x, y, TILES).convertTo(PIXELS);
                        float worldX = trapPosition.getX();
                        float worldY = trapPosition.getY();
                        // a new instance of trap is created here
                        traps.add(new Trap(tile.getTextureRegion(),worldX,worldY,TILE_SIZE,TILE_SIZE,16,16,TILE_SCREEN_SIZE * 0.8f, TILE_SCREEN_SIZE * 0.8f, 1));
                    }

                    else if (CHASING_ENEMIES.contains(tileValue)){//an enemy or a chasing enemy i myself don't know it yet
                        Tile tile = tileset[tileValue];

                        Position chasingEnemyPosition = new Position(x, y, TILES);
                        int worldX = chasingEnemyPosition.getTileX();
                        int worldY = chasingEnemyPosition.getTileY();
                        chasingEnemies.add(new ChasingEnemy(tile.getTextureRegion(), worldX, worldY, 16, 16, 10, 16, 64, 64, 3, this));

                    }
                    else { // if it is neither a trap nor a key, which is the default one

                        // There would be a random chance to change the ground tile
                        if (tileIndex == BASIC_GROUND) {
                            double random = Math.random(); // generates random number between 0.0 and 1.0
                            if (random <= 0.005 * 4) { // 0.5% chance each for four of our ground tile variant
                                tileIndex = 7 + (int) (Math.floor(random * 200)); // 1/0.5 is 200%, tileIndex can therefore be 7~10
                            }
                        }

                        Tile tile = tileset[tileIndex]; // We get the texture through this tile

                        // deal with LibGDX own library
                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tile);
                        layer.setCell(x, y, cell);

                        // create a new tile based on its type so that we won't be accessing the same tile from the array
                        // also set its position on the map
                        //createAndPlaceNewTile(x, y, tile);
                        createTile(tileValue, tile.getTextureRegion(), true, x, y); // it is still tileValue instead of tileIndex here, so the functionalities will not be aff
                    }

                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Gdx.app.error("Tiles", "Error loading tiles: ", e);
            }

            map.getLayers().add(layer);

        }

        Gdx.app.log("Tiles", "Tiled Map loaded");
        //Gdx.app.log("Tiles", "entrance position: " + entrance.getTilePosition());
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
            if (true/*tileValue != KEY*/) {
                // originally it was a Integer instead of a List<Integer>,
                // but now we can have more than one layer,
                // so we need a list to store the tiles in this specific cell at this position
                Array<Integer> list;
                if (mapData.get(position) == null){ // puts a empty list to the Map if empty
                    list = new Array<>();
                    mapData.put(position, list);
                }
                list = mapData.get(position);
                list.add(tileValue);
                if (list.size > maxTilesOnCell) maxTilesOnCell = list.size; // set maxTilesOnCell to fine the maximum # of tiles that any grid has
                mapData.put(position, list);
                //Gdx.app.log("mapData", "Put to mapData: " + position + " " + mapData.get(position));

            }
            else { // get the key position here if the specification type is x,y=KEY
                //Position position = stringToPosition(parts[0]);
                //keyTilePosition = new Position(position.getTileX(), position.getTileY(), TILES);
            }
        } catch (NumberFormatException e) {
            Gdx.app.error("TileMapParser", "Invalid tile value: " + tileType + " at position " + position, e);
        }
    }

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

    public Tile[] getTileset() {
        return tileset;
    }

    public Position getKeyTilePosition() {
        return keyTilePosition;
    }

    public Tile[][] getTileOnMap() {
        return tileOnMap;
    }

    public Tile getTileOnMap(int x, int y) {
        return tileOnMap[x][y];
    }

    public Exit getNearestExit(float playerX, float playerY){
        // find the nearest exit to (x,y) to this.exits
        if (exits == null || exits.isEmpty()) {
            throw new IllegalStateException("No exits available on the map.");
        }

        Exit nearestExit = null;
        double minDistance = Double.MAX_VALUE;

        for (Exit exit : exits) {
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

}
