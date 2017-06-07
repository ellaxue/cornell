package operator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectItem;
import project2.ParserExample;
import project2.QueryInterpreter;
import project2.Tuple;
import project2.QueryPlanBuilder.Node;

/**
 * This class project only specified columns on the current tuple
 * Created on: 03/13/2017
 * @author Ella Xue (ex32)
 *
 */
public class ProjectionOperator extends Operator{
	ArrayList<SelectItem> selectItemList;
	HashMap<String, Column[]> schema = ParserExample.schema;
	String selectItemColumnNames[];
	String tableNameOfProjectTable;
	String selectItemTableNames[];
	public ProjectionOperator(List<SelectItem> itemList){
		selectItemList = (ArrayList<SelectItem>) itemList;
		selectItemColumnNames = new String[itemList.size()];
		selectItemTableNames = new String[itemList.size()];
		for(int i = 0; i < itemList.size(); i++){
			String extractColumnName[] = (itemList.get(i).toString()).split("\\.");
			selectItemTableNames[i] = extractColumnName[0];
			selectItemColumnNames[i] = extractColumnName[1];
		}
	}
	
	public Tuple getNextTuple(Node<Operator> node,QueryInterpreter interpreter) throws 
	FileNotFoundException, IOException{
		Tuple tuple = node.getLeftChild().getOperator().getNextTuple(node.getLeftChild(), interpreter);
		if (tuple != null){
			tableNameOfProjectTable = tuple.getTableName();
			Column[] columnNames = schema.get(tableNameOfProjectTable);
			ArrayList<String> newTupleElement = new ArrayList<String>();
			
			//find the selected item name and add to the new tuple and return to parent
			for(int j = 0; j < selectItemColumnNames.length; j++){
				for(int i = 0; i < columnNames.length; i++){
					if(selectItemColumnNames[j].equals(columnNames[i].getColumnName()) 
							&& selectItemTableNames[j].equals(columnNames[i].getTable().getName())){
						newTupleElement.add(tuple.getData()[i]);
					}
				}
			}
			String data[] = new String[newTupleElement.size()];
			for(int i = 0; i < newTupleElement.size(); i++){
				data[i] = newTupleElement.get(i);
			}
			Tuple newTuple = new Tuple(data);
			newTuple.setTable(tableNameOfProjectTable);
			return newTuple;
		}
		updateSchema(tableNameOfProjectTable);
		return null;
	}

	/** Update table schema after projection
	 * @param tableName new table name
	 */
	private void updateSchema(String tableName) {
		Column[] columnNamesAfterProject = new Column[selectItemColumnNames.length];
		for(int i = 0; i < columnNamesAfterProject.length; i++){
			Column column = new Column();
			column.setColumnName(selectItemColumnNames[i]);
			columnNamesAfterProject[i] = column; 
		}
		schema.put(tableName, columnNamesAfterProject);
	}

	/**
	 * Tells the operator to reset its state and start returning its output 
	 * again from the beginning;	
	 * @throws FileNotFoundException 
	 */
	public void reset(Node<Operator> node) throws FileNotFoundException{
		node.getLeftChild().getOperator().reset(node.getLeftChild());
	}

	/**
	 * This method repeatedly calls getNextTuple() until the next tuple 
	 * is null (no more output) and writes each tuple to a suitable PrintStream.
	 * @throws IOException 
	 */
	public void dump(Node<Operator> node, QueryInterpreter interpreter) throws IOException{		
		Tuple tuple = getNextTuple(node,interpreter);
		PrintWriter writer = new PrintWriter(ParserExample.outputDir+"/query"+(ParserExample.fileCounter++));
		while(tuple != null){
			writer.print(tuple.toString()+"\n");
			tuple = getNextTuple(node,interpreter);
		}
		writer.close();
	}
}
