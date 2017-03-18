package cs4321.project1;

import cs4321.project1.list.DivisionListNode;
import cs4321.project1.list.SubtractionListNode;
import cs4321.project1.list.NumberListNode;
import cs4321.project1.list.AdditionListNode;
import cs4321.project1.list.MultiplicationListNode;
import cs4321.project1.list.UnaryMinusListNode;
import java.util.*;

/**
 * Use the standard postfix evaluation algorithm with a stack of operands.
 * If the next is an operand, push the corresponding number of operands of the stack;
 * Else evaluate the operator, pop the numbers, calculate them and push the result into the stack.
 * 
 * @author Chengcheng Ji (cj368), Chengjie Yao (cy437) and Pei Xu(px29)
 */
public class EvaluatePostfixListVisitor implements ListVisitor {

	private Stack<Double> stack= new Stack<Double>();
	private double result;


	public EvaluatePostfixListVisitor() {
		result=0;
	}

	/**
	 * Method to get the single value of the whole expression
	 * 
	 * @return the (double) value of the whole expression
	 */
	public double getResult() {
		return result; // so that skeleton code compiles
	}

	/**
	 * Visit method for the node contained a number
	 * concatenates the numeric value and push it to the stack
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(NumberListNode node) {
		result= node.getData();
		stack.push(result);
		if(node.getNext()!=null) {
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for addition node, pop numbers of the stack, 
	 * calculate the result and push it into the stack
	 * Visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionListNode node) {
		Double right= stack.pop();
		Double left= stack.pop();
		result= right+left;
		stack.push(result);
		if(node.getNext()!=null) {
			node.getNext().accept(this);
		}	
	}

	/**
	 * Visit method for subtraction node, pop numbers of the stack, 
	 * calculate the result and push it into the stack
	 * Visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionListNode node) {
		Double right= stack.pop();
		Double left= stack.pop();
		result= left-right;
		stack.push(result);
		if(node.getNext()!=null) {
			node.getNext().accept(this);
		}	
	}

	/**
	 * Visit method for multiplication node, pop numbers of the stack, 
	 * calculate the result and push it into the stack
	 * Visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationListNode node) {
		Double right= stack.pop();
		Double left= stack.pop();
		result= right*left;
		stack.push(result);
		if(node.getNext()!=null) {
			node.getNext().accept(this);
		}	
	}

	/**
	 * Visit method for division node, pop numbers of the stack, 
	 * calculate the result and push it into the stack
	 * Visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionListNode node) {
		Double right= stack.pop();
		Double left= stack.pop();
		result= left/right;
		stack.push(result);
		if(node.getNext()!=null) {
			node.getNext().accept(this);
		}	
	}

	/**
	 * Visit method for unary minus node, pop numbers of the stack, 
	 * calculate the result and push it into the stack
	 * Visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusListNode node) {
		result= -stack.pop();
		stack.push(result);
		if(node.getNext()!=null) {
			node.getNext().accept(this);
		}	
	}

}
