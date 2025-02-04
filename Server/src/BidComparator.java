package cross.server;

import java.util.Comparator;

public class BidComparator implements Comparator<Order> {

    public int compare(Order o1, Order o2) {
        return Long.compare(o2.getPrice(),o1.getPrice());
    }
}