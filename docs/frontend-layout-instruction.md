# Frontend Layout Prompt

Use this prompt with Claude or another frontend/layout assistant to redesign and complete the JavaFX FXML screens. The goal is to add missing UI components and align the frontend with the backend model.

## Prompt

You are helping design the JavaFX frontend layout for a Club Finance Management System.

The backend model is already defined. The app tracks club money by event:

- A `User` owns projects.
- A `Club` stores the user's overall club finance summary.
- A `Project` represents a club event.
- A `Pot` belongs to a project and represents a specific allocation category, such as food, operations, decoration, or equipment.
- A `Transaction` belongs to a pot and represents real money spent.

Use this financial language consistently:

- `Allocated`: planned or approved money.
- `Spent`: real money already used through transactions.
- `Remaining`: money left after spending.

Avoid using the word `budget` if it may confuse users. Prefer `Allocated`.

Do not implement controller logic. Focus on improving the FXML layout and adding the missing UI components with useful `fx:id` values so controllers can connect later.

## Screens To Update

### 1. Home / Projects Page

FXML file:

`src/main/resources/oop/mony/projects.fxml`

Purpose:

This is the home page after login. It should show the club's overall finance summary and a list of projects.

Current page already has:

- sidebar navigation
- username greeting
- total balance card
- total spent card
- remaining balance card
- create project button
- `projectsGrid` container

Add or adjust the layout so the page supports:

- Total Balance
- Total Allocated
- Total Spent
- Remaining Balance

The page currently does not have a `Total Allocated` card. Add one near the other summary cards.

The top search field should have an `fx:id`, for example:

- `projectSearchField`

The project cards displayed inside `projectsGrid` should be designed to show:

- project name
- allocated amount
- spent amount
- remaining amount
- delete action

Add support for creating a project. This can be a dialog area, modal-style form, or a form section. It should collect:

- project name
- allocated amount

Keep the page focused on project overview. Do not add transaction details here except through the sidebar navigation.

Sidebar should include:

- Projects
- Transactions
- Change password
- Logout

Logout can be placed near the sidebar footer.

### 2. Project Detail Page

FXML file:

`src/main/resources/oop/mony/projectPage.fxml`

Purpose:

This page shows one selected project/event.

Current page already has:

- project title
- summary cards
- `potsGrid`
- `transactionsContainer`
- create pot button
- record transaction button

Adjust the summary cards to match backend language:

- Allocated
- Spent
- Remaining

The page should clearly show the selected project name.

Add or design the UI structure for pot cards inside `potsGrid`. Each pot card should show:

- pot name
- allocated amount
- spent amount
- remaining amount
- delete action

Add support for creating a pot. This can be a dialog area, modal-style form, or form section. It should collect:

- pot name
- allocated amount

Add support for recording a transaction from this project page. The transaction form should collect:

- transaction name
- pot selection
- amount
- paid by
- transaction date
- proof image upload
- note

The project page has a transaction section. Design transaction rows for `transactionsContainer` that can show recent transactions for the selected project. Each row should include:

- date
- transaction name
- pot
- paid by
- amount
- truncated note
- proof action

The top search field should have an `fx:id`, for example:

- `projectPageSearchField`

Sidebar should include:

- Projects
- current project name as a sub-item
- Transactions
- Change password
- Logout

### 3. Transactions Page

FXML file:

`src/main/resources/oop/mony/transactionPage.fxml`

Purpose:

This page shows transactions across all projects and pots owned by the current user.

Current page already has:

- search field
- title
- record transaction button
- table-like layout
- `transactionsTableBody`
- hardcoded sample rows

Replace or redesign the table structure so it supports these columns:

- Date
- Transaction Name
- Project
- Pot
- Paid By
- Amount
- Note
- Proof

The note column should be truncated in the table. The full note can be shown later through a tooltip, details popup, or row click, but that interaction does not need to be implemented now.

The proof column should be shown as a user action such as:

- View Proof
- Open Proof

Do not show raw file paths in the UI.

Remove the idea of `Payment Method` from the table unless the backend later adds that field. The current backend has `paidBy`, not payment method.

The search field should search:

- transaction name
- note
- paid by

Use a clearer prompt text such as:

- `Search name, note, or paid by`

Add filters to this page:

- project dropdown
- start date
- end date
- minimum amount
- maximum amount

The project dropdown should include:

- All Projects
- each project owned by the user

The date filters can be placed near the search field or in a filter bar above the transaction table.

The amount filters can be placed near the date filters.

Add a clear/reset filters action if there is space.

The hardcoded sample rows should be removed or treated as placeholders only. The controller will eventually populate `transactionsTableBody` dynamically.

Sidebar should include:

- Projects
- Transactions
- Change password
- Logout

### 4. Change Password Page

FXML file:

`src/main/resources/oop/mony/profilePage.fxml`

Purpose:

This page lets the user update their password.

The page should show a clear change password area. It should collect:

- current password
- new password
- confirm new password

Keep logout visible and easy to find.

Sidebar should include:

- Projects
- Transactions
- Change password
- Logout

### 5. Login Page

FXML file:

`src/main/resources/oop/mony/login.fxml`

Purpose:

Allow existing users to log in.

Current layout is mostly complete.

Keep:

- username field
- password field
- login button
- create account button
- error label

Optional improvements:

- loading/disabled state while logging in
- clearer error space

Do not add unnecessary complexity.

### 6. Register Page

FXML file:

`src/main/resources/oop/mony/register.fxml`

Purpose:

Allow new users to create an account.

Current layout is mostly complete.

Keep:

- username field
- password field
- confirm password field
- register button
- back to login button
- error label

Optional improvement:

- success message area
- automatic navigation after account creation can be handled by controller later

## General Requirements

Add useful `fx:id` values for new controls so controllers can connect them later.

Suggested new IDs:

- `totalAllocatedLabel`
- `projectSearchField`
- `projectPageSearchField`
- `projectFilterComboBox`
- `startDatePicker`
- `endDatePicker`
- `minAmountField`
- `maxAmountField`
- `clearFiltersButton`
- `transactionNameField`
- `transactionAmountField`
- `paidByField`
- `transactionDatePicker`
- `proofUploadButton`
- `noteField`
- `potComboBox`
- `logoutButton`

Keep the UI understandable for beginner users. The main workflows should be obvious:

- see club summary
- create project
- create pot
- record transaction
- search/filter transactions
- view proof
- logout

Do not implement backend logic in FXML. Only design layout and name the components clearly.
