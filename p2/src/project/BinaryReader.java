package project;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class BinaryReader implements TupleReader {
	private ByteBuffer buffer=ByteBuffer.allocate(4096);
	private catalog cl = catalog.getInstance();
	private String tablename;
	private FileInputStream fin;
	private FileChannel fc;
	private int attribute_num;
	private int tuple_num;
	private String fileDirectory;
	private int count=8;


	public BinaryReader(String tablename) throws IOException {
		this.tablename=tablename;
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


	@Override
	public Tuple readNext() throws IOException {
		String [] tuple= new String[attribute_num];
		if(count<tuple_num*4) {
			for(int i=0;i<attribute_num;i++) {
				tuple[i]=Integer.toString((buffer.getInt(count)));
				count+=4;
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
			return new Tuple(tuple, schema);}
		else {
		buffer.clear();
		if(fc.read(buffer)!=-1) {
			attribute_num = buffer.getInt(0);
			tuple_num = buffer.getInt(1);
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
		fc.read(buffer);
		attribute_num = buffer.getInt(0);
		tuple_num = buffer.getInt(1);
	}

}
