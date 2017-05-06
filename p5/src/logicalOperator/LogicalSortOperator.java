package logicalOperator;

import java.util.List;
import net.sf.jsqlparser.statement.select.OrderByElement;
import project.OperationVisitor;
import project.QueryInterpreter;
import queryPlanBuilder.PhysicalPlanBuilder;

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
	public void accept(OperationVisitor visitor) throws Exception {
		visitor.visit(this);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < QueryInterpreter.level; i++){
			sb.append("-");
		}
		sb.append("Sort").append(orderElementList);
		return sb.toString();
	}
}
