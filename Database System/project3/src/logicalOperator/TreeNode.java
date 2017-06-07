package logicalOperator;

import java.io.IOException;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import project.OperationVisitor;

/**
 * Abstract class outline for logical operator
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 *
 */
public abstract class TreeNode {
	TreeNode leftChild;
	TreeNode rightChild;
	Expression expression;
	Table table;
	
	TreeNode(){leftChild = null;rightChild = null;expression = null;table = null;}
	
	public abstract void accept(OperationVisitor visitor) throws IOException;
	
	public TreeNode getLeftChild(){ return this.leftChild; }
	
	public TreeNode getRightChild(){ return this.rightChild; }
	
	public void setTable(Table table){this.table = table;}
	
	public Table getTable(){return this.table;}
	
	public void setExpressoin(Expression expression){this.expression = expression;}
	
	public Expression getExpressoin(){return this.expression;}
}
