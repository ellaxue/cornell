package project;
import java.io.*;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import physicalOperator.ScanOperator;
import physicalOperator.SortOperator;
import queryPlanBuilder.LogicalPlanBuilder;
import queryPlanBuilder.PhysicalPlanBuilder;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.expression.*;
import java.util.*;
import ChooseSelectionImp.RelationInfo;
import BPlusTree.BPlusTree;
import BPlusTree.Record;
//import BPlusTree.Utils;
import IO.BinaryReader;
import IO.DirectReader;
import IO.TupleReader;

/**
 * Main program to get the input, build the query plan tree and dump the output.
 * We first parse the query and extract various elements in the select body. We
 * use nested if clause to determine what operators we need and build the query
 * tree. For the join, we use a left deep join tree and always evaluate
 * selection condition first and then join condition.
 * 
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 */
public class QueryPlan {
	private static int queryCount = 1;
	public static final int pageSize = 4096;
	public static ArrayList<SchemaPair> schema_pair_order;
	public static ArrayList<SchemaPair> schema_pair;
	static HashMap<String, Expression> JoinEx;
	static HashMap<String, Expression> SelectEx;
	private static QueryInterpreter queryInterpreter;
	public static boolean debuggingMode = true;

	
	/**
	 * main program to parse the query, build the query plan and output the
	 * result
	 * 
	 * @param input directory and out put directory
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		catalog cl = catalog.getInstance();
		setUpFileDirectory(cl,args[0]);
		initSchema(cl.getSchemaFilePath(),cl.getDatabaseDir(),cl);
		QueryInterpreter.readStat(cl.getStatFilePath());
		buildIndex(cl);
//		else {findIndex(cl);}
		
		System.out.println("<======================================Index info for all relations=============================>" );
		cl.printIndexInfo();
		System.out.println("<==========================================================================================>\n\n\n" );
		// parse the query and output results
		CCJSqlParser parser = new CCJSqlParser(new FileReader(cl.getInputDir() + File.separator + "queries.sql"));
		Statement statement;
		
			queryCount = 1;
			try {
				while ((statement = parser.Statement()) != null) {
					Long t=System.currentTimeMillis();
					System.out.println("============================Read statement=========================================");
					System.out.println(statement+"\n=======================================================================================\n");
//					store alias information and interprets query statement
					queryInterpreter = new QueryInterpreter(statement,cl);
					
					setSchemaPair();
					LogicalPlanBuilder logicalPlan = new LogicalPlanBuilder(queryInterpreter, cl);
					logicalPlan.buildQueryPlan();
					System.out.println("=================================Print logical plan =========================================\n");
					
					queryInterpreter.printQueryPlan(logicalPlan.getRootOperator());
					System.out.println("=======================================================================================\n");
					PhysicalPlanBuilder physicalPlan = new PhysicalPlanBuilder(cl,queryInterpreter,cl.getInputDir(),logicalPlan.getUnionFind());
					logicalPlan.getRootOperator().accept(physicalPlan);
					System.out.println("=================================Print physical plan =========================================\n");
					physicalPlan.printPhysicalPlanTree(physicalPlan.result());
					System.out.println("=======================================================================================\n");
//					physicalPlan.result().dump();
					
					System.out.println("query"+(queryCount-1)+" Evaluation time:"+ (System.currentTimeMillis()-t));
					nextQuery();
				}
			} 
			catch (Exception e) {
				// System.err.println("Exception occurred during parsing");
				e.printStackTrace();
			}
	}
	
/*---------------------move logic from the main to helper functions---------------*/	
	
	/**
	 * This method checks if the index should be clustered or not clustered,
	 * and builds the index tree correspondingly
	 * @param cl catalog contains index building information and directory info
	 * @throws Exception the exception
	 */
	private static void buildIndex(catalog cl) throws Exception {
		BufferedReader indexInfoReader = new BufferedReader(new FileReader(cl.getIndexInforFilePath()));
		String line = indexInfoReader.readLine();
		while(line != null){
			BPlusTree<Integer, Record> bt = new BPlusTree<Integer, Record>(line);
			cl.setIndexInfo(bt.getTableName(), bt.getColumnName(), bt.getIsCluster());
			ArrayList<SchemaPair> list = new ArrayList<SchemaPair>();
			list.add(new SchemaPair(bt.getTableName(),bt.getColumnName()));
			TupleReader reader = null;
			int keyIndex = getColumnIndex(cl,bt);
			if(bt.getIsCluster()){
				SortOperator sortOperator = new SortOperator(new ScanOperator(bt.getTableName()),list);
				sortOperator.dump(cl.getDatabaseDir()+File.separator+bt.getTableName());
			}
			reader = new BinaryReader(new FileInputStream(cl.getDatabaseDir()+File.separator+bt.getTableName()),new String[]{bt.getTableName()});
			Tuple tuple = null;
			int count = 15;
			
			while((tuple = reader.readNext()) != null){
				int key = Integer.parseInt(tuple.getTuple()[keyIndex]);
				bt.addToRecordMap(key, new Record(reader.getCurTotalPageRead(),reader.getCurPageTupleRead()));
//				if(count-- ==0){break;} //debugging
			}
			bt.buildTree(cl.getIndexDir()+File.separator+bt.getTableName()+"."+bt.getColumnName());
			line = indexInfoReader.readLine();
		}
		indexInfoReader.close();
	}
	
	/** 
	 * if don't need to build index, find the index available.
	 * @param cl
	 * @throws Exception
	 */
	private static void findIndex(catalog cl) throws Exception {
		BufferedReader indexInfoReader = new BufferedReader(new FileReader(cl.getIndexInforFilePath()));
		String line = indexInfoReader.readLine();
		while(line != null){
			BPlusTree<Integer, Record> bt = new BPlusTree<Integer, Record>(line);
			cl.setIndexInfo(bt.getTableName(), bt.getColumnName(), bt.getIsCluster());
			line = indexInfoReader.readLine();
		}
		indexInfoReader.close();
	}

	/**
	 * Get the column index that the BPlusTree is based on building upon
	 * @param cl catalog contains index building information and directory info
	 * @param bt the BPlusTree
	 * @return the column index
	 */
	private static int getColumnIndex(catalog cl,BPlusTree<Integer, Record> bt) {
		int index = 0;
		for(String str: cl.getTableSchema().get(bt.getTableName())){
			if(bt.getColumnName().equals(str)){
				return index;
			}
			index++;
		}
		return index;
	}

	/**
	 * store directory info to catalog class
	 * @param cl catalog contains index building information and directory info
	 * @param args argument from command line argument
	 * @throws Exception the exception
	 */
	private static void setUpFileDirectory(catalog cl, String args) throws Exception {
		
		String configDir = args;
		BufferedReader configReader = new BufferedReader(new FileReader(configDir));
		
		cl.setInputDir(configReader.readLine());
		cl.setOutputdir(configReader.readLine());
		cl.setTempFileDir(configReader.readLine());
		configReader.close();
	}

	/**
	 * This method initializes database schema and stores it in a catalog class
	 * @param schemadr directory for schema file
	 * @param database the database
	 * @param cl the catalog contain tables' information and tables' alias names
	 * @throws Exception exception
	 */
	public static void initSchema(String schemadr,String database, catalog cl) throws Exception{
		// store database information
		BufferedReader schemaReader = new BufferedReader(new FileReader(schemadr));
		BufferedWriter writer  = new BufferedWriter(new FileWriter(cl.getStatFilePath(),false));;
		String line = schemaReader.readLine();
		while (line != null) {
			String tableName = line.substring(0, line.indexOf(' '));
			ArrayList<String> schema = new ArrayList<String>();
			String[] schemaSt = line.substring(line.indexOf(' ') + 1).split(" ");
			for (String s : schemaSt) {
				schema.add(s);
			}
			cl.storeTableInfo(tableName, database + File.separator + tableName, schema);
			storeStatistic(writer,tableName);
			line = schemaReader.readLine();
		}
		schemaReader.close();
		writer.close();
	}
	
	private static void storeStatistic(BufferedWriter writer, String tableName) throws Exception {
		BinaryReader statReader = new BinaryReader(tableName,writer);
		Tuple tuple = statReader.readNext();
		while(tuple!= null){
			tuple = statReader.readNext();
		}		
		statReader.close();
	}

	/**
	 * This method stores table column's data with corresponding position of 
	 * columns' names in each table
	 */
	public static void setSchemaPair(){
		// get selectItems from select clause
		//		List<SelectItem> selectItem = ((PlainSelect) select.getSelectBody()).getSelectItems();
		List<SelectItem> selectItemList = queryInterpreter.getSelectItemList();
		schema_pair = new ArrayList<SchemaPair>();
		for (SelectItem s : selectItemList) {
			if (s instanceof SelectExpressionItem) {
				Column c = (Column) ((SelectExpressionItem) s).getExpression();
				String tablename = c.getTable().getName();
				String columnName = c.getColumnName();
				schema_pair.add(new SchemaPair(tablename, columnName));
			}
		}

		// get order by Items from order by
		List<OrderByElement> ex_order = queryInterpreter.getOrderByElements();
		schema_pair_order = new ArrayList<SchemaPair>();
		if (ex_order != null && ex_order.size() > 0) {
			for (OrderByElement o : ex_order) {
				if (o instanceof OrderByElement) {
					Column col = (Column) ((OrderByElement) o).getExpression();
					String table_name = col.getTable().getName();
					String column_name = col.getColumnName();
					schema_pair_order.add(new SchemaPair(table_name, column_name));
				}
			}
		}
	}
	
	/**
	 * count the query completed
	 */
	public static void nextQuery() {
		queryCount++;
	}

	/**
	 * get the query number being dealt with
	 * 
	 * @return query number
	 */
	public static int getCount() {
		return queryCount;
	}

}	
