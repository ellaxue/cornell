package cs4321.project1;

import cs4321.project1.list.*;
import cs4321.project1.tree.*;

/**
 * Change a tree expression to prefix expression using recursive method
 * The list is built in reverse order (from right to left)
 * 
 * @author Ella Xue (ex32)
 */
public class BuildPrefixExpressionTreeVisitor implements TreeVisitor {
	private ListNode head;
	private ListNode next;
	
	public BuildPrefixExpressionTreeVisitor() {
		head = null;
		next = null;
	}
	
	/**
	 * Method to get the finished list head when visitor is done
	 * 
	 * @return double the result of the expression
	 */
	public ListNode getResult() {
		return head;
	}

	/**
	 * Visit method for leaf node; get the numeric value and 
	 * add it to the number list node
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(LeafTreeNode node) {
		ListNode newListNode = new NumberListNode(node.getData());
		setNewNode(newListNode);		
	}

	/**
	 * Visit method for unary minus tree node; get the numeric value and 
	 * add it to the unary minus list node
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusTreeNode node) {
		ListNode newListNode = new UnaryMinusListNode();
		setNewNode(newListNode);
		node.getChild().accept(this);
	}

	/**
	 * Visit method for addition tree node; get the numeric value and 
	 * add it to the addition list node
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionTreeNode node) {
		ListNode newListNode = new AdditionListNode();		
		setNewNode(newListNode);
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
	}

	/**
	 * Visit method for multiplication tree node; get the numeric value
	 * and add it to the multiplication list node
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationTreeNode node) {
		ListNode newListNode = new MultiplicationListNode();		
		setNewNode(newListNode);
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
	}

	/**
	 * Visit method for subtraction tree node; get the numeric value and 
	 * add it to the subtraction list node
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionTreeNode node) {
		ListNode newListNode = new SubtractionListNode();		
		setNewNode(newListNode);
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
	}

	/**
	 * Visit method for division tree node; get the numeric value and 
	 * add it to the division list node
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionTreeNode node) {
		ListNode newListNode = new DivisionListNode();		
		setNewNode(newListNode);
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
	}
	
	/**
	 * Linked the new node to be the next node
	 * 
	 * @param newListNode new created node
	 */
	private void setNewNode(ListNode newListNode){
		if(head == null){
			head = newListNode;
			next = head;
		}
		else{
			next.setNext(newListNode);
			next = next.getNext();
		}
	}

}
