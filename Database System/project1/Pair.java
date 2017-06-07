package cs4321.project1;

/**
 * A data structure contain two elements. First one is a operator, second element
 * is the number of operands needed to perform the corresponding operation
 * 
 * @author Ella Xue (ex32)
 */
public class Pair {
	private String operator; 
	private int needNumOfOperand;
	
	/**
	 * Constructor
	 * 
	 * @param op need to be a string of "+", "-", "*" or "/"
	 * @param n  number of operands needed to perform the corresponding operation
	 */
	Pair(String op, int n){
		operator = op;
		needNumOfOperand = n;
	}
	
	/**
	 * @return the operator
	 */
	public String getOperator(){
		return operator;
	}
	
	/**
	 * @return current number of operands needed to perform the corresponding operation
	 */
	public int getNumOfOperand()
	{
		return needNumOfOperand;
	}
	
	/**
	 *  Decrement the  number of operands needed by one each time a operand is pushed to stack
	 */
	public void updateNumOfOperandNeed(){
		needNumOfOperand--;
	}
}
