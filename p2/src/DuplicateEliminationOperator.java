import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
/**
 * operator to support DISTINCT constraint.
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */

public class DuplicateEliminationOperator extends Operator {
	private Operator child;
	private ArrayList<Tuple> distinctTuple = new ArrayList<Tuple>();
	private int count;

	/** 
	 * Assuming the tuple from child is sorted already. Simply compare
	 * the adjacent tuple and remove the duplicate tuple according to 
	 * the columns in the select clause.
	 * @param child of the operator (which must be a sort operator here)
	 * @param schema_pair the list containing <tablename,columnnmae> pair which is in the select clause
	 * @throws IOException
	 */
	public DuplicateEliminationOperator(Operator child, ArrayList<SchemaPair> schema_pair) throws IOException {
		this.child = child;
		ArrayList<Tuple> childTu = new ArrayList<Tuple>();
		Tuple tu;
		while ((tu = child.getNextTuple()) != null) {
			childTu.add(tu);
		}
		if(!childTu.isEmpty()) {Tuple firstTuple = childTu.get(0);
		for (Tuple tuple : childTu) {
			boolean repeat = false;
			if (tuple != firstTuple) {
				for (SchemaPair pair : schema_pair) {
					for (SchemaPair p : firstTuple.getSchemaList()) {
						if (pair.equalsTo(p)) {
							pair = p;
						}
					}
					int indext1 = firstTuple.getSchemaList().indexOf(pair);
					Long valuet1 = Long.parseLong(firstTuple.getTuple()[indext1]);
					for (SchemaPair p : tuple.getSchemaList()) {
						if (pair.equalsTo(p)) {
							pair = p;
						}
					}
					int indext2 = tuple.getSchemaList().indexOf(pair);
					Long valuet2 = Long.parseLong(tuple.getTuple()[indext2]);
					if (!valuet1.equals(valuet2)) {//If encounter a distinct value, break and return the tuple
						repeat = false;
						break;
					}else repeat = true;
				}
			}
		
			if (!repeat) {distinctTuple.add(tuple);}
			firstTuple=tuple;
		}
		}
	}

	/** 
	 * Constructor to deal with "select distinct *" 
	 * @param child of the operator (which must be a sort operator here)
	 * @throws IOException
	 */
	public DuplicateEliminationOperator(Operator child) throws IOException {
		this.child = child;
		ArrayList<Tuple> childTu = new ArrayList<Tuple>();
		Tuple tu;
		while ((tu = child.getNextTuple()) != null) {
			childTu.add(tu);
		}
		if(!childTu.isEmpty()) {Tuple firstTuple = childTu.get(0);
		for (Tuple tuple : childTu) {
			boolean repeat = false;
			if (tuple != firstTuple) {
				for (SchemaPair pair : firstTuple.getSchemaList()) {
					int indext1 = firstTuple.getSchemaList().indexOf(pair);
					Long valuet1 = Long.parseLong(firstTuple.getTuple()[indext1]);
					for (SchemaPair p : tuple.getSchemaList()) {
						if (pair.equalsTo(p)) {
							pair = p;
						}
					}
					int indext2 = tuple.getSchemaList().indexOf(pair);
					Long valuet2 = Long.parseLong(tuple.getTuple()[indext2]);
					if (!(valuet1.equals( valuet2))) {//If encounter a distinct value, break and return the tuple
						repeat = false;
						break;
					}else repeat = true;
				}
			}
			if (!repeat) {distinctTuple.add(tuple);}
			firstTuple=tuple;
		}}
	}
	/**
	 * Method to obtain the next tuple 
	 * 
	 * @return (Tuple) the tuple that distinct among all the tuples with respect to
	 *  the required schemas
	 */
	@Override
	public Tuple getNextTuple() throws IOException {
		if ( distinctTuple.size() != count) {
			count++;
			return  distinctTuple.get(count-1);}
		else
			return null;	}

	/**
	 * Method to reset after projection by reset all its fields
	 */
	@Override
	public void reset() {
		child.reset();
		count=0;
	}
	
	/**
	 * Method to dump the results of the DISTINCT
	 */
	@Override
	public void dump() throws IOException {
		FileWriter output = new FileWriter(catalog.getInstance().getOutputdir() + File.separator + "query"+QueryPlan.getCount(), false);
		BufferedWriter br = new BufferedWriter(output);
		Tuple tu;
		while ((tu = this.getNextTuple()) != null) {
			br.write(tu.getComplete());
			br.newLine();
		}
		br.close();
		QueryPlan.nextQuery();
	}
}
