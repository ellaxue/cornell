package cs4321.project1;

import cs4321.project1.list.*;

/**
 * Visitor to pretty-print a list expression, fully parenthesized.
 * 
 * @author Ella Xue (ex32)
 * 
 */
public class PrintListVisitor implements ListVisitor {
	private String result;

	public PrintListVisitor() {
		result = "";
	}

	/**
	 * Method to get the finished string representation when visitor is done
	 * 
	 * @return string representation of the visited list
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Visit method for list node; just concatenates the numeric value to the
	 * running string
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(NumberListNode node) {
		result += node.getData();
		if(node.getNext() != null){
			result += " ";
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for addition node in the order of the list
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionListNode node) {
		result += "+";
		if(node.getNext() != null){
			result += " ";
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for subtraction node in the order of the list
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionListNode node) {
		result += "-";
		if(node.getNext() != null){
			result += " ";
			node.getNext().accept(this);
		}
	}
	
	/**
	 * Visit method for multiplication node in the order of the list
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationListNode node) {
		result += "*";
		if(node.getNext() != null){
			result += " ";
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for division node in the order of the list
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionListNode node) {
		result += "/";
		if(node.getNext() != null){
			result += " ";
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for unary minus node; recursively visit next node and wraps
	 * result in the last node with unary -
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusListNode node) {
		result += "~";
		if(node.getNext() != null){
			result += " ";
			node.getNext().accept(this);
		}
	}

}
