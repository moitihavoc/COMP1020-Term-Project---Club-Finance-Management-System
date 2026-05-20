# Controller Error Handling Guide

This guide explains how controllers should handle backend and DAO results.

## General Rule

Controllers should:

1. Validate simple UI input.
2. Call the DAO.
3. If the DAO rejects the action, show a short user-friendly message.
4. Reload the current screen after successful create/delete/update.

Do not show raw SQL errors to the user.

## Login Errors

DAO call:

```java
UserDAO.findByUsernameAndPassword(username, password)
```

If result is `null`, show:

```text
Invalid username or password.
```

## Logout Errors

Logout should be simple:

```java
Session.clear();
```

Then navigate back to the login page.

If navigation fails, show:

```text
Could not log out.
```

## Register Errors

DAO call:

```java
UserDAO.createAccount(user)
```

If result is `false`, show:

```text
Username already exists or input is invalid.
```

Controller should also check:

- username is not empty
- password is not empty
- confirm password matches

## Edit Club Balance Errors

DAO call:

```java
ClubDAO.updateTotalBalance(userId, totalBalance)
```

If result is `false`, show:

```text
Total balance cannot be lower than allocated project money.
```

If the input is not a number, show:

```text
Please enter a valid amount.
```

## Create Project Errors

DAO call:

```java
ProjectDAO.createProject(userId, projectName, allocatedAmount)
```

If result is `null`, possible reasons:

- project name is empty
- allocated amount exceeds the club balance

Recommended message:

```text
Project could not be created. Check the name and allocated amount.
```

More specific messages can be added later if controllers validate before calling DAO.

## Create Pot Errors

DAO call:

```java
PotDAO.createPot(projectId, potName, allocatedAmount)
```

If result is `null`, possible reasons:

- pot name is empty
- total pot allocation would exceed the project allocation

Recommended message:

```text
Pot could not be created. Check the name and allocated amount.
```

## Create Transaction Errors

DAO call:

```java
TransactionDAO.createTransaction(...)
```

If result is `null`, the transaction name is empty.

Recommended message:

```text
Transaction name is required.
```

If image saving fails, catch `IOException` and show:

```text
Could not save proof image.
```

If database saving fails, catch `SQLException` and show:

```text
Could not save transaction.
```

## Delete Errors

Delete methods do not currently return success or failure.

If a delete action completes without exception, reload the current list.

If `SQLException` occurs, show:

```text
Could not delete item.
```

## Search Errors

If amount fields are not valid numbers, show:

```text
Please enter valid amount filters.
```

If date range is invalid, show:

```text
Start date cannot be after end date.
```

If DAO search throws `SQLException`, show:

```text
Could not load transactions.
```

## Empty Results

If a list is empty, show:

```text
No results found.
```

Use this for:

- no projects
- no pots
- no transactions
- search returns no results
