package queryPlanBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ChooseSelectionImp.UnionFind;
import ChooseSelectionImp.WhereProcessForUnionFind;
import logicalOperator.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectItem;
import project.QueryInterpreter;
import project.catalog;
import project.processWHERE;
public class LogicalPlanBuilder {
	private QueryInterpreter queryInterpreter;
	private TreeNode root;
	private HashMap<String, Expression> JoinEx;
	private HashMap<String, Expression> SelectEx;
	private catalog cl;
	public UnionFind unionFindConditions;
	private ArrayList<Expression> residualJoinExpression;
	private HashMap<String, Expression> residualSelectExpression;
	private static ArrayList<String> tableList;
	/**
	 * Constructor
	 * @param queryInter get statement elements from query interpreter
	 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
	 */
	public LogicalPlanBuilder(QueryInterpreter queryInter,catalog cl){
		this.queryInterpreter = queryInter;
		this.root = null;
		this.JoinEx = null;
		this.SelectEx = null;
		this.cl = cl;
		unionFindConditions = new UnionFind();
		residualJoinExpression = new ArrayList<Expression>();
		residualSelectExpression = new HashMap<String, Expression>();
		processUnionFindWhereExp();
		setJoinAndSelectCondition(cl);
	}
	
	/**
	 * @return the root operator of the query plan tree
	 */
	public TreeNode getRootOperator(){return this.root;}
	
	/**
	 * Construct a query plan with tree structure
	 * @return the root of query plan tree
	 */
	public TreeNode buildQueryPlan(){	
		Table firstTable = queryInterpreter.getFirstTable();
		//create first select operator and set up select condition
		TreeNode curOperator = new LogicalSelectOperator(new LogicalScanOperator(firstTable.getName()),unionFindConditions.getUnionFindSelectExpMap(),residualSelectExpression);
		System.out.println("The table ===========> " + queryInterpreter.getFirstTable());
		((LogicalSelectOperator)curOperator).setExpressoin(firstTable);
		
		// add join operators based on the number of join elements in the list
		List<Join> joinList = queryInterpreter.getJoinList();
		if(joinList != null){
			LogicalJoinOperator joinOperator = new LogicalJoinOperator(unionFindConditions,residualJoinExpression);
			joinOperator.addChild((LogicalSelectOperator)curOperator);
			for(Join join: joinList){
				//set up right operator condition
				LogicalSelectOperator selectOperator = new LogicalSelectOperator(new LogicalScanOperator(firstTable.getName()),unionFindConditions.getUnionFindSelectExpMap(),residualSelectExpression);
//				selectOperator.setTable((Table)join.getRightItem());
				selectOperator.setExpressoin((Table)join.getRightItem());
				System.out.println("add children==================********");
				joinOperator.addChild((LogicalSelectOperator)selectOperator);
				joinOperator.setTable((Table)join.getRightItem());
//				if(JoinEx != null && JoinEx.containsKey(tableName)){
//					joinOperator.addExpression(JoinEx.get(tableName));
//				}
			}
			ChooseJoinOrder c=new ChooseJoinOrder(joinOperator, this,queryInterpreter.getWhereCondition());
			//update current top level operator as the curOperator
			curOperator = joinOperator;
		}
		
		//Add projection operator if select item is not '*' (select all)
		List<SelectItem> itemList = queryInterpreter.getSelectItemList();
		if(!(itemList.get(0) instanceof AllColumns)){
			TreeNode projectOperator = new LogicalProjectOperator(curOperator, itemList);
			curOperator = projectOperator;
		}

		//add sort operator if orderElementList has element inside
		List<OrderByElement> orderElementList = queryInterpreter.getOrderByElements();
		if(orderElementList != null){
			TreeNode sortOperator = new LogicalSortOperator(curOperator, orderElementList);
			curOperator = sortOperator;
		}
		
		//create distinct operator in the query plan tree 
		if(queryInterpreter.getDistinct() != null){
			TreeNode distinctOperator;
			// if the curOperator is not an instance of SortOperator, need to create one for distinct
			if(!(curOperator instanceof LogicalSortOperator)){
				distinctOperator = new LogicalDulplicateEliminationOperator(new LogicalSortOperator(curOperator,orderElementList));
			}
			else{ //otherwise, set the curOperator as distinctOperator's child
				
				distinctOperator = new LogicalDulplicateEliminationOperator(curOperator);
			}
			curOperator = distinctOperator;
		}
		
		return root = curOperator;
	}
	public void processUnionFindWhereExp(){
		Expression ex = queryInterpreter.getWhereCondition();
		WhereProcessForUnionFind process = new WhereProcessForUnionFind(ex);
		unionFindConditions = process.getUnionFindResult();
		unionFindConditions.setUnionFindExpressionMap();
		residualJoinExpression = process.getResidualJoinExpression();
		residualSelectExpression = process.getResidualSelectExpression();
	}
	/**
	 * print out the stored join condition and select condition for debugging purpose
	 * @param expressions stored expression based on table name
	 */
	private void printCondition(HashMap<String, Expression> expressions) {
		// TODO Auto-generated method stub
		for(Map.Entry<String, Expression> entry: expressions.entrySet()){
			System.out.println("table " + entry.getKey() + " with expression " + entry.getValue());
		}
	}

	public UnionFind getUnionFind(){
		return this.unionFindConditions;
	}

	/**
	 * This method evaluates each table's select condition and join condition if exists.
	 * @param cl the catalog contain table information and table's alias
	 */
	public void setJoinAndSelectCondition(catalog cl){
		Expression ex = queryInterpreter.getWhereCondition();
		Table firstTable = queryInterpreter.getFirstTable();
		List<Join> joinList = queryInterpreter.getJoinList();		
		tableList = new ArrayList<String>();
		if (!cl.UseAlias()) {tableList.add(firstTable.getName());} 
		else {tableList.add(firstTable.getAlias());}
		if (joinList != null) {
			for (Join j : joinList) {
				String name = ((Table) j.getRightItem()).getName();
				if (cl.UseAlias()) {
					name = ((Table) j.getRightItem()).getAlias();
				}
				tableList.add(name);
			}
			
		}
		processWHERE pw = new processWHERE(ex, tableList);
		JoinEx = pw.getJoinEx();
		SelectEx = pw.getSelectEx();
		if (JoinEx.isEmpty()) {JoinEx = null;}
		if (SelectEx.isEmpty()) {SelectEx = null;}
	}
	
	public static ArrayList<String> getJoinOrder() {
		return tableList;
	}
}
