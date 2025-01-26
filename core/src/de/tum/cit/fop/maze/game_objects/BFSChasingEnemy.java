package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.level.Tiles;
import de.tum.cit.fop.maze.util.Position;

import java.util.*;

import static de.tum.cit.fop.maze.util.Constants.*;
import static de.tum.cit.fop.maze.util.Position.getTilePosition;

public class BFSChasingEnemy extends ChasingEnemy {

    public BFSChasingEnemy(TextureRegion textureRegion, int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight,
                           float widthOnScreen, float heightOnScreen, float lives, Tiles tiles, MazeRunnerGame game, int enemyIndex) {
        super(textureRegion, tileX, tileY, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives, tiles, game, enemyIndex);
        detectionRadius = 600f;
    }


    protected void chase(Player player, float delta) {
        // Find the path to the player using BFS
        List<Position> path = findPathToPlayer();
        if (path != null && path.size() > 1) {
            // Set the next target to the next position in the path
            Position nextPosition = path.get(1);
            targetX = nextPosition.convertTo(Position.PositionUnit.PIXELS).getX();
            targetY = nextPosition.convertTo(Position.PositionUnit.PIXELS).getY();
            //moveTowardsTarget(delta);
        }

        // Use the parent class method for movement
        super.moveTowardsTarget(delta);
        // todo: there are still problems when the player is in the wall tile, the enemy doesn't chase
    }

    private List<Position> findPathToPlayer() {
        if (player == null) return null;
        Gdx.app.log("BFS Enemy", "Finding path to player...");

        Position start = getTilePosition(x, y);
        Position goal = getTilePosition(player.getX(), player.getY());

        Queue<Position> queue = new LinkedList<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        Set<Position> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            //Gdx.app.log("BFS Enemy", "Queue is not empty");
            Position current = queue.poll();
            //Gdx.app.log("BFS", "Current position: " + current);

            if (current.equals(goal)) {
                // Path found
                Gdx.app.log("BFS", "Path found");
                return reconstructPath(cameFrom, start, goal);
            }

            for (Position neighbor : getNeighbors(current)) {
                //Gdx.app.log("BFS", "Going to neighbor: " + neighbor);
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                    cameFrom.put(neighbor, current);
                    //Gdx.app.log("BFS", "Current position: " + current);
                    //Gdx.app.log("BFS", "Queue size: " + queue.size());
                    //Gdx.app.log("BFS", "Visited: " + visited.size());
                }
            }
        }

        return null; // No path found
    }

    private List<Position> getNeighbors(Position position) {
        List<Position> neighbors = new ArrayList<>();

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : directions) {
            int newX = position.getTileX() + dir[0];
            int newY = position.getTileY() + dir[1];

            if (newX >= 0 && newY >= 0 &&
                    newX < horizontalTilesCount && newY < verticalTilesCount &&
                    isTileWalkable(newX, newY)) {
                neighbors.add(new Position(newX, newY, Position.PositionUnit.TILES));
            }
        }

        return neighbors;
    }

    private boolean isTileWalkable(int x, int y) {
        try {
            Tiles.TileType tileType = tiles.getTileEnumOnMap(x, y);
            return tileType != Tiles.TileType.WALL && tileType != Tiles.TileType.TRAP;//tileType == Tiles.TileType.OTHER || tileType == Tiles.TileType.EXIT || tileType == Tiles.TileType.EXTRA;
        }
        catch (ArrayIndexOutOfBoundsException e){
            Gdx.app.error("BFS Enemy", x  + ", " + y + e.getMessage());
            return false;
        }
    }

    private List<Position> reconstructPath(Map<Position, Position> cameFrom, Position start, Position goal) {
        List<Position> path = new ArrayList<>();
        Position current = goal;

        while (!current.equals(start)) {
            path.add(current);
            current = cameFrom.get(current);
        }

        path.add(start);
        Collections.reverse(path);
        return path;
    }

    @Override
    protected boolean isPlayerWithinDetectionRadius(Player player, float radius) {
        if (player == null) return false;
        int detectionDistance = (int) radius / TILE_SCREEN_SIZE; // in tiles

        Position start = getTilePosition(x, y);
        Position goal = getTilePosition(player.getX(), player.getY());

        Queue<Position> queue = new LinkedList<>();
        Set<Position> visited = new HashSet<>();
        Map<Position, Integer> distances = new HashMap<>();

        queue.add(start);
        visited.add(start);
        distances.put(start, 0);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            int currentDistance = distances.get(current);
            if (currentDistance > detectionDistance) {
                return super.isPlayerWithinDetectionRadius(player, radius);
            }

            if (current.equals(goal)) {
                return currentDistance <= detectionDistance; // Check if the player is within 10 tiles
            }

            for (Position neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                    distances.put(neighbor, currentDistance + 1);
                }
            }
        }

        // The Player is out of range (somewhere in the wall, or on the tile that has parts of walls on it
        //Gdx.app.log("BFS Enemy", "detect cc");
        return super.isPlayerWithinDetectionRadius(player, radius); //then, we do normal detection
    }

    @Override
    public void draw(SpriteBatch batch, TextureRegion textureRegion) {
        super.draw(batch, textureRegion);
    }
}
