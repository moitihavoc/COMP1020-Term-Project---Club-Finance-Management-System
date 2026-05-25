package oop.mony.utils;

import javafx.scene.control.TextField;

import java.text.DecimalFormat;

public final class MoneyUtils {
    private static final DecimalFormat GROUPED_NUMBER = new DecimalFormat("#,##0");

    private MoneyUtils() {
    }

    public static String formatVnd(double amount) {
        return GROUPED_NUMBER.format(Math.round(amount)) + " \u0111";
    }

    public static String format(double amount) {
        return GROUPED_NUMBER.format(Math.round(amount));
    }

    public static void attach(TextField field) {
        if (field == null) {
            return;
        }

        field.textProperty().addListener((obs, oldValue, newValue) -> {
            String current = newValue == null ? "" : newValue;
            String formatted = formatDigits(current);
            if (current.equals(formatted)) {
                return;
            }

            int digitCountBeforeCaret = countDigitsBefore(field.getText(), field.getCaretPosition());
            field.setText(formatted);
            field.positionCaret(caretPositionAfterDigitCount(formatted, digitCountBeforeCaret));
        });

        field.setText(formatDigits(field.getText()));
    }

    public static double parse(TextField field) {
        return parse(field == null ? "" : field.getText());
    }

    public static double parse(String text) {
        String digits = digitsOnly(text);
        if (digits.isEmpty()) {
            throw new NumberFormatException("Amount is empty.");
        }
        return Double.parseDouble(digits);
    }

    private static String formatDigits(String text) {
        String digits = digitsOnly(text);
        if (digits.isEmpty()) {
            return "";
        }
        return GROUPED_NUMBER.format(Long.parseLong(digits));
    }

    private static String digitsOnly(String text) {
        return text == null ? "" : text.replaceAll("[^0-9]", "");
    }

    private static int countDigitsBefore(String text, int caretPosition) {
        if (text == null || caretPosition <= 0) {
            return 0;
        }

        int count = 0;
        int limit = Math.min(caretPosition, text.length());
        for (int i = 0; i < limit; i++) {
            if (Character.isDigit(text.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    private static int caretPositionAfterDigitCount(String text, int digitCount) {
        if (digitCount <= 0) {
            return 0;
        }

        int seenDigits = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                seenDigits++;
                if (seenDigits == digitCount) {
                    return i + 1;
                }
            }
        }
        return text.length();
    }
}
