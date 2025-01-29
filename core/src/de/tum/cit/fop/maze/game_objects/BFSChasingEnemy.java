package de.tum.cit.fop.maze.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.level.LevelManager;
import de.tum.cit.fop.maze.tiles.TileType;
import de.tum.cit.fop.maze.util.Position;

import java.util.*;

import static de.tum.cit.fop.maze.util.Constants.*;
import static de.tum.cit.fop.maze.util.Position.getTilePosition;
import static java.lang.Math.abs;

public class BFSChasingEnemy extends ChasingEnemy {

    private final Random random;
    private final List<int[]> shuffledDirections;

    /**
     * Constructs a new BFSChasingEnemy object, initializing its properties and setting up the enemy's behavior.
     * This constructor sets the detection radius, random seed, and initializes the movement directions with
     * randomness for exploration. It also calls the superclass constructor to initialize shared attributes.
     *
     * @param textureRegion   The texture region to represent the enemy's appearance.
     * @param tileX           The initial X coordinate of the enemy in the grid (tile-based system).
     * @param tileY           The initial Y coordinate of the enemy in the grid (tile-based system).
     * @param width           The width of the enemy's hitbox in pixels.
     * @param height          The height of the enemy's hitbox in pixels.
     * @param hitboxWidth     The width of the enemy's hitbox on the screen.
     * @param hitboxHeight    The height of the enemy's hitbox on the screen.
     * @param widthOnScreen   The width of the enemy on the screen in world units.
     * @param heightOnScreen  The height of the enemy on the screen in world units.
     * @param lives           The number of lives the enemy has.
     * @param levels          The LevelManager responsible for managing levels in the game.
     * @param game            The main game instance.
     * @param enemyIndex      The unique index of the enemy, used for identifying the enemy in a collection.
     *
     * Initializes the enemy with the following properties:
     * <ul>
     *     <li>Detection radius is set to 600 units.</li>
     *     <li>Random seed is generated using the object's hash code and the enemy's tile position to ensure
     *         randomness based on its location.</li>
     *     <li>Movement directions (right, left, up, down) are defined and shuffled to introduce unpredictability
     *         in exploration patterns.</li>
     * </ul>
     */
    public BFSChasingEnemy(TextureRegion textureRegion, int tileX, int tileY, int width, int height, int hitboxWidth, int hitboxHeight,
                           float widthOnScreen, float heightOnScreen, float lives, LevelManager levels, MazeRunnerGame game, int enemyIndex) {
        super(textureRegion, tileX, tileY, width, height, hitboxWidth, hitboxHeight, widthOnScreen, heightOnScreen, lives, levels, game, enemyIndex);
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

        if (handleCooldown(player, delta)) return;

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

            for (ChasingEnemy enemy : iterate(levels.chasingEnemies)) {
                if (!enemy.equals(this) && enemy.isTouching(this)) {
                    targetX = x + (x - enemy.getX()) * 5000;
                    targetY = y + (y - enemy.getY()) * 5000;
                    moveTowardsTarget(delta);
                    System.out.println("Towards Target Moved Away from Other enemies because of touching...");
                }
            }
            return true;

        }
        return false; // No valid path
    }

    /**
     * Finds the shortest path from the current position to the player's position using Breadth-First Search (BFS).
     * This method returns a list of positions representing the path from the current location to the target (player) location.
     * If no path is found, it returns null.
     *
     * @param playerX The X-coordinate of the player's position in world coordinates.
     * @param playerY The Y-coordinate of the player's position in world coordinates.
     * @return A list of positions representing the path from the current position to the player's position, or null if no path is found.
     */
    private List<Position> findPathTo(float playerX, float playerY) {
        if (player == null) return null;

        Position start = getTilePosition(x, y);
        Position goal = getTilePosition(playerX, playerY);

        Queue<Position> queue = new LinkedList<>();
        Map<Position, Position> cameFrom = new HashMap<>();
        Set<Position> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            if (current.equals(goal)) {
                // Path found
                Gdx.app.log("BFS", "Path found");
                return reconstructPath(cameFrom, start, goal);
            }

            for (Position neighbor : getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }

        return null; // No path found
    }

    /**
     * Returns a list of neighboring positions for a given position based on valid movement directions.
     * It checks adjacent tiles in all four cardinal directions (up, down, left, right), ensuring they are within bounds
     * and walkable.
     *
     * @param position The current position for which the neighbors are being calculated.
     * @return A list of valid neighboring positions that are walkable and within the grid boundaries.
     */
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

    /**
     * Checks if a tile at the specified coordinates (x, y) is walkable.
     * The tile is considered walkable if its type is not a wall or trap.
     *
     * @param x The X-coordinate of the tile on the map.
     * @param y The Y-coordinate of the tile on the map.
     * @return True if the tile is walkable (not a wall or trap), false otherwise.
     */
    private boolean isTileWalkable(int x, int y) {
        try {
            TileType tileType = levels.getTileEnumOnMap(x, y);
            return tileType != TileType.WALL && tileType != TileType.TRAP;//tileType == LevelManager.TileType.OTHER || tileType == LevelManager.TileType.EXIT || tileType == LevelManager.TileType.EXTRA;
        }
        catch (ArrayIndexOutOfBoundsException e){
            Gdx.app.error("BFS Enemy", x  + ", " + y + e.getMessage());
            return false;
        }
    }


    /**
     * Reconstructs the path from the start position to the goal position using the "cameFrom" map,
     * which tracks the positions visited during pathfinding.
     * The method follows the trail back from the goal to the start, and then reverses the list
     * to provide the path in the correct order.
     *
     * @param cameFrom A map that tracks the previous position for each visited position.
     * @param start The starting position from which the pathfinding began.
     * @param goal The goal position that the pathfinding tried to reach.
     * @return A list of positions representing the path from the start to the goal, in the correct order.
     */
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
        if (levels.getTileEnumOnMap(playerPosition.getTileX(), playerPosition.getTileY()).equals(TileType.WALL)) {
            //return super.isPlayerWithinDetectionRadius(player, radius);
            // then, we do normal detection
            // actually, manhattan distance
            float dx = player.getX() - x;
            float dy = player.getY() - y;
            return dx + dy <= radius;
        }
        else{
            // surrounded by walls, let's just give up
            return false;
        }

    }

    static float directionChangeTimer = 0; // Tracks time since last direction change

    @Override
    public void setDirection(){
        directionChangeTimer += Gdx.graphics.getDeltaTime();

        if (abs(velX) > abs(velY)){
            previousDirection = (velX > 0) ? Direction.right : Direction.left;
        }

        if (abs(velY) > abs(velX) &&
                //abs(velY) > ENEMY_BASE_SPEED / 5 &&
                abs(((velX * velX) + (velY * velY)) - (previousVelX * previousVelX) + (previousVelY * previousVelY)) > 1000){
            previousDirection = (velY > 0) ? Direction.up : Direction.down;
        }

        directionChangeTimer = 0;

        previousVelX = velX;
        previousVelY = velY;
    }

    @Override
    public void draw(SpriteBatch batch, TextureRegion textureRegion) {
        super.draw(batch, textureRegion);
    }
}
