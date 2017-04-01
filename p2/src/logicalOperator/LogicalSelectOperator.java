package logicalOperator;

import java.io.IOException;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import project.OperationVisitor;

/**
 * A logical operator class for selection query
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class LogicalSelectOperator extends TreeNode{
	
	/**
	 * constructor 
	 * @param leftChild the child operator
	 */
	public LogicalSelectOperator(LogicalScanOperator leftChild) {
		this.leftChild = leftChild;
	}

	@Override
	public void accept(OperationVisitor visitor) throws IOException {
		visitor.visit(this);
	}

}
