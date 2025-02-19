# READ ME

## Project Introduction

At McGill University, as part of ECSE 321, we are developing an application for board game enthusiasts. The objective of this application is to enable the lending and borrowing of board games, the creation and joining of events, and connecting with other board game enthusiasts. The project report can be found [here](https://github.com/McGill-ECSE321-Winter2025/project-group-8/wiki/Project-report)

## Database Configuration
- port: 5433
- password: skibidi

## Project Scope
### Functional Scope
- User authentication and role-based access control
-  Game collection management for game owners
-  Event creation and event registration system
-   Borrow requests

### Technical Scope
- Java Spring Boot backend
- PostgreSQL database
- RESTful API architecture
- Gradle build system
- Comprehensive tests (JUnit)
- Development tasks and sprint planning managed through GitHub Issues

## Team Introduction - LG Smart Fridge
### Team Members, Roles and Contributions

| Name                              | Role                  | Deliverable 1 (hrs) | Deliverable 2 (hrs) | Deliverable 3 (hrs) | Total Hours |
|-----------------------------------|-----------------------|---------------------|---------------------|---------------------|-------------|
| Alexander Kudinov                | Backend Developer     | 7                   | X                   | X                   | X           |
| Shayan Yamanidouzi Sorkhabi       | Frontend Developer    | 6                   | X                   | X                   | X           |
| Jiwoong Choi                      | Test Lead            | 2                   | X                   | X                   | X           |
| Youssef El Kesti                  | Scrum Master         | 6                   | X                   | X                   | X           |
| David Wang                        | Project Manager      | 5.5                   | X                   | X                   | X           |
| Yessine Chaari                    | DevOps               | 8.0                   | X                   | X                   | X           |
| Rayan Baida                       | Fullstack Developer  | 7.5                   | X                   | X                   | X           |
- The details on the contribution of each member are available at [projects/Deliverable1](https://github.com/orgs/McGill-ECSE321-Winter2025/projects/22) and [meeting minutes](https://github.com/McGill-ECSE321-Winter2025/project-group-8/wiki/Project-report#meeting-minutes)

## Info on Project Branches

```main``` is the stable branch of our project, it will capture the state of our project at ***deliverable submission*** time.

```main-development``` is where we push working features/code for our *current deliverable* that 
***passes tests*** and/or has been ***peer-reviewed*** by teammates.

```dev-{your_username}``` is the branch where you will work on your own tasks assigned to you for the deliverable so that if 
you write bad code here (we all do at one point), it won't affect ```main``` or ```main-development```.

## Commit Message Methodology

### Commit Types:
* ```Feature``` -> Implemented/worked on a feature
* ```Fix``` -> Bug fix, fixing broken stuff, etc.
* ```Maintain``` -> Minor changes that are more for quality of life

```<type of commit>: <short description of feature(s) affected/what was done>```

* ```Ex: Feature: Implementing email sending to customers upon successful cart checkout```
* ```Ex: Fix: Fixed bug where Bogo sort would not run in O(n!)```
* ```Ex: Mtn: Refactored code to make it more readable```
