package logicalOperator;

import java.io.IOException;

import project.OperationVisitor;

/**
 * A logical operator class for scanning a table
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class LogicalScanOperator extends TreeNode {
	
	/**
	 * default constructor
	 */
	public LogicalScanOperator(){}

	@Override
	public void accept(OperationVisitor visitor) throws IOException {
		visitor.visit(this);
	}
}
