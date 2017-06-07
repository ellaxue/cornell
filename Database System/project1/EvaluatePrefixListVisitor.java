package cs4321.project1;

import java.util.Stack;

import cs4321.project1.list.*;

/**
 * Traverse a prefix list and calculate the result using two stacks.
 * One stores the value and the operator while the other one stores
 * the result at each stage
 * 
 * @author Ella Xue (ex32)
 */

public class EvaluatePrefixListVisitor implements ListVisitor {
	private Stack<Pair> operatorStack;
	private Stack<Double> operandStack;
	
	/**
	 * Constructor
	 */
	public EvaluatePrefixListVisitor() {
		operatorStack = new Stack<Pair>();
		operandStack = new Stack<Double>();
	}

	/**
	 * Method to get the finished number when visitor is done
	 * 
	 * @return double the result of the expression
	 */
	public double getResult() {
		// TODO fill me in
		return operandStack.pop(); // so that skeleton code compiles
	}

	/**
	 * Visit method for the whole list. Track all nodes in the 
	 * list in Prefix order and calculate the values using the 
	 * corresponding operators
	 *  
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(NumberListNode node) {
		operandStack.push(node.getData());
		
		while(!operatorStack.isEmpty()){
			operatorStack.peek().updateNumOfOperandNeed();
		
			if(operatorStack.peek().getNumOfOperand() == 0){
				eval();
			}
			else break;
		}
		if(node.getNext() != null){
			node.getNext().accept(this);
		}		
	}

	/**
	 * Visit method for addition node; keep looping the next node, push 
	 * the operator and value pair to stack operator, and push the result 
	 * after addition to stack operands
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionListNode node) {
		operatorStack.push(new Pair("+", 2));
		node.getNext().accept(this);
	}

	/**
	 * Visit method for subtraction node; keep looping the next node, push 
	 * the operator and value pair to stack operator, and push the result 
	 * after subtraction to stack operands
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionListNode node) {
		operatorStack.push(new Pair("-", 2));
		node.getNext().accept(this);
	}

	/**
	 * Visit method for multiplication node; keep looping the next node, push 
	 * the operator and value pair to stack operator, and push the result 
	 * after multiplication to stack operands
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationListNode node) {
		operatorStack.push(new Pair("*", 2));
		node.getNext().accept(this);
	}

	/**
	 * Visit method for division node; keep looping the next node, push 
	 * the operator and value pair to stack operator, and push the result 
	 * after division to stack operands
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionListNode node) {
		operatorStack.push(new Pair("/", 2));
		node.getNext().accept(this);
	}

	/**
	 * Visit method for unary minus node; keep looping the next node, push 
	 * the operator and value pair to stack operator, and push the result 
	 * after unary minus to stack operands
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusListNode node) {
		operatorStack.push(new Pair("~", 1));
		node.getNext().accept(this);
	}
	
	
	/**
	 * Evaluate the expression on the current stacks depends on the operator
	 */
	private void eval(){
		String operator = operatorStack.pop().getOperator();
		double second, first;
		
		switch(operator){
		case "+": 	operandStack.push(operandStack.pop() + operandStack.pop());
					break;
				
		case "-":	second = operandStack.pop();
					first  = operandStack.pop();
					operandStack.push(first - second);
					
					break;
				
		case "/":	second = operandStack.pop();
					first  = operandStack.pop();
					operandStack.push(first / second);
					break;
				
		case "*": 	operandStack.push(operandStack.pop() * operandStack.pop());
					break;
				
		case "~":   operandStack.push(-operandStack.pop());
					break;
				
		default:	break;
		}
	}
}
