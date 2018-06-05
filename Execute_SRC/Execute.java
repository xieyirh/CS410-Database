import java.io.*;
import java.sql.*;
import java.util.*;

import com.jcraft.jsch.*;

/**
 * Query or Update tuples with specified task files and parameters
 * @author Yi Xie
 * @version 1.0
 *
 */
/**
 * @author xieyi
 *
 */
public class Execute {

	public static void main(String[] args) throws SQLException {
			
			argsCheck(args);
			
			Connection con = null;
			Session session = null;
			try
			{ 	int queryMinArgs = 7;
				int updateMinArgs = 6;
				String queryOrUpdate= args[3];
				int paramNum = 0;
				if(queryOrUpdate.equalsIgnoreCase("query")){
					paramNum = args.length-queryMinArgs;
				}
				else if (queryOrUpdate.equalsIgnoreCase("update")){
					paramNum = args.length-updateMinArgs;
				}
				else {
					System.out.println("Usage Execute <BroncoUser> <BroncoPassword> <DBname> <query/update> <TaskNumber> <TaskQuery> <outputFile> <parametersforQuery>");
					System.exit(0);
				}
				String broncoUserName = args[0];                  // SSH loging username
				String broncoPW = args[1];                   // SSH login password
				String dbName = args[2];
				
				int taskNum = Integer.parseInt(args[4]);
				String taskFile = args[5];
				String outputFile = args[6];
				String[] paramForQuery;			//Using an array to store command line arguments
				String path = "";		//Default Task file is under the same directory, otherwise path has to be specified
				
				String sshHostName = "onyx.boisestate.edu";          // hostname or ip or SSH server
				int sshPortNum = 22;                                    // remote SSH host port number
				String remoteHostName = "localhost";  // hostname or ip of your database server
				int localPortName = 3367;  // local port number use to bind SSH tunnel
				
				String sandBoxUserName = "msandbox";                    // database loging username
				String sanBoxPW = "yxiedb";                    // database login password
				int remotePortNum = 10126; // remote port number of database
				
				/* CREATE a SSH session to ONYX */
				session = Execute.doSshTunnel(broncoUserName, broncoPW, sshHostName, sshPortNum, remoteHostName, localPortName, remotePortNum);
				
				/* LOAD the Database DRIVER and obtain a CONNECTION */
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection("jdbc:mysql://localhost:"+localPortName, sandBoxUserName, sanBoxPW);
				
				/*Using database*/
				useDB(con, dbName);
				
				/*Query tasks*/
				if (queryOrUpdate.equalsIgnoreCase("query")){
					
					switch(taskNum){
						case 1: 
						case 2:
						case 4:
							queryDB(con,dbName, path, taskFile, taskNum, outputFile);	//task 1, 2, 4 without any command line arguments
							break;
						
						case 3:
							paramForQuery = new String[paramNum];
							for (int i = 0; i < paramNum; i++){
								paramForQuery[i] = args[i + queryMinArgs];
							}
							queryDB(con,dbName, path, taskFile, taskNum, outputFile, paramForQuery);
							break;
						default:
							break;
						}
				}
				
				/*update tuples*/
				if(queryOrUpdate.equalsIgnoreCase("update")){
					if (paramNum > 0) {
						paramForQuery = new String[paramNum];
						for (int i = 0; i < paramNum; i++){
							paramForQuery[i] = args[i + updateMinArgs];
						}
					
						switch(taskNum){
							case 5:
								addNewUser(con,dbName, path, taskFile, taskNum, paramForQuery);
								break;
							case 6:
								addNewTweet(con,dbName, path, taskFile, taskNum, paramForQuery);
								break;
							case 7:
								addNewFollowers(con,dbName, path, taskFile, taskNum, paramForQuery);
								break;
							case 8:
								deleteUser(con,dbName, path, taskFile, taskNum, paramForQuery);
								break;
							default:
								System.out.println("Task Number is out of range for update operation!");
								break;
						}
					}
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			finally{
				 /* CLOSE CONNECTION AND SSH SESSION*/
				con.close();
				session.disconnect();
			}
		
	}
	
	/**
	 *SSH connection 
	 * @param broncoUserName
	 * @param broncoPW
	 * @param sshHostName
	 * @param sshPortNum
	 * @param remoteHostName
	 * @param localPortName
	 * @param remotePortNum
	 * @return
	 * @throws JSchException
	 */
	private static Session doSshTunnel( String broncoUserName, String broncoPW, String sshHostName, int sshPortNum, String remoteHostName, int localPortName, int remotePortNum ) throws JSchException{
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
	 * Use specified Database
	 * @param con Connection object
	 * @param DBName Database name
	 * @throws SQLException
	 */
	private static void useDB(Connection con, String DBName) throws SQLException{
		String sql;
		Statement stmt = null;
		stmt = con.createStatement();
		sql = "USE " + DBName;
		stmt.executeUpdate(sql);
		System.out.println("Using datebase " + DBName + "..." );
		
	}
	
	/**
	 * Query specified task from task file
	 * @param con Connection object
	 * @param DBName Database name
	 * @param path Task file directory
	 * @param taskFile	Task file name
	 * @param taskNum Task Number
	 * @param outputFile	Query output files
	 * @throws SQLException	
	 * @throws IOException
	 */
	private static void queryDB(Connection con, String DBName,String path, String taskFile, int taskNum, String outputFile) throws SQLException, IOException{
		String sql;
 		Task task = new Task(path, taskFile);
		sql = task.getStatement(path, taskFile);
		File resultFile = new File(outputFile);
		FileWriter fw = new FileWriter(resultFile);
		
 		System.out.println("Query Task" + taskNum + " :... ");
 		Statement stmt = con.createStatement();
 		ResultSet resultSet = stmt.executeQuery(sql);
 		ResultSetMetaData rsmd = resultSet.getMetaData();
 		int columnNum = rsmd.getColumnCount();
 		
 		String title = String.format("%-20s", "USER NAME") + String.format("%-20s", "ID") + "LOCATION";
 		String  line = "================================================================================";
 		fw.write(title + "\n");
 		fw.write(line + "\n");
 		
 		while(resultSet.next()){
 			String record = "";
 			for (int i = 1; i <= columnNum; i++){
 				record = record + String.format("%-20s", resultSet.getString(i));
 			}
 			record = record.trim();
 			fw.write(record + "\n");
 		}
 		
 		fw.flush();
 		fw.close();
 		System.out.println("Task" + taskNum + " query result was sent to the " + outputFile);
		
	}
	
	/**
	 * Query Task files with parameters from command line
	 * @param con
	 * @param DBName
	 * @param path
	 * @param taskFile
	 * @param taskNum
	 * @param outputFile
	 * @param paramForQuery
	 * @throws SQLException
	 * @throws IOException
	 */
	private static void queryDB(Connection con, String DBName,String path, String taskFile, int taskNum, String outputFile, String[] paramForQuery) throws SQLException, IOException{
		String sql;
		//int paramNum = paramForQuery.length;
		
 		Task task = new Task(path, taskFile);
		sql = task.getStatement(path, taskFile);
		File resultFile = new File(outputFile);
		FileWriter fw = new FileWriter(resultFile);
		
 		System.out.println("Query Task" + taskNum + " :... ");
 		PreparedStatement stmt = con.prepareStatement(sql);
 		String str = "%" + paramForQuery[0] + "%";
 		stmt.setString(1, str);
 		ResultSet resultSet = stmt.executeQuery();
 		ResultSetMetaData rsmd = resultSet.getMetaData();
 		int columnNum = rsmd.getColumnCount();
 		
 		String title = String.format("%-30s", "USER NAME") + String.format("%-30s", "ID") + String.format("%-30s", "Location") + "Tweet";
 		String  line = "=============================================================================================================================";
 		fw.write(title + "\n");
 		fw.write(line + "\n");
 		
 		while(resultSet.next()){
 			String record = "";
 			for (int i = 1; i <= columnNum; i++){
 				record = record + String.format("%-30s", resultSet.getString(i));
 			}
 			record = record.trim();
 			fw.write(record + "\n");
 		}
 		
 		fw.flush();
 		fw.close();
 		System.out.println("Task" + taskNum + " query result was sent to the " + outputFile);
		
	}
	
	/**
	 * Add new Tweet user to the User table
	 * @param con
	 * @param DBName
	 * @param path
	 * @param taskFile
	 * @param taskNum
	 * @param paramForQuery
	 * @throws SQLException
	 * @throws IOException
	 */
	private static void addNewUser(Connection con, String DBName, String path, String taskFile, int taskNum, String[] paramForQuery) throws SQLException, IOException{
		
		String sql;
		Task task = new Task(path, taskFile);
		sql = task.getStatement(path, taskFile);
		PreparedStatement stmt = con.prepareStatement(sql);
		int userID = Integer.parseInt(paramForQuery[1]);
		
 		System.out.println("Task" + taskNum + " : adding a new user... ");
 		
 		if (searchUser(con, DBName, userID)){
 			System.out.println("User has been In the User table, try again!");
 			System.exit(-1);
 		}
 		else {
	 		stmt.setString(1, paramForQuery[0]); 
	 		stmt.setInt(2, userID);
	 		stmt.setString(3,paramForQuery[2]);
	 		
	 		stmt.executeUpdate();
	 		
	 		System.out.println("Task" + taskNum + ": new user has been added to User table!");
 		}
		
	}
	
	/**
	 * Add new Tweet to the Tweet table
	 * @param con
	 * @param DBName
	 * @param path
	 * @param taskFile
	 * @param taskNum
	 * @param paramForQuery
	 * @throws SQLException
	 * @throws IOException
	 */
	private static void addNewTweet(Connection con, String DBName, String path, String taskFile, int taskNum, String[] paramForQuery) throws SQLException, IOException{
		String sql;
		Task task = new Task(path, taskFile);
		sql = task.getStatement(path, taskFile);
		PreparedStatement stmt = con.prepareStatement(sql);
		int userID = Integer.parseInt(paramForQuery[0]);
		
		/*TweetID is auto increased by the MySQL, and TweetTime is generated by MYSQL time stamp*/
 		System.out.println("Task" + taskNum + " : adding a new tweet... ");
 		if (searchUser(con,DBName, userID)){
 			stmt.setInt(1, userID);
 			stmt.setString(2, paramForQuery[1]);
	 		stmt.executeUpdate();
	 		System.out.println("Task" + taskNum + ": new tweet has been added to Tweet table!");
 		}
 		else {
 			System.out.println("User is not found in the User table!");
 			System.out.println("You may need add this user as following: ");
 			System.out.println("	Task 5 usage Execute <BroncoUser> <BroncoPassword> <DBname> <update> <TaskNumber> <TaskQuery> <UserName> <UserID> <Location>");
 			System.out.println("Task 6 usage Execute <BroncoUser> <BroncoPassword> <DBname> <update> <TaskNumber> <TaskQuery> <UserID> <Tweet>");
 			
 		}
	}
	
	/**
	 * Add new Followers to the Followers table
	 * @param con
	 * @param DBName
	 * @param path
	 * @param taskFile
	 * @param taskNum
	 * @param paramForQuery
	 * @throws SQLException
	 * @throws IOException
	 */
	private static void addNewFollowers(Connection con, String DBName, String path, String taskFile, int taskNum, String[] paramForQuery) throws SQLException, IOException{
		String sql;
		Task task = new Task(path, taskFile);
		sql = task.getStatement(path, taskFile);
		PreparedStatement stmt = con.prepareStatement(sql);
		int followee = Integer.parseInt(paramForQuery[0]);
		
		for( int i = 0; i < paramForQuery.length; i++){
			if(!searchUser(con, DBName, Integer.parseInt(paramForQuery[i]))){
				System.out.println("ID " + Integer.parseInt(paramForQuery[i]) +" is not found in the User table! Double check your inputs.");
				System.exit(0);
			}
		}
		
 		System.out.println("Task" + taskNum + " : adding followers... ");
 		
 		for (int i = 1; i < paramForQuery.length; i++ ){
 			stmt.setString(1, paramForQuery[i]);
 			stmt.setInt(2, followee);
	 		stmt.executeUpdate();
 		}
 		System.out.println("Task" + taskNum + ": new follower with followee has been added to 'Followers' table");
	}
	
	/**
	 * Cascade delete User
	 * @param con
	 * @param DBName
	 * @param path
	 * @param taskFile
	 * @param taskNum
	 * @param paramForQuery
	 * @throws SQLException
	 * @throws IOException
	 */
	private static void deleteUser(Connection con, String DBName, String path, String taskFile, int taskNum, String[] paramForQuery) throws SQLException, IOException{
		String sql;
		Task task = new Task(path, taskFile);
		sql = task.getStatement(path, taskFile);
		PreparedStatement stmt = con.prepareStatement(sql);
		int userID = Integer.parseInt(paramForQuery[0]);
		
 		System.out.println("Task" + taskNum + " : Deleting user from User table... ");
 		if (searchUser(con,DBName, userID)){
 			stmt.setInt(1, userID);
	 		stmt.executeUpdate();
 		}
 		else {
 			System.out.println("User is not in the User table, try again!");
 			System.exit(-1);
 			
 		}
 		
 		System.out.println("Task" + taskNum + ": user has been deleted from database!");
		
	}
	
	/**
	 * Helper method to search user in User table
	 * @param con
	 * @param DBName
	 * @param userID
	 * @return
	 * @throws SQLException
	 */
	private static Boolean searchUser(Connection con, String DBName, int userID) throws SQLException{
		String sql = " SELECT ID FROM " + DBName + ".User WHERE ID=" + userID;
		Statement stmt = con.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
		return resultSet.next();
	}
	
	/**
	 * Helper method to check command line arguments
	 * @param args
	 */
	private static void argsCheck(String[] args){
		if (args.length < 7){
			System.out.println("Usage Execute <BroncoUser> <BroncoPassword> <DBname> <query/update> <TaskNumber> <TaskQuery> <outputFile> <parametersforQuery>");
			System.out.println("	Task Query File");
			System.out.println("		1. Display details of Twitter users with ‘NO’ followers.");
			System.out.println("		2. Display details of Twitter users with maximum Re-tweets. ");
			System.out.println("		3. Display details of Twitter users along with the tweet who tweeted with a hashtag given in command line arguments (<parametersforQuery>). ");
			System.out.println("		4. Display details of Twitter users with more than 1,000 followers");
			System.out.println("		5. Add a new user. Provide user info in command line arguments (<parametersforQuery>)");
			System.out.println("		6. Add a new tweet for a user. Provide tweet info in command line arguments (<parametersforQuery>). If the tweeting user is not in the Database, prompt to ask new user info and insert the new user in the database. ");
			System.out.println("		7. Add new followers for user. Provide followers info in command line arguments (<parametersforQuery>)");
			System.out.println("		8.(Extra Credit) Delete a user from the database. Include CASCADE claus");
		}
		else {
			switch(Integer.parseInt(args[4])){
				case 1: 
				case 2:
				case 4:
					if(args.length != 7){
						System.out.println("Task 1/2 usage Execute <BroncoUser> <BroncoPassword> <DBname> <query> <TaskNumber> <TaskQuery> <outputFile>");
						System.exit(0);
					}
					break;
				case 3:
					if(args.length != 8){
						System.out.println("Task 3 usage Execute <BroncoUser> <BroncoPassword> <DBname> <query> <TaskNumber> <TaskQuery> <outputFile> < Hashtag>");
						System.exit(0);
					}
					break;
				case 5:
					if(args.length != 9){
						System.out.println("Task 5 usage Execute <BroncoUser> <BroncoPassword> <DBname> <update> <TaskNumber> <TaskQuery> <UserName> <UserID> <Location>");
						//system will auto create a user ID for new User
						System.exit(0);
					}
					break;
				case 6:
					if(args.length != 8){
						System.out.println("Task 6 usage Execute <BroncoUser> <BroncoPassword> <DBname> <update> <TaskNumber> <TaskQuery> <UserID> <Tweet>");
						System.exit(0);
					}
					break;
				case 7:
					if(args.length < 8){
						System.out.println("Task 7 usage Execute <BroncoUser> <BroncoPassword> <DBname> <update> <TaskNumber> <TaskQuery> <followee> <follower 1> <follower 2> <follower 3> ....");
						System.exit(0);
					}
					break;
				case 8:
					if(args.length != 7){
						System.out.println("Task 8 usage Execute <BroncoUser> <BroncoPassword> <DBname> <update> <TaskNumber> <TaskQuery> <UserID>");
						System.exit(0);
					}
					break;
				default:
					System.out.println("invalid taks number!");
					System.exit(0);
					break;
			}
		}
		
	}

}
