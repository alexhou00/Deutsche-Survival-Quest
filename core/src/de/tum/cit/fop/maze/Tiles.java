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

import static de.tum.cit.fop.maze.Constants.TILE_SIZE;

public class Tiles {

    public TiledMapTileLayer layer;

    public TiledMap loadTiledMap(String mapFilePath, String tileSheetPath, int mapWidthInTiles, int mapHeightInTiles) {
        // Load tile sheet
        var tileSheet = new Texture(tileSheetPath);
        int tileCols = tileSheet.getWidth() / TILE_SIZE;
        int tileRows = tileSheet.getHeight() / TILE_SIZE;

        // Create tiles based on tile sheet
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
                if (index == 0) { // Tile 0: Wall
                    tiles[index].getProperties().put("collidable", true);
                } else if (index == 3) { // Tile 3: Trap
                    tiles[index].getProperties().put("collidable", true);
                } else {
                    tiles[index].getProperties().put("collidable", false);
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

                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tiles[tileValue]);
                layer.setCell(x, y, cell);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Gdx.app.error("Tiles", "Error loading tiles: ", e);
        }


        map.getLayers().add(layer);
        Gdx.app.log("Tiles", "Tiled Map loaded");
        return map;
    }

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
