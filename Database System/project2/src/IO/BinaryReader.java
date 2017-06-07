package IO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

import ChooseSelectionImp.RelationInfo;
import project.QueryPlan;
import project.SchemaPair;
import project.Tuple;
import project.catalog;

/**
 * This class reads in binary files
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class BinaryReader implements TupleReader {
	private ByteBuffer buffer=ByteBuffer.allocate(QueryPlan.pageSize);
	private catalog cl = catalog.getInstance();
	private String tablename[];
	private FileInputStream fin;
	private FileChannel fc;
	private int attribute_num;
	private int tuple_num;
	private int count=8;
	private String fileDirectory;
	private String filename;
	private int totalCount;
	private int maxTupleNumber;
	private int pageIndex=1;
	private int curTotalPageRead = 0;
	private int curPageTupleRead = -1;
	boolean firstReadNext = true;
	int times = 0;
	private int tupleNumInRelation;
	private int attributeMin[];
	private int attributeMax[];
	private String attributeNames[];
	private boolean readStat = false;
	private int attrNameIndex = 0;
	private BufferedWriter stateWriter;
	public BinaryReader(String tablename) throws Exception {
		this.tablename = new String[1];
		this.tablename[0]=tablename;
		if (cl.UseAlias()) {
			fileDirectory = cl.getTableLocation().get(cl.getAlias().get(tablename));
		} else {
			fileDirectory = cl.getTableLocation().get(tablename);
		}
//		 System.out.println("read path" + fileDirectory);
		fin = new FileInputStream(fileDirectory);
		fc = fin.getChannel();
		fc.read(buffer);
		attribute_num = buffer.getInt(0);
		tuple_num = buffer.getInt(4);
	}
	/**
	 * Reader for writing stat of each relation
	 * @param tablename
	 * @param writer
	 * @throws Exception
	 */
	public BinaryReader(String tablename, BufferedWriter writer) throws Exception{
		stateWriter = writer;
		this.tablename = new String[1];
		this.tablename[0]=tablename;
		if (cl.UseAlias()) {
			fileDirectory = cl.getTableLocation().get(cl.getAlias().get(tablename));
		} else {
			fileDirectory = cl.getTableLocation().get(tablename);
		}
//		 System.out.println("read path" + fileDirectory);
		fin = new FileInputStream(fileDirectory);
		fc = fin.getChannel();
		fc.read(buffer);
		attribute_num = buffer.getInt(0);
		tuple_num = buffer.getInt(4);
		tupleNumInRelation = 0;
		attributeMin = new int[attribute_num];
		Arrays.fill(attributeMin,Integer.MAX_VALUE);
		attributeMax = new int[attribute_num];
		Arrays.fill(attributeMax,Integer.MIN_VALUE);
		attributeNames = new String[attribute_num];
		readStat = true;
	}
	
	public BinaryReader(String tableName[], String fileName) throws Exception{
		tablename = tableName;
		this.filename=fileName;
		File file = new File(cl.getTempFileDir()+File.separator+toString(tableName)+fileName);
		//System.out.println("read from temp file " + file);
		fin = new FileInputStream(file);
		fc = fin.getChannel();
		fc.read(buffer);
		attribute_num = buffer.getInt(0);
		tuple_num = buffer.getInt(4);
	}
	
	public BinaryReader(FileInputStream stream, String tableName[]) throws Exception{
		tablename = tableName;
		fin = stream;
		fc = fin.getChannel();
		fc.read(buffer);
		attribute_num = buffer.getInt(0);
		tuple_num = buffer.getInt(4);
		readStat = false; //not read relation for recording stat.txt
	}

	@Override
	public Tuple readNext() throws Exception {
		String [] tuple= new String[attribute_num];
		int actualcount=totalCount-(pageIndex-1)*4096+count;
		if(count<tuple_num*attribute_num*4+8 && count+attribute_num*4<=buffer.limit()) {
			for(int i=0;i<attribute_num;i++) {
				tuple[i]=Integer.toString((buffer.getInt(count)));
				count+=4;
			}
			if(readStat == true)findMinAndMaxForAttribute(tuple);
			ArrayList<SchemaPair> schema = new ArrayList<SchemaPair>();
			for(int i = 0; i < tablename.length; i++){
				String curTableName = tablename[i];
				if (cl.UseAlias()) {
					for (String s : cl.getTableSchema().get(cl.getAlias().get(curTableName))) {
						if(readStat && attrNameIndex++ < attribute_num) attributeNames[attrNameIndex-1] = s;
						schema.add(new SchemaPair(curTableName, s));
					}
					  
				} else {
					for (String s : cl.getTableSchema().get(curTableName)) {
						if(readStat && attrNameIndex++ < attribute_num) attributeNames[attrNameIndex-1] = s;
						schema.add(new SchemaPair(curTableName, s));
					}
				}
			}
			curPageTupleRead++; //count how many tuples read in current page
			tupleNumInRelation++;
			return new Tuple(tuple, schema);
		}
		else {
			curTotalPageRead++; //count how many pages read
			curPageTupleRead = -1; //reset the tuple number read for next page
			buffer.clear();
			if(fc.read(buffer)!=-1) {
				attribute_num = buffer.getInt(0);
				tuple_num = buffer.getInt(4);
				count=8;
				return readNext();
			}
		}
		fc.close();
		fin.close();
		if(readStat == true){
			writeStatToFile();
//			RelationInfo Rinfo = new RelationInfo(attributeMin, attributeMax, tupleNumInRelation, tablename[0],attributeNames);
//			cl.setRelationInfo(Rinfo);
		}
		return null;
	}
	
	private void writeStatToFile() throws Exception {
		// TODO Auto-generated method stub
		stateWriter.write(tablename[0] +" "+tupleNumInRelation+ " ");		
		for(int i = 0; i < attributeNames.length;i++){
			stateWriter.write(attributeNames[i]+","+attributeMin[i]+","+attributeMax[i]+" ");
		}
		stateWriter.write("\n");
	}
	private void findMinAndMaxForAttribute(String[] tuple) {
		for(int i = 0; i < tuple.length; i++){
			if(Integer.parseInt(tuple[i]) > attributeMax[i]){
				attributeMax[i] = Integer.parseInt(tuple[i]);
			}
			if(Integer.parseInt(tuple[i]) < attributeMin[i]){
				attributeMin[i] = Integer.parseInt(tuple[i]);
			}
		}
	}

	@Override
	public Tuple readNext(int pageID, int tupleID) throws Exception {
		buffer.clear();
		fc.position((long)4096*pageID);
		fc.read(buffer);
		buffer.flip();
			count = 8+tupleID*4*attribute_num;

		String [] tuple= new String[attribute_num];
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
	
	/**
	 * Read next tuple from a certain location
	 *
	 */
	@Override
	public Tuple readNext(int pageID, int tupleID, boolean unclustered) throws Exception {
		boolean enter = unclustered||firstReadNext;
		if(enter){
			times++;
			//count = 8+pageID*4096+tupleID*4*attribute_num;
			buffer.clear();
			fc.position((long)4096*pageID);
			fc.read(buffer);
			buffer.flip();
			firstReadNext = false;
			count = 8+tupleID*4*attribute_num;
		}
		String [] tuple= new String[attribute_num];
		if(count<tuple_num*attribute_num*4+8 && count+attribute_num*4<=buffer.limit()) {
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
			curPageTupleRead++; //count how many tuples read in current page
			return new Tuple(tuple, schema);
		}
		else {
			curTotalPageRead++; //count how many pages read
			curPageTupleRead = -1; //reset the tuple number read for next page
			buffer.clear();
			if(fc.read(buffer)!=-1) {
				attribute_num = buffer.getInt(0);
				tuple_num = buffer.getInt(4);
				count=8;
				return readNext();
			}
		}
		fc.close();
		fin.close();
		return null;
	}

	/**
	 * reset the reader buffer
	 */
	@Override
	public void reset() throws Exception {
		count=8;
//		System.out.println("file to be open" + fileDirectory);
		fin = new FileInputStream(fileDirectory);
		fc = fin.getChannel();
		buffer=ByteBuffer.allocate(QueryPlan.pageSize);
		fc.read(buffer);
		attribute_num = buffer.getInt(0);
		tuple_num = buffer.getInt(4);
		firstReadNext = true;
	}
	public String toString(String name[]){
		String res = "";
		for(int i = 0; i < name.length; i++){
			res+= name[i];
		}
		return res;
	}

	/**
	 * reset the reader buffer
	 */
	@Override
	public void reset(int index) throws Exception {
		maxTupleNumber = 4088/(attribute_num*4);
		pageIndex = (int)Math.ceil((double)index/(double)maxTupleNumber);
		totalCount = (index/maxTupleNumber)*4096+(index%maxTupleNumber)*attribute_num*4;
		if(index%maxTupleNumber!=0) totalCount+=8;
		if(fc.isOpen()) {
		if ((fc.position()/4096+1)>pageIndex) tuple_num=maxTupleNumber;
		fc.position(totalCount);
		buffer.clear();
		buffer.limit(pageIndex*4096-totalCount);
		fc.read(buffer);
		count=0;
		}
		else {
			File file=	new File(cl.getTempFileDir()+File.separator+toString(tablename)+filename);;
			fin = new FileInputStream(file);
			fc = fin.getChannel();
			if ((fc.size()/4096+1)>pageIndex) tuple_num=maxTupleNumber;
			fc.position(totalCount);
			buffer.clear();
			buffer.limit(pageIndex*4096-totalCount);
			fc.read(buffer);
			count=0;
		}
	}
	
	@Override
	public void close() throws Exception{
		this.fc.close();
//		System.out.println("close");
		this.fin.close();
	}
	/**
	 * return how many pages read so far
	 */
	@Override
	public int getCurTotalPageRead(){
		return this.curTotalPageRead;
	}
	/**
	 * return how many tuple read in current page
	 */
	@Override
	public int getCurPageTupleRead(){
		return this.curPageTupleRead;
	}
}
