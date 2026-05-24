package oop.mony.utils;

import java.text.DecimalFormat;

public final class MoneyFormatter {
    private static final DecimalFormat VND_NUMBER = new DecimalFormat("#,##0");

    private MoneyFormatter() {
    }

    public static String formatVnd(double amount) {
        return VND_NUMBER.format(Math.round(amount)) + " ₫";
    }

    public static String formatEditableNumber(double amount) {
        if (amount == Math.rint(amount)) {
            return String.valueOf((long) amount);
        }
        return String.valueOf(amount);
    }
}
