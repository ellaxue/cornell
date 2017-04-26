package IO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import project.QueryPlan;
import project.Tuple;
import project.catalog;

public class BinaryWriter implements TupleWriter{
	FileOutputStream fout;
	FileChannel fc;
	private catalog cl = catalog.getInstance();
	ByteBuffer buffer = ByteBuffer.allocate(4096);
	private int attribute_num = 0;
	private int firstCall = 0;
	private int tuple_num = 0;
	private int limit;
	private int count=0;
	
	public  BinaryWriter() throws IOException{
		firstCall = 0;
		tuple_num = 0;
		fout = new FileOutputStream(cl.getOutputdir()+File.separator+"query"+QueryPlan.getCount(), false);
		fc = fout.getChannel();
	}
	
	public  BinaryWriter(String fileName) throws IOException{
		firstCall = 0;
		tuple_num = 0;
		fout = new FileOutputStream(fileName, false);
		fc = fout.getChannel();
	}
	
	@Override
	public void writeNext(Tuple tu) throws IOException {
		count++;
		if(firstCall == 0){
			attribute_num = tu.getTuple().length;
			if(attribute_num==0) return;
			firstCall = 1;
			limit = 4088/(4*attribute_num);
			buffer.putInt(attribute_num);
			
			buffer.putInt(0);
		}
		if(tuple_num<limit){
			for(String s: tu.getTuple()){
				int n = Integer.parseInt(s);
				buffer.putInt(n);
			}	
			tuple_num++;
		}else{
			fillZero(buffer);
			buffer.putInt(4, tuple_num);
			//buffer.flip();
			buffer.flip();
			fc.write(buffer);
				
			//reset for new buffer page	
			count--;
			tuple_num = 0;
			firstCall = 0;
			buffer.clear();
			fillZero(buffer);
			buffer.clear();
			writeNext(tu);
			
		}	
	}

	@Override
	public void close() throws IOException {
		if(attribute_num==0){
			fc.close();
			fout.close();
		}
		else{
			fillZero(buffer);
			//buffer.putInt(0, 3);
			buffer.putInt(4, tuple_num);
			buffer.flip();				
			fc.write(buffer);
			fc.close();
			fout.close();
		}
	}
	
	private void fillZero(ByteBuffer b){
		while(b.hasRemaining()){
			b.putInt(0);
		}
	}

	@Override
	public void writeNext(String str) throws IOException {
//		System.out.println("page " + str);
		String values[] = str.split(" ");
		buffer.clear();
		for(int i = 0; i < values.length; i++){
			buffer.putInt(Integer.parseInt(values[i]));
		}
		fillZero(buffer);
		buffer.flip();
		fc.write(buffer);
		
	}

	@Override
	public void writeHeader(String page) throws IOException {
		System.out.println("header " + page);
		String values[] = page.split(" ");
		buffer.clear();
		int offset = 0;
		fc.position(offset);//set channel's write position to be the beginning of the file
		for(int i = 0; i < values.length; i++){
			buffer.putInt(Integer.parseInt(values[i]));
		}
		fillZero(buffer);
		buffer.flip();
		fc.write(buffer);
		
	}

}
