package project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DirectWriter implements TupleWriter {
	private BufferedWriter br;


	public  DirectWriter() throws IOException{
		FileWriter output= new FileWriter(catalog.getInstance().getOutputdir()+File.separator+"query"+QueryPlan.getCount(),false);
		br = new BufferedWriter(output);
	}

	@Override
	public void writeNext(Tuple tu) throws IOException {
			br.write(tu.getComplete());  
			br.newLine();	
	}

	@Override
	public void close() throws IOException {
		br.close();
	}


}
