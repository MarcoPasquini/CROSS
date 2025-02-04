package cross.server;

public class DaySummary {
	String day;
    long openingPrice;
    long closingPrice;
    long minPrice;
    long maxPrice;
    
    public DaySummary() {
        this.openingPrice = -1;
        this.closingPrice = -1;
        this.minPrice = -1;
        this.maxPrice = -1;
    }

    public DaySummary(String day, long openingPrice, long closingPrice, long minPrice, long maxPrice) {
        this.day = day;
        this.openingPrice = openingPrice;
        this.closingPrice = closingPrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}