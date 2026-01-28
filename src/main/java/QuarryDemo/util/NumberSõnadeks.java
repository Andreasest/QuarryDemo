package QuarryDemo.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberSõnadeks {

    private static final String[] UNITS = {
            "", "üks", "kaks", "kolm", "neli", "viis", "kuus", "seitse", "kaheksa", "üheksa"
    };

    private static final String[] TEENS = {
            "kümme", "üksteist", "kaksteist", "kolmteist", "neliteist",
            "viisteist", "kuusteist", "seitseteist", "kaheksateist", "üheksateist"
    };

    private static final String[] TENS = {
            "", "", "kakskümmend", "kolmkümmend", "nelikümmend", "viiskümmend",
            "kuuskümmend", "seitsekümmend", "kaheksakümmend", "üheksakümmend"
    };

    public static String formatDouble(Double arv) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.##", symbols);
        return df.format(arv);
    }

    public static String convert(double number) {
        if (number < 0 || number > 1_000_000) {
            return "Toetus ainult 0 kuni 1 000 000";
        }

        int wholePart = (int) number;
        int fractionalPart = (int) Math.round((number - wholePart) * 100);


        StringBuilder result = new StringBuilder();


        result.append(convertBelowMillion(wholePart)+" eurot");

        // komaosa
        result.append(" ja ");
        if (fractionalPart < 10) result.append("0");
        result.append(fractionalPart+" senti.");
        String vastus=result.toString().trim();

        return (String.valueOf(vastus.charAt(0)).toUpperCase()+vastus.substring(1)).trim().replaceAll(" +", " ");
    }

    private static String convertBelowMillion(int number) {
        if (number == 0) return "null";
        if (number == 1_000_000) return "miljon";

        StringBuilder words = new StringBuilder();

        if (number >= 1000) {
            int thousands = number / 1000;
            if (thousands == 1) {
                words.append("üks tuhat ");
            } else {
                words.append(convertBelowThousand(thousands)).append(" tuhat ");
            }
            number %= 1000;
        }

        if (number > 0) {
            words.append(convertBelowThousand(number));
        }

        return words.toString();
    }

    private static String convertBelowThousand(int number) {
        StringBuilder part = new StringBuilder();

        if (number >= 100) {
            int hundreds = number / 100;
            part.append(UNITS[hundreds]).append("sada ");
            number %= 100;
        }

        if (number >= 20) {
            int tens = number / 10;
            part.append(TENS[tens]).append(" ");
            number %= 10;
        } else if (number >= 10) {
            part.append(TEENS[number - 10]).append(" ");
            number = 0;
        }

        if (number > 0 && number < 10) {
            part.append(UNITS[number]);
        }

        return part.toString();
    }
}
