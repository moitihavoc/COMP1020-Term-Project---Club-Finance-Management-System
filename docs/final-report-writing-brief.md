# Final Report Writing Brief

Use this file to write the final report. It follows the official sections 1-10
from the final report guideline and answers the required bullet points directly.

Keep the report centered on one concrete use case:

> A basketball club treasurer uses Mony to manage event budgets, split money
> into spending categories, record transactions with proof, and search old
> expenses when the club needs financial transparency.

The final report is limited to 10 pages. Put long test cases and extra
screenshots in the Appendix.

## 1. Introduction And Project Overview

### Problem Statement And Motivation

Small clubs often manage money through notebooks, spreadsheets, chat messages,
or scattered receipts. This makes it difficult for the treasurer to answer
simple finance questions later, such as which event spent the most money, which
category a payment belonged to, or whether a transaction had proof. Mony solves
this by giving the treasurer one structured place to manage budgets and
transactions.

### Project Objectives And Scope

The objective is to build a Java desktop application that helps a club treasurer
manage club finances. The scope includes user login, club balance management,
project budgets, pot/category budgets, transaction recording, proof image
support, and transaction search. The app is designed for a treasurer or finance
manager, not for every club member.

### Key Features And Functionalities

- Register and log in to protect finance records.
- View and update club balance.
- Create, edit, and delete projects for events or activities.
- Create, edit, and delete pots inside each project to divide the project
  budget into categories.
- Record transactions under a selected pot with amount, payer, date, note, and
  optional proof image.
- View project totals: allocated, spent, and remaining amount.
- Search transactions by keyword, date range, amount range, and project.
- Prevent invalid budget actions such as negative amounts and over-spending a
  pot.

### Modifications Since Proposal Or Interim Stage

Write this if it matches the team's progress:

The final version focuses more clearly on the treasurer use case. The project
structure was refined into `Club -> Project -> Pot -> Transaction`, transaction
search was strengthened with a seeded basketball-club dataset, and proof images
were added to support transparency.

## 2. System Requirements And Specifications

### Core Functionalities

- Account management: register, log in, change password, and log out.
- Dashboard: show the club balance and project overview.
- Project management: create, edit, delete, and open projects.
- Pot management: create, edit, delete, and track spending inside each project
  pot.
- Transaction management: add transactions to a pot and display them in a
  transaction table.
- Proof support: attach and view proof images for transactions.
- Search and filtering: search across transaction name, project, pot, payer, and
  note; filter by date, amount, and project.

### User Requirements And Expected Behaviors

The main user is a club treasurer with basic computer skills. The treasurer
expects the app to show clear totals, prevent invalid spending, preserve records
between app runs, and make old transactions easy to find. For example, the
basketball club treasurer should be able to search for water expenses, referee
fees, hotel payments, or transactions above a chosen amount.

### Performance, Usability, And Scalability Considerations

- Performance: the app is intended for club-scale data, such as dozens of
  projects and hundreds of transactions. Search is fast enough for this scale.
- Usability: the JavaFX interface uses forms, tables, progress bars, and clear
  validation messages so a non-technical treasurer can use it.
- Scalability: the current design uses local SQLite storage. This is suitable
  for one treasurer on one computer, but a larger organization could later add
  multi-user access, cloud sync, or SQL-level indexed search.

## 3. System Design And Architecture

### High-Level Architecture And Module Organization

Mony uses a layered JavaFX desktop architecture:

- UI layer: FXML screens and CSS define the interface.
- Controller layer: controllers handle user actions from each screen.
- Service layer: `ClubFinanceService` applies business rules and coordinates
  updates.
- Model layer: `User`, `Club`, `Project`, `Pot`, and `Transaction` represent
  the finance domain.
- DAO layer: DAO classes read and write records.
- Database layer: SQLite stores users, clubs, projects, pots, and transactions
  in `app-data/mony.db`.

### Diagrams To Include

Include two diagrams:

- Class diagram: show `User`, `Club`, `Project`, `Pot`, `Transaction`, service,
  and DAO classes.
- Sequence diagram: show the behavior flow for recording a club expense. Use
  the guide in `docs/sequence-diagram-guide.md`.

### OOP Concepts, Main Classes, Responsibilities, And Interactions

- `User`: represents the logged-in account.
- `Club`: represents the club budget and owns a list of projects.
- `Project`: represents an event or activity and owns a list of pots.
- `Pot`: represents a budget category and owns a list of transactions.
- `Transaction`: represents one recorded expense with amount, payer, date, note,
  and proof path.
- `ClubFinanceService`: connects the UI with business rules and persistence.
- DAO classes: separate database operations from UI and model logic.

Main OOP ideas:

- Encapsulation: fields are private and accessed through methods.
- Composition: a club contains projects, a project contains pots, and a pot
  contains transactions.
- Abstraction: controllers do not directly manage database details; DAO and
  service classes hide those details.
- Separation of concerns: UI, business logic, model objects, and database access
  are split into different packages.

Important design point:

`Transaction` belongs to `Pot` because every expense must be checked against a
specific budget category. If transactions were stored freely under a project,
the app could not clearly enforce category-level spending limits.

## 4. Data Structures And Algorithms

### Selected Data Structures And Reasons

- `ArrayList<Project>` inside `Club`: stores all projects for the club.
- `ArrayList<Pot>` inside `Project`: stores the budget categories for one
  project.
- `ArrayList<Transaction>` inside `Pot`: stores transactions for one budget
  category.

These lists match the natural hierarchy of the app and are simple to iterate
when displaying dashboards, project details, totals, and search results.

### Time And Space Complexity Considerations

For transaction search:

- Let P = number of projects.
- Let K = average number of pots per project.
- Let T = average number of transactions per pot.
- Search checks each transaction once, so time complexity is O(P * K * T).
- The loaded object graph uses O(P + P*K + P*K*T) space.

This is acceptable because the app is designed for a club treasurer, not a
large banking system.

### Algorithms Implemented And Their Roles

- Budget total calculation:
  - Club total allocated.
  - Club total spent.
  - Project total allocated to pots.
  - Project total spent.
  - Pot total spent.
- Budget validation:
  - A project cannot exceed the club balance.
  - A pot cannot exceed the project allocation.
  - A transaction cannot exceed the selected pot's remaining budget.
- Transaction search:
  - The app checks keyword, date range, amount range, and selected project.
  - Keyword search covers transaction name, project name, pot name, payer, and
    note.

### Trade-Offs And Optimization Decisions

The app loads the club's finance structure into objects because it makes the OOP
design clear and keeps the UI easy to update. The trade-off is that search is
performed by iterating through loaded objects. For the expected club-scale
dataset, this is simple and efficient enough. If the app grew much larger,
search could be optimized with SQL queries and database indexes.

## 5. Implementation Details

### Programming Language, Libraries, And Frameworks

- Java 25.
- JavaFX 21.0.6 for the desktop interface.
- FXML for screen layout.
- CSS for styling.
- SQLite for local data storage.
- `sqlite-jdbc` for Java-to-SQLite connection.
- Maven for dependency management and running the app.

### Important Implementation Details

- The app creates and uses a local database file at `app-data/mony.db`.
- The database tables are created automatically if they do not exist.
- Controllers respond to JavaFX user actions.
- `ClubFinanceService` checks budget rules before saving changes.
- Money input is parsed/formatted through utility code so amounts display
  consistently in VND.
- The transaction page can search across the full project-pot-transaction
  structure.

### File Structure And Package Organization

- `src/main/java/oop/mony/models/`: domain objects.
- `src/main/java/oop/mony/controllers/`: JavaFX screen controllers.
- `src/main/java/oop/mony/dao/`: database access objects.
- `src/main/java/oop/mony/utils/`: helper classes for navigation, dialogs,
  money formatting, and transaction table rendering.
- `src/main/resources/oop/mony/`: FXML screens and stylesheet.
- `pom.xml`: Maven configuration and dependencies.
- `local-docs/seed_basketball_club_transactions.sql`: sample basketball club
  data for testing and demonstration.

### External Tools Or APIs

The project does not depend on an external web API. It uses local JavaFX and a
local SQLite database, so the app can run without an internet connection after
dependencies are installed.

## 6. Testing And Evaluation

### Test Cases And Testing Methodology

Use scenario-based manual testing with the basketball club treasurer use case.
The test data should include many projects, pots, and transactions so search
can be evaluated meaningfully. The seed SQL creates a basketball club account
with 10 projects, 25 pots, and 100 transactions.

Main tests to discuss:

- Register and log in.
- Create a project for a club event.
- Create pots inside that project.
- Record a valid transaction under a pot.
- Reject a negative or zero transaction amount.
- Reject a transaction that exceeds the pot budget.
- Search transactions by keyword, date range, amount range, and project.
- Check that totals update after transactions are added.
- View proof image behavior.

Put the full test-case table in the Appendix.

### Screenshots Or Sample Outputs

Include only the most important screenshots in the main report:

- Login or dashboard screen.
- Project page with pots and budget totals.
- Transaction form.
- Transaction search page with meaningful basketball club results.
- Validation error for an invalid or over-budget transaction.

Put extra screenshots in the Appendix.

### Performance Evaluation Or Benchmarking

For this project, a simple evaluation is enough:

The app was tested with seeded basketball club data containing 100 transactions.
The transaction search and page updates remained responsive for the expected
club-scale dataset.

### Correctness, Robustness, And Usability

- Correctness: totals are calculated from actual projects, pots, and
  transactions; transactions appear under the selected pot.
- Robustness: the app handles empty names, invalid numbers, negative amounts,
  zero amounts, and over-budget spending attempts.
- Usability: the screens are organized around the treasurer's workflow:
  dashboard -> project -> pot -> transaction -> search.

## 7. Challenges And Solutions

### Technical Difficulties

Challenge: at the interim stage, the team had completed login, dashboard, and
basic project creation, but the main finance features were still unfinished:
pots, transactions, proof attachments, and search.

Solution: the team completed the finance workflow by adding pots under projects,
transactions under pots, proof image support, and a transaction search page. The
final app now supports the full treasurer flow: dashboard -> project -> pot ->
transaction -> search.

Challenge: keeping project, pot, and transaction totals correct after users add,
edit, or delete records.

Solution: the app calculates totals from the current object structure instead
of manually storing separate totals. Club totals come from projects, project
totals come from pots, and pot totals come from transactions. After each update,
the app reloads the club data so the UI shows the latest spent and remaining
amounts.

Challenge: learning and combining the required technologies, especially JavaFX
for the interface and SQLite for persistent storage.

Solution: the team used self-study, examples, and repeated testing to connect
the JavaFX screens with backend logic and local database storage.

### Design Or Architectural Issues

Challenge: deciding whether transactions should belong directly to a project or
inside a pot.

Solution: transactions are stored under pots because pots represent budget
categories. This lets the app check the selected pot's remaining budget before
accepting an expense. This design also makes the finance structure easier to
explain: a club has projects, a project has pots, and a pot has transactions.

Challenge: integrating the JavaFX frontend with the backend structure without
making controllers too responsible for data and business rules.

Solution: the app uses a layered structure. FXML files define the screens,
controllers handle user actions, model classes represent finance objects, DAO
classes handle database operations, and `ClubFinanceService` coordinates the
main business logic.

Challenge: making transaction search meaningful for the final demo and report.

Solution: the team prepared a realistic basketball club dataset with many
projects, pots, and transactions. This allows the search page to demonstrate
real use cases, such as finding water expenses, referee fees, hotel payments, or
transactions above a chosen amount.

### Team Collaboration And Task Coordination

Challenge: early team coordination was difficult because responsibilities and
progress were not always clear.

Solution: the team clarified responsibilities in a shared document, held regular
progress meetings, and divided work by feature area: UI screens, model/service
logic, database/DAO work, testing, and report/presentation preparation. For the
final stage, the team also used the same basketball club treasurer scenario
across the report, slides, poster, and demo to keep the submission consistent.

Challenge: the interim timeline underestimated the amount of work needed for
core finance features.

Solution: the team broke large tasks into smaller parts, such as pot creation,
transaction creation, proof image support, search filters, and test data
preparation. This made progress easier to track and helped the team complete the
missing features before the final submission.

### Solutions Implemented And Lessons Learned

- A layered structure makes the app easier to maintain and explain.
- Budget checks should happen before saving a project, pot, or transaction.
- Transactions should belong to pots because pots control spending categories.
- Realistic test data is necessary to demonstrate search clearly.
- Large tasks should be broken into smaller milestones earlier.
- A concrete use case is easier to present than disconnected CRUD examples.

## 8. Conclusion And Limitations

### Overall Achievements And Completed Objectives

Mony successfully implements a working JavaFX club finance management system.
It supports login, project budgets, pot/category budgets, transaction recording,
proof image support, persistent SQLite storage, and transaction search. The
final version demonstrates OOP through the `Club -> Project -> Pot ->
Transaction` structure.

### Strengths Of The Developed System

- Clear treasurer-focused workflow.
- Strong budget hierarchy.
- Transactions are tied to budget categories.
- Search helps check old spending quickly.
- Proof images support transparency.
- SQLite persistence keeps data between app sessions.

### Current Limitations Or Unresolved Issues

- The app is local-device based and not designed for multiple simultaneous
  users.
- Password handling should be improved with hashing in a production version.
- Search is suitable for club-scale data but not optimized for very large
  databases.
- Proof images depend on local file paths.
- The app does not yet export finance reports to PDF or CSV.

### Possible Future Improvements Or Extensions

- Add password hashing and stronger account security.
- Add export features for reports, CSV files, or receipts.
- Add charts for monthly spending and category comparisons.
- Add role-based access for treasurer, club president, and auditor.
- Add cloud backup or multi-device synchronization.
- Add SQL indexes or database-level search for larger datasets.

## 9. Team Contributions

Use a table. Replace the names and responsibilities with the real team details.

| Member | Main Responsibility | Specific Contributions |
| --- | --- | --- |
| Member 1 | UI / JavaFX screens | Built or improved login, dashboard, project, or transaction screens. |
| Member 2 | Models and business logic | Worked on `Club`, `Project`, `Pot`, `Transaction`, and budget validation. |
| Member 3 | Database and DAO layer | Implemented SQLite tables and DAO operations. |
| Member 4 | Testing and demo data | Prepared test cases, screenshots, and basketball club seed data. |
| Member 5 | Report, poster, and presentation | Organized documentation, slides, poster, and final demo script. |

Make the final version specific. Mention actual files, features, or test cases
each person handled.

## 10. References

Use one consistent citation style. Include only sources actually used.

Recommended references:

- Official Java documentation.
- Official JavaFX documentation.
- SQLite documentation.
- Maven documentation.
- Xerial SQLite JDBC documentation.
- COMP1020 course materials and final project guidelines.

The References section does not count toward the 10-page limit.

## Appendix Reminder

The Appendix is not section 1-10, but it is allowed by the guideline and does
not count toward the 10-page limit. Put extended test cases, extra screenshots,
and large diagrams there.
