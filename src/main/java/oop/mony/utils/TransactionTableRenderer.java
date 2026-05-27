package oop.mony.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import oop.mony.models.Pot;
import oop.mony.models.Project;
import oop.mony.models.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class TransactionTableRenderer {
    private TransactionTableRenderer() {
    }

    public static void renderProjectTransactions(
            VBox container,
            Project project,
            Function<Double, String> moneyFormatter,
            Function<LocalDate, String> dateFormatter,
            Consumer<String> proofViewer) {

        container.getChildren().clear();
        if (project == null) {
            return;
        }

        for (Pot pot : project.getPots()) {
            for (Transaction transaction : pot.getTransactions()) {
                container.getChildren().add(createRow(
                        dateFormatter.apply(transaction.getTransactionDate()),
                        transaction.getTransactionName(),
                        project.getProjectName(),
                        pot.getPotName(),
                        transaction.getPaidBy(),
                        moneyFormatter.apply(transaction.getAmount()),
                        transaction.getNote() != null && !transaction.getNote().isEmpty()
                                ? transaction.getNote()
                                : "-",
                        transaction.getProofPath(),
                        proofViewer
                ));
            }
        }
    }

    public static void renderTransactions(
            VBox container,
            List<Transaction> records,
            Function<LocalDate, String> dateFormatter,
            Consumer<String> proofViewer) {

        container.getChildren().clear();
        for (Transaction record : records) {
            container.getChildren().add(createRow(
                    dateFormatter.apply(record.getTransactionDate()),
                    record.getTransactionName(),
                    record.getProjectName(),
                    record.getPotName(),
                    record.getPaidBy(),
                    MoneyUtils.formatVnd(record.getAmount()),
                    record.getShortNote(30),
                    record.getProofPath(),
                    proofViewer
            ));
        }
    }

    private static GridPane createRow(
            String date,
            String name,
            String project,
            String pot,
            String paidBy,
            String amount,
            String note,
            String proofPath,
            Consumer<String> proofViewer) {

        GridPane row = new GridPane();
        row.setHgap(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("transaction-row");
        row.getColumnConstraints().addAll(
                column(11), column(18), column(11), column(10),
                column(12), column(14), column(14), column(10)
        );

        row.add(cell(date), 0, 0);
        row.add(cell(name), 1, 0);
        row.add(cell(project), 2, 0);
        row.add(cell(pot), 3, 0);
        row.add(cell(paidBy), 4, 0);
        row.add(amountCell(amount), 5, 0);
        row.add(cell(note), 6, 0);
        row.add(proofCell(proofPath, proofViewer), 7, 0);
        return row;
    }

    private static Label cell(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("transaction-cell");
        label.setWrapText(true);
        return label;
    }

    private static Label amountCell(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("transaction-amount");
        return label;
    }

    private static VBox proofCell(String proofPath, Consumer<String> proofViewer) {
        VBox proofBox = new VBox();
        proofBox.setAlignment(Pos.TOP_LEFT);
        if (proofPath != null && !proofPath.isEmpty()) {
            Button viewProofBtn = new Button("View");
            viewProofBtn.getStyleClass().add("proof-button");
            viewProofBtn.setOnAction(e -> proofViewer.accept(proofPath));
            proofBox.getChildren().add(viewProofBtn);
        } else {
            Label noProofLabel = new Label("-");
            noProofLabel.getStyleClass().add("muted-table-cell");
            proofBox.getChildren().add(noProofLabel);
        }
        return proofBox;
    }

    private static ColumnConstraints column(double percentWidth) {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        return column;
    }
}
