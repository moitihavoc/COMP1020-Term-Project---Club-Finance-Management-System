# src/main File Guide

This guide explains the purpose of every file in `src/main`. It is written for group members who need to quickly understand the Club Finance Management System before presenting the poster.

## Big Picture

The project is a JavaFX desktop application called **Mony**. It helps a club manage money by organizing finance data in this order:

1. A user logs in.
2. Each user has one club finance record.
3. The club has a total balance.
4. The club balance is allocated into projects.
5. Each project is split into pots.
6. Each pot contains transactions.

The code is organized into these main layers:

- `controllers`: handle JavaFX screen actions and user input.
- `dao`: read and write data in the SQLite database.
- `models`: represent finance objects such as users, clubs, projects, pots, and transactions.
- `utils`: shared helper code for navigation, dialogs, money formatting, and transaction tables.
- `resources`: JavaFX FXML screen layouts and shared CSS styling.

## Main Java Files

### `src/main/java/oop/mony/Application.java`

Starts the JavaFX application window. It loads `login.fxml` as the first screen, attaches `styles.css`, sets the app title to `Mony`, configures minimum window size, and opens the app maximized.

Use this in the presentation when explaining: **where the GUI application begins**.

### `src/main/java/oop/mony/Launcher.java`

Contains the standard `main` method. It launches the JavaFX `Application` class.

Use this in the presentation when explaining: **how Java starts the program**.

### `src/main/java/oop/mony/Session.java`

Stores the currently logged-in user while the app is running. Controllers use it to know who is logged in, check whether a user session exists, and clear the session during logout.

Use this in the presentation when explaining: **how the app remembers the active user between screens**.

### `src/main/java/oop/mony/ClubFinanceService.java`

Acts as the central business logic layer between controllers and DAOs. It loads a complete club with projects, pots, and transactions; creates, updates, and deletes projects, pots, and transactions; checks allocation rules; updates the club balance; and searches transactions.

Important rules handled here include:

- A project cannot be allocated more than the club's available balance.
- A pot cannot be allocated more than its project's allocation.
- A transaction cannot spend more than its pot's remaining allocation.
- Updates cannot reduce an allocation below money that has already been spent.

Use this in the presentation when explaining: **the main finance rules and system logic**.

## Controllers

Controllers connect FXML screens to Java behavior. They receive button clicks, validate form input, call `ClubFinanceService` or DAOs, then refresh the screen.

### `src/main/java/oop/mony/controllers/LoginController.java`

Controls the login screen. It validates username and password input, checks the account through `UserDAO`, stores the logged-in user in `Session`, and navigates to the dashboard. It also supports showing or hiding the password field.

Related screen: `login.fxml`.

### `src/main/java/oop/mony/controllers/RegisterController.java`

Controls the account registration screen. It checks that all fields are filled, confirms that passwords match, requires at least 8 password characters, creates a new `User`, and shows success or error messages. It also supports password visibility toggles.

Related screen: `register.fxml`.

### `src/main/java/oop/mony/controllers/DashboardController.java`

Controls the dashboard screen. It loads the logged-in user's club data, displays total balance, total allocated amount, total spent, and remaining balance. It renders project cards, creates new projects, edits the club total balance, and navigates to other screens.

Related screen: `dashboard.fxml`.

### `src/main/java/oop/mony/controllers/ProjectController.java`

Controls the detail page for one project. It loads the selected project, displays project totals, handles editing or deleting the project, and coordinates the two included project sections: pots and project transactions.

Related screen: `project.fxml`.

### `src/main/java/oop/mony/controllers/ProjectPotSectionController.java`

Controls the pots section inside the project page. It displays pot cards, creates new pots, edits pot name/allocation, deletes pots, and checks that pot allocation does not exceed the project allocation.

Related screen: `projectPotSection.fxml`.

### `src/main/java/oop/mony/controllers/ProjectTransactionSectionController.java`

Controls the transaction section inside the project page. It displays transactions for the selected project, records new transactions, allows the user to choose which pot the transaction belongs to, supports optional proof image upload, and opens proof images for viewing.

Related screen: `projectTransactionSection.fxml`.

### `src/main/java/oop/mony/controllers/TransactionPageController.java`

Controls the full transactions page. It loads all transactions for the current user and supports searching/filtering by keyword, project, date range, minimum amount, and maximum amount. It also clears filters and opens proof images.

Related screen: `transactionPage.fxml`.

### `src/main/java/oop/mony/controllers/ChangePasswordController.java`

Controls the change password screen. It verifies the current password, checks that the new password is at least 8 characters, confirms the new passwords match, updates the password through `UserDAO`, and refreshes the current `Session` user.

Related screen: `changePassword.fxml`.

## DAO Files

DAO means **Data Access Object**. These classes are responsible for database operations. The app uses SQLite and stores its database at `app-data/mony.db`.

### `src/main/java/oop/mony/dao/Database.java`

Creates the `app-data` folder, opens a SQLite database connection, and creates the database tables if they do not already exist.

Tables created:

- `users`
- `clubs`
- `projects`
- `pots`
- `transactions`

Use this in the presentation when explaining: **where persistent data is stored**.

### `src/main/java/oop/mony/dao/UserDAO.java`

Handles user database operations. It creates accounts, prevents duplicate usernames, finds a user by username/password during login, and updates a user's password.

### `src/main/java/oop/mony/dao/ClubDAO.java`

Handles club database operations. It finds the club record for a user, creates one automatically if needed, and updates the club's total balance.

### `src/main/java/oop/mony/dao/ProjectDAO.java`

Handles project database operations. It creates projects, loads all projects for a user, updates project details, and deletes a project. When deleting a project, it also deletes the related pots and transactions through the DAO layer.

### `src/main/java/oop/mony/dao/PotDAO.java`

Handles pot database operations. It creates pots, loads all pots for a project, updates pot details, and deletes pots. When deleting a pot, it also deletes related transactions.

### `src/main/java/oop/mony/dao/TransactionDAO.java`

Handles transaction database operations. It creates transactions, loads transactions for a pot, deletes transactions, and manages proof image files. Uploaded proof images are copied into `app-data/proofs`.

Use this in the presentation when explaining: **how receipt/proof images are saved and connected to transactions**.

## Model Files

Models represent the main objects in the finance system. They also contain small calculation and validation methods.

### `src/main/java/oop/mony/models/User.java`

Represents an app user with user ID, username, and password. It also has a `createAccount` method that delegates account creation to `UserDAO`.

### `src/main/java/oop/mony/models/Club.java`

Represents a club finance account for one user. It stores the club balance and a list of projects. It calculates total allocated amount, total spent, remaining balance, checks whether a project can be added, finds projects by ID, and searches transactions across all projects and pots.

### `src/main/java/oop/mony/models/Project.java`

Represents one club project. It stores project ID, user ID, project name, allocated amount, and pots. It calculates total pot allocation, total spent, remaining amount, checks whether a pot can be added, and finds pots by ID.

### `src/main/java/oop/mony/models/Pot.java`

Represents one budget pot inside a project. It stores pot ID, project ID, pot name, allocated amount, and transactions. It calculates total spent, remaining amount, and checks whether a new transaction can be added.

### `src/main/java/oop/mony/models/Transaction.java`

Represents one spending record. It stores transaction name, project/pot information, amount, payer, date, proof image path, and note. It also includes helper methods for search filters, date filters, amount filters, project filters, and shortened notes.

## Utility Files

Utilities are shared helper classes used by multiple controllers.

### `src/main/java/oop/mony/utils/NavigationUtils.java`

Handles screen navigation. It loads FXML files, switches the scene root, passes session/project data to controllers, handles logout, and sizes the sidebar consistently.

Use this in the presentation when explaining: **how the app moves between screens without opening new windows**.

### `src/main/java/oop/mony/utils/MoneyUtils.java`

Formats and parses money values. It adds comma grouping while the user types, parses formatted text back into numbers, and formats displayed values with the Vietnamese dong symbol.

Use this in the presentation when explaining: **how the app keeps money input readable and consistent**.

### `src/main/java/oop/mony/utils/DialogUtils.java`

Styles JavaFX dialogs and opens proof images. It applies the shared CSS to dialogs, styles confirmation/error buttons, and shows uploaded proof images in a maximized image window.

### `src/main/java/oop/mony/utils/TransactionTableRenderer.java`

Builds transaction table rows in Java code. Both the project transaction section and the full transactions page use it, so transaction rows look consistent across the app.

## Resource Files

Resource files define the JavaFX user interface. The `.fxml` files describe screen layout, while `styles.css` contains reusable styling.

### `src/main/resources/oop/mony/login.fxml`

Defines the login screen UI. It contains username and password fields, a password visibility button, login error label, login button, and navigation to registration.

Controller: `LoginController`.

### `src/main/resources/oop/mony/register.fxml`

Defines the registration screen UI. It contains username, password, confirm password fields, password visibility buttons, error/success labels, register button, and back-to-login button.

Controller: `RegisterController`.

### `src/main/resources/oop/mony/dashboard.fxml`

Defines the dashboard UI. It contains the sidebar, finance summary cards, edit balance action, project creation form, and project card grid.

Controller: `DashboardController`.

### `src/main/resources/oop/mony/project.fxml`

Defines the project detail page UI. It contains the sidebar, project summary values, edit/delete project actions, and includes the pots and transactions sections using `fx:include`.

Controller: `ProjectController`.

Included files:

- `projectPotSection.fxml`
- `projectTransactionSection.fxml`

### `src/main/resources/oop/mony/projectPotSection.fxml`

Defines the pots section shown inside the project page. It contains the create-pot form and the pot card grid.

Controller: `ProjectPotSectionController`.

### `src/main/resources/oop/mony/projectTransactionSection.fxml`

Defines the project transaction section shown inside the project page. It contains the record-transaction form, pot dropdown, date picker, proof upload button, note field, and transaction table layout.

Controller: `ProjectTransactionSectionController`.

### `src/main/resources/oop/mony/transactionPage.fxml`

Defines the full transactions page UI. It contains the sidebar, keyword search, project filter, date filters, amount filters, clear filters button, and transaction table.

Controller: `TransactionPageController`.

### `src/main/resources/oop/mony/changePassword.fxml`

Defines the change password screen UI. It contains the sidebar, current password field, new password field, confirm password field, visibility buttons, and update password action.

Controller: `ChangePasswordController`.

### `src/main/resources/oop/mony/styles.css`

Defines reusable JavaFX styling for scrollbars, budget progress bars, finance cards, buttons, transaction rows, proof buttons, inline error/success text, and dialogs.

Use this in the presentation when explaining: **how the app keeps a consistent visual design across screens**.

## Suggested Poster Explanation

For the poster, the system can be explained as:

> Mony is a JavaFX desktop finance manager for clubs. Users log in, set a club balance, divide that balance into projects, divide projects into pots, and record transactions with optional proof images. The app uses a layered structure: controllers manage UI events, the service layer enforces finance rules, DAOs store data in SQLite, and models calculate totals/search results.

## Quick File-to-Feature Map

| Feature | Main files involved |
| --- | --- |
| App startup | `Launcher.java`, `Application.java`, `login.fxml` |
| Login/logout | `LoginController.java`, `UserDAO.java`, `Session.java`, `NavigationUtils.java` |
| Registration | `RegisterController.java`, `User.java`, `UserDAO.java`, `register.fxml` |
| Dashboard totals | `DashboardController.java`, `ClubFinanceService.java`, `Club.java`, `dashboard.fxml` |
| Club balance editing | `DashboardController.java`, `ClubFinanceService.java`, `ClubDAO.java` |
| Project management | `ProjectController.java`, `ProjectDAO.java`, `Project.java`, `project.fxml` |
| Pot management | `ProjectPotSectionController.java`, `PotDAO.java`, `Pot.java`, `projectPotSection.fxml` |
| Transaction recording | `ProjectTransactionSectionController.java`, `TransactionDAO.java`, `Transaction.java`, `projectTransactionSection.fxml` |
| Transaction search | `TransactionPageController.java`, `ClubFinanceService.java`, `Club.java`, `TransactionTableRenderer.java` |
| Proof images | `TransactionDAO.java`, `DialogUtils.java` |
| Shared styling | `styles.css`, `DialogUtils.java` |
