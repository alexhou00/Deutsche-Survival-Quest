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
    public TiledMapTileLayer layer;

    //public Position entrancePosition;
    //public Position entranceTilePosition;
    /** List of positions of the exit tiles in world coordinates. (there might be more than one exit) */
    //public List<Position> exitPositions;
    private Position keyTilePosition;

    /** entrance tile, coordinates of the tile can be accessed through this */
    public Entrance entrance;
    /** exit tile, coordinates of the tile can be accessed through this */
    public List<Exit> exits;

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
        // tiles is the tileset
        tiles = new StaticTiledMapTile[tileCols * tileRows];
        // Create tiles based on the tile sheet
        for (int y = 0; y < tileRows; y++) {
            for (int x = 0; x < tileCols; x++) {
                int index = y * tileCols + x;
                TextureRegion tileRegion = new TextureRegion(tileSheet, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                if (WALLS.contains(index)){
                    tiles[index] = new Wall(tileRegion);
                    tiles[index].getProperties().put("collidable", true);
                }
                else if (index == ENTRANCE){
                    entrance = new Entrance(tileRegion);
                    tiles[index] = entrance;
                    tiles[index].getProperties().put("isEntrance", true); // TODO: need to change this later maybe? not that many boolean values...
                }
                else if (index == EXIT){
                    Exit exit = new Exit(tileRegion);
                    exits.add(exit);
                    tiles[index] = exit;
                    tiles[index].getProperties().put("isExit", true);
                }
                else {
                    tiles[index] = new StaticTiledMapTile(tileRegion);
                }
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

                // Set entrance and exit positions when the entrances and exits are met,
                // We only know the position after parsing and start to create our map
                if (tile instanceof Entrance){ // Tile 13: Entrance
                    entrance.setTilePosition(new Position(x, y, TILES));
                }
                if (tile instanceof Exit){ // Tile 20: Exit
                    Exit exit = exits.get(exits.indexOf(tile));
                    exit.setTilePosition(new Position(x, y, TILES));
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

    public StaticTiledMapTile[] getTiles() {
        return tiles;
    }

    public Position getKeyTilePosition() {
        return keyTilePosition;
    }

}
