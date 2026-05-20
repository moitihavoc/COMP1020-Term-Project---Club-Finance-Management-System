# Frontend Screen Guide

This guide explains what each main screen should display. It is written for the frontend team so the UI matches the backend model.

## Core Display Language

Use these words consistently:

- `Allocated`: planned or approved money.
- `Spent`: actual money already used through transactions.
- `Remaining`: money left after spending.

Avoid using `budget` if it may confuse users. Prefer `Allocated`.

## Home Page

FXML file: `src/main/resources/oop/mony/projects.fxml`

Purpose:

Show the club finance summary and all projects for the current user.

Main values to display:

- Total Balance
- Total Allocated
- Total Spent
- Remaining Balance

Current FXML already has:

- `totalBalanceLabel`
- `totalSpentLabel`
- `remainingBalanceLabel`
- `projectsGrid`

Suggested addition later:

- A label/card for `totalAllocated`

Project cards should show:

- Project name
- Allocated amount
- Spent amount
- Remaining amount
- Delete button

Recommended card example:

```text
Welcome Party
Allocated: $1,000
Spent: $350
Remaining: $650
Delete
```

Home page navigation should include:

- `Projects`
- `Transactions`
- `View profile`

The `Transactions` sidebar tab should open the all-transactions page.

## Project Page

FXML file: `src/main/resources/oop/mony/projectPage.fxml`

Purpose:

Show one selected project/event.

Main values to display:

- Project name
- Project allocated amount
- Project spent amount
- Project remaining amount

Pot cards should show:

- Pot name
- Allocated amount
- Spent amount
- Remaining amount
- Delete button

Recommended pot card example:

```text
Food
Allocated: $500
Spent: $220
Remaining: $280
Delete
```

The project page should also show recent transactions for that project if the controller team supports it.

Current FXML containers:

- `potsGrid`: display pot cards here.
- `transactionsContainer`: display transaction rows here.

## All Transactions Page

FXML file: `src/main/resources/oop/mony/transactionPage.fxml`

Purpose:

Show all transactions across the current user's projects and pots.

Recommended table columns:

- Date
- Transaction Name
- Project
- Pot
- Paid By
- Amount
- Note
- Proof

The note column should be truncated. Do not show a very long note directly in the table.

Example row:

```text
2026-05-20 | Pizza | Welcome Party | Food | John | $120 | Snacks for welcome... | View Proof
```

Current FXML container:

- `transactionsTableBody`: display transaction rows here.

The current sample rows in `transactionPage.fxml` are placeholder data. They should be replaced by rows loaded from `TransactionDAO.searchTransactionsForUser(...)`.

## Proof Display

The backend stores proof images as file paths in `proofPath`.

The UI should show this as:

- `View Proof`
- `Open Proof`

Do not show the raw file path to the user unless debugging.

## Empty States

Use simple empty state text:

- No projects yet.
- No pots yet.
- No transactions found.

Avoid long explanations inside the app UI.
