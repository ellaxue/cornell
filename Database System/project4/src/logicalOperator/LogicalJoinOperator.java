package logicalOperator;
import project.OperationVisitor;

/**
 * A logical operator class for join query
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class LogicalJoinOperator extends TreeNode{

	/**
	 * default constructor
	 */
	public LogicalJoinOperator(){}

	public LogicalJoinOperator(TreeNode leftChild, TreeNode rightChild) {
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}
	@Override
	public void accept(OperationVisitor visitor) throws Exception {
		visitor.visit(this);
	}

}
