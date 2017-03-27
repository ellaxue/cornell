package project;

import java.awt.print.Printable;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
			System.out.println(cl.getTableSchema().get(tablename));
			for (String s : cl.getTableSchema().get(tablename)) {
				schema.add(new SchemaPair(tablename, s));
			}
		}
		return new Tuple(line.split(","), schema);
	}

	@Override
	public void reset() throws IOException {
      bufferedReader.reset();
	}

}
