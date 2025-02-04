package cross.server;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import cross.utils.DateValidator;

public class GetPriceHistoryStrategy extends ActionStrategy {
    
    private static final String DATE_FORMAT = "MM-yyyy";
    
    public GetPriceHistoryStrategy(ClientHandler clientHandler) {
        super(clientHandler);
    }

    public boolean requiresLogin() {
        return false;
    }

    public boolean requiresLogout() {
        return false;
    }

    public JsonObject constructResponse(JsonObject message) {
        JsonObject values = message.getAsJsonObject("values");
        if (values == null) return createHistoryResponse(101, null);

        String month = retriveStringValue(values, "month");
        if (!DateValidator.isPastMonth(month)) return createHistoryResponse(101, null);

        long[] firstAndLast = getFirstAndLastTimestamps(month);
        if (firstAndLast == null) return createHistoryResponse(101, null);
        
        String monthAndYear = getMonthAndYear(firstAndLast[0]);

        //Lettura degli ordini evasi dal file salvato in memoria
        String ordersFilePath = getSharedResources().getOrdersFile();
        Queue<Order> existingOrders = readExistingOrders(ordersFilePath);

        if (existingOrders == null) return createHistoryResponse(101, null);

        Map<String, DaySummary> monthMap = processOrders(existingOrders, firstAndLast, monthAndYear);

        //Costruisce la risposta JSON
        JsonArray days = buildJsonDays(monthMap);

        JsonObject root = new JsonObject();
        root.addProperty("response", 100);
        root.add("values", days);
        return root;
    }

    //Elabora gli ordini e crea il riepilogo per ogni giorno
    private Map<String, DaySummary> processOrders(Queue<Order> orders, long[] firstAndLast, String monthAndYear) {
        Map<String, DaySummary> monthMap = new HashMap<>();
        while (!orders.isEmpty()) {
            Order tmp = orders.poll();
            long orderTime = tmp.getTime() * 1000;

            //Ignora gli ordini fuori dal periodo richiesto
            if (orderTime < firstAndLast[0] || orderTime > firstAndLast[1]) {
                continue;
            }

            int day = getDayFromTimestamp(orderTime);
            String dayKey = String.valueOf(day);

            //Crea un nuovo riepilogo per il giorno se non esiste
            monthMap.putIfAbsent(dayKey, new DaySummary());

            DaySummary dS = monthMap.get(dayKey);
            if (dS.day == null) dS.day = day + "-" + monthAndYear;
            updateDaySummary(dS, tmp);
        }
        return monthMap;
    }
    //Aggiorna valori del giorno
    private void updateDaySummary(DaySummary daySummary, Order order) {
        int price = order.getPrice();
        if (daySummary.openingPrice == -1) daySummary.openingPrice = price;
        daySummary.closingPrice = price;
        daySummary.maxPrice = Math.max(daySummary.maxPrice, price);
        daySummary.minPrice = (daySummary.minPrice == -1) ? price : Math.min(daySummary.minPrice, price);
    }

    private JsonArray buildJsonDays(Map<String, DaySummary> monthMap) {
        JsonArray days = new JsonArray();
        for (int i = 1; i <= 31; i++) {
            String dayKey = String.valueOf(i);
            if (monthMap.containsKey(dayKey)) {
                DaySummary dS = monthMap.get(dayKey);
                JsonElement jsonElement = new GsonBuilder().setPrettyPrinting().create().toJsonTree(dS);
                days.add(jsonElement);
            }
        }
        return days;
    }

    private Queue<Order> readExistingOrders(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            Gson gson = new Gson();
            return gson.fromJson(jsonObject.get("trades"), new TypeToken<Queue<Order>>() {}.getType());
        } catch (IOException e) {
            return null;
        }
    }

    private String getMonthAndYear(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        return sdf.format(date);
    }

    private static long[] getFirstAndLastTimestamps(String monthYear) {
        long[] timestamps = new long[2];
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMyyyy", Locale.ENGLISH);
        
        try {
            Date date = dateFormat.parse(monthYear);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            //Primo giorno del mese
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            timestamps[0] = calendar.getTimeInMillis();

            //Ultimo giorno del mese
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            timestamps[1] = calendar.getTimeInMillis();
        } catch (ParseException e) {
            System.out.println("Invalid format");
            return null;
        }
        return timestamps;
    }

    private static int getDayFromTimestamp(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    private JsonObject createHistoryResponse(int response, JsonArray values) {
        JsonObject root = new JsonObject();
        root.addProperty("response", response);
        root.add("values", (values == null) ? new JsonArray() : values);
        return root;
    }
}
