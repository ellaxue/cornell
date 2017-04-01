package project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class reads in human readable input form input path
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class DirectReader implements TupleReader {
	private BufferedReader bufferedReader;
	private String tablename;
	catalog cl = catalog.getInstance();
	

	/**
	 * constructor of reader
	 * @param tablename 
	 * 				  table to be read
	 */
	public DirectReader(String tablename) throws IOException {	
		this.tablename=tablename;
		String fileDirectory;
		if (cl.UseAlias()) {
			fileDirectory = cl.getTableLocation().get(cl.getAlias().get(tablename));
		} else {
			fileDirectory = cl.getTableLocation().get(tablename);
		}
		FileReader toRead = new FileReader(fileDirectory);
		bufferedReader = new BufferedReader(toRead);
	}

	/**
	 * This method reads next line of the input file
	 */
	@Override
	public Tuple readNext() throws IOException {
		String line = bufferedReader.readLine();
		if (line == null) {
			bufferedReader.close();
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
	 * reset input reader
	 */
	@Override
	public void reset() throws IOException {
		String fileDirectory;
		if (cl.UseAlias()) {
			fileDirectory = cl.getTableLocation().get(cl.getAlias().get(tablename));
		} else {
			fileDirectory = cl.getTableLocation().get(tablename);
		}
		FileReader toRead = new FileReader(fileDirectory);
		bufferedReader = new BufferedReader(toRead);
	}

}
