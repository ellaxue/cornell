package cs4321.project1;

import cs4321.project1.list.*;
import cs4321.project1.tree.*;

/**
 * Change a tree expression to postfix expression using recursive method
 *  
 * @author Ella Xue (ex32)
 */
public class BuildPostfixExpressionTreeVisitor implements TreeVisitor {
	private ListNode head;
	private ListNode next;
	
	/**
	 * Constructor
	 * 
	 **/
	public BuildPostfixExpressionTreeVisitor() {
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
	 * @param node the node to be visited
	 *            
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
	 * @param node the node to be visited
	 *
	 **/
	@Override
	public void visit(UnaryMinusTreeNode node) {
		node.getChild().accept(this);
		ListNode newListNode = new UnaryMinusListNode();
		setNewNode(newListNode);
	}

	/**
	 * Visit method for addition tree node; get the numeric value and 
	 * add it to the addition list node
	 * 
	 * @param node the node to be visited
	 *            
	 */
	@Override
	public void visit(AdditionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		ListNode newListNode = new AdditionListNode();		
		setNewNode(newListNode);

	}

	/**
	 * Visit method for multiplication tree node; get the numeric value
	 * and add it to the multiplication list node
	 * 
	 * @param node the node to be visited
	 */
	@Override
	public void visit(MultiplicationTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);	
		ListNode newListNode = new MultiplicationListNode();		
		setNewNode(newListNode);

	}

	/**
	 * Visit method for subtraction tree node; get the numeric value and 
	 * add it to the subtraction list node
	 * 
	 * @param node  the node to be visited
	 *           
	 */
	@Override
	public void visit(SubtractionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		ListNode newListNode = new SubtractionListNode();		
		setNewNode(newListNode);
	}

	/**
	 * Visit method for division tree node; get the numeric value and 
	 * add it to the division list node
	 * 
	 * @param node the node to be visited
	 *            
	 */
	@Override
	public void visit(DivisionTreeNode node) {
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
		ListNode newListNode = new DivisionListNode();		
		setNewNode(newListNode);			
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
