package logicalOperator;
import java.util.List;

import net.sf.jsqlparser.statement.select.SelectItem;
import project.OperationVisitor;

/**
 * A logical operator class for projection query
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class LogicalProjectOperator extends TreeNode{
	List<SelectItem> itemList;

	/**
	 * default constructor
	 */
	public LogicalProjectOperator(){
		
	}
	public LogicalProjectOperator(TreeNode leftChild, List<SelectItem> itemList) {
		this.leftChild = leftChild;
		this.itemList = itemList;
	}

	@Override
	public void accept(OperationVisitor visitor) throws Exception {
		visitor.visit(this);
	}

}
