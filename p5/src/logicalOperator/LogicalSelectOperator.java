package logicalOperator;
import java.util.HashMap;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import project.OperationVisitor;
import project.QueryInterpreter;

/**
 * A logical operator class for selection query
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class LogicalSelectOperator extends TreeNode{
	HashMap<String, Expression> unionFindSelectExpMap;
	HashMap<String, Expression> residualSelectExpression;
	/**
	 * constructor 
	 * @param leftChild the child operator
	 */
	public LogicalSelectOperator(){}
	public LogicalSelectOperator(LogicalScanOperator scanOp,HashMap<String, Expression> unionFindSelectExpMap,
			HashMap<String, Expression> residualSelectExpression) {
		this.leftChild = scanOp;
		this.residualSelectExpression = residualSelectExpression;
		this.unionFindSelectExpMap = unionFindSelectExpMap;
	}
	@Override
	public void accept(OperationVisitor visitor) throws Exception {
		visitor.visit(this);
	}

	public void processExpression( HashMap<String, Expression> unionFindSelectExpMap, HashMap<String, Expression> residualSelectExpression){
		String tableName = cl.UseAlias() ? this.getTable().getAlias() : this.getTable().getName();
		//set unionFind selection condition for current selection operator first
		this.setExpressoin(unionFindSelectExpMap.get(tableName));
		
		//set additional selection condition for current selection operator
		if(this.getExpressoin() != null && residualSelectExpression.containsKey(tableName)){
			this.setExpressoin(new AndExpression(this.getExpressoin(),residualSelectExpression.get(tableName)));
		}
		else if(residualSelectExpression.containsKey(tableName)){
			this.setExpressoin(residualSelectExpression.get(tableName));
		}
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Select[").append(expression).append("]\n");
		return sb.toString();
	}
	public void setExpressoin(Table table) {
		this.setTable(table);
		String tableName = cl.UseAlias() ? table.getAlias() : table.getName();
		//set unionFind selection condition for current selection operator first
		this.setExpressoin(unionFindSelectExpMap.get(tableName));
		
		//set additional selection condition for current selection operator
		if(this.getExpressoin() != null && residualSelectExpression.containsKey(tableName)){
			this.setExpressoin(new AndExpression(this.getExpressoin(),residualSelectExpression.get(tableName)));
		}
		else if(residualSelectExpression.containsKey(tableName)){
			this.setExpressoin(residualSelectExpression.get(tableName));
		}
	}
}
