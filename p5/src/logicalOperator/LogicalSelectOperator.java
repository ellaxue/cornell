package logicalOperator;
import project.OperationVisitor;
import project.QueryInterpreter;

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
	public void accept(OperationVisitor visitor) throws Exception {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < QueryInterpreter.level; i++){
			sb.append("-");
		}		
		if(expression == null){
			sb.append("Leaf[").append(table.getName()).append("]\n");
		}
		else{
			sb.append("Select[").append(expression).append("]\n");
			
			for(int i = 0; i < QueryInterpreter.level+1; i++){
				sb.append("-");
			}	
			sb.append("Leaf[").append(table.getName()).append("]\n");
		}	
		return sb.toString();
	}
}
