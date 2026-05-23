# Controller Implementation Guide

This guide is for the teammate implementing the JavaFX controllers. The frontend FXML and backend/core classes already exist; the missing work is connecting them.

Recommended structure: keep this as one controller guide. Each controller needs to connect FXML fields, navigation, backend calls, and rendering, so one document is easier to follow than separate frontend/backend docs.

## Main Rule

Controllers should stay thin:

1. Read input from FXML fields.
2. Validate simple user input.
3. Call `ClubFinanceSystem`, model methods, or DAO/model login methods.
4. Update labels/containers.
5. Navigate between FXML screens.

Do not put finance calculations in controllers. Totals and search are already handled by model/core classes.

## Important Backend/Core Classes

Use these as the main controller API:

```java
ClubFinanceSystem.loadFullClubForUser(userId, username)
ClubFinanceSystem.createProject(club, name, allocatedAmount)
ClubFinanceSystem.createPot(club, projectId, name, allocatedAmount)
ClubFinanceSystem.createTransaction(club, potId, name, amount, paidBy, date, proofImage, note)
ClubFinanceSystem.deleteProject(club, projectId)
ClubFinanceSystem.deletePot(club, potId)
ClubFinanceSystem.deleteTransaction(club, transactionId, potId)
ClubFinanceSystem.searchTransactions(club, keyword, startDate, endDate, minAmount, maxAmount, projectId)
ClubFinanceSystem.updateTotalBalance(club, totalBalance)
```

Important model methods:

```java
Club.getProjects()
Club.getTotalBalance()
Club.getTotalAllocated()
Club.getTotalSpent()
Club.getTotalRemaining()
Club.findProjectById(projectId)

Project.getPots()
Project.getTotalSpent()
Project.getRemainingAmount()
Project.findPotById(potId)

Pot.getTransactions()
Pot.getTotalSpent()
Pot.getRemainingAmount()

TransactionRecord.getShortNote()
```

## Session Handling

Use `Session` to remember the logged-in user:

```java
Session.setCurrentUser(user);
Session.getCurrentUser();
Session.hasCurrentUser();
Session.clear();
```

Suggested login update:

- After successful login, get the full `User` object with `UserDAO.findByUsernameAndPassword(username, password)`.
- Store it with `Session.setCurrentUser(user)`.
- Load `projects.fxml`.
- In `ProjectsController`, load the full club from `ClubFinanceSystem.loadFullClubForUser(...)`.

If `Session.hasCurrentUser()` is false on a protected page, navigate back to `login.fxml`.

## Shared Controller State

Each main controller should usually keep:

```java
private User currentUser;
private Club club;
```

`ProjectPageController` should also keep:

```java
private Project selectedProject;
private Path selectedProofImage;
```

`TransactionPageController` may keep:

```java
private Integer selectedProjectFilterId;
```

## Navigation Pattern

Use `FXMLLoader` to change scenes:

```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("/oop/mony/projects.fxml"));
HBox root = loader.load();
ProjectsController controller = loader.getController();
controller.loadFromSession();
stage.getScene().setRoot(root);
```

Add a public setup method to each page controller, for example:

```java
public void loadFromSession()
public void loadProjectFromSession(int projectId)
```

Do not pass many unrelated values between controllers. Prefer loading `User` from `Session`, then loading `Club` through `ClubFinanceSystem`.

## ProjectsController

FXML file: `projects.fxml`

Existing important fields:

```java
usernameLabel
sidebarUsername
projectSearchField
totalBalanceLabel
totalAllocatedLabel
totalSpentLabel
remainingBalanceLabel
createProjectBtn
createProjectForm
newProjectNameField
newProjectAllocatedField
createProjectErrorLabel
projectsGrid
```

Handlers needed:

```java
handleGoToTransactions()
handleViewProfile()
handleEditBalance()
handleCreateProject()
handleConfirmCreateProject()
handleCancelCreateProject()
handleLogout()
```

Suggested methods to add:

```java
public void loadFromSession()
private void refreshPage()
private void refreshSummary()
private void renderProjects()
private VBox createProjectCard(Project project)
private double parseAmount(TextField field)
private void showCreateProjectForm(boolean visible)
```

Controller flow:

1. `loadFromSession()` gets the current user from `Session`.
2. Load the full club:

```java
club = ClubFinanceSystem.loadFullClubForUser(currentUser.getUserId(), currentUser.getUsername());
```

3. Update username labels.
4. Update summary labels:

```java
club.getTotalBalance()
club.getTotalAllocated()
club.getTotalSpent()
club.getTotalRemaining()
```

5. Render one project card per `club.getProjects()`.

Create project flow:

1. `handleCreateProject()` shows `createProjectForm`.
2. `handleConfirmCreateProject()` reads project name and allocated amount.
3. Validate:
   - name not empty
   - amount is a valid number
   - amount is not negative
   - amount does not exceed available club balance
4. Call:

```java
club = ClubFinanceSystem.createProject(club, name, allocatedAmount);
```

5. Clear/hide the form and refresh the page.

Project card should show:

- project name
- allocated amount
- spent amount
- remaining amount
- button/click action to open `projectPage.fxml`

## ProjectPageController

FXML file: `projectPage.fxml`

Important fields:

```java
sidebarProjectNameLabel
sidebarUsername
logoutButton
usernameLabel
projectPageSearchField
allocatedAmountLabel
totalSpentLabel
totalRemainingLabel
projectNameLabel
editMenuBtn
createPotForm
newPotNameField
newPotAllocatedField
createPotErrorLabel
potsGrid
createTransactionForm
transactionNameField
potComboBox
transactionAmountField
paidByField
transactionDatePicker
proofUploadButton
noteField
createTransactionErrorLabel
transactionsContainer
```

Handlers needed:

```java
handleGoToProjects()
handleGoToTransactions()
handleViewProfile()
handleLogout()
handleEditMenu()
handleCreatePot()
handleConfirmCreatePot()
handleCancelCreatePot()
handleCreateTransaction()
handleProofUpload()
handleConfirmCreateTransaction()
handleCancelCreateTransaction()
```

Suggested methods:

```java
public void loadProjectFromSession(int projectId)
private void refreshPage()
private void refreshSummary()
private void renderPots()
private void renderProjectTransactions()
private VBox createPotCard(Pot pot)
private GridPane createTransactionRow(Transaction transaction, int rowNumber)
private void refreshPotComboBox()
private Pot selectedPotFromComboBox()
private double parseAmount(TextField field)
private void showCreatePotForm(boolean visible)
private void showCreateTransactionForm(boolean visible)
```

Page load flow:

1. Get current user from `Session`.
2. Load full club.
3. Find the selected project:

```java
selectedProject = club.findProjectById(projectId);
```

4. If no project is found, return to projects page.
5. Fill labels from `selectedProject`.
6. Render pots and transactions.

Create pot flow:

1. Read pot name and allocated amount.
2. Validate:
   - name not empty
   - amount valid and not negative
   - `selectedProject.canAddPot(amount)`
3. Call:

```java
club = ClubFinanceSystem.createPot(club, selectedProject.getProjectId(), name, allocatedAmount);
selectedProject = club.findProjectById(selectedProject.getProjectId());
```

4. Clear/hide form and refresh.

Create transaction flow:

1. Read transaction name, selected pot, amount, paid by, date, proof path, and note.
2. Validate:
   - transaction name not empty
   - pot selected
   - amount valid and greater than 0
   - paid by not empty
   - date selected
3. Call:

```java
club = ClubFinanceSystem.createTransaction(
        club,
        selectedPot.getPotId(),
        name,
        amount,
        paidBy,
        date,
        selectedProofImage,
        note
);
```

4. Reload `selectedProject`, clear/hide form, and refresh.

Proof upload:

- Use `FileChooser`.
- Store the selected path in `selectedProofImage`.
- Update `proofUploadButton` text to the selected file name.

## TransactionPageController

FXML file: `transactionPage.fxml`

Important fields:

```java
sidebarUsername
logoutButton
usernameLabel
searchField
projectNameLabel
recordTransactionButton
projectFilterComboBox
startDatePicker
endDatePicker
minAmountField
maxAmountField
clearFiltersButton
allTabButton
incomeTabButton
expenseTabButton
transactionsTableBody
```

Handlers needed:

```java
handleGoToProjects()
handleBackToProjectDetail()
handleViewProfile()
handleLogout()
handleRecordTransaction()
handleClearFilters()
handleShowAllTransactions()
handleShowIncomeTransactions()
handleShowExpenseTransactions()
```

Suggested methods:

```java
public void loadFromSession()
private void refreshPage()
private void refreshProjectFilter()
private void renderTransactions(List<TransactionRecord> records)
private GridPane createTransactionRow(TransactionRecord record, int rowNumber)
private Double parseOptionalAmount(TextField field)
private Integer selectedProjectFilterId()
```

Search/filter flow:

1. Load `club` through `ClubFinanceSystem.loadFullClubForUser(...)`.
2. Populate `projectFilterComboBox` with:
   - "All Projects"
   - each project from `club.getProjects()`
3. When search/filter values change, call:

```java
ArrayList<TransactionRecord> results = ClubFinanceSystem.searchTransactions(
        club,
        searchField.getText(),
        startDatePicker.getValue(),
        endDatePicker.getValue(),
        minAmount,
        maxAmount,
        selectedProjectId
);
```

4. Clear `transactionsTableBody`.
5. Add one row per result.

Important note about Income/Expense tabs:

- The current backend model does not store transaction type.
- Amount is always non-negative.
- Therefore `Income` and `Expenses` tabs cannot be implemented correctly yet.
- Recommended controller behavior for now:
  - `All` shows all transactions.
  - `Income` and `Expenses` either show a message like "Transaction type is not available yet" or behave as placeholders.

## ProfileController

FXML file: `profilePage.fxml`

Important fields:

```java
sidebarUsername
logoutButton
usernameLabel
profileUsernameLabel
changePasswordForm
currentPasswordField
newPasswordField
confirmNewPasswordField
changePasswordErrorLabel
```

Handlers needed:

```java
handleGoToProjects()
handleGoToTransactions()
handleViewProfile()
handleLogout()
handleChangePassword()
handleConfirmChangePassword()
handleCancelChangePassword()
```

Suggested methods:

```java
public void loadFromSession()
private void showChangePasswordForm(boolean visible)
private void clearChangePasswordForm()
```

Profile page flow:

1. Get current user from `Session`.
2. Set:

```java
usernameLabel.setText(currentUser.getUsername());
sidebarUsername.setText(currentUser.getUsername());
profileUsernameLabel.setText(currentUser.getUsername());
```

3. `handleChangePassword()` shows `changePasswordForm`.
4. `handleCancelChangePassword()` clears and hides the form.

Important note about password update:

- There is currently no DAO method for updating a password.
- Either leave change password as a placeholder, or add a small `UserDAO.updatePassword(userId, newPassword)` method.
- If adding it, controller validation should check:
  - current password matches `currentUser.getPassword()`
  - new password length at least 8
  - confirm password matches new password

## LoginController

Current login validation exists, but improve session setup.

After successful login:

1. Use:

```java
User savedUser = UserDAO.findByUsernameAndPassword(username, password);
Session.setCurrentUser(savedUser);
```

2. Load `projects.fxml`.
3. Call:

```java
controller.loadFromSession();
```

This is better than only calling `controller.setUsername(username)` because the other controllers need the user id to load data.

## RegisterController

Current register flow mostly works.

FXML now contains both:

```java
errorLabel
successLabel
```

Controller currently only declares `errorLabel`. Add:

```java
@FXML private Label successLabel;
```

Suggested behavior:

- Use `errorLabel` for errors.
- Use `successLabel` for successful account creation.
- Clear one label when showing the other.

## Rendering Helpers

Controllers need to create cards/rows manually because there are no separate FXML card templates.

Project card minimum data:

```text
Project name
Allocated amount
Spent amount
Remaining amount
Open/View button
```

Pot card minimum data:

```text
Pot name
Allocated amount
Spent amount
Remaining amount
Delete/edit action if desired
```

Transaction row minimum data:

```text
Row number
Transaction name or short note
Project name
Pot name
Date
Paid by
Amount
Proof indicator/action if proofPath is not empty
```

Use helper methods like `formatMoney(double amount)` and `formatDate(LocalDate date)` so formatting is consistent.

## Error Handling

For controller methods that call backend/core methods:

```java
try {
    // backend call
} catch (SQLException e) {
    errorLabel.setText("Database error. Please try again.");
    e.printStackTrace();
} catch (IOException e) {
    errorLabel.setText("File error. Please try again.");
    e.printStackTrace();
}
```

Do not let exceptions crash the UI.

## Implementation Order

1. Update `LoginController` to store `Session.currentUser`.
2. Finish `ProjectsController`.
3. Implement navigation shared by all main pages.
4. Implement `ProjectPageController`.
5. Implement `TransactionPageController`.
6. Implement `ProfileController`.
7. Add password update support only if the team decides change-password must be fully functional.

## Acceptance Checklist

- Login stores the current user in `Session`.
- Projects page loads real club data.
- Create project works and refreshes the page.
- Project cards open project detail page.
- Project detail page loads real pots and transactions.
- Create pot works.
- Record transaction works.
- Transaction page search/filter uses `ClubFinanceSystem.searchTransactions(...)`.
- Profile page displays current user info.
- Logout clears `Session` and returns to login.
- Empty controllers are no longer empty.
- Every FXML `fx:id` has a matching `@FXML` field if the controller needs it.
- Every FXML `onAction`/`onMouseClicked` handler exists in its controller.

