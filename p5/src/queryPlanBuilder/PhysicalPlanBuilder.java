package queryPlanBuilder;

import java.awt.SystemTray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import BPlusTree.IndexInfo;
import ChooseSelectionImp.Element;
import ChooseSelectionImp.ExtractColumnFromExpression;
import ChooseSelectionImp.RelationInfo;
import ChooseSelectionImp.SELECT_METHOD;
import IO.BinaryReader;
import logicalOperator.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
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
	public static int level = 0; 
	private IndexInfo index;
	
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
		joinPageSize = 4;
		sortPageSize = 4;
	}

	/**
	 * @return the root of the physical query plan tree
	 */
	public Operator result(){
		System.out.println("root ===>"+ rootOperator.getClass());
		return this.rootOperator;
	}

	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	public void visit(LogicalSelectOperator node) throws Exception {
		Operator selectOperator=null;
		String tableOriginalName = node.getTable().getName();
		String tableName = getTableName(node);
		Expression exp = node.getExpressoin();
//		System.out.println("exp ====>" + exp + " table name " + tableName);
		SELECT_METHOD selectionMethod = computeSelectCost(tableOriginalName, exp);
		
//		String indexFileName= index != null? cl.getIndexDir()+File.separator+node.getTable().getName()+"."+index.getIndexCol() : null;

		switch(selectionMethod){
			case FULL_SCAN:
				selectOperator = new SelectOperator(new ScanOperator(tableName),exp,tableOriginalName);
				System.out.println("full scan opterator chosen with table name " + tableName + " exp " + exp );
				break;
			case INDEX_SCAN:
				if (exp ==null) {
					selectOperator= new IndexScanOperator(tableName,index);
					System.out.println("index scan opterator chosen with table name " + tableName + " exp " + exp);
				}
				else {
					selectOperator= new IndexScanOperator(tableName,index);
					selectOperator=new SelectOperator(selectOperator, exp,tableOriginalName);
					System.out.println("index scan and select opterator chosen with table name " + tableName + " exp " + exp);
				}
			default:break;
		}
		
//		if(!useIndex || index==null) {
//			selectOperator = new SelectOperator(new ScanOperator(tableName),node.getExpressoin());
//			}
//		else {
//			IndexScanConditionExtration condition= new IndexScanConditionExtration(node.getExpressoin(), index);
//			if(condition.getLowKey()==null && condition.getHighKey() == null){
//				selectOperator= new SelectOperator(new ScanOperator(tableName), condition.getFullScan());}	
//			else if (condition.getFullScan()==null) {
//				selectOperator= new IndexScanOperator(tableName,condition.getLowKey(), condition.getHighKey(), index.getClustered(), indexFileName);
//			}
//			else {
//				selectOperator= new IndexScanOperator(tableName,condition.getLowKey(), condition.getHighKey(), index.getClustered(), indexFileName);
//				selectOperator=new SelectOperator(selectOperator, condition.getFullScan());
//			}
//		}
		
		if(rootOperator == null){
			rootOperator = selectOperator;
		}
		else if(curOperator instanceof JoinOperator || curOperator instanceof SMJoinOperator 
				|| curOperator instanceof BNLJOperator){
				
			curOperator.addChildren(selectOperator);
		}
		else{curOperator.setLeftChild(selectOperator);}
	}

	/**
	 * This method compute witch scanning method is better for a selection.
	 * @param tableName current relation's name
	 * @param exp the select condition for current relation
	 * @return FULL_SCAN OR INDEX_SCAN  
	 * @throws Exception THE EXPCETION
	 */
	private SELECT_METHOD computeSelectCost(String tableName, Expression exp) throws Exception {
		System.out.println("table name : " +tableName );
		//get relation information such as tuple #, # of attribute per tuple
		RelationInfo relation = cl.getRelation(tableName);
		int totalTupleInTheRelation = relation.getTotalTupleInRelation();
		int numOfAttribute = relation.getNumOfAttribute();
		int fullScanCost = totalTupleInTheRelation * numOfAttribute * 4 / QueryPlan.pageSize;
		
		if (exp == null) {return SELECT_METHOD.FULL_SCAN;}
		//get all attributes for current relation's select condition
		ExtractColumnFromExpression ext = new ExtractColumnFromExpression();
		exp.accept(ext);
		HashSet<Column> colSet = ext.getColumnResult();
		
		int indexCost = Integer.MAX_VALUE, pageNumber = fullScanCost;
		double reductionFactor = 0.0;

		if(colSet!=null)
		for(Column col: colSet){
			String colName = col.getColumnName();
			Element element = LogicalPlanBuilder.unionFindConditions.findElement(colName);
			//element != null means it's operator is ==, >= , <= , > or < which are qualified for index scan
			if(element != null){
				Boolean isCluster = false;
				if(cl.hasIndex(colName,tableName)){
					reductionFactor = computeReductionFactor(element, relation,colName);
					int curIndexCost = 0;
					
					//compute indexScan cost if index is clustered on this attribute
					if(cl.isIndexClustered(colName, tableName)){
						curIndexCost = (int)(3 + reductionFactor*pageNumber);
						isCluster = true;
					}
					else{//compute indexScan cost if index is not clustered on this attribute
						FileInputStream inputStream = new FileInputStream(cl.getIndexDir()+File.separator+tableName+"."+colName);
						FileChannel fc =  inputStream.getChannel();
						ByteBuffer buffer = ByteBuffer.allocate(QueryPlan.pageSize);
						fc.read(buffer);
						int leafNum = buffer.getInt(4);
						curIndexCost = (int)(3 + leafNum * reductionFactor + totalTupleInTheRelation * reductionFactor);
						inputStream.close();
						fc.close();
						isCluster = false;
					}
					//update with a lower indexCost and store it's index info 
					if(curIndexCost < indexCost){
						indexCost = curIndexCost;
						// If equality exists in this element, the attribute's lowerBound == upperBound
						Integer equal = element.getEqualityConstraint() == null? null:element.getEqualityConstraint().intValue();
						Integer indexLowBound = equal == null? (element.getLowerBound() == null? null :element.getLowerBound().intValue()):equal;
						Integer indexUpBound = equal == null? (element.getUpperBound() == null? null: element.getUpperBound().intValue()): equal;
						index = new IndexInfo(tableName,colName,isCluster,indexLowBound, indexUpBound);
					}
				}
			}
		}
		return fullScanCost < indexCost ? SELECT_METHOD.FULL_SCAN : SELECT_METHOD.INDEX_SCAN;
	}

	/**
	 * This method compute reduction factor for the selection condition
	 * @param element the element contains all the unionFind attribute
	 * @param relation the current table of the select operator
	 * @param colName the table's column name
	 * @return reduction factor value
	 */
	protected static double computeReductionFactor(Element element, RelationInfo relation, String colName) {
		// If equality exists in this element, the attribute's lowerBound == upperBound
		// System.out.println(element+"element");
		Integer equal = element.getEqualityConstraint() == null? null:element.getEqualityConstraint().intValue();
		Integer indexLowBound = equal == null? element.getLowerBound() == null? null:element.getLowerBound().intValue():equal;
		Integer indexUpBound = equal == null? element.getUpperBound() == null? null:element.getUpperBound().intValue() :equal;
		System.out.println("indexlow"+indexLowBound);
		System.out.println("indexup"+indexUpBound);

		//current relation's min - max value
		int attributeMinVal = relation.getMinValOfAttr(colName);
		int attributeMaxVal = relation.getMaxValOfAttr(colName);
		
		// if either lowerBound or upperBound's value is null, set it to be table's min/max value
		int min = indexLowBound != null? indexLowBound :attributeMinVal;
		int max = indexUpBound != null? indexUpBound :attributeMaxVal;
		return (max - min + 1.0) / (attributeMaxVal - attributeMinVal + 1.0) ;
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
			joinOperator = new JoinOperator(node.GetResidualJoinExpression(), node.GetUnionFindJoinExpression(),node.getExpressoin());
//			((JoinOperator)joinOperator).setResidualJoinExpression(node.GetResidualJoinExpression());
//			((JoinOperator)joinOperator).setUnionFindJoinExpList(node.GetUnionFindJoinExpression());;
			System.out.println("TNLJ method chosen");
		}
		else if(joinMethod == 1){
			System.out.println("BNLJ method chosen with join page size " + joinPageSize);
			joinOperator = new BNLJOperator(node.GetResidualJoinExpression(), node.GetUnionFindJoinExpression(), node.getExpressoin(), joinPageSize);
		}
		else{
			System.out.println("SMJoin method chosen with sort page size " + sortPageSize);
			JoinAttributesExtraction jae = new JoinAttributesExtraction
					(node.getExpressoin(),LogicalPlanBuilder.getJoinOrder());
			joinOperator= new SMJoinOperator(node.GetResidualJoinExpression(), node.GetUnionFindJoinExpression(), jae.getLeft(), jae.getRight());
		}

		if(rootOperator == null){ rootOperator = joinOperator; }
		else{curOperator.setLeftChild(joinOperator);}

		for(TreeNode child: node.getChildren()){
			curOperator = joinOperator;
//			System.out.println(" joins' child " + child.getTable() + " exp " + child.getExpressoin() + "==============>");
			child.accept(this);		
		}
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
		Operator sortOperator;
		if(sortMethod == 0){
			sortOperator = new SortOperator(null,QueryPlan.schema_pair_order);
			System.out.println("internal sort method chosen");
		}
		else{
			sortOperator = new ExternalSortOperator(null,QueryPlan.schema_pair_order,sortPageSize);
			System.out.println("external sort method chosen with sort page size " + sortPageSize);
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
		System.out.println(op);
		level++;
		printPhysicalPlanTree(op.getLeftChild());
		printPhysicalPlanTree(op.getRightChild());
		
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
