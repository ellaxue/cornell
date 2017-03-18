import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.sf.jsqlparser.expression.Expression;

/**
 * Join operator to execute the join operation
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */

public class JoinOperator extends Operator {
	private ArrayList<Tuple> leftset;
	private Operator leftChild;
	private Operator rightChild;
	private Expression ex;
	private int count;

	public JoinOperator(Operator leftChild, Operator rightChild, Expression ex) throws IOException {
		Tuple left;
		this.leftset = new ArrayList<>();
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		while ((left = leftChild.getNextTuple()) != null) {
			this.leftset.add(left);
		}// get and store tuple from the outer
		
		
		this.ex = ex;
		this.count = 0;
	}

	/**
	 * Method to obtain a tuple from the outer and a tuple from the inner and glue them
	   together
	 * 
	 * @return (Tuple) the tuple matches the join condition(If the join is a cross product, all pairs of
	   tuples are returned)
	 */
	@Override
	public Tuple getNextTuple() throws IOException {
		Tuple left;
		String[] lt;
		Tuple right;
		String[] rt;
		while (count<leftset.size()) {//get tuple from the outer
			left = leftset.get(count);
			ArrayList<SchemaPair> def_schema = new ArrayList<SchemaPair>(left.getSchemaList());
			lt = left.getTuple();
			StringBuffer sb = new StringBuffer();
			for (String s : lt) {
				sb.append(s).append(",");
			}
			
			while ((right = rightChild.getNextTuple()) != null) {//get tuple from the inner
				StringBuffer sb_next = new StringBuffer(sb);
				ArrayList<SchemaPair> def_schema_next = new ArrayList<SchemaPair>(def_schema);
				for (SchemaPair s : new ArrayList<SchemaPair>(right.getSchemaList())) {
					def_schema_next.add(s);
				}
				rt = right.getTuple();
				for (String s : rt) {
					sb_next.append(s).append(",");
				}
				Tuple tu = new Tuple(sb_next.toString().split(","), def_schema_next);
				if (ex != null) {
					conditionEvaluator eva = new conditionEvaluator(tu, ex);
					if (eva.getResult()) {
						return tu;
					}
				} else
					return tu;
			}
			rightChild.reset();
			count ++;
		}
		return null;
	}

	/**
	 * Method to reset by reset all its fields
	 */
	@Override
	public void reset() {
		leftChild.reset();
		rightChild.reset();
		count = 0;
		leftset = new ArrayList<Tuple>();
	}

	/**
	 * Method to dump the results of the join operator
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
