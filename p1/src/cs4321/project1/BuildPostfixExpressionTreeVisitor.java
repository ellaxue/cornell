package cs4321.project1;

import cs4321.project1.list.*;
import cs4321.project1.tree.*;

/**
 * Traverse the tree for postfix expressions and build up a running list.
 * 
 * @author Chengcheng Ji (cj368)，Chengjie Yao (cy437)， and Pei Xu (px29)
 */
public class BuildPostfixExpressionTreeVisitor implements TreeVisitor {

	private ListNode result;
	private ListNode lastNode;

	public BuildPostfixExpressionTreeVisitor() {
		result=null;
		lastNode=null;
	}

	/**
	 * Method to get the ListNode which start the output traverse, that is the first ListNode
	 * 
	 * @return the first ListNode
	 */
	public ListNode getResult() {
		return result;
	}

	/**
	 * Visit method for a leaf node; just turn TreeNode to a ListNode with the same value
	 * If it is the first node of the tree, it becomes the result to output; 
	 * Else, it becomes the "next" node of the last node
	 * Save it as the last node to be added the "next" pointer in the next round
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(LeafTreeNode node) {
		NumberListNode n= new NumberListNode(node.getData());
		if (result!=null) {
			lastNode.setNext(n);
			lastNode=lastNode.getNext();
		}
		if (result==null) {
			result=n;
			lastNode=n;
		}
	}

	/**
	 * Visit method for unary minus node; just turn TreeNode to a ListNode as a UnaryMinusListNode
	 * If it is the first node of the tree, it becomes the result to output;
	 * Else, it becomes the "next" node of the last node
	 * Save it as the last node to be added the "next" pointer in the next round
	 * Finally visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusTreeNode node) {
		node.getChild().accept(this);
		lastNode.setNext(new UnaryMinusListNode()); 
		lastNode=lastNode.getNext();
	}

	/**
	 * Visit method for addition node; just turn TreeNode to a ListNode as a AdditionListNode
	 * If it is the first node of the tree, it becomes the result to output;
	 * Else, it becomes the "next" node of the last node
	 * Save it as the last node to be added the "next" pointer in the next round
	 * Finally visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		lastNode.setNext(new AdditionListNode()); 
		lastNode=lastNode.getNext();
	}

	/**
	 * Visit method for multiplication node; just turn TreeNode to a ListNode as a MultiplicationListNode
	 * If it is the first node of the tree, it becomes the result to output;
	 * Else, it becomes the "next" node of the last node
	 * Save it as the last node to be added the "next" pointer in the next round
	 * Finally visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		lastNode.setNext(new MultiplicationListNode()); 
		lastNode=lastNode.getNext();}

	/**
	 * Visit method for subtraction node; just turn TreeNode to a ListNode as a SubtractionListNode
	 * If it is the first node of the tree, it becomes the result to output;
	 * Else, it becomes the "next" node of the last node
	 * Save it as the last node to be added the "next" pointer in the next round
	 * Finally visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		lastNode.setNext(new SubtractionListNode());
		lastNode=lastNode.getNext();}

	/**
	 * Visit method for division node; just turn TreeNode to a ListNode as a DivisionListNode
	 * If it is the first node of the tree, it becomes the result to output;
	 * Else, it becomes the "next" node of the last node
	 * Save it as the last node to be added the "next" pointer in the next round
	 * Finally visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		lastNode.setNext(new DivisionListNode());
		lastNode=lastNode.getNext();
	}

}
