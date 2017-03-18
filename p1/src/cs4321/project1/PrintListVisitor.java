package cs4321.project1;

import cs4321.project1.list.*;

/**
 * Visitor to pretty-print a tree expression, without parenthesized. 
 * 
 * @author Chengcheng Ji (cj368) and Chengjie Yao (cy437)
 */

public class PrintListVisitor implements ListVisitor {

	private String result;

	public PrintListVisitor() {
		result="";
	}

	/**
	 * Method to get the finished string representation when visitor is done
	 * 
	 * @return string representation of the visited tree
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Visit method for node which contained a number; just concatenates the numeric value to the
	 * running string
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(NumberListNode node) {
		result +=node.getData();
		if (node.getNext()!=null) {
			result +=" ";
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for addition node and recursively get the next node of the list
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionListNode node) {	
		result += "+";
		if (node.getNext()!=null) {
			result += " ";
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for subtraction node and recursively get the next node of the list
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionListNode node) {
		result += "-";
		if (node.getNext()!=null) {
			result += " ";
			node.getNext().accept(this);
		}	
	}

	/**
	 * Visit method for multiplication node and recursively get the next node of the list
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationListNode node) {
		result += "*";
		if (node.getNext()!=null) {
			result += " ";
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for division node and recursively get the next node of the list
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionListNode node) {
		result += "/";
		if (node.getNext()!=null) {
			result += " ";
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for unary minus node and recursively get the next node of the list
	 * Use the symbol ~ rather than - for a unary minus
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusListNode node) {
		result += "~";
		if (node.getNext()!=null) {
			result += " ";
			node.getNext().accept(this);
		}	
	}

}
