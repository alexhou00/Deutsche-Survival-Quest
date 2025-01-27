package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.level.Tiles;
import de.tum.cit.fop.maze.tiles.TileType;
import de.tum.cit.fop.maze.util.Position;

import java.util.*;

import static de.tum.cit.fop.maze.util.Constants.*;
import static de.tum.cit.fop.maze.util.Position.getTilePosition;

public class BFSChasingEnemy extends ChasingEnemy {

    private final Random random;
    private final List<int[]> shuffledDirections;

    public BFSChasingEnemy(TextureRegion textureRegion, int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight,
                           float widthOnScreen, float heightOnScreen, float lives, Tiles tiles, MazeRunnerGame game, int enemyIndex) {
        super(textureRegion, tileX, tileY, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives, tiles, game, enemyIndex);
        detectionRadius = 600f;
        random = new Random(this.hashCode() + tileX * 31L + 31L * 31 * tileY); // Seed the random generator with the unique hashcode
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        // Shuffle the direction array to introduce randomness in exploration
        shuffledDirections = Arrays.asList(directions);
        Collections.shuffle(shuffledDirections, random);
    }

    /**
     * Chases the player by determining the optimal path to the player's position using BFS (Breadth-First Search).
     * Override method of {@link ChasingEnemy#chase}
     * <p>
     * If the path to the player is unavailable, the method checks the surrounding 3x3 grid
     * to find an alternative path. If all paths fail, the default chase behavior from the superclass is invoked.
     * <p>
     * This method reduces the enemy's alert time and ensures it moves strategically towards the player.
     *
     * @param player The {@link Player} instance that the enemy chases.
     * @param delta  The time in seconds since the last frame.
     */
    protected void chase(Player player, float delta) {
        // Remember: decrease the alert timer
        alertTime -= delta;

        // Find the path to the player using BFS
        List<Position> path = findPathTo(player.getX(), player.getY());
        // If a path exists and has more than one step
        if (processPath(path, delta))
            return;

        // ELSE path isn't found:
        // If no direct path is found, search in a surrounding 3x3 grid for an alternative path
        boolean reachedAlternative = false;
        for (int offsetX = -1; offsetX <= 1; offsetX++){
            for (int offsetY = -1; offsetY <= 1; offsetY++){
                if (offsetX == 0 && offsetY == 0) continue; // Skip the current tile
                // Attempt to find a path to the player from an adjacent tile
                float altX = player.getX() + offsetX * TILE_SCREEN_SIZE;
                float altY = player.getY() + offsetY * TILE_SCREEN_SIZE;
                if ((altX - x) * (altX - x) + (altY - y) * (altY - y) < TILE_SCREEN_SIZE * TILE_SCREEN_SIZE * 2){
                    reachedAlternative = true;
                    break;
                }

                List<Position> alternativePath = findPathTo(altX, altY);
                if (!reachedAlternative && processPath(alternativePath, delta)){
                    Gdx.app.log("BFS alt", "Alternative path found, chasing...");
                    return;
                }

            }
        }

        // If no alternative path is found, fallback to the default chase behavior of the ChasingEnemy
        // foundAlternative == false:
        super.chase(player, delta); // normal chase

    }

    /**
     * Processes the path and moves towards the next position if a valid path exists.
     *
     * @param path  The list of positions representing the path to the target.
     * @param delta The time in seconds since the last frame.
     * @return {@code true} if the path is valid and the enemy moves; {@code false} otherwise.
     */
    private boolean processPath(List<Position> path, float delta) {
        if (path != null && path.size() > 1) {
            // Move towards the next position in the path
            Position nextPosition = path.get(1);
            targetX = nextPosition.convertTo(Position.PositionUnit.PIXELS).getX();
            targetY = nextPosition.convertTo(Position.PositionUnit.PIXELS).getY();
            super.moveTowardsTarget(delta);
            return true;
        }
        return false; // No valid path
    }

    private List<Position> findPathTo(float playerX, float playerY) {
        if (player == null) return null;
        //Gdx.app.log("BFS Enemy", "Finding path to player...");

        Position start = getTilePosition(x, y);
        Position goal = getTilePosition(playerX, playerY);

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

        for (int[] dir : shuffledDirections) {
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
            TileType tileType = tiles.getTileEnumOnMap(x, y);
            return tileType != TileType.WALL && tileType != TileType.TRAP;//tileType == Tiles.TileType.OTHER || tileType == Tiles.TileType.EXIT || tileType == Tiles.TileType.EXTRA;
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
            /*if (currentDistance > detectionDistance) {
                return super.isPlayerWithinDetectionRadius(player, radius);
            }*/

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
        // or surrounded by walls
        //Gdx.app.log("BFS Enemy", "detect cc");
        Position playerPosition = getTilePosition(player.getX(), player.getY());
        if (tiles.getTileEnumOnMap(playerPosition.getTileX(), playerPosition.getTileY()).equals(TileType.WALL)) {
            return super.isPlayerWithinDetectionRadius(player, radius); //then, we do normal detection
        }
        else{
            // surrounded by walls, let's just give up
            return false;
        }

    }

    @Override
    public void draw(SpriteBatch batch, TextureRegion textureRegion) {
        super.draw(batch, textureRegion);
    }
}
