# Custom QG

In order to test a personalized Quality Gate, the IES project was reused. This project consists of 3 Web Apps and a Fetcher utility which communicates with a Message Broker, a MySQL database and a MongoDB database.

Considering the characteristics of the project, the following metrics were chosen to evaluate the (overall) project's quality:

![Custom QG](readme-imgs/qg_custom.png)

## Conditions on New Code

| Metric | Operator | Value |
|--------|----------|-------|
| Maintainability Rating | is worse than | B |
| Bugs | greater than | 0 |
| Reliability Rating | is worse than | A |
| Security Rating | is worse than | A |

- **Maintainability Rating (<B)**: At the level that the project is, the entire code could use plenty of refactoring work, but at least new code should have an acceptable minimum of maintainability.
- **Bugs (>0)**: 

## Conditions on Overall Code

| Metric | Operator | Value |
|--------|----------|-------|
| Blocker issues | greater than | 0 |
| Bugs | greater than | 0 |
| Code smells | greater than | 50 |
| Critical issues | greater than | 5 |
| Vulnerabilities | greater than | 0 |


- **Vulnerabilities (>0)**: There should absolutely be no bugs, especially in the Security Guard's web app, which deals with private user information. Not only that, the