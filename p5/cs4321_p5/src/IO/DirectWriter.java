package IO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
	 * @throws Exception
	 */
	public  DirectWriter() throws Exception{
		FileWriter output= new FileWriter(catalog.getInstance().getOutputdir()+File.separator+"query"+QueryPlan.getCount()+"_humanreadable",false);
		br = new BufferedWriter(output);
	}

	public DirectWriter(String fileName) throws Exception{
		file = new File(fileName);
		FileWriter output= new FileWriter(file,false);
		br = new BufferedWriter(output);
	}
	
	
	/**
	 * writes out next tuple
	 */
	@Override
	public void writeNext(Tuple tu) throws Exception {
			br.write(tu.getComplete());  
			br.newLine();	
	}

	
	/**
	 * close the write after done writing outputs
	 */
	@Override
	public void close() throws Exception {
		br.close();
	}

	@Override
	public void writeNext(String str) throws Exception {
		// TODO Auto-generated method stub
		br.write(str+"\n");
//		br.newLine();
	}

	@Override
	public void writeHeader(String line) throws Exception {
		// TODO Auto-generated method stub
		br.write(line+"\n");
	}
}
