
import java.io.*;
import java.sql.*;
import java.util.*;

import com.jcraft.jsch.*;

/**
 * Create Database-410G11, insert tuples
 * @author xieyi
 * @version 1.0
 *
 */
public class CreateDatabase {

	public static void main(String[] args) throws SQLException {
		if (args.length != 3){
			System.out.println("Usage CreateDatabase <BroncoUser> <BroncoPassword> <DBname>");
		}
		else{
			Connection con = null;
			Session session = null;
			try
			{
				String broncoUserName = args[0];                  // SSH loging username
				String broncoPW = args[1];                   // SSH login password
				String dbName = args[2];
				String sshHostName = "onyx.boisestate.edu";          // hostname or ip or SSH server
				int sshPortNum = 22;                                    // remote SSH host port number
				String remoteHostName = "localhost";  // hostname or ip of your database server
				int localPortName = 3367;  // local port number use to bind SSH tunnel
				
				String sandBoxUserName = "msandbox";                    // database loging username
				String sanBoxPW = "yxiedb";                    // database login password
				int remotePortNum = 10126; // remote port number of database
				
				/* CREATE a SSH session to ONYX */
				session = CreateDatabase.doSshTunnel(broncoUserName, broncoPW, sshHostName, sshPortNum, remoteHostName, localPortName, remotePortNum);
				
				/* LOAD the Database DRIVER and obtain a CONNECTION */
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection("jdbc:mysql://localhost:"+localPortName, sandBoxUserName, sanBoxPW);
				
				/*Create database*/
				createDB(con, dbName);
				
				/*create tables*/
				createTables(con,dbName);
				
				/*Insert data*/
				insertData(con,dbName);
				
				/*Drop database*/
				//dropDB(con, dbName); //If we create unnecessary database, we could drop it.

				
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			finally{
				/*
				 * STEP 5
				 * CLOSE CONNECTION AND SSH SESSION
				 * 
				 * */
				con.close();
				session.disconnect();
			}
		}
	}
	
	/**
	 *SSH Tunnel connection
	 * @param broncoUserName Bronco User Name
	 * @param broncoPW	User's Bronco password
	 * @param sshHostName "onyx.boisestat.edu"
	 * @param sshPortNum	22
	 * @param remoteHostName	"localhost"
	 * @param localPortName 3367
	 * @param remotePortNum user's personal port number assigned by sandbox
	 * @return
	 * @throws JSchException
	 */
	private static Session doSshTunnel( String broncoUserName, String broncoPW, String sshHostName, int sshPortNum, String remoteHostName, int localPortName, int remotePortNum ) throws JSchException{
		/*This is one of the available choices to connect to mysql
		 * If you think you know another way, you can go ahead*/
		
		final JSch jsch = new JSch();
		java.util.Properties configuration = new java.util.Properties();
		configuration.put("StrictHostKeyChecking", "no");

		Session session = jsch.getSession( broncoUserName, sshHostName, 22 );
		session.setPassword( broncoPW );

		session.setConfig(configuration);
		session.connect();
		session.setPortForwardingL(localPortName, remoteHostName, remotePortNum);
		return session;
	}
	
	/**
	 * Create Database
	 * @param con Connection object
	 * @param DBName	Database name
	 * @throws SQLException	
	 */
	private static void createDB(Connection con, String DBName) throws SQLException{
		String sql;
		Statement stmt = null;
		System.out.println("Creating datebase " + DBName + "..." );
		stmt = con.createStatement();
		sql = "CREATE DATABASE " + DBName;
		stmt.executeUpdate(sql);
		System.out.println("Database " + DBName + " created successfully");
		
	}
	
	/**
	 * Drop database
	 * @param con Connection object
	 * @param DBName Database name
	 * @throws SQLException
	 */
	private static void dropDB(Connection con, String DBName) throws SQLException{
		String sql;
		Statement stmt = null;
		System.out.println("Dropping datebase " + DBName + "...");
		stmt = con.createStatement();
		sql = "DROP DATABASE " + DBName;
		stmt.executeUpdate(sql);
		System.out.println("Database " + DBName + " dropped successfully");
		
	}
	
	/**
	 * Create Tables
	 * @param con Connection object
	 * @param DBName Database name
	 * @throws SQLException
	 */
	private static void createTables(Connection con, String DBName) throws SQLException{
		String[] sql;
		String sqlDBName;
		
 		Statement stmt1 = con.createStatement();
  		sqlDBName = "USE " + DBName;
 		stmt1.executeUpdate(sqlDBName);		
 		
		sql = new String[6];
		sql[0] = "CREATE TABLE User (UserName CHAR(50) NOT NULL, ID BIGINT NOT NULL, Location CHAR(50), PRIMARY KEY (ID))";
		sql[1] = "CREATE TABLE Tweets ( UserID BIGINT NOT NULL, TweetID BIGINT NOT NULL AUTO_INCREMENT, TimeStamp DATETIME NOT NULL, Tweet CHAR(250) NOT NULL, PRIMARY KEY (TweetID), FOREIGN KEY (UserID) REFERENCES User (ID) ON DELETE CASCADE)";
 		sql[2] = "CREATE TABLE ReTweet( User1 BIGINT NOT NULL, User2 BIGINT NOT NULL, PRIMARY KEY(User1, User2), FOREIGN KEY (User1) REFERENCES User(ID) ON DELETE CASCADE, FOREIGN KEY (USER2) REFERENCES User(ID) ON DELETE CASCADE)";
 		sql[3] = "CREATE TABLE Mention(User1 BIGINT NOT NULL, User2 BIGINT NOT NULL, PRIMARY KEY(User1, User2), FOREIGN KEY (User1) REFERENCES User(ID) ON DELETE CASCADE, FOREIGN KEY (USER2) REFERENCES User(ID) ON DELETE CASCADE)";
 		sql[4] = "CREATE TABLE Reply(User1 BIGINT NOT NULL, User2 BIGINT NOT NULL, PRIMARY KEY(User1, User2), FOREIGN KEY (User1) REFERENCES User(ID) ON DELETE CASCADE, FOREIGN KEY (USER2) REFERENCES User(ID) ON DELETE CASCADE)";
 		sql[5] = "CREATE TABLE Followers( Follower BIGINT NOT NULL, Followee BIGINT NOT NULL, PRIMARY KEY(Follower, Followee), FOREIGN KEY (Follower) REFERENCES User(ID) ON DELETE CASCADE, FOREIGN KEY (Followee) REFERENCES User(ID) ON DELETE CASCADE )";
 		
 		System.out.println("Creating Tables: User, Tweets, ReTweet, Mention, Replay, Followers... ");
 		stmt1 = con.createStatement();
 		for(int i = 0; i < 6; i++){
 			stmt1.executeUpdate(sql[i]);
 		}
 		System.out.println("Tweet database tables created successfully.");
		
	}
	
	/**
	 * Insert tuples
	 * @param con Connection object
	 * @param DBName Database name
	 * @throws SQLException
	 */
	private static void insertData(Connection con, String DBName) throws SQLException {
		String[] sql;
		String sqlDBName;
		String sqlForeignKey;
		
 		Statement stmt1 = con.createStatement();
  		sqlDBName = "USE " + DBName;
 		stmt1.executeUpdate(sqlDBName);		
 		
 		Statement stmt2 = con.createStatement();
 		sqlForeignKey = "SET FOREIGN_KEY_CHECKS=0";
 		stmt2.executeUpdate(sqlForeignKey);
		
		sql = new String[6];
		sql[0] = "LOAD DATA INFILE '/home/YIXIE/workspace/CS410/user.csv'  INTO TABLE User FIELDS TERMINATED BY '|'  LINES TERMINATED BY '\n'";
		sql[1] = "LOAD DATA INFILE '/home/YIXIE/workspace/CS410/tweets.csv'  INTO TABLE Tweets FIELDS TERMINATED BY '|'  LINES TERMINATED BY '\n'";
 		sql[2] = "LOAD DATA INFILE '/home/YIXIE/workspace/CS410/ReTweet.csv'  INTO TABLE ReTweet FIELDS TERMINATED BY '|'  LINES TERMINATED BY '\n'";
 		sql[3] = "LOAD DATA INFILE '/home/YIXIE/workspace/CS410/Reply.csv'  INTO TABLE Reply FIELDS TERMINATED BY '|'  LINES TERMINATED BY '\n'";
 		sql[4] = "LOAD DATA INFILE '/home/YIXIE/workspace/CS410/Mention.csv'  INTO TABLE Mention FIELDS TERMINATED BY '|'  LINES TERMINATED BY '\n'";
 		sql[5] = "LOAD DATA INFILE '/home/YIXIE/workspace/CS410/followers.csv'  INTO TABLE Followers FIELDS TERMINATED BY '|'  LINES TERMINATED BY '\n'";
 		
 		System.out.println("Inserting data to tables: User, Tweets, ReTweet, Mention, Replay, Followers... ");
 		stmt1 = con.createStatement();
 		for(int i = 0; i < 6; i++){
 			stmt1.executeUpdate(sql[i]);
 		}
 		
 		Statement stmt3 = con.createStatement();
 		sqlForeignKey = "SET FOREIGN_KEY_CHECKS=1";
 		stmt3.executeUpdate(sqlForeignKey);
		System.out.println("Table data inserted successfully.");
	}
	

}
