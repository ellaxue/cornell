/**
 * Using singleton pattern to store the information of basic information, such
 * as table file location, table schema and alias.
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */

package project;
import java.io.File;
import java.util.*;

import BPlusTree.IndexInfo;
import ChooseSelectionImp.RelationInfo;

public final class catalog {
	private static final catalog INSTANCE = new catalog();
	private HashMap<String, String> tableLocation = new HashMap<String, String>();
	private HashMap<String, ArrayList<String>> tableSchema = new HashMap<String, ArrayList<String>>();
	private HashMap<String, String> alias = new HashMap<String, String>();
	private HashMap<String, RelationInfo> relationInfoMap = new HashMap<String, RelationInfo>();
	private HashMap<String, HashMap<String,Boolean>> indexes = new HashMap<String, HashMap<String,Boolean>>();
	private String outputdir;
	private Boolean usingAlias = false;
	private String tempFileDir;
	private String inputDir;
	private String indexDir;
	private String schemaFilePath;
	private String database;
	private String indexInforFilePath;
	private String statsFilePath;
	private Boolean buildIndex;
	private Boolean evalQuery;
	
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

	public void setIndexInfo(String tableName, String column, Boolean cluster){
		if(!indexes.containsKey(tableName)){
			HashMap<String, Boolean> map = new HashMap<String, Boolean>();
			map.put(column, cluster);
			indexes.put(tableName,map);			
		}
	}
	
	public HashMap<String,Boolean> getAllTableIndexes(String tableName){
		if(indexes.containsKey(tableName)){
			return indexes.get(tableName);
		}
		return null;
	}
	
	public void printIndexInfo(){
		for(Map.Entry<String, HashMap<String, Boolean>> entry:indexes.entrySet()){
			for(Map.Entry<String, Boolean> indexes:  entry.getValue().entrySet()){
				System.out.println("Table " + entry.getKey() + " has an index on " + 
						indexes.getKey() + ", is cluster = " + indexes.getValue());
			}			
		}
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
		this.tempFileDir = dir;
	}
	
	public String getTempFileDir(){
		return this.tempFileDir;
	}

	public void setInputDir(String inputDir) {
		this.inputDir = inputDir;
		schemaFilePath = inputDir + File.separator + "db" + File.separator + "schema.txt";
		database = inputDir + File.separator + "db" + File.separator + "data"; 
		indexInforFilePath = inputDir + File.separator + "db" + File.separator+"index_info.txt";
		indexDir = inputDir+File.separator+"db"+File.separator+"indexes";
		statsFilePath = inputDir + File.separator + "db" + File.separator + "stats.txt";
	}
	
	public String getInputDir(){
		return this.inputDir;
	}
	
	public String getSchemaFilePath(){
		return this.schemaFilePath;
	}
	
	public String getDatabaseDir(){
		return this.database;
	}
	
	public String getIndexInforFilePath(){
		return indexInforFilePath;
	}
	
	public String getIndexDir(){
		return this.indexDir;
	}

	public void setBuildIndex(boolean flag) {
		buildIndex = flag;
	}
	
	public Boolean shouldBuildIndex(){
		return this.buildIndex;
	}
	
	public Boolean shouldEvalQuery(){
		return this.evalQuery;
	}

	public void setEvalQuery(boolean flag) {
		this.evalQuery = flag;
	}

	public HashMap<String, HashMap<String,Boolean>> getIndexes() {
		return indexes;
	}

	public void setIndexes(HashMap<String, HashMap<String,Boolean>> indexes) {
		this.indexes = indexes;
	}

	/**
	 * 
	 * @param rinfo relation info, the table name is the original name (not alias name)
	 */
	public void setRelationInfo(RelationInfo rinfo) {
		// TODO Auto-generated method stub
		System.out.println("===============> " + rinfo.getTableName());
		relationInfoMap.put(rinfo.getTableName(), rinfo);
	}
	
	public String getStatFilePath(){
		return this.statsFilePath;
	}
	
	public RelationInfo getRelation(String tableName){
		return relationInfoMap.get(tableName);
	}

	public boolean hasIndex(String columnName, String tableName) {
		// TODO Auto-generated method stub
		if(indexes.containsKey(tableName)){
			return indexes.get(tableName).containsKey(columnName);		
		}
		return false;
	}
	
	public boolean isIndexClustered(String columnName, String tableName){
		if(hasIndex(columnName,tableName)){
			return indexes.get(tableName).get(columnName);		
		}
		return false;
	}
}
