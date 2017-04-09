package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class BinaryReader implements TupleReader {
	private ByteBuffer buffer=ByteBuffer.allocate(QueryPlan.pageSize);
	private catalog cl = catalog.getInstance();
	private String tablename[];
	private FileInputStream fin;
	private FileChannel fc;
	private int attribute_num;
	private int tuple_num;
	private String fileDirectory;
	private int count=8;

	public BinaryReader(String tablename) throws IOException {
		this.tablename = new String[1];
		this.tablename[0]=tablename;
		if (cl.UseAlias()) {
			fileDirectory = cl.getTableLocation().get(cl.getAlias().get(tablename));
		} else {
			fileDirectory = cl.getTableLocation().get(tablename);
		}
		fin = new FileInputStream(fileDirectory);
		fc = fin.getChannel();
		fc.read(buffer);
		attribute_num = buffer.getInt(0);
		tuple_num = buffer.getInt(4);

	}
	
	public BinaryReader(String tableName[], String fileName) throws IOException{
		tablename = tableName;
//		System.out.println("table name " + tablename);
		File file = new File(cl.getTempFileDir()+File.separator+toString(tableName)+fileName);
//		System.out.println("read from temp file " + file);
		fin = new FileInputStream(file);
		fc = fin.getChannel();
		fc.read(buffer);
		attribute_num = buffer.getInt(0);
		tuple_num = buffer.getInt(4);
	}
	
	@Override
	public Tuple readNext() throws IOException {
		String [] tuple= new String[attribute_num];

		if(count<tuple_num*attribute_num*4+8) {
			for(int i=0;i<attribute_num;i++) {
				tuple[i]=Integer.toString((buffer.getInt(count)));
				count+=4;
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
			
			return new Tuple(tuple, schema);
		}
		else {
			buffer.clear();
			if(fc.read(buffer)!=-1) {
				attribute_num = buffer.getInt(0);
				tuple_num = buffer.getInt(4);
				count=8;
				return readNext();}
		}
		fc.close();
		fin.close();
		return null;
	}


	@Override
	public void reset() throws IOException {
		count=8;
		fin = new FileInputStream(fileDirectory);
		fc = fin.getChannel();
		buffer=ByteBuffer.allocate(QueryPlan.pageSize);
		fc.read(buffer);
		attribute_num = buffer.getInt(0);
		tuple_num = buffer.getInt(4);
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
		int maxTupleNumber=4088/(attribute_num*4);
		int pageIndex=(int)Math.ceil((double)index/(double)maxTupleNumber);
		count=pageIndex*8+index*attribute_num*4;
		fc.position(count);
		fc.read(buffer);
	}
}
