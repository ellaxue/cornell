/**
 * Using singleton pattern to store the information of basic information, such
 * as table file location, table schema and alias.
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */

package project;
import java.util.*;

public final class catalog {
	private static final catalog INSTANCE = new catalog();
	private HashMap<String, String> tableLocation = new HashMap<String, String>();
	private HashMap<String, ArrayList<String>> tableSchema = new HashMap<String, ArrayList<String>>();
	private HashMap<String, String> alias = new HashMap<String, String>();
	private String outputdir;
	private Boolean usingAlias = false;
	private String tempFileDir;
	private catalog() {
	}

	/** 
	 * get the only instance of catalog
	 * 
	 * @return the calalog instance
	 */
	public static catalog getInstance() {
		return INSTANCE;
	}

	/**
	 * set the output directory
	 * @param output directory
	 */
	public void setOutputdir(String outputdir) {
		this.outputdir = outputdir;
	}

	/**
	 * get the output directory
	 * @return output directory
	 */
	public String getOutputdir() {
		return outputdir;
	}

	/**
	 * method to store table info into catalog
	 * @param full table name
	 * @param corresponding file directory of the table
	 * @param column names of the table
	 */
	public void storeTableInfo(String tableName, String fileDirectory, ArrayList<String> schema) {
		tableLocation.put(tableName, fileDirectory);
		tableSchema.put(tableName, schema);
	}

	/**
	 * store alias information into catalog
	 * @param alias
	 * @param corresponding table name
	 */
	public void storeAlias(String alias, String tablename) {
		this.alias.put(alias, tablename);
	}

	/**
	 * set whether a query use alias
	 * @param boolean whether a query use alias
	 */
	public void setUseAlias(boolean t) {
		usingAlias = t;
	}

	/**
	 * get the hashmap that store the table location info
	 * @return hashmap 
	 */
	public HashMap<String, String> getTableLocation() {
		return tableLocation;
	}

	/**
	 * get the hashmap that store table schema info
	 * @return hashmap
	 */
	public HashMap<String, ArrayList<String>> getTableSchema() {
		return tableSchema;
	}

	/**
	 * get the HashMap that store alias info(pointing from alias to full table name)
	 * @return hashmap
	 */
	public HashMap<String, String> getAlias() {
		return alias;
	}

	/**
	 * return the boolean whether a query evaluated now uses alias;
	 * @return true if using alias, else false
	 */
	public boolean UseAlias() {
		return usingAlias;
	}

	public void setTempFileDir(String dir) {
		// TODO Auto-generated method stub
		this.tempFileDir = dir;
	}
	
	public String getTempFileDir(){
		return this.tempFileDir;
	}
}
