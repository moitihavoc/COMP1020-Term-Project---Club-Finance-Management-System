# Backend Model Guide

This guide explains the current backend model for the Club Finance Management System. It is meant to help teammates understand the intention behind the Java classes before working on controllers, FXML, or database features.

## Main Idea

The app is for tracking club money by event.

A club may create a project for each event. Each project can have smaller budget pots for specific purposes, such as food, equipment, decoration, or operations. Transactions are the actual money spent from those pots.

The important idea is:

```text
Allocated = planned or approved money
Spent = actual money used through transactions
Remaining = allocated - spent
```

Avoid thinking of "allocated" as money already spent. It is only the amount the club allowed for that project or pot.

## Object Relationship

The backend follows this structure:

```text
User
  owns a Club
  owns Projects

Project
  belongs to a User
  has Pots

Pot
  belongs to a Project
  has Transactions

Transaction
  belongs to a Pot
```

In database terms:

```text
users.id              -> projects.user_id
projects.id           -> pots.project_id
pots.id               -> transactions.pot_id
users.id              -> clubs.user_id
```

## User

File:

```text
src/main/java/oop/mony/models/User.java
```

Purpose:

Represents a user account.

Current attributes:

```java
private final int userId;
private final String username;
private final String password;
```

Notes:

- `userId` identifies the user in the database.
- `username` is used for login and display.
- `password` is currently plain text for beginner scope.

## Club

File:

```text
src/main/java/oop/mony/models/Club.java
```

Purpose:

Represents the home page finance summary for a user.

Current attributes:

```java
private final int clubId;
private final int userId;
private final String clubName;
private final double totalBalance;
private final double totalAllocated;
private final double totalSpent;
```

Meaning:

```text
totalBalance
```

The total real money the club has.

```text
totalAllocated
```

The total planned money assigned to projects.

```text
totalSpent
```

The total money actually spent through transactions.

```text
totalRemaining
```

Calculated by:

```java
totalBalance - totalSpent
```

Important:

`totalAllocated` and `totalSpent` should not be manually typed by the user. They should come from project and transaction data.

## Project

File:

```text
src/main/java/oop/mony/models/Project.java
```

Purpose:

Represents one club event or activity.

Examples:

```text
Welcome Party
Fundraising Booth
Annual Dinner
Sports Day
```

Current attributes:

```java
private final int projectId;
private final int userId;
private final String projectName;
private final double allocatedAmount;
private final double spentAmount;
```

Meaning:

```text
allocatedAmount
```

The amount of money the club planned or approved for this project.

```text
spentAmount
```

The total amount spent through transactions under this project's pots.

```text
remainingAmount
```

Calculated by:

```java
allocatedAmount - spentAmount
```

Example:

```text
Project: Welcome Party
Allocated: 1000
Spent: 350
Remaining: 650
```

## Pot

File:

```text
src/main/java/oop/mony/models/Pot.java
```

Purpose:

Represents a smaller budget category inside a project.

Examples:

```text
Food
Decorations
Transport
Operations
Equipment
```

Current attributes:

```java
private final int potId;
private final int projectId;
private final String potName;
private final double allocatedAmount;
private final double spentAmount;
```

Meaning:

```text
allocatedAmount
```

The amount of money planned or approved for this pot.

```text
spentAmount
```

The total amount spent through transactions under this pot.

```text
remainingAmount
```

Calculated by:

```java
allocatedAmount - spentAmount
```

Example:

```text
Project: Welcome Party

Pot: Food
Allocated: 500
Spent: 220
Remaining: 280

Pot: Decorations
Allocated: 200
Spent: 80
Remaining: 120
```

## Transaction

File:

```text
src/main/java/oop/mony/models/Transaction.java
```

Purpose:

Represents real money spent from a pot.

Current attributes:

```java
private final int transactionId;
private final int potId;
private final String transactionName;
private final double amount;
private final String paidBy;
private final LocalDate transactionDate;
private final String proofPath;
private final String note;
```

Meaning:

```text
transactionName
```

Short name of what was bought.

Examples:

```text
Pizza
Poster Printing
Extension Cable
```

```text
amount
```

The real amount of money spent.

```text
paidBy
```

The member who paid first.

```text
transactionDate
```

The date the spending happened. This uses `LocalDate`.

```text
proofPath
```

The saved file path of the uploaded proof image.

The image itself is copied into:

```text
data/proofs/
```

The database stores only the path.

```text
note
```

Extra details about the transaction.

## Planned, Spent, Remaining

This is the most important concept in the backend.

Use these three values everywhere possible:

```text
Allocated
Spent
Remaining
```

For projects and pots:

```java
remainingAmount = allocatedAmount - spentAmount;
```

For the club:

```java
totalRemaining = totalBalance - totalSpent;
```

Example:

```text
Club Balance: 5000

Project: Welcome Party
Allocated: 1000
Spent: 350
Remaining: 650

Pot: Food
Allocated: 500
Spent: 220
Remaining: 280
```

## What Should Be Calculated

These should be calculated by DAO/model logic:

```text
Project spent amount
Project remaining amount
Pot spent amount
Pot remaining amount
Club total allocated
Club total spent
Club total remaining
```

These should be directly entered by the user:

```text
Club total balance
Project allocated amount
Pot allocated amount
Transaction amount
```

## DAO Responsibility

DAO classes handle database work.

Current DAO files:

```text
UserDAO.java
ClubDAO.java
ProjectDAO.java
PotDAO.java
TransactionDAO.java
Database.java
```

Main responsibilities:

```text
UserDAO
- create account
- login
- find user

ClubDAO
- get or create club for a user
- update total balance
- calculate club totals

ProjectDAO
- create project
- find projects for a user
- delete project
- calculate project/user spent totals

PotDAO
- create pot
- find pots for a project
- delete pot
- calculate allocated pot totals

TransactionDAO
- create transaction
- find transactions for a pot
- delete transaction
- calculate spent amount
- save/delete proof images
```

## Delete Behavior

Manual cascade delete is used.

When deleting a pot:

```text
1. Delete all transactions under the pot
2. Delete the pot
3. Delete proof image files for those transactions
```

When deleting a project:

```text
1. Find all pots under the project
2. Delete transactions under each pot
3. Delete each pot
4. Delete the project
5. Delete proof image files for deleted transactions
```

This is done manually in Java instead of relying on database cascade rules.

## Current Scope

The backend model is ready for controller work, but the UI is not fully connected yet.

Controllers should use DAO methods instead of directly writing SQL.

Frontend and controller teams should keep the user-facing words simple:

```text
Allocated
Spent
Remaining
```

Avoid showing too many extra financial terms unless they are needed later.
