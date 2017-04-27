package project;
import java.io.*;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
<<<<<<< HEAD
=======
import physicalOperator.ScanOperator;
import physicalOperator.SortOperator;
>>>>>>> ella/BPlusTree
import queryPlanBuilder.LogicalPlanBuilder;
import queryPlanBuilder.PhysicalPlanBuilder;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.expression.*;
import java.util.*;

<<<<<<< HEAD
=======
import BPlusTree.BPlusTree;
import BPlusTree.Record;
//import BPlusTree.Utils;
import IO.BinaryReader;
import IO.DirectReader;
import IO.TupleReader;

>>>>>>> ella/BPlusTree
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
<<<<<<< HEAD
	public static boolean debuggingMode = false;

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

=======
	public static boolean debuggingMode = true;

	
>>>>>>> ella/BPlusTree
	/**
	 * main program to parse the query, build the query plan and output the
	 * result
	 * 
<<<<<<< HEAD
	 * @param input
	 *            directory and out put directory
=======
	 * @param input directory and out put directory
>>>>>>> ella/BPlusTree
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		catalog cl = catalog.getInstance();
<<<<<<< HEAD

		cl.setOutputdir(args[1]);
		cl.setTempFileDir(args[2]);
		String inputdir = args[0];
		String schemadr = args[0] + File.separator + "db" + File.separator + "schema.txt";
		String database = args[0] + File.separator + "db" + File.separator + "data";
		String configDir = args[0] + File.separator + "plan_builder_config.txt";
		
		initSchema(schemadr,database,cl);

		// parse the query and output results
		
		CCJSqlParser parser = new CCJSqlParser(new FileReader(inputdir + File.separator + "queries.sql"));
		Statement statement;
		try {
			while ((statement = parser.Statement()) != null) {
				Long t=System.currentTimeMillis();
				System.out.println("============================Read statement=========================================");
				//store alias information and interprets query statement
				queryInterpreter = new QueryInterpreter(statement,cl);
				setSchemaPair();
				LogicalPlanBuilder logicalPlan = new LogicalPlanBuilder(queryInterpreter, cl);
				logicalPlan.buildQueryPlan();
				queryInterpreter.printQueryPlan(logicalPlan.getRootOperator());
				PhysicalPlanBuilder physicalPlan = new PhysicalPlanBuilder(cl,queryInterpreter,configDir);
				logicalPlan.getRootOperator().accept(physicalPlan);
				physicalPlan.printPhysicalPlanTree(physicalPlan.result());
				physicalPlan.result().dump();
				System.out.println("query"+(queryCount-1)+" Evaluation time:"+ (System.currentTimeMillis()-t));
			}
		} 
		catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
=======
		setUpFileDirectory(cl,args[0]);
		initSchema(cl.getSchemaFilePath(),cl.getDatabaseDir(),cl);
		
		if(cl.shouldBuildIndex()) {
			buildIndex(cl);
		}
		
		System.out.println("index info" );
		cl.printIndexInfo();
		// parse the query and output results
		CCJSqlParser parser = new CCJSqlParser(new FileReader(cl.getInputDir() + File.separator + "queries.sql"));
		Statement statement;
		if(cl.shouldEvalQuery()){
			queryCount = 1;
			try {
				while ((statement = parser.Statement()) != null) {
					Long t=System.currentTimeMillis();
					System.out.println("============================Read statement=========================================");
					//store alias information and interprets query statement
					queryInterpreter = new QueryInterpreter(statement,cl);
					setSchemaPair();
					LogicalPlanBuilder logicalPlan = new LogicalPlanBuilder(queryInterpreter, cl);
					logicalPlan.buildQueryPlan();
					queryInterpreter.printQueryPlan(logicalPlan.getRootOperator());
					PhysicalPlanBuilder physicalPlan = new PhysicalPlanBuilder(cl,queryInterpreter,cl.getInputDir());
					logicalPlan.getRootOperator().accept(physicalPlan);
					physicalPlan.printPhysicalPlanTree(physicalPlan.result());
					 
					physicalPlan.result().dump();
					System.out.println("query"+(queryCount-1)+" Evaluation time:"+ (System.currentTimeMillis()-t));
				}
			} 
			catch (Exception e) {
				System.err.println("Exception occurred during parsing");
				e.printStackTrace();
			}
>>>>>>> ella/BPlusTree
		}
	}
	
/*---------------------move logic from the main to helper functions---------------*/	
	
	/**
<<<<<<< HEAD
=======
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
				reader = new BinaryReader(new FileInputStream(cl.getDatabaseDir()+File.separator+bt.getTableName()),new String[]{bt.getTableName()});
			}
			else{reader = new BinaryReader(new FileInputStream(cl.getDatabaseDir()+File.separator+bt.getTableName()),new String[]{bt.getTableName()});}
			
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
		String configDir = args + File.separator + "interpreter_config_file.txt";
		BufferedReader configReader = new BufferedReader(new FileReader(configDir));
		
		cl.setInputDir(configReader.readLine());
		cl.setOutputdir(configReader.readLine());
		cl.setTempFileDir(configReader.readLine());
		cl.setBuildIndex(configReader.readLine().equals("1")? true : false);
		cl.setEvalQuery(configReader.readLine().equals("1")? true : false);
		
		configReader.close();
	}

	/**
>>>>>>> ella/BPlusTree
	 * This method initializes database schema and stores it in a catalog class
	 * @param schemadr directory for schema file
	 * @param database the database
	 * @param cl the catalog contain tables' information and tables' alias names
<<<<<<< HEAD
	 * @throws IOException exception
	 */
	public static void initSchema(String schemadr,String database, catalog cl) throws IOException{
=======
	 * @throws Exception exception
	 */
	public static void initSchema(String schemadr,String database, catalog cl) throws Exception{
>>>>>>> ella/BPlusTree
		// store database information
		BufferedReader schemaReader = new BufferedReader(new FileReader(schemadr));
		String line = schemaReader.readLine();
		while (line != null) {
			String tableName = line.substring(0, line.indexOf(' '));
			ArrayList<String> schema = new ArrayList<String>();
			String[] schemaSt = line.substring(line.indexOf(' ') + 1).split(" ");
			for (String s : schemaSt) {
				schema.add(s);
			}
			cl.storeTableInfo(tableName, database + File.separator + tableName, schema);
			line = schemaReader.readLine();
		}
		schemaReader.close();
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
<<<<<<< HEAD
=======
	
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

>>>>>>> ella/BPlusTree
}	
