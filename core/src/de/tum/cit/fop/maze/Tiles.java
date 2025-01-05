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

import static de.tum.cit.fop.maze.Constants.*;
import static de.tum.cit.fop.maze.Position.PositionUnit.*;

public class Tiles {

    public TiledMapTileLayer layer;
    /** Coordinates of the entrance tile in world coordinates. */
    public Position entrancePosition;
    public Position entranceTilePosition;
    /** List of positions of the exit tiles in world coordinates. (there might be more than one exit) */
    public List<Position> exitPositions;
    public Position keyTilePosition;

    public static final int WALL = 1;
    public static final int KEY = 6;
    public static final int ENTRANCE = 13;
    public static final int EXIT = 21;

    /**
     * Constructor: initializes the Tiles object with default values.
     */
    public Tiles() {
        entrancePosition = new Position(0, 0, PIXELS); // null; // PIXELS
        entranceTilePosition = new Position(0, 0, TILES); // null; // TILES
        exitPositions = new ArrayList<>();
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

        // Create tiles based on the tile sheet
        StaticTiledMapTile[] tiles = new StaticTiledMapTile[tileCols * tileRows];
        for (int y = 0; y < tileRows; y++) {
            for (int x = 0; x < tileCols; x++) {
                int index = y * tileCols + x;
                tiles[index] = new StaticTiledMapTile(new TextureRegion(tileSheet, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE));

                /* this is for replacing the code below to avoid a long chain of else-if conditions
                // outside the loop
                Set<Integer> collidableTiles = new HashSet<>(Arrays.asList(0, 3));


                // inside here
                    boolean isCollidable = collidableTiles.contains(index);
                    tiles[index].getProperties().put("collidable", isCollidable);
                */

                // the "collidable" property for specific tiles (e.g., Walls, Traps)
                if (index == WALL) { // Tile 0: Wall
                    tiles[index].getProperties().put("collidable", true);
                } else if (index == 3) { // Tile 3: Trap
                    tiles[index].getProperties().put("collidable", true);
                } else {
                    tiles[index].getProperties().put("collidable", false);
                }

                if (index == EXIT){
                    tiles[index].getProperties().put("isExit", true);
                }

                if (index == KEY) {
                    tiles[index].getProperties().put("isKey", true);
                }
            }

        }

        // Parse properties file
        ObjectMap<String, Integer> mapData = parsePropertiesFile(mapFilePath);

        // Create a TiledMap
        TiledMap map = new TiledMap();
        layer = new TiledMapTileLayer(mapWidthInTiles, mapHeightInTiles, TILE_SIZE, TILE_SIZE); // put our width/height here

        // Populate the layer with tiles
        try{
            for (String key : mapData.keys()) {
                String[] parts = key.split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int tileValue = mapData.get(key);

                if (tileValue != KEY) {
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(tiles[tileValue]);
                    layer.setCell(x, y, cell);

                    // Set entrance and exit positions when the entrances and exits are met
                    if (tileValue == ENTRANCE){ // Tile 13: Entrance
                        entranceTilePosition = new Position(x, y, TILES);
                        entrancePosition = entranceTilePosition.convertTo(PIXELS);
                    }
                    if (tileValue == EXIT){ // Tile 20: Exit
                        Position exitPosition = new Position(x, y, TILES).convertTo(PIXELS);
                        exitPositions.add(exitPosition);
                    }
                }
                else{ // tile is KEY -> record the location.
                    keyTilePosition = new Position(x, y, TILES);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.error("Tiles", "Error loading tiles: ", e);
        }


        map.getLayers().add(layer);
        Gdx.app.log("Tiles", "Tiled Map loaded");
        Gdx.app.log("Tiles", "entrance position: " + entrancePosition);
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
                if (!line.contains("=")) continue;
                String[] parts = line.split("=");
                mapData.put(parts[0], Integer.parseInt(parts[1]));
                Gdx.app.log("Tiles", "Parsed: " + parts[0] + " = " + parts[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapData;
    }

}
