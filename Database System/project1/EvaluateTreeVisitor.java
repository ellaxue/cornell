package cs4321.project1;
import java.util.*;
import cs4321.project1.tree.DivisionTreeNode;
import cs4321.project1.tree.LeafTreeNode;
import cs4321.project1.tree.SubtractionTreeNode;
import cs4321.project1.tree.AdditionTreeNode;
import cs4321.project1.tree.MultiplicationTreeNode;
import cs4321.project1.tree.UnaryMinusTreeNode;

/**
 * Traverse a tree and calculate the result recursively using a stack
 * 
 * @author Ella Xue (ex32)
 */
public class EvaluateTreeVisitor implements TreeVisitor {
	private Stack<Double> stack;
	
	/**
	 * Constructor
	 */
	public EvaluateTreeVisitor() {
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
	 * Visit method for leaf node; just concatenates the numeric value and 
	 * push it to treeStack
	 * 
	 * @param node the node to be visited
	 *            
	 */
	@Override
	public void visit(LeafTreeNode node) {
		stack.push(node.getData());
	}

	/**
	 * Visit method for unary minus node; recursively visit subtree and push 
	 * the result after unary minus to treeStack
	 * 
	 * @param node the node to be visited
	 *            
	 */
	@Override
	public void visit(UnaryMinusTreeNode node) {
		node.getChild().accept(this);
		stack.push(-stack.pop());
	}

	/**
	 * Visit method for addition node; recursively visit subtree and push 
	 * the result after addition to treeStack
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionTreeNode node) {
		node.getRightChild().accept(this);
		node.getLeftChild().accept(this);
		stack.push(stack.pop() + stack.pop());
	}

	/**
	 * Visit method for multiplication node; recursively visit subtree and push 
	 * the result after multiplication to treeStack
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationTreeNode node) {
		node.getRightChild().accept(this);
		node.getLeftChild().accept(this);
		stack.push(stack.pop() * stack.pop());
	}

	/**
	 * Visit method for subtraction node; recursively visit subtree and push 
	 * the result after subtraction to treeStack
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionTreeNode node) {
		node.getRightChild().accept(this);
		node.getLeftChild().accept(this);
		stack.push(stack.pop() - stack.pop());
	}

	/**
	 * Visit method for division node; recursively visit subtree and push 
	 * the result after division to treeStack
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionTreeNode node) {
		node.getRightChild().accept(this);
		node.getLeftChild().accept(this);
		stack.push(stack.pop() / stack.pop());	
	}
}
