# Controller DAO and Error Handling Guide

This guide explains how controllers should connect UI actions to DAO methods, and how each result should be handled.

Controllers should not write SQL directly. The controller's job is to read UI input, validate simple mistakes, call a DAO method, and update the screen.

## General Controller Flow

Use this flow for most button actions:

1. Read values from FXML fields.
2. Trim text input where appropriate.
3. Convert numbers and dates.
4. Validate simple UI mistakes.
5. Get the current user or selected object ID.
6. Call the DAO method.
7. Handle `null`, `false`, or thrown exceptions.
8. Reload the affected data on success.

Example pattern:

```java
try {
    // read input
    // validate input
    // call DAO
    // reload UI
} catch (NumberFormatException e) {
    // show invalid number message
} catch (SQLException e) {
    // show database error message
}
```

Do not show raw SQL errors to the user.

## Current User

Before loading pages that require login, check:

```java
Session.hasCurrentUser()
```

To get the current user:

```java
User user = Session.getCurrentUser();
int userId = user.getUserId();
```

If there is no current user, navigate back to `login.fxml`.

## Login

Steps:

1. Read `usernameField`.
2. Read `passwordField`.
3. Check that both are not empty.
4. Call the DAO.

DAO call:

```java
User user = UserDAO.findByUsernameAndPassword(username, password);
```

Success:

```java
Session.setCurrentUser(user);
```

Then navigate to the home page.

Failure:

If `user == null`, show:

```text
Invalid username or password.
```

If `SQLException` happens, show:

```text
Could not log in.
```

## Register

Steps:

1. Read username.
2. Read password.
3. Read confirm password.
4. Check that username is not empty.
5. Check that password is not empty.
6. Check that password and confirm password match.
7. Call the DAO.

DAO call:

```java
boolean created = UserDAO.createAccount(new User(username, password));
```

Success:

Load the saved user:

```java
User user = UserDAO.findByUsernameAndPassword(username, password);
Session.setCurrentUser(user);
```

Then navigate to the home page.

Failure:

If `created == false`, show:

```text
Username already exists or input is invalid.
```

If `SQLException` happens, show:

```text
Could not create account.
```

## Logout

Steps:

1. Call `Session.clear()`.
2. Navigate back to `login.fxml`.

Code:

```java
Session.clear();
```

If navigation fails, show:

```text
Could not log out.
```

Do not leave the user on a protected page after logout.

## Load Home Page

Steps:

1. Get current user from `Session`.
2. Call `ClubDAO.getOrCreateClubForUser(...)`.
3. Call `ProjectDAO.findProjectsForUser(...)`.
4. Display club totals.
5. Display project cards.

DAO calls:

```java
Club club = ClubDAO.getOrCreateClubForUser(userId, username);
List<Project> projects = ProjectDAO.findProjectsForUser(userId);
```

Display:

- `club.getTotalBalance()`
- `club.getTotalAllocated()`
- `club.getTotalSpent()`
- `club.getTotalRemaining()`

If `SQLException` happens, show:

```text
Could not load home page.
```

If project list is empty, show:

```text
No projects yet.
```

## Edit Club Balance

Steps:

1. Read balance input.
2. Convert it to `double`.
3. Call the DAO.
4. Reload home summary on success.

DAO call:

```java
boolean updated = ClubDAO.updateTotalBalance(userId, totalBalance);
```

Success:

If `updated == true`, reload the club summary.

Failure:

If `updated == false`, show:

```text
Total balance cannot be lower than allocated project money.
```

If amount is not a number, show:

```text
Please enter a valid amount.
```

If `SQLException` happens, show:

```text
Could not update balance.
```

## Create Project

Steps:

1. Read project name.
2. Read allocated amount.
3. Convert allocated amount to `double`.
4. Call the DAO.
5. Reload project list on success.

DAO call:

```java
Project project = ProjectDAO.createProject(userId, projectName, allocatedAmount);
```

Success:

If `project != null`, reload:

```java
ProjectDAO.findProjectsForUser(userId);
```

Failure:

If `project == null`, possible reasons are:

- project name is empty
- allocated amount exceeds the club balance

Show:

```text
Project could not be created. Check the name and allocated amount.
```

If amount is not a number, show:

```text
Please enter a valid allocated amount.
```

If `SQLException` happens, show:

```text
Could not create project.
```

## Delete Project

Steps:

1. Get selected `projectId`.
2. Get current `userId`.
3. Call the DAO.
4. Reload project list.

DAO call:

```java
ProjectDAO.deleteProject(projectId, userId);
```

This manually deletes:

- transactions under the project's pots
- proof image files for those transactions
- pots under the project
- the project

If `SQLException` happens, show:

```text
Could not delete project.
```

## Load Project Page

Steps:

1. Get selected `projectId`.
2. Load pots for that project.
3. Display project information passed from the previous page or loaded from a selected `Project`.
4. Display pot cards.

DAO call:

```java
List<Pot> pots = PotDAO.findPotsForProject(projectId);
```

If the pot list is empty, show:

```text
No pots yet.
```

If `SQLException` happens, show:

```text
Could not load project.
```

## Create Pot

Steps:

1. Read pot name.
2. Read allocated amount.
3. Convert allocated amount to `double`.
4. Call the DAO.
5. Reload pot list on success.

DAO call:

```java
Pot pot = PotDAO.createPot(projectId, potName, allocatedAmount);
```

Success:

If `pot != null`, reload:

```java
PotDAO.findPotsForProject(projectId);
```

Failure:

If `pot == null`, possible reasons are:

- pot name is empty
- total pot allocation would exceed the project allocation

Show:

```text
Pot could not be created. Check the name and allocated amount.
```

If amount is not a number, show:

```text
Please enter a valid allocated amount.
```

If `SQLException` happens, show:

```text
Could not create pot.
```

## Delete Pot

Steps:

1. Get selected `potId`.
2. Get current `projectId`.
3. Call the DAO.
4. Reload pot list.

DAO call:

```java
PotDAO.deletePot(potId, projectId);
```

This manually deletes:

- transactions under the pot
- proof image files for those transactions
- the pot

If `SQLException` happens, show:

```text
Could not delete pot.
```

## Create Transaction

Steps:

1. Select the pot the transaction belongs to.
2. Read transaction name.
3. Read amount.
4. Read paid by. This can be empty.
5. Read transaction date as `LocalDate`.
6. Read note.
7. Let the user choose a proof image file if available.
8. Pass the selected proof image as a `Path`, or pass `null`.
9. Call the DAO.
10. Reload transactions and affected spent/remaining totals.

DAO call:

```java
Transaction transaction = TransactionDAO.createTransaction(
    potId,
    transactionName,
    amount,
    paidBy,
    transactionDate,
    proofImagePath,
    note
);
```

Success:

If `transaction != null`, reload transaction rows and related pot/project totals.

Failure:

If `transaction == null`, show:

```text
Transaction name is required.
```

If amount is not a number, show:

```text
Please enter a valid amount.
```

If image saving fails, catch `IOException` and show:

```text
Could not save proof image.
```

If database saving fails, catch `SQLException` and show:

```text
Could not save transaction.
```

## Load Transactions For One Pot

Steps:

1. Get selected `potId`.
2. Call DAO.
3. Render each transaction row.

DAO call:

```java
List<Transaction> transactions = TransactionDAO.findTransactionsForPot(potId);
```

If the list is empty, show:

```text
No transactions found.
```

## Search All Transactions

Steps:

1. Get current `userId`.
2. Read keyword from search field.
3. Read start date and end date.
4. Read minimum and maximum amount.
5. Read selected project from dropdown.
6. Validate date and amount filters.
7. Call DAO.
8. Clear current table rows.
9. Add one row per `TransactionRecord`.

DAO call:

```java
List<TransactionRecord> results = TransactionDAO.searchTransactionsForUser(
    userId,
    keyword,
    startDate,
    endDate,
    minAmount,
    maxAmount,
    projectId
);
```

Filter rules:

- Empty keyword can be passed as `null` or empty string.
- No start date means pass `null`.
- No end date means pass `null`.
- No min amount means pass `null`.
- No max amount means pass `null`.
- `All Projects` means pass `null` for `projectId`.
- A selected project means pass that project's `projectId`.

Validation errors:

If amount fields are not valid numbers, show:

```text
Please enter valid amount filters.
```

If start date is after end date, show:

```text
Start date cannot be after end date.
```

If `SQLException` happens, show:

```text
Could not load transactions.
```

If results are empty, show:

```text
No transactions found.
```

## Proof Images

The DAO stores proof images by copying selected files into:

```text
data/proofs/
```

The database stores only the saved path.

Controllers should display this as a user-facing action like:

```text
View Proof
```

Do not show raw file paths unless debugging.

## Final Reminder

Controllers should stay focused on UI flow.

Business rules belong in the DAO/model layer. For example:

- project allocation cannot exceed club balance
- pot allocation cannot exceed project allocation
- manual cascade delete
- proof image cleanup
