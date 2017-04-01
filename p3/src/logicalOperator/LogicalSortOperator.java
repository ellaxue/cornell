package logicalOperator;

import java.io.IOException;
import java.util.List;

import net.sf.jsqlparser.statement.select.OrderByElement;
import project.OperationVisitor;

/**
 * A logical operator class for sort query
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class LogicalSortOperator extends TreeNode{
	List<OrderByElement> orderElementList;

	/**
	 * constructor
	 * @param leftChild child operator
	 * @param orderElementList the orderBy query column list
	 */
	public LogicalSortOperator(TreeNode leftChild,List<OrderByElement> orderElementList) {
		this.leftChild = leftChild;
		this.orderElementList = orderElementList;
	}

	@Override
	public void accept(OperationVisitor visitor) throws IOException {
		visitor.visit(this);
	}
}
