package queryPlanBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import BPlusTree.IndexInfo;
import logicalOperator.*;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import physicalOperator.*;
import project.IndexScanConditionExtration;
import project.JoinAttributesExtraction;
import project.OperationVisitor;
import project.QueryInterpreter;
import project.QueryPlan;
import project.catalog;
import project.conditionEvaluator;

/**
 * This class recursively builds a physical query plan with a tree structure
 * based on the passed in logical query plan tree
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class PhysicalPlanBuilder implements OperationVisitor{
	private Operator rootOperator = null;
	private Operator curOperator = null;
	private catalog cl;
	private int joinPageSize;
	private static int sortPageSize;
	QueryInterpreter queryInterpreter;
	private String configDir;
	private static int sortMethod;
	private int joinMethod;
	private Boolean useIndex=false;
	/**
	 * Constructor
	 * @param cl the catalog store table information and tables' alias 
	 * @param queryInterpreter query interpreter
	 * @t hrows Exception 
	 */
	public PhysicalPlanBuilder(catalog cl,QueryInterpreter queryInterpreter, String inputDir) throws Exception
	{
		this.cl = cl;
		this.queryInterpreter = queryInterpreter;
		this.configDir = inputDir+File.separator+"plan_builder_config.txt";
		setOperatorMethod();
	}

	/**
	 * @return the root of the physical query plan tree
	 */
	public Operator result(){
		return this.rootOperator;
	}

	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	public void visit(LogicalSelectOperator node) throws Exception {
		Operator selectOperator=null;
		String tableName = getTableName(node);
		IndexInfo index= cl.getIndexes().get(node.getTable().getName());
		String indexFileName= cl.getIndexDir()+File.separator+node.getTable().getName()+"."+index.getIndexCol();
		if(!useIndex || index==null) {selectOperator = new SelectOperator(new ScanOperator(tableName),node.getExpressoin());}
		else {
			IndexScanConditionExtration condition= new IndexScanConditionExtration(node.getExpressoin(), index);
			if(condition.getLowKey()==null && condition.getHighKey() == null){
				selectOperator= new SelectOperator(new ScanOperator(tableName), condition.getFullScan());}	
			else if (condition.getFullScan()==null) {
				selectOperator= new IndexScanOperator(tableName,condition.getLowKey(), condition.getHighKey(), index.getClustered(), indexFileName);
			}
			else {
				selectOperator= new IndexScanOperator(tableName,condition.getLowKey(), condition.getHighKey(), index.getClustered(), indexFileName);
				selectOperator=new SelectOperator(selectOperator, condition.getFullScan());
			}
		}
		
		if(rootOperator == null){
			rootOperator = selectOperator;
		}
		else if(curOperator instanceof JoinOperator || curOperator instanceof SMJoinOperator 
				|| curOperator instanceof BNLJOperator){
			if((curOperator).getLeftChild() == null){
				(curOperator).setLeftChild(selectOperator);
			}
			else{
				(curOperator).setRightChild(selectOperator);
			}
		}
		else{curOperator.setLeftChild(selectOperator);}
	}

	/**
	 * This method return a table's name in a string format
	 * @param node the logical operator that contain table information
	 * @return the table's name
	 */
	private String getTableName(TreeNode node) {
		if(cl.UseAlias()){return node.getTable().getAlias();}
		return node.getTable().getName();
	}

	public void visit(LogicalScanOperator node){}

	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	@Override
	public void visit(LogicalJoinOperator node) throws Exception {

		Operator joinOperator = null;
		if(joinMethod == 0){
			joinOperator = new JoinOperator(null, null,node.getExpressoin());
			//System.out.println("TNLJ method chosen");
		}
		else if(joinMethod == 1){
			//System.out.println("BNLJ method chosen with join page size " + joinPageSize);
			joinOperator = new BNLJOperator(null, null, node.getExpressoin(), joinPageSize);
		}
		else{
			//System.out.println("SMJoin method chosen with sort page size " + sortPageSize);
			JoinAttributesExtraction jae = new JoinAttributesExtraction
					(node.getExpressoin(),LogicalPlanBuilder.getJoinOrder());
			joinOperator= new SMJoinOperator(null, null, jae.getLeft(), jae.getRight());
		}

		if(rootOperator == null){ rootOperator = joinOperator; }
		else{curOperator.setLeftChild(joinOperator);}

		curOperator = joinOperator;
		if(node.getLeftChild() != null) {node.getLeftChild().accept(this);}
		//reset the current operator to this joinOperator for attaching the right child
		curOperator = joinOperator;
		if(node.getRightChild() != null) {node.getRightChild().accept(this);}
	}

	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	@Override
	public void visit(LogicalProjectOperator node) throws Exception {

		ProjectOperator projectOperator = new ProjectOperator(null,QueryPlan.schema_pair);
		if(rootOperator == null){
			rootOperator = projectOperator;
		}
		else{
			curOperator.setLeftChild(projectOperator);
		}
		curOperator = projectOperator;
		if(node.getLeftChild() != null ) {node.getLeftChild().accept(this);}
	}

	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	@Override
	public void visit(LogicalSortOperator node) throws Exception {
		// call different constructor depends on if query contains orderBy
		Operator sortOperator;
		if(sortMethod == 0){
			sortOperator = new SortOperator(null,QueryPlan.schema_pair_order);
			//System.out.println("internal sort method chosen");
		}
		else{
			sortOperator = new ExternalSortOperator(null,QueryPlan.schema_pair_order,sortPageSize);
			//System.out.println("external sort method chosen with sort page size " + sortPageSize);
		}

		if(rootOperator == null){rootOperator = sortOperator;}
		else{curOperator.setLeftChild(sortOperator);}

		curOperator = sortOperator;
		if(node.getLeftChild() != null) {node.getLeftChild().accept(this);}
	}

	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	@Override
	public void visit(LogicalDulplicateEliminationOperator node) throws Exception {
		//call different constructor depends on if projection is needed  
		DuplicateEliminationOperator distinctOperator;
		if(queryInterpreter.getSelectItemList().get(0) instanceof AllColumns){
			distinctOperator = new DuplicateEliminationOperator(null);
		}
		else{
			distinctOperator = new DuplicateEliminationOperator(null,QueryPlan.schema_pair);
		}

		if(rootOperator == null){rootOperator = distinctOperator;}
		else{curOperator.setLeftChild(distinctOperator);}

		curOperator = distinctOperator;
		if(node.getLeftChild() != null) {node.getLeftChild().accept(this);}
	}

	/**
	 * prints out the built physical plan tree for debugging purpose in postfix order
	 * @param op the root operator
	 */
	public void printPhysicalPlanTree(Operator op){
		if (op == null) return;
		printPhysicalPlanTree(op.getLeftChild());
		printPhysicalPlanTree(op.getRightChild());
		//System.out.println("physical operator " + op.getClass());
	}

	/**
	 * Read the config file and set join method and sorting method
	 * @throws Exception
	 */
	private void setOperatorMethod() throws Exception {
		BufferedReader configReader = new BufferedReader(new FileReader(configDir));
		String line = configReader.readLine();
		if(line != null){
			String splitLine[] = line.split(" ");
			joinMethod = Integer.parseInt(splitLine[0]);
			if(joinMethod == 1){
				joinPageSize = Integer.parseInt(splitLine[1]);
			}
			if((line = configReader.readLine()) != null){
				splitLine = line.split(" ");
				sortMethod = Integer.parseInt(splitLine[0]);
				if(sortMethod != 0){
					sortPageSize = Integer.parseInt(splitLine[1]);
				}
			}
			if((line = configReader.readLine()) != null){
				splitLine = line.split(" ");
				Integer IndexMethod = Integer.parseInt(splitLine[0]);
				if(IndexMethod != 0){
					useIndex = true;
				}
			}
		}
		configReader.close();
	}

	public static int getSortMethod() {
		return sortMethod;
	}

	public static int getSortPageNumber() {
		return sortPageSize;
	}
}
