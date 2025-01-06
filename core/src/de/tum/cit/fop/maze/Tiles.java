package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
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
    public TiledMapTileLayer layer;

    private Position keyTilePosition;

    /** entrance tile, coordinates of the tile can be accessed through this */
    public Entrance entrance;
    /** exit tile, coordinates of the tile can be accessed through this */
    public List<Exit> exits;

    private Tile[] tileset;
    private Tile[][] tileOnMap;

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
        keyTilePosition = new Position(0, 0, TILES);
        entrance = null;
        exits = new ArrayList<>();
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
        // `tileset` is the tileset
        tileset = new Tile[tileCols * tileRows];
        // Create the tileset to reference back to the tile type based on the tile sheet
        for (int y = 0; y < tileRows; y++) {
            for (int x = 0; x < tileCols; x++) {
                int index = y * tileCols + x;
                TextureRegion tileRegion = new TextureRegion(tileSheet, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                if (WALLS.contains(index)){
                    tileset[index] = new Wall(tileRegion);
                    //tileset[index].getProperties().put("collidable", true);
                }
                else if (index == ENTRANCE){
                    entrance = new Entrance(tileRegion);
                    tileset[index] = entrance;
                    //tileset[index].getProperties().put("isEntrance", true);
                }
                else if (index == EXIT){
                    Exit exit = new Exit(tileRegion);
                    exits.add(exit);
                    tileset[index] = exit;
                    //tileset[index].getProperties().put("isExit", true);
                }
                else {
                    tileset[index] = new Tile(tileRegion);
                }
            }
        }

        // Parse ".properties" file
        ObjectMap<String, Integer> mapData = parsePropertiesFile(mapFilePath);

        // Create a TiledMap
        TiledMap map = new TiledMap();
        layer = new TiledMapTileLayer(mapWidthInTiles, mapHeightInTiles, TILE_SIZE, TILE_SIZE); // put our width/height here
        tileOnMap = new Tile[mapWidthInTiles][mapHeightInTiles];

        // Populate the layer with tiles
        try{
            for (String key : mapData.keys()) {
                Position position = stringToPosition(key);
                int x = position.getTileX();
                int y = position.getTileY();
                int tileValue = mapData.get(key);

                Tile tile = tileset[tileValue];

                // deal with LibGDX own library
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tile);
                layer.setCell(x, y, cell);

                // create a new tile based on its type so that we won't be accessing the same tile from the array
                // also set its position on the map
                createAndPlaceNewTile(x, y, tile);
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
                if (!line.contains("=")) continue; // skip invalid lines

                String[] parts = line.split("="); // split into key-value, parts[0] is position (String) and parts[1] is the tileType or tileValue
                if (parts.length != 2) continue; // ignore malformed lines

                String position = parts[0];
                String[] tileTypes = parts[1].split(","); // Handle multiple tile types (could contain the key)

                for (String tileType : tileTypes) {
                    processTile(position, tileType, mapData);
                }

            }
        } catch (IOException e) {
            Gdx.app.error("TileMapParser", String.valueOf(e));
        }

        return mapData;
    }

    /**
     * Processes an individual tile and updates the map data.
     *
     * @param position   The position key of the tile. (Format: "x,y")
     * @param tileType   The tile value or type but as a string.
     * @param mapData    The mapData to update.
     */
    private void processTile(String position, String tileType, ObjectMap<String, Integer> mapData) {
        try {
            int tileValue = Integer.parseInt(tileType);
            if (tileValue != KEY) {
                mapData.put(position, tileValue);
            }
            else { // Handle the key tile
                Position parsedPosition = stringToPosition(position);
                keyTilePosition = new Position(parsedPosition.getTileX(), parsedPosition.getTileY(), TILES);
            }
        } catch (NumberFormatException e) {
            Gdx.app.error("TileMapParser", "Invalid tile value: " + tileType + " at position " + position, e);
        }
    }

    private Position stringToPosition(String string) {
        String[] parts = string.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        return new Position(x, y, TILES);
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

    /**
     * Creates a new tile based on its type (Wall, Entrance, Exit, or generic Tile),
     * so that we won't be accessing the same tile from the array if we still use the {@code Tile tile = tileset[tileValue];}; <br>
     * All types of existing tiles should be managed here,
     * and this method update its position on the tileOnMap array
     *
     * @param x    The x-coordinate of the tile in the map grid.
     * @param y    The y-coordinate of the tile in the map grid.
     * @param tile The original tile to process and to replace.
     */
    private void createAndPlaceNewTile(int x, int y, Tile tile) {
        // Set entrance and exit positions when the entrances and exits are met,
        // We only know the position after parsing and start to create our map
        Tile newTile;

        if (tile instanceof Wall) {
            newTile = new Wall(tile.getTextureRegion());
        }
        else if (tile instanceof Entrance) {
            newTile = new Entrance(tile.getTextureRegion());

            entrance.setTilePosition(new Position(x, y, TILES));
        }
        else if (tile instanceof Exit) {
            newTile = new Exit(tile.getTextureRegion());

            Exit exit = exits.get(exits.indexOf(tile));
            exit.setTilePosition(new Position(x, y, TILES));
        }
        else {
            newTile = new Tile(tile.getTextureRegion());
        }

        tileOnMap[x][y] = newTile;
        tileOnMap[x][y].setTilePosition(new Position(x, y, TILES));
    }

}
