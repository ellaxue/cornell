package logicalOperator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ChooseSelectionImp.Element;
import ChooseSelectionImp.UnionFind;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import project.OperationVisitor;
import project.QueryInterpreter;
import project.catalog;

/**
 * A logical operator class for join query
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class LogicalJoinOperator extends TreeNode{
	private ArrayList<TreeNode> children;
	private ArrayList<Expression> expressions;
	private HashMap<String, Expression> unionFindSelectExpMap; //key is the alias name if alias is used
	private ArrayList<Element> unionFindJoinExpList;//usable join expression during unionFind operation
	private catalog cl = catalog.getInstance();
	private UnionFind unionFind;
	private ArrayList<Expression> residualJoinExpression; //exp not in the unionFind element set 
	private HashMap<String, Expression> residualSelectExpression;//exp not in the unionFind element set
	/**
	 * default constructor
	 */
	public LogicalJoinOperator(UnionFind unionfind, ArrayList<Expression> joinExp,HashMap<String, Expression> selectExp){
		unionFind = unionfind;
		setChildren(new ArrayList<TreeNode>());
		expressions = new ArrayList<Expression>();
		if(unionFind != null){
			unionFindSelectExpMap = unionFind.getUnionFindSelectExpMap();
			unionFindJoinExpList = unionFind.getUnionFindJoinExpList();
		}
		residualJoinExpression = joinExp;
		residualSelectExpression = selectExp;
	}
	
	public ArrayList<Expression> GetResidualJoinExpression(){
		return residualJoinExpression;
	}
	
	
	public ArrayList<Element> GetUnionFindJoinExpression(){
		return unionFindJoinExpList;
	}
	public LogicalJoinOperator(TreeNode leftChild, TreeNode rightChild) {
		setChildren(new ArrayList<TreeNode>());
		expressions = new ArrayList<Expression>();
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}
	
	@Override
	public void accept(OperationVisitor visitor) throws Exception {
		visitor.visit(this);
	}

	public ArrayList<TreeNode> getChildren(){
		return children;
	}
	public void addChild(LogicalSelectOperator selectOperator) {
		String tableName = cl.UseAlias() ? selectOperator.getTable().getAlias() : selectOperator.getTable().getName();
		//set unionFind selection condition for current selection operator first
		selectOperator.setExpressoin(unionFindSelectExpMap.get(tableName));
		//set additional selection condition for current selection operator
		if(selectOperator.getExpressoin() != null && residualSelectExpression.containsKey(tableName)){
			selectOperator.setExpressoin(new AndExpression(selectOperator.getExpressoin(),residualSelectExpression.get(tableName)));
		}
		else if(residualSelectExpression.containsKey(tableName)){
			selectOperator.setExpressoin(residualSelectExpression.get(tableName));
		}
		getChildren().add(selectOperator);		
	}
	
	public void addExpression(Expression exp){
		expressions.add(exp);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < QueryInterpreter.level; i++){
			sb.append("-");
		}
		sb.append("Join").append(residualJoinExpression).append("\n");
		
		for(Element e:unionFindJoinExpList){
			sb.append(e);
		}
		
		QueryInterpreter.level++;
		for(TreeNode node: getChildren()){
			sb.append(node);
		}
		return sb.toString();
	}

	public void setChildren(ArrayList<TreeNode> children) {
		this.children = children;
	}
}
