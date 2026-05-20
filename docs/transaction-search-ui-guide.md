# Transaction Search UI Guide

This guide explains how the all-transactions search and filter UI should work.

## Purpose

The transactions page should let users view and search transactions across all projects and pots in their club.

Use this backend method:

```java
TransactionDAO.searchTransactionsForUser(
    userId,
    keyword,
    startDate,
    endDate,
    minAmount,
    maxAmount,
    projectId
)
```

This returns `List<TransactionRecord>`.

## Table Columns

Recommended columns:

- Date
- Transaction Name
- Project
- Pot
- Paid By
- Amount
- Note
- Proof

The note column should be truncated.

Use:

```java
transactionRecord.getShortNote()
```

For proof, show a button like:

```text
View Proof
```

Do not show the raw proof path in the table.

## Keyword Search

The search box should search across:

- transaction name
- note
- paid by

Suggested placeholder:

```text
Search name, note, or paid by
```

Examples:

- Searching `pizza` can match transaction name.
- Searching `john` can match paid by.
- Searching `welcome` can match note.

If the search field is empty, pass `null` or an empty string as `keyword`.

## Date Range Filter

Use two date inputs:

- Start date
- End date

Controller passes:

- `LocalDate startDate`
- `LocalDate endDate`

If no date is selected, pass `null`.

Validation:

- If both dates exist, start date should not be after end date.

## Amount Range Filter

Use two amount inputs:

- Minimum amount
- Maximum amount

Controller passes:

- `Double minAmount`
- `Double maxAmount`

If no amount is entered, pass `null`.

Validation:

- Amounts should be valid numbers.
- Minimum should not be greater than maximum.

## Project Filter

The project filter should be a dropdown.

Load project choices using:

```java
ProjectDAO.findProjectsForUser(userId)
```

Dropdown should include:

- All Projects
- Each project owned by the current user

Behavior:

- `All Projects` means pass `null` as `projectId`.
- A selected project means pass that project's `projectId`.

Example:

```java
Integer selectedProjectId = null; // All Projects
```

or:

```java
Integer selectedProjectId = selectedProject.getProjectId();
```

## Clear Filters

Add a clear/reset option if possible.

Clear should reset:

- keyword
- start date
- end date
- minimum amount
- maximum amount
- project dropdown back to `All Projects`

Then reload all transactions for the user.

## Controller Flow

Recommended flow:

1. Read keyword from search field.
2. Read start and end dates.
3. Read minimum and maximum amount.
4. Read selected project from dropdown.
5. Validate dates and amounts.
6. Call `TransactionDAO.searchTransactionsForUser(...)`.
7. Clear `transactionsTableBody`.
8. Add one row per `TransactionRecord`.

## Empty State

If no transactions match, show:

```text
No transactions found.
```
