/**
 * Schema Pair class to store table name and schema information as a pair
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
package project;
public class SchemaPair {
    private String tablename;
	private String schema;
	
	public SchemaPair(String t, String s) {
		tablename=t;
		schema=s;
	}
	/**
	 * Method to test the schema pair's value 
	 * 
	 * @return (boolean) whether the schema pair has the given 
	 * table name and schema
	 */
	public boolean equalsTo(String tablename, String schema) {
		return this.tablename.equals(tablename) && this.schema.equals(schema);
	}
	
	
	/**
	 * Method to test the schema pair's value 
	 * 
	 * @return (boolean) whether the schema pair equals the given schema
	 * pair information
	 */
	public boolean equalsTo(SchemaPair sch) {
		return this.tablename.equals(sch.tablename) && this.schema.equals(sch.schema);
	}
	
	/**
	 * Method to return table name 
	 * 
	 * @return (String) table name stored in the schema pair
	 */
	public String getTableName() {
		return tablename;
	}
	
	/**
	 * Method to return schema 
	 * 
	 * @return (String) schema stored in the schema pair
	 */
	public String getSchema() {
		return schema;
	}
	
	/**
	 * Method to return whole schema pair information 
	 * 
	 * @return (String) table name and table name stored in the schema pair
	 */
	public String toString() {
		return tablename+"."+schema;
	}
}
