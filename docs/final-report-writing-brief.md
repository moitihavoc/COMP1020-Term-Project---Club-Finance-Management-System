# Final Report Writing Brief

## Report Goal

The final report should prove that Mony is a completed Java OOP and data
structures project, not just a set of screens. It should explain the problem,
the intended user, the object model, the data flow, the algorithms/search logic,
the database-backed implementation, and the evidence that the app works.

The report has a maximum of 10 pages, excluding References and Appendix.
Move long screenshots and full test cases into the Appendix.

## Project Summary To Use

Mony is a club finance management system for a club treasurer. The application
supports account login, club budget setup, project creation, pot allocation,
transaction recording, proof image attachment, and transaction search/filtering.
The main use case is a basketball club treasurer managing event and training
expenses in VND.

Recommended concrete scenario:

> A basketball club treasurer receives a yearly or event budget, creates
> projects such as a city tournament or training camp, divides each project into
> spending pots such as referee fees, food, transport, hotel, equipment, and
> medical support, records every transaction with payer/date/note/proof, then
> searches past transactions when the club needs to check spending transparency.

## Required Sections And Suggested Content

### 1. Introduction And Project Overview

Write:

- Problem: small clubs often track money manually, which makes it hard to check
  allocations, spending limits, receipts, and old transactions.
- User: the primary user is the club treasurer or finance manager.
- Objective: build a desktop app that organizes club money into projects, pots,
  and transactions.
- Key features:
  - Login and registration.
  - Dashboard with total club balance and projects.
  - Project budget allocation.
  - Pot allocation inside a project.
  - Transaction recording under a pot.
  - Proof image upload/viewing.
  - Transaction search by keyword, date range, amount range, and project.
- Mention any changes since proposal/interim if applicable.

### 2. System Requirements And Specifications

Functional requirements:

- A user can register and log in.
- A user can view and update the club balance.
- A user can create, edit, and delete projects.
- A user can create, edit, and delete pots inside a project.
- A user can record transactions under a selected pot.
- A user can attach and view proof images for transactions.
- A user can search/filter transactions.
- The system prevents invalid budget operations, such as spending more than the
  available pot allocation.

Non-functional requirements:

- Usability: should be usable by a non-technical treasurer on a normal office
  computer.
- Reliability: data is stored in SQLite so records persist between runs.
- Transparency: proof images and notes support later checking.
- Robustness: validation handles empty names, negative amounts, zero
  transactions, and over-allocation.
- Maintainability: code is organized into models, controllers, DAOs, utilities,
  FXML resources, and a service layer.

### 3. System Design And Architecture

Describe the architecture as JavaFX MVC-style with persistence:

- View layer: FXML files and CSS in `src/main/resources/oop/mony/`.
- Controller layer: JavaFX controllers in `src/main/java/oop/mony/controllers/`.
- Service layer: `ClubFinanceService` coordinates business rules and reloads the
  full club object graph after changes.
- Model layer: `User`, `Club`, `Project`, `Pot`, and `Transaction`.
- DAO layer: `UserDAO`, `ClubDAO`, `ProjectDAO`, `PotDAO`, and `TransactionDAO`
  use SQLite.
- Database setup: `Database` creates required tables in `app-data/mony.db`.

Important design explanation:

- A `Transaction` belongs to a `Pot` because spending must be checked against a
  specific allocated category. If transactions floated freely under a project,
  the app could not enforce category-level limits or explain which budget bucket
  the money came from.
- A `Pot` belongs to a `Project` because each event or activity has its own
  budget structure.
- A `Project` belongs to a `Club`/user so each treasurer only sees their own
  finance data.

Recommended diagrams:

- Class diagram: show `User`, `Club`, `Project`, `Pot`, `Transaction`, service,
  and DAOs.
- Sequence diagram: show "record transaction" flow from UI controller to service
  to model validation to DAO to SQLite, then reload UI.
- Optional architecture diagram: JavaFX UI -> Controllers -> Service -> Models
  and DAOs -> SQLite.

### 4. Data Structures And Algorithms

Data structures:

- `ArrayList<Project>` in `Club` stores projects.
- `ArrayList<Pot>` in `Project` stores pots.
- `ArrayList<Transaction>` in `Pot` stores transactions.
- Lists are appropriate because the project is small to medium scale, and the
  UI often needs ordered iteration for display.

Algorithms:

- Budget aggregation:
  - `Club.getTotalAllocated()`
  - `Club.getTotalSpent()`
  - `Project.getTotalAllocatedToPots()`
  - `Project.getTotalSpent()`
  - `Pot.getTotalSpent()`
- Validation:
  - `Club.canAddProject(amount)`
  - `Project.canAddPot(amount)`
  - `Pot.canAddTransaction(amount)`
- Search/filter:
  - `Club.searchTransactions(...)` iterates through projects, pots, and
    transactions, then applies keyword/date/amount/project filters.
  - `Transaction.matchesKeyword(...)` checks transaction name, project name, pot
    name, payer, and note.

Complexity:

- Let P = number of projects, K = average pots per project, and T = average
  transactions per pot.
- Full transaction search is O(P * K * T), which is acceptable for the course
  project and expected club-scale usage.
- Space for the loaded object graph is also O(P + P*K + P*K*T).

Trade-off:

- Loading the full object graph makes UI display and search simple, readable,
  and strongly tied to the OOP model.
- A larger production system could push more filtering into SQL or add indexes.

### 5. Implementation Details

Technologies:

- Java 25 configured in Maven.
- JavaFX 21.0.6 for desktop UI.
- FXML for screen layout.
- CSS for styling.
- SQLite with `sqlite-jdbc` for local persistence.
- Maven wrapper for repeatable build/run commands.

Important files:

- `pom.xml`: dependencies and JavaFX Maven plugin.
- `src/main/java/oop/mony/Application.java`: JavaFX application entry.
- `src/main/java/oop/mony/Launcher.java`: launcher main class.
- `src/main/java/oop/mony/ClubFinanceService.java`: business operations.
- `src/main/java/oop/mony/models/`: OOP domain classes.
- `src/main/java/oop/mony/dao/`: database access classes.
- `src/main/java/oop/mony/controllers/`: JavaFX screen logic.
- `src/main/resources/oop/mony/`: FXML and CSS.
- `local-docs/seed_basketball_club_transactions.sql`: demo dataset.

Mention that the database file is created under `app-data/mony.db`.

### 6. Testing And Evaluation

Use the basketball club scenario as the main evaluation story.

Main test scenario:

1. Log in as the seeded basketball club treasurer.
2. Confirm existing projects and pots are loaded.
3. Open a project such as the Ho Chi Minh City basketball tournament.
4. Review pot allocations and progress bars.
5. Create a transaction under a pot.
6. Try an invalid transaction amount that exceeds the pot allocation.
7. Search transactions by keyword such as water, jersey, hotel, referee, or
   Facebook ads.
8. Filter by date range, amount range, and project.
9. View proof image behavior where proof exists.

Testing evidence:

- Login/register screenshots.
- Dashboard screenshot.
- Project page screenshot with pots.
- Transaction creation form.
- Validation error for invalid amount.
- Transaction search page with meaningful seeded results.
- Appendix table of test cases.

### 7. Challenges And Solutions

Potential points:

- Challenge: deciding where transactions should belong.
  - Solution: attach transactions to pots to enforce category-level budget
    limits.
- Challenge: keeping UI data synchronized after create/edit/delete operations.
  - Solution: service methods reload the full club object graph after changes.
- Challenge: validating budget operations at multiple levels.
  - Solution: model methods calculate totals and service/controller checks block
    invalid allocation/spending.
- Challenge: making search meaningful.
  - Solution: seeded dataset with many transactions and filters across keyword,
    date, amount, and project.
- Challenge: storing local data without a server.
  - Solution: SQLite database in `app-data`.

### 8. Conclusion And Limitations

Achievements:

- Working JavaFX finance app.
- Persistent database.
- Clear OOP hierarchy.
- Budget validation.
- Searchable transaction history.
- Proof-image support.

Limitations:

- Passwords appear to be stored simply; production should hash passwords.
- Single-user/local SQLite design, not multi-device or cloud synchronized.
- Search is in-memory after loading the object graph; acceptable for club-scale
  data but not optimized for very large datasets.
- Proof image handling is local-file based.
- Reporting/export features could be added later.

Future improvements:

- Password hashing.
- Export to CSV/PDF.
- More analytics charts.
- Role-based access for president, treasurer, and auditor.
- SQL-level indexed search for large datasets.
- Backup/import features.

### 9. Team Contributions

Ask each member for concrete contribution bullets. Do not write generic lines.
Use a table with:

- Member name.
- Main responsibility.
- Files/features contributed.
- Testing/documentation contribution.

### 10. References

Possible references:

- Java documentation.
- JavaFX documentation.
- SQLite documentation.
- Maven documentation.
- sqlite-jdbc documentation.
- Course materials or assignment guidelines.

References do not count toward the 10-page limit.

### 11. Appendix

Put long material here:

- Full test case table.
- Extra screenshots.
- Full class diagram if too large.
- Seed data description.
- Known edge cases.

Appendix does not count toward the 10-page limit.

## Writing Priorities

Use the concrete basketball club treasurer story throughout. Avoid describing
the app as generic CRUD. The strongest report will connect every technical
choice back to finance transparency, budget allocation, and transaction
tracking.
