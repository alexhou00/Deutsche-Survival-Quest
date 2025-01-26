package de.tum.cit.fop.maze.tiles;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Enum with constructor and ID parameter
public enum TileType{
    // Each TileType is defined with its basic ID, and optionally a range of IDs for special cases.

    // Represents walls, includes IDs 0, 20-79, and 100-149.
    WALL        (0, 20, 79, 100, 149),
    // Represents the entrance, with ID 1.
    ENTRANCE    (1),
    // Represents exits, includes IDs 2 and 12-14.
    EXIT        (2, 12, 14),
    // Represents traps, includes IDs 3 and 80-89.
    TRAP        (3, 80, 89),
    // Represents enemies, includes IDs 4 and 150-159.
    ENEMY       (4, 150, 159),
    // Represents the key, with ID 5.
    KEY         (5),
    // Represents speed boosts, includes IDs 90-99.
    SPEED_BOOST (90, null, 99),
    // Represents basic ground tiles, with ID 10.
    GROUND       (10), // or maybe other tiles that work like a ground
    // Represents extra tiles like trains, with ID 220.
    EXTRA       (220); // like the train, consider to be ground but coins shouldn't be generated there

    private final int basicId;          // The basic ID for the tile type.
    private final Integer secondId;     // The starting ID of an additional range (optional).
    private final Integer finalId;      // The ending ID of the additional range (optional).
    private final Integer secondId2;    // A second starting ID range (optional, for multiple ranges).
    private final Integer finalId2;     // A second ending ID range (optional).

    // Constructor for types with one range of additional IDs.
    TileType(int basicId, Integer secondId, Integer finalId) {
        this.basicId = basicId;
        this.secondId = secondId;
        this.finalId = finalId;
        this.secondId2 = null;
        this.finalId2 = null;
    }

    // Constructor for types with only a basic ID.
    TileType(int basicId) {
        this.basicId = basicId;
        this.secondId = null;
        this.finalId = null;
        this.secondId2 = null;
        this.finalId2 = null;
    }

    // Constructor for types with two ranges of additional IDs.
    TileType(int basicId, Integer secondId, Integer finalId, Integer secondId2, Integer finalId2) {
        this.basicId = basicId;
        this.secondId = secondId;
        this.finalId = finalId;

        this.secondId2 = secondId2;
        this.finalId2 = finalId2;
    }

    // Getter for the basic ID.
    public int getId() {
        return basicId;
    }

    public int getFirst() {
        return getId();
    }

    // Getter for the first range's starting ID.
    public Integer getSecond() {
        return secondId;
    }

    public Integer getFinal() {
        return finalId;
    }

    // Combines all ID ranges into a Set of integers.
    public Set<Integer> getAll() {
        if (secondId2 != null && finalId2 != null && getSecond() != null && getFinal() != null) { // like wall
            // Combines the basic ID, first range, and second range.
            return IntStream.concat(IntStream.concat(
                                    IntStream.of(getId()),
                                    IntStream.rangeClosed(getSecond(), getFinal())),
                            IntStream.rangeClosed(secondId2, finalId2))
                    .boxed()
                    .collect(Collectors.toSet());
        }
        else if (getSecond() != null && getFinal() != null) { // like enemy
            // Combines the basic ID and the first range.
            return IntStream.concat(
                            IntStream.of(getId()),
                            IntStream.rangeClosed(getSecond(), getFinal()))
                    .boxed()//Converts the primitive int values in the stream into their wrapper class, Integer
                    //[1, 2, 3, 10, 11, ...] becomes [Integer(1), Integer(2), ...]
                    .collect(Collectors.toSet());//Collects the Integer values from the stream and stores them in a Set
        }
        else if (getSecond() == null && getFinal() != null) {
            // If only a single range is provided, includes all IDs in that range.
            return IntStream.rangeClosed(getId(), getFinal())
                    .boxed()
                    .collect(Collectors.toSet());
        }
        // If no additional ranges are provided, return an empty set.
        return new HashSet<>();
    }
}