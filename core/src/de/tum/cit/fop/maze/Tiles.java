package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.ObjectMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.tum.cit.fop.maze.Constants.*;
import static de.tum.cit.fop.maze.Position.PositionUnit.*;

/** this is like a TilesManager */
public class Tiles {
    /* TODO: add Entrance entrance and Exit exit so that we can access something like tiles.entrance.position.getX()
    *   and they should extend the GameObject (Or Tile? Or Cell?)
    */
    public TiledMapTileLayer layer;

    //public Position entrancePosition;
    //public Position entranceTilePosition;
    /** List of positions of the exit tiles in world coordinates. (there might be more than one exit) */
    //public List<Position> exitPositions;
    public Position keyTilePosition;

    /** entrance tile, coordinates of the tile can be accessed through this */
    public Entrance entrance;
    /** exit tile, coordinates of the tile can be accessed through this */
    public Exit exit;

    private StaticTiledMapTile[] tiles;

    // Create an immutable Set of integers representing wall
    // IntStream.concat(IntStream.rangeClosed(10, 29),IntStream.rangeClosed(64, 66)) in case i want to concat two sections in the future
    private static final Set<Integer> WALLS = IntStream.rangeClosed(10, 29)
            .boxed()
            .collect(Collectors.toSet());
    public static final int KEY = 6;
    public static final int ENTRANCE = 1;
    public static final int EXIT = 2;

    /**
     * Constructor: initializes the Tiles object with default values.
     */
    public Tiles() {
        //entrancePosition = new Position(0, 0, PIXELS); // null; // PIXELS
        //entranceTilePosition = new Position(0, 0, TILES); // null; // TILES
        //exitPositions = new ArrayList<>();
        keyTilePosition = new Position(0, 0, TILES);
    }

    /**
     * Loads a tiled map from the specified map and tile sheet files.
     *
     * @param mapFilePath       Path to the map properties file.
     * @param tileSheetPath     Path to the tile sheet image.
     * @param mapWidthInTiles   Width of the map in tiles.
     * @param mapHeightInTiles  Height of the map in tiles.
     * @return The created {@link TiledMap} object.
     */
    public TiledMap loadTiledMap(String mapFilePath, String tileSheetPath, int mapWidthInTiles, int mapHeightInTiles) {
        // Load the tile sheet
        var tileSheet = new Texture(tileSheetPath);
        int tileCols = tileSheet.getWidth() / TILE_SIZE;
        int tileRows = tileSheet.getHeight() / TILE_SIZE;
        tiles = new StaticTiledMapTile[tileCols * tileRows];
        // Create tiles based on the tile sheet
        for (int y = 0; y < tileRows; y++) {
            for (int x = 0; x < tileCols; x++) {
                int index = y * tileCols + x;
                TextureRegion tileRegion = new TextureRegion(tileSheet, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                if (WALLS.contains(index)){
                    tiles[index] = new StaticTiledMapTile(tileRegion);
                    tiles[index].getProperties().put("collidable", true);
                }
                else if (index == ENTRANCE){
                    entrance = new Entrance(tileRegion);
                    tiles[index] = entrance;
                    tiles[index].getProperties().put("isEntrance", true); // TODO: need to change this later maybe? not that many boolean values...
                }
                else if (index == EXIT){
                    exit = new Exit(tileRegion);
                    tiles[index] = exit;
                    tiles[index].getProperties().put("isExit", true);
                }
                else {
                    tiles[index] = new StaticTiledMapTile(tileRegion);
                }


                /* this is for replacing the code below to avoid a long chain of else-if conditions
                // outside the loop
                Set<Integer> collidableTiles = new HashSet<>(Arrays.asList(0, 3));


                // inside here
                    boolean isCollidable = collidableTiles.contains(index);
                    tiles[index].getProperties().put("collidable", isCollidable);
                */
            }
        }

        // Parse ".properties" file
        ObjectMap<String, Integer> mapData = parsePropertiesFile(mapFilePath);

        // Create a TiledMap
        TiledMap map = new TiledMap();
        layer = new TiledMapTileLayer(mapWidthInTiles, mapHeightInTiles, TILE_SIZE, TILE_SIZE); // put our width/height here

        // Populate the layer with tiles
        try{
            for (String key : mapData.keys()) {
                Position position = stringToPosition(key);
                int x = position.getTileX();
                int y = position.getTileY();
                int tileValue = mapData.get(key);

                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                StaticTiledMapTile tile = tiles[tileValue];
                cell.setTile(tile);
                layer.setCell(x, y, cell);

                // Set entrance and exit positions when the entrances and exits are met
                if (tile instanceof Entrance){ // Tile 13: Entrance
                    entrance.setTilePosition(new Position(x, y, TILES));
                    //entranceTilePosition = new Position(x, y, TILES);
                    //entrancePosition = entranceTilePosition.convertTo(PIXELS);
                }
                if (tile instanceof Exit){ // Tile 20: Exit
                    //Position exitPosition = new Position(x, y, TILES).convertTo(PIXELS);
                    //exitPositions.add(exitPosition);
                    exit.addTilePosition(new Position(x, y, TILES));
                    Gdx.app.log("Exit", "exit found at: " + x + ", " + y);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.error("Tiles", "Error loading tiles: ", e);
        }


        map.getLayers().add(layer);
        Gdx.app.log("Tiles", "Tiled Map loaded");
        Gdx.app.log("Tiles", "entrance position: " + entrance.getTilePosition());
        return map;
    }

    /**
     * Parses the properties file for the tile map.
     *
     * @param filePath Path to the properties file.
     * @return An {@link ObjectMap} containing map data.
     */
    private ObjectMap<String, Integer> parsePropertiesFile(String filePath) {
        ObjectMap<String, Integer> mapData = new ObjectMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("=")) continue; // ignore this line
                String[] parts = line.split("="); // split, parts[0] is position (String) and parts[1] is the tileType or tileValue
                String[] tileTypes = parts[1].split(","); // could contain key so a grid might have duplicates
                // if there is the key, iterate through tileTypes
                for (String tileType : tileTypes) {
                    int tileValue = Integer.parseInt(tileType);
                    if (tileValue != KEY)
                        mapData.put(parts[0], tileValue);
                    else { // get the key position
                        Position position = stringToPosition(parts[0]);
                        keyTilePosition = new Position(position.getTileX(), position.getTileY(), TILES);
                    }
                    Gdx.app.log("Tiles", "Parsed: " + parts[0] + " = " + tileType + "\tItems on this grid: " + tileTypes.length);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapData;
    }

    private Position stringToPosition(String string) {
        String[] parts = string.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        return new Position(x, y, TILES);
    }

    public StaticTiledMapTile[] getTiles() {
        return tiles;
    }

}
