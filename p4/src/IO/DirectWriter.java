package IO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import project.QueryPlan;
import project.Tuple;
import project.catalog;

/**
 * This method writes tuple outputs to file with human readable format
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class DirectWriter implements TupleWriter {
	private BufferedWriter br;
	private File file;
	/**
	 * Constructor sets up output path for writer 
	 * @throws IOException
	 */
	public  DirectWriter() throws IOException{
		FileWriter output= new FileWriter(catalog.getInstance().getOutputdir()+File.separator+"query"+QueryPlan.getCount()+"_humanreadable",false);
		br = new BufferedWriter(output);
	}

	public DirectWriter(String fileName) throws IOException{
		file = new File(fileName);
		FileWriter output= new FileWriter(file,false);
		br = new BufferedWriter(output);
	}
	
	
	/**
	 * writes out next tuple
	 */
	@Override
	public void writeNext(Tuple tu) throws IOException {
			br.write(tu.getComplete());  
			br.newLine();	
	}

	
	/**
	 * close the write after done writing outputs
	 */
	@Override
	public void close() throws IOException {
		br.close();
	}

	@Override
	public void writeNext(String str) throws IOException {
		// TODO Auto-generated method stub
		br.write(str+"\n");
//		br.newLine();
	}

	@Override
	public void writeHeader(String line) throws IOException {
		// TODO Auto-generated method stub
		br.write(line+"\n");
	}
}
