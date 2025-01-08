package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.ObjectMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.tum.cit.fop.maze.Constants.*;
import static de.tum.cit.fop.maze.Position.PositionUnit.*;

/** this is like a TilesManager or ".properties" File Reader
 * It manages tile (and also other objects for the level) creation for each level
 */
public class Tiles {
    public TiledMapTileLayer layer;

    private Position keyTilePosition;
    List<Trap> traps;

    /** entrance tile, coordinates of the tile can be accessed through this */
    public Entrance entrance;
    /** exit tile, coordinates of the tile can be accessed through this */
    public List<Exit> exits;

    private Tile[] tileset;
    private Tile[][] tileOnMap;

    int maxTilesOnCell;

    // Create an immutable Set of integers representing wall
    // IntStream.concat(IntStream.rangeClosed(10, 29),IntStream.rangeClosed(64, 66)) in case i want to concat two sections in the future
    private static final Set<Integer> WALLS = IntStream.concat(IntStream.rangeClosed(10, 29),IntStream.rangeClosed(60, 149))
            .boxed()
            .collect(Collectors.toSet());
    private static final Set<Integer> TRAPS = IntStream.rangeClosed(30, 39)
            .boxed()
            .collect(Collectors.toSet());
    public static final int KEY = 6;
    public static final int ENTRANCE = 1;
    public static final Set<Integer> EXIT = IntStream.rangeClosed(2, 5)
            .boxed()
            .collect(Collectors.toSet());

    /**
     * Constructor: initializes the Tiles object with default values.
     */
    public Tiles() {
        keyTilePosition = new Position(0, 0, TILES);
        entrance = null;
        exits = new ArrayList<>();

        traps = new ArrayList<>();
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
     * @param mapWidthInTiles   Width of the map in tiles.
     * @param mapHeightInTiles  Height of the map in tiles.
     * @return The created {@link TiledMap} object.
     */
    public TiledMap loadTiledMap(String mapFilePath, String tileSheetPath, int mapWidthInTiles, int mapHeightInTiles) {
        // To completely load the tiled map,
        // FIRST,
        // Load the tile sheet
        tileset = loadTileSheet(tileSheetPath);

        // SECOND,
        // Parse ".properties" file. The position of the key will also be handled here.
        ObjectMap<String, List<Integer>> mapData = parsePropertiesFile(mapFilePath);


        // THIRD,
        // Put the tiles on the map. And if the tile is a trap/enemy, create a trap/enemy.
        return createTiledMap(mapData, mapWidthInTiles, mapHeightInTiles);
    }


    private Tile[] loadTileSheet(String tileSheetPath) {
        var tileSheet = new Texture(tileSheetPath);
        int tileCols = tileSheet.getWidth() / TILE_SIZE;
        int tileRows = tileSheet.getHeight() / TILE_SIZE;

        // `tileset` is the tileset
        Tile[] tileset = new Tile[tileCols * tileRows];

        // Create the tileset to reference back to the tile type based on the tile sheet
        for (int y = 0; y < tileRows; y++) {
            for (int x = 0; x < tileCols; x++) {
                int index = y * tileCols + x;
                TextureRegion tileRegion = new TextureRegion(tileSheet, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
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
    private ObjectMap<String, List<Integer>> parsePropertiesFile(String filePath) {
        ObjectMap<String, List<Integer>> mapData = new ObjectMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("=")) continue; // skip invalid lines

                String[] parts = line.split("="); // split into key-value, parts[0] is position (String) and parts[1] is the tileType or tileValue
                if (parts.length != 2) continue; // ignore malformed lines

                if (!Objects.equals(parts[0], "keyPosition")){ // null-safe equal, the key is not "keyPosition"
                    String position = parts[0];
                    String[] tileTypes = parts[1].split(","); // Handle multiple tile types (could contain the key)

                    for (String tileType : tileTypes) {
                        //Gdx.app.log("Parse", "Tile Parsed: " + position);
                        putTileDataInMap(position, tileType, mapData);
                    }
                }
                else{
                    keyTilePosition = stringToPosition(parts[1], TILES).convertTo(PIXELS);
                }



            }
        } catch (IOException e) {
            Gdx.app.error("TileMapParser", String.valueOf(e));
        }

        return mapData;
    }

    private TiledMap createTiledMap(ObjectMap<String, List<Integer>> mapData, int mapWidthInTiles, int mapHeightInTiles) {
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
                    if (mapData.get(key).size() <= layerI)
                        continue;
                    int tileValue = mapData.get(key).get(layerI);
                    Position position = stringToPosition(key, TILES);
                    int x = position.getTileX();
                    int y = position.getTileY();

                    if (!TRAPS.contains(tileValue)){

                        Tile tile = tileset[tileValue];

                        // deal with LibGDX own library
                        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                        cell.setTile(tile);
                        layer.setCell(x, y, cell);

                        // create a new tile based on its type so that we won't be accessing the same tile from the array
                        // also set its position on the map
                        //createAndPlaceNewTile(x, y, tile);
                        createTile(tileValue, tile.getTextureRegion(), true, x, y);
                    }
                    else{ // a trap
                        Tile tile = tileset[tileValue];

                        Position trapPosition = new Position(x, y, TILES).convertTo(PIXELS);
                        float worldX = trapPosition.getX();
                        float worldY = trapPosition.getY();
                        // a new instance of trap is created here
                        traps.add(new Trap(tile.getTextureRegion(),worldX,worldY,TILE_SIZE,TILE_SIZE,16,16,TILE_SCREEN_SIZE, TILE_SCREEN_SIZE, 2));
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
     * Processes an individual tile to update the map data.
     *
     * @param position   The position key of the tile. (Format: "x,y")
     * @param tileType   The tile value or type but as a string.
     * @param mapData    The mapData to update.
     */
    private void putTileDataInMap(String position, String tileType, ObjectMap<String, List<Integer>> mapData) {
        try {
            int tileValue = Integer.parseInt(tileType);
            if (tileValue != KEY) {
                // originally it was a Integer instead of a List<Integer>,
                // but now we can have more than one layer,
                // so we need a list to store the tiles in this specific cell at this position
                List<Integer> list;
                if (mapData.get(position) == null){ // puts a empty list to the Map if empty
                    list = new ArrayList<>();
                    mapData.put(position, list);
                }
                list = mapData.get(position);
                list.add(tileValue);
                if (list.size() > maxTilesOnCell) maxTilesOnCell = list.size(); // set maxTilesOnCell to fine the maximum # of tiles that any grid has
                mapData.put(position, list);
                //Gdx.app.log("mapData", "Put to mapData: " + position + " " + mapData.get(position));

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

}
