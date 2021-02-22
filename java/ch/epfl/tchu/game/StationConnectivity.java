package ch.epfl.tchu.game;

public interface StationConnectivity {

    /**
     * Verifies if two stations are connected
     * @param s1 : first train station
     * @param s2 : second train station
     * @return : true if they are connected by a (single) players wagons, false otherwise
     */
    public abstract boolean connected(Station s1, Station s2);

    //more to be added in later steps (4)
}
