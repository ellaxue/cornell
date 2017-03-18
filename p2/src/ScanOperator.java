import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
/**
 * Scan operator to scan the information of base table
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
public class ScanOperator extends Operator {
	private int count = 0;
	private String tablename;

	public ScanOperator(String tablename) {
		this.tablename = tablename;
	}

	/**
	 * Method to read the next line from the file
	 * 
	 * @return (Tuple)  the next tuple
	 */
	@Override
	public Tuple getNextTuple() throws IOException {
		catalog cl = catalog.getInstance();
		String fileDirectory;
	
		if (cl.UseAlias()) {
			fileDirectory = cl.getTableLocation().get(cl.getAlias().get(tablename));
		} else {
			fileDirectory = cl.getTableLocation().get(tablename);
		}
	
		FileReader toRead = new FileReader(fileDirectory);
		BufferedReader br = new BufferedReader(toRead);
		int n = 0;
		String line = br.readLine();
		while (n != count) {
			line = br.readLine();
			n++;
		}
		br.close();
		count++;
		if (line == null) {
			return null;
		}
		ArrayList<SchemaPair> schema = new ArrayList<SchemaPair>();
		if (cl.UseAlias()) {
			for (String s : cl.getTableSchema().get(cl.getAlias().get(tablename))) {
				schema.add(new SchemaPair(tablename, s));
			}
		} else {
			for (String s : cl.getTableSchema().get(tablename)) {
				schema.add(new SchemaPair(tablename, s));
			}
		}
		return new Tuple(line.split(","), schema);
	}

	/**
	 * Method to reset 
	 */
	@Override
	public void reset() {
		count = 0;
	}

	/**
	 * Method to dump the results 
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
