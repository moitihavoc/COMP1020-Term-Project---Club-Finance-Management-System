# Sequence Diagram: App Behavior Flow

Draw this sequence diagram for the final report:

**Record A Club Expense**

This diagram should show what happens in the app from the treasurer's point of
view. Do not include code classes, methods, DAO, or database details.

## Lifelines

Put these boxes across the top, from left to right:

1. **Treasurer**
2. **Login Screen**
3. **Dashboard**
4. **Project Page**
5. **Pot Section**
6. **Transaction Form**
7. **Transaction List**

Use dashed vertical lines under each box.

## Arrows To Include

Use these exact arrow labels, from top to bottom:

1. Treasurer -> Login Screen  
   **Enter username and password**

2. Login Screen -> Dashboard  
   **Open club dashboard**

3. Treasurer -> Dashboard  
   **Select a project**

4. Dashboard -> Project Page  
   **Show project budget details**

5. Project Page -> Pot Section  
   **Show pots and remaining budgets**

6. Treasurer -> Pot Section  
   **Choose the correct pot**

7. Treasurer -> Transaction Form  
   **Enter expense details**

8. Treasurer -> Transaction Form  
   **Upload proof image**

9. Treasurer -> Transaction Form  
   **Click Confirm**

10. Transaction Form -> Pot Section  
    **Check remaining pot budget**

11. Pot Section -> Transaction Form  
    **Budget is enough**

12. Transaction Form -> Transaction List  
    **Add new transaction**

13. Transaction List -> Project Page  
    **Update spent and remaining amounts**

14. Project Page -> Treasurer  
    **Show updated project page**

## Optional Error Branch

If there is space, add an `alt` box after **Check remaining pot budget**.

Condition:

**Expense is higher than remaining pot budget**

Inside that branch, write:

1. Pot Section -> Transaction Form  
   **Budget is not enough**

2. Transaction Form -> Treasurer  
   **Show error message**

3. Treasurer -> Transaction Form  
   **Edit amount or choose another pot**

## Words To Put Under The Diagram

Use this caption:

> This sequence diagram shows the behavior flow when a treasurer records a club
> expense. The treasurer logs in, opens a project, chooses the correct pot, and
> enters the transaction details with proof. Before the expense is accepted, the
> app checks whether the selected pot still has enough budget. If the budget is
> valid, the transaction appears in the transaction list and the project totals
> are updated. This shows why each transaction must belong to a pot: the pot is
> the budget category that controls whether the expense is allowed.

## Drawing Notes

- Solid arrows = user actions or screen changes.
- Dashed arrows = app responses.
- Yellow vertical rectangles = active screens.
- Keep labels short.
- Focus on what the user sees and does.
- The most important message is: **a transaction is recorded inside a pot so the
  app can check the correct budget category.**
