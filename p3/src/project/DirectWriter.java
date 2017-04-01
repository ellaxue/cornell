package project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This method writes tuple outputs to file with human readable format
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class DirectWriter implements TupleWriter {
	private BufferedWriter br;

	/**
	 * Constructor sets up output path for writer 
	 * @throws IOException
	 */
	public  DirectWriter() throws IOException{
		FileWriter output= new FileWriter(catalog.getInstance().getOutputdir()+File.separator+"query"+QueryPlan.getCount()+"_humanreadable",false);
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
}
