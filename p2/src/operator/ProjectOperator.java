package operator;
import project.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
/**
 * Project operator to execute the projection
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
public class ProjectOperator extends Operator{
	private Operator child;
	private ArrayList<SchemaPair> schema_pair;

	
	public ProjectOperator(Operator child, ArrayList<SchemaPair> schema_pair){
		this.child = child; //could be either a SelectOperator or a ScanOperator
		this.schema_pair = schema_pair;
		
	}
	
	/**
	 * Method to get the next tuple of its child operator(could be
	   either a SelectOperator or a ScanOperator)
	 * 
	 * @return (Tuple) result of the projection operator
	 */
	@Override
	public Tuple getNextTuple() throws IOException {
		Tuple tu;
		String[] t;
		StringBuffer sb = new StringBuffer();

		while((tu = child.getNextTuple())!=null){
			ArrayList<SchemaPair> sp = tu.getSchemaList();
			t = tu.getTuple();
			for(SchemaPair require_sp: schema_pair){//get the required columns
				for(SchemaPair sch:sp){
					String tablename = require_sp.getTableName();
					String columnname = require_sp.getSchema();
					if (sch.equalsTo(tablename, columnname)){
						int index=tu.getSchemaList().indexOf(sch);
						sb.append(t[index]).append(",");						
						}
				}
			}
			return new Tuple(sb.toString().split(","), schema_pair);
		}	
		return null;
	}

	/**
	 * Method to reset after projection by reset its child
	 */
	@Override
	public void reset() throws IOException {
		child.reset();
	}

	/**
	 * Method to dump the results of the projection operator
	 */
	@Override
	public void dump() throws IOException {
		FileWriter output= new FileWriter(catalog.getInstance().getOutputdir()+File.separator+"query"+QueryPlan.getCount(),false);
		BufferedWriter br= new BufferedWriter(output);
		Tuple tu;
		
	    while ((tu=this.getNextTuple())!=null) {
	    br.write(tu.getComplete());  
	    br.newLine();
	    }
	    br.close();
	    QueryPlan.nextQuery();
	}

}
