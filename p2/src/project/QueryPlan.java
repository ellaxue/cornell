package project;
import java.io.*;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.expression.*;
import java.util.*;

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
	static ArrayList<SchemaPair> schema_pair_order;
	static ArrayList<SchemaPair> schema_pair;
	static HashMap<String, Expression> JoinEx;
	static HashMap<String, Expression> SelectEx;
	private static QueryInterpreter queryInterpreter;

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

	/**
	 * main program to parse the query, build the query plan and output the
	 * result
	 * 
	 * @param input
	 *            directory and out put directory
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		catalog cl = catalog.getInstance();

		cl.setOutputdir(args[1]);
		String inputdir = args[0];
		String schemadr = args[0] + File.separator + "db" + File.separator + "schema.txt";
		String database = args[0] + File.separator + "db" + File.separator + "data";

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
				PhysicalPlanBuilder physicalPlan = new PhysicalPlanBuilder(cl,queryInterpreter);
				logicalPlan.getRootOperator().accept(physicalPlan);
				physicalPlan.result().dump();
				System.out.println("query"+(queryCount-1)+" Evaluation time:"+ (System.currentTimeMillis()-t));
			}
		} 
		catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
	
/*---------------------move logic from the main to helper functions---------------*/	
	
	/**
	 * This method initializes database schema and stores it in a catalog class
	 * @param schemadr directory for schema file
	 * @param database the database
	 * @param cl the catalog contain tables' information and tables' alias names
	 * @throws IOException exception
	 */
	public static void initSchema(String schemadr,String database, catalog cl) throws IOException{
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
}	
