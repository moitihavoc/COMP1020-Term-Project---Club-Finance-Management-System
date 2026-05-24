package oop.mony.controllers;

import java.text.DecimalFormat;

final class MoneyFormatter {
    private static final DecimalFormat VND_NUMBER = new DecimalFormat("#,##0");

    private MoneyFormatter() {
    }

    static String formatVnd(double amount) {
        return VND_NUMBER.format(Math.round(amount)) + " ₫";
    }

    static String formatEditableNumber(double amount) {
        if (amount == Math.rint(amount)) {
            return String.valueOf((long) amount);
        }
        return String.valueOf(amount);
    }
}
