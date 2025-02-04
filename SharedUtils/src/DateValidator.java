package cross.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateValidator {
	public static boolean isValidMonth(String date) {
        if (date == null || date.length() != 7) {
            return false; // Deve avere esattamente 7 caratteri
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMMYYYY", Locale.ENGLISH);
        sdf.setLenient(false); // Disabilita la tolleranza per valori ambigui

        try {
            sdf.parse(date); // Prova il parsing
            return true;
        } catch (ParseException e) {
            return false; // Parsing fallito, data non valida
        }
    }

    // Metodo per verificare se è un mese del passato
    public static boolean isPastMonth(String date) {
        if (!isValidStringFormat(date) || !isValidMonth(date)) {
            return false; // Se la data non è valida, non può essere un mese del passato
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMMYYYY", Locale.ENGLISH);
        try {
            Date inputDate = sdf.parse(date); // Converte la stringa in data
            Date currentDate = new Date(); // Ottiene la data corrente
            return !inputDate.after(currentDate); // Verifica se è nel passato
        } catch (ParseException e) {
            return false; // In caso di errore, considera la data non valida
        }
    }
    private static boolean isValidStringFormat(String month) {
		// Controlla se la stringa segue il formato "MMMYYYY" (es. "Jan2025")
        String regex = "^[A-Za-z]{3}\\d{4}$";  // Espressione regolare: 3 lettere seguite da 4 cifre
        if (!month.matches(regex))
            return false; // Se non corrisponde al formato, ritorna false
        return true;
	}
}
