# READ ME

## Project Introduction

At McGill University, as part of ECSE 321, we are developing an application for board game enthusiasts. The objective of this application is to enable the lending and borrowing of board games, the creation and joining of events, and connecting with other board game enthusiasts.

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
| Alexander Kudinov                | Backend Developer     | X                   | X                   | X                   | X           |
| Shayan Yamanidouzi Sorkhabi       | Frontend Developer    | X                   | X                   | X                   | X           |
| Jiwoong Choi                      | Test Lead            | X                   | X                   | X                   | X           |
| Youssef El Kesti                  | Scrum Master         | X                   | X                   | X                   | X           |
| David Wang                        | Project Manager      | 5.5                   | X                   | X                   | X           |
| Yessine Chaari                    | DevOps               | X                   | X                   | X                   | X           |
| Rayan Baida                       | Fullstack Developer  | X                   | X                   | X                   | X           |


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
