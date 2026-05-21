# Login and Create Account Usage

This project currently has two classes for login/register work:

- `User` in `src/main/java/oop/mony/models/User.java`
- `UserDAO` in `src/main/java/oop/mony/dao/UserDAO.java`

Controller files can use either `User` or `UserDAO`, but using `User` is simpler.

## Recommended Controller Usage

Import the `User` class:

```java
import oop.mony.models.User;
```

Create a user object from the text fields:

```java
String username = usernameField.getText();
String password = passwordField.getText();

User user = new User(username, password);
```

## Login

Use:

```java
boolean success = user.login();
```

Example:

```java
User user = new User(usernameField.getText(), passwordField.getText());

if (user.login()) {
    // Login success
    // Go to next page
} else {
    // Login failed
    errorLabel.setText("Invalid username or password.");
}
```

`login()` returns:

- `true` if username and password match a user in the database
- `false` if username is empty, password is empty, username does not exist, or password is wrong

## Create Account

Use:

```java
boolean created = user.createAccount();
```

Example:

```java
User user = new User(usernameField.getText(), passwordField.getText());

if (user.createAccount()) {
    // Account created
    // Go to login page or next page
} else {
    // Account not created
    errorLabel.setText("Username already exists or input is empty.");
}
```

`createAccount()` returns:

- `true` if the account was created
- `false` if username is empty, password is empty, or username already exists

## Optional Static Usage

You can also call the static methods directly:

```java
boolean loginSuccess = User.login(username, password);
boolean accountCreated = User.createAccount(username, password);
```

This does the same thing as creating a `User` object first.

## Direct DAO Usage

Only use this if you specifically want the controller to call the database layer directly.

Import:

```java
import oop.mony.dao.UserDAO;
```

Usage:

```java
boolean loginSuccess = UserDAO.login(username, password);
boolean accountCreated = UserDAO.createAccount(new User(username, password));
```

These DAO methods can throw `SQLException`, so the controller must handle it:

```java
try {
    boolean loginSuccess = UserDAO.login(username, password);
} catch (SQLException e) {
    errorLabel.setText("Database error.");
}
```

Using `User` is easier because `User.login()` and `User.createAccount()` already handle the `SQLException`.

## Database

The SQLite database file is:

```text
app-data/mony.db
```

The current `users` table stores:

```text
id
username
password
created_at
```

Only login and create account are supported right now.
