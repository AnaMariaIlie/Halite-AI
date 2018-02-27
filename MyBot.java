import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class MyBot {
	public static void main(String[] args) throws java.io.IOException {

		final InitPackage iPackage = Networking.getInit();
		final int myID = iPackage.myID;
		final GameMap gameMap = iPackage.map;

		Networking.sendInit("MyJavaBot");

		while (true) {
			List<Move> moves = new ArrayList<Move>();
			PriorityQueue<Site> priorityQueue = new PriorityQueue<Site>(
					new SiteComparator());

			Networking.updateFrame(gameMap);
			/**
			 * Initializam fiecare casuta.
			 **/
			for (int y = 0; y < gameMap.height; y++) {
				for (int x = 0; x < gameMap.width; x++) {
					final Location location = gameMap.getLocation(x, y);
					final Site site = location.getSite();
					site.setX(x);
					site.setY(y);
					site.setInserted(false);
					site.setPower(Integer.MIN_VALUE);
					site.futureStrength = 0;
					site.combat = 0;
					site.futureDirection = Direction.STILL;
				}
			}

			/*
			 * Dam o putere fiecarui vecin care nu este al nostru si daca nu a
			 * fost deja inserat in coada de prioritati,il inseram.
			 */
			for (int y = 0; y < gameMap.height; y++) {
				for (int x = 0; x < gameMap.width; x++) {
					final Location location = gameMap.getLocation(x, y);
					final Site site = location.getSite();

					if (site.owner == myID) {
						site.futureStrength = site.strength;
						for (Site neighbour : site.getNeighbours(gameMap,
								location)) {
							if (neighbour.owner != myID
									&& neighbour.inserted == false) {
								neighbour.setInserted(true);
								neighbour.setPower(7 * neighbour.production
										- neighbour.strength);
								priorityQueue.add(neighbour);
							}
						}
					}

				}
			}
			/*
			 * Pentru vecinii neinserati care ne apartin, ai fiecarei casute din
			 * PQ setam o noua putere.
			 */
			while (!priorityQueue.isEmpty()) {
				Site currentSite = priorityQueue.poll();

				for (Site neighbour : currentSite.getNeighbours(gameMap, gameMap
						.getLocation(currentSite.xCoord, currentSite.yCoord))) {

					if (neighbour.owner == myID
							&& neighbour.inserted == false) {
						neighbour.setInserted(true);
						neighbour.setPower(currentSite.power - 10);
						priorityQueue.add(neighbour);
					}
				}
			}

			for (int y = 0; y < gameMap.height; y++) {
				for (int x = 0; x < gameMap.width; x++) {
					final Location location = gameMap.getLocation(x, y);
					final Site site = location.getSite();

					if (site.owner == myID) {
						int max = Integer.MIN_VALUE;
						int position = 0;

						for (int i = 0; i < site
								.getNeighbours(gameMap, location).size(); i++) {

							if (site.getNeighbours(gameMap, location)
									.get(i).power > max) {
								max = site.getNeighbours(gameMap, location)
										.get(i).power;
								position = i;

							}
						}

						Direction direction = bestDirection(position);
						if (isolated(site, myID, gameMap, location) == 3) {

							forceMove(site, myID, gameMap, location, moves);

						} else {
							if (isolated(site, myID, gameMap, location) == 4
									&& site.production < site.strength) {

								moveOK(gameMap.getSite(location, direction),
										site, myID, gameMap, location,
										direction, moves);

							} else {

								if (over(site, myID, gameMap, location) == 5) {
									if (gameMap.getSite(location,
											direction).owner == myID) {

										if (site.production < site.strength) {
											moveOK(gameMap.getSite(location,
													direction), site, myID,
													gameMap, location,
													direction, moves);
										} else {
											moves.add(new Move(location,
													Direction.STILL));
										}
									}

									else {
										if (site.strength > gameMap.getSite(
												location, direction).strength) {
											moveOK(gameMap.getSite(location,
													direction), site, myID,
													gameMap, location,
													direction, moves);
										} else {
											moves.add(new Move(location,
													Direction.STILL));
										}
									}

								} else {
									moveOK(gameMap.getSite(location, direction),
											site, myID, gameMap,
											location, bestDirection(over(site,
													myID, gameMap, location)),
											moves);
								}
							}

						}

					}
				}
			}

			Networking.sendFrame(moves);
		}
	}

	/*
	* Trateaza cazul de overkill.
	*/
	public static int over(Site mySite, int id, GameMap gameMap,
			Location location) {

		int dmg_maxS = 0, pos = 5, posP = 5, dmg_maxP = 0;

		List<Site> neighbours = new ArrayList<Site>();
		neighbours = mySite.getNeighbours(gameMap, location);

		for (int i = 0; i < neighbours.size(); i++) {
			if (neighbours.get(i).owner != id) {
				List<Site> neighbours_new = new ArrayList<Site>();
				int dmgS = 0;
				int dmgP = 0;

				Location new_loc = new Location(neighbours.get(i).xCoord,
						neighbours.get(i).yCoord, neighbours.get(i));
				neighbours_new = neighbours.get(i).getNeighbours(gameMap,
						new_loc);

				if (neighbours.get(i).owner != 0
						&& neighbours.get(i).owner != id) {
					dmgS += neighbours.get(i).strength;
					dmgP += neighbours.get(i).production;
				}

				for (int o = 0; o < neighbours_new.size(); o++) {
					if (neighbours_new.get(o).owner != id
							&& neighbours_new.get(o).owner != 0) {
						dmgS += neighbours_new.get(o).strength;
						dmgP += neighbours.get(i).production;
					}
				}

				if (dmgS < dmg_maxS) {
					pos = i;
					dmg_maxS = dmgS;
				}

				if (dmgP > dmg_maxP) {
					posP = i;
					dmg_maxP = dmgP;
				}
			}
		}

		if (pos == posP) {
			return pos;
		} else {
			return posP;
		}
	}

	/*
	* Deblocheaza celulele puternice lasandu-le sa atace marginile.
	*/
	public static void forceMove(Site mySite, int id, GameMap gameMap,
			Location location, List<Move> moves) {

		List<Site> neighbours = new ArrayList<Site>();
		neighbours = mySite.getNeighbours(gameMap, location);
		Direction direction;
		for (int i = 0; i < neighbours.size(); i++) {
			if (neighbours.get(i).owner != id
					&& neighbours.get(i).strength < mySite.strength) {
				direction = bestDirection(i);
				moveOK(gameMap.getSite(location, direction), mySite, id,
						gameMap, location, direction, moves);
			}
		}
	}

	/*
	*Alegem directia cea mai buna.
	*/
	public static Direction bestDirection(int position) {
		Direction direction = Direction.STILL;
		switch (position) {
		case 0:
			direction = Direction.NORTH;
			break;

		case 1:
			direction = Direction.SOUTH;
			break;

		case 2:
			direction = Direction.WEST;
			break;

		case 3:
			direction = Direction.EAST;
			break;

		default:
			direction = Direction.STILL;
			break;
		}

		return direction;
	}

	/*
	*Verifica de cate celule de ale noastre este inconjurata o celula.
	*/
	public static int isolated(Site mySite, int id, GameMap gameMap,
			Location location) {

		List<Site> neighbours = new ArrayList<Site>();
		neighbours = mySite.getNeighbours(gameMap, location);
		int in = 0;
		for (int i = 0; i < neighbours.size(); i++) {
			if (neighbours.get(i).owner == id) {
				in++;
			}
		}

		return in;

	}

	/*
	*Testeza daca o casuta se poate muta intr-un anumit loc, pentru ca nu-si da singur overkill.
	*/
	public static void moveOK(Site neutralSite, Site mySite, int id,
			GameMap gameMap, Location location, Direction direction,
			List<Move> moves) {

		if (mySite.strength + neutralSite.futureStrength > 255
				&& neutralSite.owner == id) {

			moves.add(new Move(location, Direction.STILL));
		} else {

			moves.add(new Move(location, direction));
			neutralSite.futureStrength += mySite.strength;
			mySite.futureStrength = 0;
			mySite.strength = 0;
		}

	}

	private static class SiteComparator implements Comparator<Site> {

		/**
		 * Functie ce compara doua puterile a doua casute.
		 */
		@Override
		public int compare(Site a, Site b) {

			return (int) (b.power - a.power);

		}
	}
}
