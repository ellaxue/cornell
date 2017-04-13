package IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import project.SchemaPair;
import project.Tuple;
import project.catalog;

/**
 * This class reads in human readable input form input path
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class DirectReader implements TupleReader {
	private BufferedReader bufferedReader;
	private String tablename[];
	catalog cl = catalog.getInstance();
	private String filenameID;

	/**
	 * constructor of reader
	 * @param tablename 
	 * 				  table to be read
	 */
	public DirectReader(String tablename) throws IOException {	
		this.tablename = new String[1];
		this.tablename[0]=tablename;
		String fileDirectory;
		if (cl.UseAlias()) {
			fileDirectory = cl.getTableLocation().get(cl.getAlias().get(tablename));
		} else {
			fileDirectory = cl.getTableLocation().get(tablename);
		}
		FileReader toRead = new FileReader(fileDirectory+"_humanreadable");
		bufferedReader = new BufferedReader(toRead);
	}

	public DirectReader(String tableName[], String fileNameID) throws FileNotFoundException{
		filenameID = fileNameID;
		tablename = tableName;
		File file = new File(cl.getTempFileDir()+File.separator+this.toString(tablename)+fileNameID);
		FileReader toRead = new FileReader(file);
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
		for(int i = 0; i < tablename.length; i++){
			String curTableName = tablename[i];
			if (cl.UseAlias()) {
				for (String s : cl.getTableSchema().get(cl.getAlias().get(curTableName))) {
					schema.add(new SchemaPair(curTableName, s));
				}
			} else {
				for (String s : cl.getTableSchema().get(curTableName)) {
					schema.add(new SchemaPair(curTableName, s));
				}
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
			fileDirectory = cl.getTableLocation().get(cl.getAlias().get(this.toString(tablename)));
		} else {
			fileDirectory = cl.getTableLocation().get(this.toString(tablename));
		}

		FileReader toRead = new FileReader(fileDirectory+"_humanreadable");
		bufferedReader = new BufferedReader(toRead);
	}

	public String toString(String name[]){
		String res = "";
		for(int i = 0; i < name.length; i++){
			res+= name[i];
		}
		return res;
	}

	@Override
	public void reset(int index) throws IOException {
		File file=	new File(cl.getTempFileDir()+File.separator+toString(tablename)+filenameID);
		bufferedReader = new BufferedReader(new FileReader(file));
		for(int i=0;i<index;i++) {
			bufferedReader.readLine();
		}
		
	}
}
