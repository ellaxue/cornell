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
	private String FinalOrder=null;
	/**
	 * default constructor
	 */
	public LogicalJoinOperator(UnionFind unionfind, ArrayList<Expression> joinExp){
		unionFind = unionfind;
		setChildren(new ArrayList<TreeNode>());
		expressions = new ArrayList<Expression>();
		if(unionFind != null){
			unionFindSelectExpMap = unionFind.getUnionFindSelectExpMap();
			unionFindJoinExpList = unionFind.getUnionFindJoinExpList();
		}
		residualJoinExpression = joinExp;
//		residualSelectExpression = selectExp;
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


	public void addChild(LogicalSelectOperator selectOperator) {
		children.add(selectOperator);		
	}
	
	public void addExpression(Expression exp){
		expressions.add(exp);
	}
	
	public ArrayList<TreeNode> getChildren(){
		return children;
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Join").append(residualJoinExpression).append("\n");
		
		for(Element e:unionFindJoinExpList){
			sb.append(e);
		}
		return sb.toString();
	}

	public void setChildren(ArrayList<TreeNode> children) {
		this.children = children;
	}

	public String getFinalOrder() {
		return FinalOrder;
	}

	public void setFinalOrder(String finalOrder) {
		FinalOrder = finalOrder;
	}
}
