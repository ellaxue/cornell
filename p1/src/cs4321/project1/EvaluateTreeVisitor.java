package cs4321.project1;

import cs4321.project1.tree.DivisionTreeNode;
import cs4321.project1.tree.LeafTreeNode;
import cs4321.project1.tree.SubtractionTreeNode;
import cs4321.project1.tree.AdditionTreeNode;
import cs4321.project1.tree.MultiplicationTreeNode;
import cs4321.project1.tree.UnaryMinusTreeNode;
import java.util.*;

/**
 * Visitor to evaluate the tree to a single number
 * 
 * @author Chengcheng Ji (cj368) and Chengjie Yao (cy437) and Pei Xu (px29)
 */
public class EvaluateTreeVisitor implements TreeVisitor {
    
	private double result;
	private Stack<Double> stack= new Stack<Double>();
	
	public EvaluateTreeVisitor() {
		result=0;
	}
	
	/**
	 * Method to get the finished result of the whole visited expression
	 * 
	 * @return (double) result of the whole expression
	 */
	public double getResult() {
		return result; // so that skeleton code compiles
	}

	/**
	 * Visit method for leaf node; just concatenates the numeric value to the tree node
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(LeafTreeNode node) {
		result=node.getData();
		stack.push(result);
	}

	/**
	 * Visit method for unary minus node; recursively visit subtree and wraps
	 * result in the negative value of the result of its subtree
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusTreeNode node) {
		node.getChild().accept(this);
		result=-stack.pop();
		stack.push(result);
	}

	/**
	 * Visit method for addition node based on post-order tree traversal
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		Double right= stack.pop();
		Double left= stack.pop();
		result= left + right;
		stack.push(result);
	}

	/**
	 * Visit method for multiplication node based on post-order tree traversal
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		Double right= stack.pop();
		Double left= stack.pop();
		result= left * right;
		stack.push(result);
	}

	/**
	 * Visit method for subtraction node based on post-order tree traversal
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		Double right= stack.pop();
		Double left= stack.pop();
		result= left - right;
		stack.push(result);
	}

	/**
	 * Visit method for division node based on post-order tree traversal
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		Double right= stack.pop();
		Double left= stack.pop();
		result= left / right;
		stack.push(result);
	}
}
