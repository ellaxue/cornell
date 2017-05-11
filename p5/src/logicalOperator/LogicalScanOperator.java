package logicalOperator;
import project.OperationVisitor;
import project.QueryInterpreter;

/**
 * A logical operator class for scanning a table
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class LogicalScanOperator extends TreeNode {
	private String tableName;
	/**
	 * default constructor
	 */
	public LogicalScanOperator(String tableName){
		this.tableName = tableName;
	}

	@Override
	public void accept(OperationVisitor visitor) throws Exception {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
	
		sb.append("Leaf[").append(tableName).append("]\n");
		return sb.toString();
	}
}
