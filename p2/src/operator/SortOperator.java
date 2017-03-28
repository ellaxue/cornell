package operator;
import project.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Sort operator to sort data in ascending order according to 
 * a subset of required columns for ordering. Then sort the rest
 * columns.
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
public class SortOperator extends Operator {
	private Operator child;
	private ArrayList<Tuple> sorted_tuples;
	int count = 0;

	public SortOperator(Operator child, ArrayList<SchemaPair> schema_pair) throws IOException {
		this.child = child;
		this.sorted_tuples = organize(child, schema_pair);
	}
	
	/**
	 * Method to obtain a tuple sorted in ascending order
	 * 
	 * @return (Tuple) the tuple sorted according to the required columns
	 */
	@Override
	public Tuple getNextTuple() throws IOException {
		if (sorted_tuples.size() != count) {
			count++;
			return sorted_tuples.get(count-1);}
		else
			return null;
	}

	/**
	 * Method to reset by reset all its fields
	 */
	@Override
	public void reset() throws IOException {
		child.reset();
		count = 0; 
	}
 
	/**
	 * Method to dump the results
	 */
	@Override
	public void dump() throws IOException {
	    Tuple tu;
        TupleWriter writer= new DirectWriter();
    	while ((tu=this.getNextTuple())!=null) {
    		writer.writeNext(tu);
    	}
    	writer.close();
		QueryPlan.nextQuery();
	}

	private ArrayList<Tuple> organize(Operator child, ArrayList<SchemaPair> schema_pair) throws IOException {
		ArrayList<Tuple> tupleList = new ArrayList<Tuple>();
		Tuple tu;
		while ((tu = child.getNextTuple()) != null) {
			tupleList.add(tu);
		}
		Comparator<Tuple> compare = new Comparator<Tuple>() {
			@Override
			// sort required columns in "order by"
			
			
			public int compare(Tuple t1, Tuple t2) {
				if(schema_pair!=null) {
				for (SchemaPair pair : schema_pair) {
					for (SchemaPair p : t1.getSchemaList()) {
						if (pair.equalsTo(p)) {
							pair = p;
						}
					}
					int indext1 = t1.getSchemaList().indexOf(pair);
					Long valuet1 = Long.parseLong(t1.getTuple()[indext1]);
					for (SchemaPair p : t2.getSchemaList()) {
						if (pair.equalsTo(p)) {
							pair = p;
						}
					}
					int indext2 = t2.getSchemaList().indexOf(pair);
					Long valuet2 = Long.parseLong(t2.getTuple()[indext2]);
					if (valuet1 > valuet2) {
						return 1;
					} else if (valuet1 < valuet2) {
						return -1;
					}
					continue;
				}
				// sort other columns not in "order by"
				for (SchemaPair pair : t1.getSchemaList()) {
					if (!schema_pair.contains(pair)) {
						for (SchemaPair p : t1.getSchemaList()) {
							if (pair.equalsTo(p)) {
								pair = p;
							}
						}
						int indext1 = t1.getSchemaList().indexOf(pair);
						for (SchemaPair p : t2.getSchemaList()) {
							if (pair.equalsTo(p)) {
								pair = p;
							}
						}
						int indext2 = t2.getSchemaList().indexOf(pair);
						Long valuet1 = Long.parseLong(t1.getTuple()[indext1]);
						Long valuet2 = Long.parseLong(t2.getTuple()[indext2]);
						if (valuet1 > valuet2) {
							return 1;
						} else if (valuet1 < valuet2) {
							return -1;
						}
						continue;
					}
				}
				return 0;
			}
				else {
					for (SchemaPair pair : t1.getSchemaList()) {
							int indext1 = t1.getSchemaList().indexOf(pair);
							Long valuet1 = Long.parseLong(t1.getTuple()[indext1]);
							for (SchemaPair p : t2.getSchemaList()) {
								if (pair.equalsTo(p)) {
									pair = p;
								}
							}
							int indext2 = t2.getSchemaList().indexOf(pair);
							Long valuet2 = Long.parseLong(t2.getTuple()[indext2]);
							if (valuet1 > valuet2) {
								return 1;
							} else if (valuet1 < valuet2) {
								return -1;
							}
							continue;
					}
					return 0;
				}
				}
		};
		Collections.sort(tupleList, compare);
		return tupleList;
	}
}
