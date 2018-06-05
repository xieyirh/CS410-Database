# Final Project: Twitter Database Schema and Data
* Author: Joshua Claus, Yi Xie
* Class: CS410
* Semester: Spring 2017
## Overview
Use JAVA JDBC to create database and do query and update database from outside. There are 3 main portions: a Java program to create and populate an SQL database, a Java program to query or update said database, and some data analysis.

## Compiling and Using
### 1. Create the database, insert tuples
**Compiling** 
`javac CreateDatabase.java`

**Using**
`java CreateDatabase <<Broncouser> <broncopassword> <DBname>`
* Broncouser- bronco username
* broncopasspword - user's password to access bronco account
* DBname - our Database name is 410G11

**Note**
1. Some nesscesary access parameters has been hardcoded into the program, such as ssh host name, port number, sandbox name and password etc. 
2. There is an extra method in the program to help user drop the schema. it is commented out by default.

### 2.Java program to query or update the database
**Compiling**
`javac Execute.java`
**Using**
There are 8 tasks in this part:
* Task 1 Display details of Twitter users with ‘NO’ followers.
    `java Execute <BroncoUser> <BroncoPassword> <DBname> <query> <TaskNumber> <TaskQuery> <outputFile>`
* Task 2 Display details of Twitter users with maximum Re-tweets.
    `java java Execute <BroncoUser> <BroncoPassword> <DBname> <query> <TaskNumber> <TaskQuery> <outputFile>`
* Task 3 Display details of Twitter users along with the tweet who tweeted with a hashtag given in command line arguments (<parametersforQuery>). User could type # in the command line.
`java Execute <BroncoUser> <BroncoPassword> <DBname> <query> <TaskNumber> <TaskQuery> <outputFile> < Hashtag>`
* Task 4 Display details of Twitter users with more than 1,000 followers
`java Execute <BroncoUser> <BroncoPassword> <DBname> <query> <TaskNumber> <TaskQuery> <outputFile>`
* Task 5 Add a new user. Provide user info in command line arguments (<parametersforQuery>)
`java Execute <BroncoUser> <BroncoPassword> <DBname> <update> <TaskNumber> <TaskQuery> <UserName> <UserID> <Location>`
* Task 6 Add a new tweet for a user. Provide tweet info in command line arguments (<parametersforQuery>). If the tweeting user is not in the Database, prompt to ask new user info and insert the new user in the database.
`java Execute <BroncoUser> <BroncoPassword> <DBname> <update> <TaskNumber> <TaskQuery> <UserID> <Tweet>`
* Task 7 Add new followers for user. Provide followers info in command line arguments (<parametersforQuery>)
`java Execute <BroncoUser> <BroncoPassword> <DBname> <update> <TaskNumber> <TaskQuery> <followee> <follower 1> <follower 2> <follower 3> ....`
* Task 8 (Extra Credit) Delete a user from the database. Include CASCADE claus
`java Execute <BroncoUser> <BroncoPassword> <DBname> <update> <TaskNumber> <TaskQuery> <UserID>`

***@parameters***
* Broncouser- bronco username
* broncopasspword - user's password to access bronco account
* DBname - our Database name is 410G11
* TaskNumber - correlated task number
* TaskQuery - Task file named like task1.txt task2.txt...
* outputFile - User specifies the output file name
* Hashtag - such as %#%
* UserName - User name of the tweeter user in the `User` table
* UserID - ID number of the tweeter user in the `User` table
* Location - Location of the tweeter user in the `User`table
* Tweet - tweet contents e.g. " I am happy today"
* followee - follwee's user ID
* follower - followers' user ID

**Note**
The program assume the execution file and task file under the same folder, otherwise, user has to sepcify the path name in the code with `path` variable.

### Exercise 3

------------------------------------------------
|  TASK  |  NORMAL TIME  |  IGNORE INDEX TIME  |
------------------------------------------------
|    1   |     2.8294    |       0.00033       |
------------------------------------------------
|    2   |     0.3488    |       0.00073       |
------------------------------------------------
|    3   |     0.0236    |       0.00682       |
------------------------------------------------
|    4   |     2.2554    |       0.00035       |
------------------------------------------------

TASK:  Number of task used for analysis.

NORMAL TIME:  Time in seconds taken for task to complete under normal
  usage. Averaged time of 5 runs.

IGNORE INDEX TIME:  Time in seconds taken for task to complete adding 
  'IGNORE TABLE' into query. Averaged time of 5 runs.


## Discussion
Creating this project took more time that originally realized, despite the difficulty not being super hard.  Much of the  time was in figuring out how to get the queries correct and in getting Java and MySQL to play nice together.

## Testing
 Most of our testing was simply running the queries and modifying them until we got the correct results.

## Extra Credit
 Task 8 was accomplished.

## Sources used
Text book