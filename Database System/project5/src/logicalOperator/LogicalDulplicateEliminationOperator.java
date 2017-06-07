package logicalOperator;

import project.OperationVisitor;
import project.QueryInterpreter;

/**
 * A logical operator class for distinct query
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class LogicalDulplicateEliminationOperator extends TreeNode {
	public LogicalDulplicateEliminationOperator(TreeNode leftChild) {
		this.leftChild = leftChild;
	}
	
	@Override
	public void accept(OperationVisitor visitor) throws Exception {
		visitor.visit(this);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
	
		sb.append("DulElim\n");
		return sb.toString();
	}
}
