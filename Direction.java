import java.util.Random;

public enum Direction {
    STILL, NORTH, EAST, SOUTH, WEST;

    public static final Direction[] DIRECTIONS = new Direction[]{STILL, NORTH, EAST, SOUTH, WEST};
    public static final Direction[] CARDINALS = new Direction[]{NORTH, EAST, SOUTH, WEST};

    public static Direction randomDirection() {
        Direction[] values = values();
        return values[new Random().nextInt(values.length)];
    }

    public static Direction randomNWDirection() {
    	//values- intoarce un array cu toate valorile din enum in ordinea declararii
        Direction[] values = new Direction[]{NORTH,WEST};
        return values[new Random().nextInt(values.length)];
    }
}
