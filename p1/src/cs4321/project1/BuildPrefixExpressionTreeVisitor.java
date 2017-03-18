package cs4321.project1;

import cs4321.project1.list.*;
import cs4321.project1.tree.*;

/**
 * Traverse the tree for prefix expressions and build up a running list.
 * 
 * @author Chengcheng Ji (cj368), Chengjie Yao (cy437) and Pei Xu (px29)
 */
public class BuildPrefixExpressionTreeVisitor implements TreeVisitor {

	private ListNode last;
	private ListNode result;

	public BuildPrefixExpressionTreeVisitor() {
		result=null;
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
		ListNode nleaf = new NumberListNode(node.getData());
		if(result==null){
			result=nleaf;
			last=nleaf;
		}else{
			last.setNext(nleaf);
			last=nleaf;
		}
	}

	/**
	 * Visit method for unary minus node; turn TreeNode to a ListNode as a UnaryMinusListNode
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
		ListNode n1 = new UnaryMinusListNode();
		if(result==null){
			result=n1;
			last=n1;
		}else{
			last.setNext(n1);
			last=n1;
		}
		node.getChild().accept(this);
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
		ListNode n1 = new AdditionListNode();
		if(result==null){
			result=n1;
			last=n1;
		}else{
			last.setNext(n1);
			last=n1;
		}
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
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
		ListNode n1 = new MultiplicationListNode();
		if(result==null){
			result=n1;
			last=n1;
		}else{
			last.setNext(n1);
			last=n1;
		}
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
	}

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
		ListNode n1 = new SubtractionListNode();
		if(result==null){
			result=n1;
			last=n1;
		}else{
			last.setNext(n1);
			last=n1;
		}
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
	}

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
		ListNode n1 = new DivisionListNode();
		if(result==null){
			result=n1;
			last=n1;
		}else{
			last.setNext(n1);
			last=n1;
		}
		node.getLeftChild().accept(this);
		node.getRightChild().accept(this);
	}

}
