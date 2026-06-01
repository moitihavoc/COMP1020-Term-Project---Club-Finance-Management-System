# Mony - Club Finance Management System

Mony is a JavaFX desktop app for a club treasurer to manage club finances. It
organizes money into projects, pots, and transactions so expenses can be tracked
with clear budget categories and proof images.

## Features

- Register, log in, log out, and change password.
- View and update club balance.
- Create, edit, delete, and open projects.
- Create, edit, and delete pots inside projects.
- Record transactions under pots with payer, date, note, amount, and proof image.
- Search and filter transactions by keyword, project, date range, and amount.

## Requirements

- JDK 25 or newer.
- Maven dependencies are handled by the included Maven wrapper.

## How To Run

From the project root:

### Windows

```powershell
.\mvnw.cmd clean javafx:run
```

### macOS/Linux

```bash
./mvnw clean javafx:run
```

The app launches from:

```text
oop.mony.Launcher
```

## Demo Account

The main demo use case is a treasurer managing a large basketball club.

The project can be evaluated in two ways:

- Create a new account from the register screen.
- Use the demo account below to view preloaded basketball club records.

Demo login:

```text
username: admin
password: 12345678
```

The demo account contains:

- Total balance: `180,000,000 VND`
- 4 projects
- 8 pots
- 50 transactions

## Project Structure

```text
src/main/java/oop/mony/
  Application.java
  Launcher.java
  Session.java
  ClubFinanceService.java

src/main/java/oop/mony/controllers/
  JavaFX screen controllers

src/main/java/oop/mony/models/
  User, Club, Project, Pot, Transaction

src/main/java/oop/mony/dao/
  SQLite database access classes

src/main/java/oop/mony/utils/
  Navigation, dialog, money-formatting, and table-rendering helpers

src/main/resources/oop/mony/
  FXML screens and styles.css

app-data/
  Local SQLite database and proof images
```

## Design Notes

The main object hierarchy is:

```text
User -> Club -> Project -> Pot -> Transaction
```

A transaction belongs to a pot because each expense must be checked against a
specific budget category. This lets the app prevent a treasurer from spending
more than the selected pot's allocated amount.

Transaction search traverses:

```text
Club -> Project -> Pot -> Transaction
```

and applies keyword, project, date, and amount filters.
