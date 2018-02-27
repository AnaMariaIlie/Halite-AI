import java.util.ArrayList;
import java.util.List;

public class Site {

    public final int production;
    public int owner, strength;
    public int power;
    public int xCoord;
    public int yCoord;
    public int futureStrength;
    public Direction futureDirection;
    public int combat;

    boolean inserted;

    public void setPower(int power) {
    	this.power = power;
    }

    public void setX(int x) {
    	this.xCoord = x;
    }

    public void setY(int y) {
    	this.yCoord =y;
    }

    public void setInserted(boolean val) {
    	this.inserted =val;
    }

    public Site(int production) {
        this.production = production;
    }



    public List<Site> getNeighbours(GameMap map, Location location) {
    	List<Site> neighbours = new ArrayList<Site>();

    	neighbours.add(map.getSite(location, Direction.NORTH));
    	neighbours.add(map.getSite(location, Direction.SOUTH));
    	neighbours.add(map.getSite(location, Direction.WEST));
    	neighbours.add(map.getSite(location, Direction.EAST));

    	return neighbours;
    }



}

