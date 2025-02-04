package cross.server;

import java.util.Comparator;

public class TimeOrderComparator implements Comparator<Order> {

    public int compare(Order o1, Order o2) {
        //Ordina per prezzo
        int priceComparison = Double.compare(o1.getTime(), o2.getTime());
        
        //Se i timestamp sono uguali, ordina per id
        if (priceComparison == 0) {
            return Integer.compare(o1.getOrderId(), o2.getOrderId());
        }
        
        return priceComparison;
    }
}