package project;
import java.util.ArrayList;
/**
 * Tuple class to store tuple data and schema list information
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
public class Tuple{
	private String[] tuple;
	private ArrayList<SchemaPair> schemaList;

	public Tuple(String[] tuple, ArrayList<SchemaPair> schemaList) {
		this.tuple=tuple;
		this.schemaList=schemaList;
	}
	
	/**
	 * Method to return tuple 
	 * 
	 * @return (String[]) tuple information 
	 */
	public String[] getTuple() {
		return tuple;
	}
	
	/**
	 * Method to return tuple in a visible way
	 * 
	 * @return (String) tuple information 
	 */
	public String getComplete() {
		int l= tuple.length;
		int n=0;
		String s="";
		while(n<l) {	    
			s = s + tuple[n];
			if (n!=l-1) {s=s+",";}
			n++;
		}
		return s;
	}
	
	/**
	 * Method to return schema list information
	 * 
	 * @return (ArrayList<SchemaPair>) Schema list stored in tuple
	 */
	public ArrayList<SchemaPair> getSchemaList() {
		return schemaList;
	}
	
	public void setSchemaList(ArrayList schema) {
		schemaList=schema;
	}
	}
