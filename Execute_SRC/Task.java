import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
/**
 * Task Object
 * @author Yi Xie
 *
 */
public class Task {
	
	String fileName;
	String path;
	
	/**
	 * Constructor
	 * @param path task file directory
	 * @param fileName task file name
	 */
	public Task(String path, String fileName){
		this.fileName = fileName;
	}
	
	/**
	 * Return Statement for the MySQL
	 * @param path
	 * @param fileName
	 * @return
	 */
	public String getStatement(String path, String fileName){
		String stmt ="";
		String taskFile = path + fileName;
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(taskFile));
		} catch (FileNotFoundException e){
			e.printStackTrace();
		}
		
		while(scanner.hasNextLine()){
			stmt = stmt + " " + scanner.nextLine();
		}
		
		scanner.close();
		return stmt;
	}
}
