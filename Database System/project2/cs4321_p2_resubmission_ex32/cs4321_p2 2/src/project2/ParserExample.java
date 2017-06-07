package project2;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.schema.*;

/**
 * Example class for getting started with JSQLParser. Reads SQL statements from
 * a file and prints them to screen; then extracts SelectBody from each query
 * and also prints it to screen.
 * 
 * @author Lucja Kot
 * @Modified Ella Xue (ex32)
 */
public class ParserExample {

	private static String queriesFile = "queries.sql";
	public static String inputDir;
	public static int fileCounter = 1;
	public static String outputDir;
	public static HashMap<String, Column[]> schema = new HashMap<String, Column[]>();
	public static void main(String[] args) {
		if(args.length < 2){
			System.out.println("Usage: inputDir outputDir");
			System.exit(0);
		}

		inputDir = args[0];
		outputDir = args[1];
		queriesFile = inputDir+"/queries.sql";
		//Continuously read in SQL query
		try {
			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
			Statement statement;
			while ((statement = parser.Statement()) != null) {
					//interpreter the SQL query first
					QueryInterpreter queryInter = new QueryInterpreter(statement);
					//use the interpreted query statement to construct a query plan tree
					QueryPlanBuilder queryPlan = new QueryPlanBuilder(queryInter);
					initSchema(queryInter); //read in schema of all tables
					queryInter.setQueryPlan(queryPlan.getQueryPlan());
//					queryInter.printQueryPlan(queryPlan.getRoot());
					queryInter.executeQuery(queryPlan.getRoot());
					schema.clear();
					
			println("----------------------Next Statement-------------------------");
			}

		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
	
	/**
	 * initialize the database schema from the text file
	 * Saved in hashMAP <tablename, tablecolums>
	 * @param queryInter
	 */
	public static void initSchema(QueryInterpreter queryInter){
		//Check if alias exists in the table
		HashMap<String, String> baseTableNameToAlias = new HashMap<String, String>();
		String tableAlias = queryInter.getFromItem().getAlias();
		if(tableAlias != null){
			String splitAliasStatement[] = queryInter.getFromItem().toString().split(" ");
			baseTableNameToAlias.put(tableAlias, splitAliasStatement[0]+"");
			List<Join> joinItemList = queryInter.getJoinList();
			if(joinItemList != null){
				for(Join join: joinItemList){
					splitAliasStatement = join.getRightItem().toString().split(" ");
					baseTableNameToAlias.put(join.getRightItem().getAlias(),splitAliasStatement[0]);
				}
			}
		}
		
		BufferedReader input;
		try {
			/*read from the input file and store the schema*/
			input  = new BufferedReader(new FileReader(new File(inputDir+"/db/schema.txt")));
			String line;
			while((line = input.readLine()) != null){
				String items[] = line.split(" ");
				String name = items[0];
				Table table = new Table();
				table.setName(name);
				Column[] tableColumns = new Column[items.length - 1];

				//create table columns with all table column info
				for(int i = 1; i < items.length; i++){
					tableColumns[i-1] = new Column(table, items[i]);
				}

				//table name matches it's table columns
				schema.put(name, tableColumns);
			}
			
			//handle alias table name, store in the schema
			for(Map.Entry<String, String> entry:baseTableNameToAlias.entrySet()){
				String tableName = entry.getValue();
				if(schema.containsKey(tableName)){
					Column[] orginalColumn = schema.get(tableName);
					Column[] columnNameForAliasCol = new Column[orginalColumn.length];
					for(int i = 0 ; i < orginalColumn.length; i++){
						Column newColumn = new Column();
						newColumn.setColumnName(orginalColumn[i].getColumnName());
						Table table = new Table();
						table.setName(entry.getKey());
						newColumn.setTable(table);
						columnNameForAliasCol[i] = newColumn;
					}
					schema.put(entry.getKey(), columnNameForAliasCol);
				}
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
//		printschema(schema);
	}
	
	
	/**
	 * Print the schema
	 * @param map
	 */
	public static void printschema(HashMap<String, Column[]> schema){
		System.out.println("==========database schema==========");
		for(Entry<String, Column[]> entry: schema.entrySet()){
			print(entry.getKey() + " ");
			for(Column col:entry.getValue()){
				print(col.getColumnName()  + " "+ col.getTable().getName() + " ");
			}
			println("");	
		}
		System.out.println("==========database schema==========");
		
	}
	
	public static void println(String sentence){
		System.out.println(sentence);
	}
	public static void print(String sentence){
		System.out.print(sentence);
	}
	
}

