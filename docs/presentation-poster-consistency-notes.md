# Presentation, Poster, And Report Consistency Notes

The TA specifically said the report, recorded presentation, poster, and slides
should be consistent. Use this file as the shared message bank.

## One-Sentence Project Description

Mony is a JavaFX desktop app that helps a club treasurer manage club money by
organizing budgets into projects, pots, and proof-backed transactions.

## Primary User

The primary user is a club treasurer or finance manager. The app is not meant
for every club member. It is designed for the person responsible for recording,
checking, and explaining club spending.

## Main Use Case

A basketball club treasurer manages a tournament budget. The treasurer creates a
project for the tournament, divides the budget into pots such as registration,
referee fees, food, transport, hotel, and equipment, then records transactions
inside the correct pot with payer, amount, date, note, and proof image. Later,
the treasurer searches transactions to answer questions such as "How much did we
spend on water?" or "Which hotel payments were made for the Da Nang trip?"

## STAR Poster Structure

### Situation

Small clubs often manage money with scattered notes, receipts, chat messages, or
spreadsheets. This makes it difficult for a treasurer to prove where money went,
especially after many events and transactions.

### Task

Build a desktop finance management application for a club treasurer. The app
must support budget allocation, transaction recording, proof tracking, and
searchable financial history.

### Action

The team implemented a JavaFX and SQLite application using an OOP hierarchy:

- `Club`
- `Project`
- `Pot`
- `Transaction`

The app uses controllers for UI behavior, DAO classes for database access, and a
service layer for business rules such as preventing over-allocation.

### Result

The finished app can create projects, divide money into pots, record
transactions, attach proof images, calculate spent/remaining amounts, and search
transactions using keyword, date, amount, and project filters.

## Demo Script Outline

Use slides briefly, then show the running app.

Suggested flow:

1. Explain login:
   - Login limits access to the treasurer account.
2. Open dashboard:
   - Show club balance and projects.
3. Open a basketball club project:
   - Explain that a project is an event or activity.
4. Show pots:
   - Explain that pots divide one project budget into categories.
5. Create or review a transaction:
   - Explain that every transaction belongs to a pot.
   - Mention proof image support for transparency.
6. Show validation:
   - Try a zero, negative, or over-budget amount.
7. Show transaction search:
   - Use seeded data to search by keyword/date/amount/project.
8. End with limitation/future work:
   - Password hashing, export reports, more analytics, multi-user roles.

Skip or speed up repetitive typing. Pause only on the parts that prove the app's
main value.

## Phrases To Use Consistently

- "club treasurer"
- "budget allocation"
- "project"
- "pot"
- "transaction"
- "proof image"
- "financial transparency"
- "searchable spending history"
- "prevents over-allocation"

## Avoid These Descriptions

- Do not describe the app as only "CRUD".
- Do not say it is for all club members.
- Do not show random projects or transactions with no story.
- Do not make the report, poster, and video use different examples.

## Final Submission Dates

- Final report: June 2, 2026, 11:59 PM.
- Recorded video: June 2, 2026, 11:59 PM.
- Poster PDF: June 2, 2026, 11:59 PM.
- Final product/code package: June 2, 2026, 11:59 PM.
- Poster presentation: June 4, 2026, 1:30 PM to 3:00 PM.
