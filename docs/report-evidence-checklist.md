# Report Evidence Checklist

Use this checklist to collect proof for the final report, appendix, slides, and
poster.

## Screenshots To Capture

- Login screen.
- Register screen.
- Dashboard after login.
- Club balance editing or display.
- Project list with multiple meaningful projects.
- Project detail page showing allocated amount, spent amount, and remaining
  amount.
- Pot cards with spent/allocated progress bars.
- Create pot form.
- Create transaction form.
- Transaction table inside a project.
- Main transaction search page.
- Search result for a meaningful keyword, such as:
  - water
  - jersey
  - hotel
  - referee
  - Facebook
  - Da Nang
- Date range filter result.
- Amount range filter result.
- Project filter result.
- Validation error for negative or invalid amount.
- Validation error for over-spending a pot.
- Proof image upload/view behavior.

## Test Cases For Appendix

| ID | Scenario | Steps | Expected Result | Evidence |
| --- | --- | --- | --- | --- |
| TC01 | Register account | Enter username/password and submit | Account is created and user can log in | Screenshot |
| TC02 | Login | Use valid credentials | Dashboard opens | Screenshot |
| TC03 | Reject invalid login | Use wrong password | Error is shown | Screenshot |
| TC04 | Create project | Add project with valid budget | Project appears on dashboard | Screenshot |
| TC05 | Reject project over-allocation | Add project above club balance | Error/no invalid update | Screenshot |
| TC06 | Create pot | Add pot within project allocation | Pot appears in project page | Screenshot |
| TC07 | Reject pot over-allocation | Add pot above project remaining allocation | Error is shown | Screenshot |
| TC08 | Create transaction | Add transaction within pot allocation | Transaction appears and totals update | Screenshot |
| TC09 | Reject zero/negative transaction | Enter amount <= 0 | Error is shown | Screenshot |
| TC10 | Reject transaction over pot remaining | Enter amount above remaining pot budget | Error is shown | Screenshot |
| TC11 | Search by keyword | Search seeded transactions | Matching records appear | Screenshot |
| TC12 | Search by date range | Select start/end date | Only transactions in range appear | Screenshot |
| TC13 | Search by amount range | Set min/max amount | Only matching amounts appear | Screenshot |
| TC14 | Filter by project | Select one project | Only that project's transactions appear | Screenshot |
| TC15 | View proof | Open proof for a transaction with proof path | Proof viewer opens or no-proof state is clear | Screenshot |

## Data Evidence

Use `local-docs/seed_basketball_club_transactions.sql` as demo data.

It creates:

- User: `basketball_admin`
- Password: `basketball123`
- Club: basketball club demo account
- 10 projects
- 25 pots
- 100 transactions

This dataset is useful because the TA specifically recommended preparing many
transactions so search demonstrates real value.

## Diagrams To Prepare

### Class Diagram

Include these main classes:

- `User`
- `Club`
- `Project`
- `Pot`
- `Transaction`
- `ClubFinanceService`
- DAO classes:
  - `UserDAO`
  - `ClubDAO`
  - `ProjectDAO`
  - `PotDAO`
  - `TransactionDAO`

Important relationships:

- `Club` has many `Project`.
- `Project` has many `Pot`.
- `Pot` has many `Transaction`.
- Controllers call `ClubFinanceService`.
- Service calls DAOs and model methods.
- DAOs persist data to SQLite.

### Sequence Diagram

Recommended sequence: "Create Transaction".

Flow:

1. Treasurer fills transaction form.
2. `ProjectTransactionSectionController` validates required UI inputs.
3. Controller asks selected `Pot` whether transaction can be added.
4. Controller calls `ClubFinanceService.createTransaction(...)`.
5. Service validates club, pot, transaction name, amount, and pot remaining
   allocation.
6. Service calls `TransactionDAO.createTransaction(...)`.
7. DAO inserts transaction into SQLite.
8. Service reloads full club data.
9. Controller refreshes project totals, pot progress, and transaction table.

### Architecture Diagram

Recommended layers:

```text
JavaFX FXML/CSS UI
        |
Controllers
        |
ClubFinanceService
        |
Models + DAOs
        |
SQLite database in app-data/mony.db
```

## Metrics And Complexity Evidence

Use these in the Data Structures and Algorithms section:

- Transaction search iterates through projects, pots, and transactions.
- Complexity is O(P * K * T), where:
  - P = number of projects
  - K = average pots per project
  - T = average transactions per pot
- This is acceptable for a club finance app because data volume is modest and
  the object graph is easy to display in JavaFX.

## Final Product Evidence

Before final submission, confirm the package includes:

- `src/`
- `pom.xml`
- `mvnw`
- `mvnw.cmd`
- required resources under `src/main/resources`
- seed SQL or sample data instructions
- README with build/run instructions
- any app data policy or reset instructions

Potential commands to document:

```powershell
.\mvnw.cmd clean javafx:run
```

If packaging a zip, avoid including unnecessary generated build folders unless
the instructor requests them.
