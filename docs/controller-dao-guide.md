# Controller DAO Guide

This guide explains which DAO methods controllers should call. Controllers should collect UI input, call DAO methods, and update the screen. Controllers should not write SQL directly.

## Current User

Use `Session.getCurrentUser()` to get the logged-in user.

The logged-in user's ID is:

```java
Session.getCurrentUser().getUserId()
```

Controllers should check that a user exists before loading pages that need login.

## Logout

Use:

```java
Session.clear();
```

After clearing the session, navigate back to `login.fxml`.

Controllers should not leave the user on a protected page after logout.

## Login

Use:

```java
UserDAO.findByUsernameAndPassword(username, password)
```

If it returns `null`, login failed.

If it returns a `User`, save it:

```java
Session.setCurrentUser(user);
```

## Register

Use:

```java
UserDAO.createAccount(new User(username, password))
```

Returns:

- `true`: account created.
- `false`: invalid input or duplicate username.

After successful registration, controllers can call `UserDAO.findByUsernameAndPassword(...)` to load the saved user and put it into `Session`.

## Load Club Home Summary

Use:

```java
ClubDAO.getOrCreateClubForUser(userId, username)
```

This returns the club summary for the current user.

Display:

- `club.getTotalBalance()`
- `club.getTotalAllocated()`
- `club.getTotalSpent()`
- `club.getTotalRemaining()`

## Edit Club Balance

Use:

```java
ClubDAO.updateTotalBalance(userId, totalBalance)
```

Returns:

- `true`: balance updated.
- `false`: balance is lower than the user's already allocated project total.

## Create Project

Use:

```java
ProjectDAO.createProject(userId, projectName, allocatedAmount)
```

Returns:

- `Project`: project created.
- `null`: project name is empty or allocation exceeds club balance.

After creating, reload:

```java
ProjectDAO.findProjectsForUser(userId)
```

## Load Projects

Use:

```java
ProjectDAO.findProjectsForUser(userId)
```

Each `Project` includes:

- `projectName`
- `allocatedAmount`
- `spentAmount`
- `remainingAmount`

## Delete Project

Use:

```java
ProjectDAO.deleteProject(projectId, userId)
```

This performs manual cascade delete:

- deletes transactions under the project's pots
- deletes pots
- deletes the project
- deletes proof image files for those transactions

After deleting, reload the project list.

## Create Pot

Use:

```java
PotDAO.createPot(projectId, potName, allocatedAmount)
```

Returns:

- `Pot`: pot created.
- `null`: pot name is empty or total pot allocation exceeds the project's allocated amount.

After creating, reload:

```java
PotDAO.findPotsForProject(projectId)
```

## Load Pots

Use:

```java
PotDAO.findPotsForProject(projectId)
```

Each `Pot` includes:

- `potName`
- `allocatedAmount`
- `spentAmount`
- `remainingAmount`

## Delete Pot

Use:

```java
PotDAO.deletePot(potId, projectId)
```

This deletes:

- transactions under the pot
- proof image files for those transactions
- the pot

After deleting, reload the pot list.

## Create Transaction

Use:

```java
TransactionDAO.createTransaction(
    potId,
    transactionName,
    amount,
    paidBy,
    transactionDate,
    proofImagePath,
    note
)
```

`transactionDate` should be a `LocalDate`.

`proofImagePath` should be a `Path` from the selected proof image. Pass `null` if no proof image is selected.

Returns:

- `Transaction`: transaction created.
- `null`: transaction name is empty.

The DAO copies the proof image into `data/proofs/` and stores the saved path.

## Load Transactions For One Pot

Use:

```java
TransactionDAO.findTransactionsForPot(potId)
```

This is useful on a project detail screen if showing transactions under a selected pot.

## Search All Transactions

Use:

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

Use this for the all-transactions page.
