package oop.mony.utils;

import javafx.scene.control.TextField;

import java.text.DecimalFormat;

public final class MoneyInputFormatter {
    private static final DecimalFormat GROUPED_NUMBER = new DecimalFormat("#,##0");

    private MoneyInputFormatter() {
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

    public static String format(double amount) {
        return GROUPED_NUMBER.format(Math.round(amount));
    }

    private static String formatDigits(String text) {
        String digits = digitsOnly(text);
        if (digits.isEmpty()) {
            return "";
        }
        return GROUPED_NUMBER.format(Double.parseDouble(digits));
    }

    private static String digitsOnly(String text) {
        return text == null ? "" : text.replaceAll("[^0-9]", "");
    }

    private static int countDigitsBefore(String text, int caretPosition) {
        int count = 0;
        int safeCaret = Math.min(Math.max(caretPosition, 0), text == null ? 0 : text.length());
        for (int i = 0; i < safeCaret; i++) {
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

        int seen = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                seen++;
                if (seen == digitCount) {
                    return i + 1;
                }
            }
        }
        return text.length();
    }
}
