package cs4321.project1;
import java.util.Stack;
import cs4321.project1.list.DivisionListNode;
import cs4321.project1.list.SubtractionListNode;
import cs4321.project1.list.NumberListNode;
import cs4321.project1.list.AdditionListNode;
import cs4321.project1.list.MultiplicationListNode;
import cs4321.project1.list.UnaryMinusListNode;

/**
 * Traverse a post fix list and calculate the result using a stack
 * 
 * @author Ella Xue (ex32)
 */
public class EvaluatePostfixListVisitor implements ListVisitor {
	private Stack<Double> stack;
	
	/**
	 * Constructor
	 */
	public EvaluatePostfixListVisitor() {
		stack = new Stack<Double>();
	}

	/**
	 * Method to get the finished number when visitor is done
	 * 
	 * @return double the result of the expression
	 */
	public double getResult() {
		return stack.pop(); 
	}

	/**
	 * Visit method for the whole list. Track all nodes in the 
	 * list in Postfix order and calculate the values using the 
	 * corresponding operators
	 *  
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(NumberListNode node) {
		stack.push(node.getData());
		if(node.getNext() != null){
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for addition node; keep looping the next node, push 
	 * the result to stack postListStack after addition 
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionListNode node) {
		stack.push(stack.pop() + stack.pop());
		if(node.getNext() != null){
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for subtraction node; keep looping the next node, push 
	 * the result to stack postListStack after subtraction 
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionListNode node) {
		Double second = stack.pop();
		Double first = stack.pop();
		stack.push(first - second);
		if(node.getNext() != null){
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for multiplication node; keep looping the next node, push 
	 * the result to stack postListStack after multiplication 
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationListNode node) {
		Double second = stack.pop();
		Double first = stack.pop();
		stack.push(first * second);
		if(node.getNext() != null){
			node.getNext().accept(this);
		}

	}

	/**
	 * Visit method for division node; keep looping the next node, push 
	 * the result to stack postListStack after division 
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionListNode node) {
		Double second = stack.pop();
		Double first = stack.pop();
		stack.push(first / second);
		if(node.getNext() != null){
			node.getNext().accept(this);
		}

	}

	/**
	 * Visit method for unary minus node; keep looping the next node, push 
	 * the result to stack postListStack after unary minus 
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusListNode node) {
		stack.push(-stack.pop());
		if(node.getNext() != null){
			node.getNext().accept(this);
		}

	}

}
