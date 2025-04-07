# READ ME

## Project Introduction

At McGill University, as part of ECSE 321, we are developing an application for board game enthusiasts. The objective of this application is to enable the lending and borrowing of board games, the creation and joining of events, and connecting with other board game enthusiasts. The project report for deliverable 2 can be found [here](https://github.com/McGill-ECSE321-Winter2025/project-group-8/wiki/Project-Report-2)

## Database Configuration
- port: 5433
- password: skibidi

## Build System
The build system documentation can be found [here](https://github.com/McGill-ECSE321-Winter2025/project-group-8/wiki/Build-System-Documentation)

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
| Alexander Kudinov                | Backend Developer     | 7                   | 15                   | 15                   | 37           |
| Shayan Yamanidouzi Sorkhabi       | Frontend Developer    | 6                   | 15                   | X                   | X           |
| Jiwoong Choi                      | Test Lead            | 2                   | 18                   | X                   | X           |
| Youssef El Kesti                  | Scrum Master         | 6                   | 14                   | X                   | X           |
| David Wang                        | Project Manager      | 5.5                   | 15                   | X                   | X           |
| Yessine Chaari                    | DevOps               | 8.0                   | 15                   | X                   | X           |
| Rayan Baida                       | Fullstack Developer  | 7.5                   | 17                   | X                   | X           |
- The details on the contribution of each member are available at [Team Contribution Report](https://github.com/McGill-ECSE321-Winter2025/project-group-8/wiki/Team-contributions#team-contributions), as well as [projects/Deliverable1](https://github.com/orgs/McGill-ECSE321-Winter2025/projects/22), [projects/Deliverable2](https://github.com/orgs/McGill-ECSE321-Winter2025/projects/34), and [meeting minutes](https://github.com/McGill-ECSE321-Winter2025/project-group-8/wiki/Project-report#meeting-minutes)

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

## Email Configuration

For the password reset functionality to work, you need to configure email credentials in your environment variables or in a `.env` file.

1. Copy the `.env.example` file to a new file named `.env` 
2. Update the email credentials in the `.env` file:
   ```
   EMAIL_USERNAME=your-email@gmail.com
   EMAIL_PASSWORD=your-app-password
   ```

**Note for Gmail users:** If you're using Gmail and have 2-Step Verification enabled, you'll need to generate an App Password:
1. Go to your Google Account settings
2. Navigate to Security → App passwords
3. Generate a new app password for the application
4. Use this password in the `.env` file instead of your regular password

The system will use these credentials to send password reset emails without requiring manual authentication through the console.

## JWT Configuration

The application uses JSON Web Tokens (JWT) for authentication. You need to configure a secure secret key:

1. In your `.env` file, set a strong secret key that is at least 64 bytes (512 bits) long:
   ```
   JWT_SECRET=ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZab
   ```

**Important:** 
- The JWT secret must be at least 64 bytes long as it's using the HS512 algorithm.
- The JWT secret must NOT contain hyphens or special characters that are invalid in Base64 encoding. Use only letters, numbers, and underscores.
