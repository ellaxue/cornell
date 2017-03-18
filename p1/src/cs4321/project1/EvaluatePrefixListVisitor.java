package cs4321.project1;

import cs4321.project1.list.*;
import java.util.*;

/**
 * Use the standard prefix evaluation algorithm 
 * with a stack of operands and a stack of pairs of operators and the number of remaining operands.
 * 
 * @author Chengcheng Ji (cj368), Chengjie Yao (cy437) and Pei Xu(px29)
 */

public class EvaluatePrefixListVisitor implements ListVisitor {

	private Pair<String,Integer> pair;
	private Stack<Double> stackDouble= new Stack<>();
	private Stack<Pair<String,Integer>> stackPair = new Stack<>();
	private double result = 0;
	private double left;
	private double right;

	public EvaluatePrefixListVisitor() {

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
	 * concatenates the numeric value and push it into the stackDouble
	 * Detect the number of remaining operands of the current pair
	 * If the number becomes 0 then calculate the result and push it into the stackDouble 
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(NumberListNode node) {
		result = node.getData();
		stackDouble.push(result);
		if(!stackPair.isEmpty()) {
			pair = stackPair.peek();
			pair.setSecond(pair.getSecond()-1);

			while(!stackPair.isEmpty() && stackPair.peek().getSecond()==0){
				pair = stackPair.pop();
				String s = pair.getFirst();
				switch(s){
				case "+": 
					right = stackDouble.pop();
					left = stackDouble.pop();
					result = left + right;
					break;
				case "-": 
					right = stackDouble.pop();
					left = stackDouble.pop();
					result = left - right;
					break;
				case "*": 
					right = stackDouble.pop();
					left = stackDouble.pop();
					result = left * right;
					break;
				case "/": 
					right = stackDouble.pop();
					left = stackDouble.pop();
					result = left / right;
					break;
				case "~": 
					result = - stackDouble.pop();
					break;
				}
				stackDouble.push(result);
				if(!stackPair.isEmpty()){
					pair = stackPair.peek();
					pair.setSecond(pair.getSecond()-1);
				}
			}
		}
		if(node.getNext()!=null) {
			node.getNext().accept(this);
		}
	}

	/**
	 * Visit method for addition node, push it as String into stackPair 
	 * whose number of remaining operands is 2
	 * Visit the subtree recursively
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(AdditionListNode node) {
		stackPair.push(new Pair<>("+", 2));
		node.getNext().accept(this);
	}

	/**
	 * Visit method for subtraction node, push it as String into stackPair 
	 * whose number of remaining operands is 2
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(SubtractionListNode node) {
		stackPair.push(new Pair<>("-", 2));
		node.getNext().accept(this);
	}

	/**
	 * Visit method for multiplication node, push it as String into stackPair 
	 * whose number of remaining operands is 2
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(MultiplicationListNode node) {
		stackPair.push(new Pair<>("*", 2));
		node.getNext().accept(this);
	}

	/**
	 * Visit method for division node, push it as String into stackPair 
	 * whose number of remaining operands is 2
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(DivisionListNode node) {
		stackPair.push(new Pair<>("/", 2));
		node.getNext().accept(this);
	}

	/**
	 * Visit method for unary minus node, push it as String into stackPair 
	 * whose number of remaining operands is 2
	 * 
	 * @param node
	 *            the node to be visited
	 */
	@Override
	public void visit(UnaryMinusListNode node) {
		stackPair.push(new Pair<>("~", 1));
		node.getNext().accept(this);
	}
	
	/**
	 * A structure for storing two different types of data.
	 * 
	 * @author Chengcheng Ji (cj368) and Chengjie Yao (cy437)
	 */
	public class Pair<V, T> {
		private V v; //the first parameter in pair
		private T t; //the second parameter in pair

		public Pair(V vv, T tt) {
			this.v = vv;
			this.t = tt;
		}
		/**
		 * Set the first parameter as the given value
		 * 
		 * @param vv
		 *            the value is to be set as the first parameter
		 */
		public void setFirst(V vv) {
			this.v = vv;
		}
		/**
		 * Set the second parameter as the given value
		 * 
		 * @param tt
		 *            the value is to be set as the second parameter
		 */
		public void setSecond(T tt) {
			this.t = tt;
		}
		/**
		 * Get the value of the first parameter
		 * 
		 * @return v
		 *            the value of the first parameter
		 */
		public V getFirst() {
			return v;
		}
		/**
		 * Get the value of the second parameter
		 * 
		 * @return t
		 *            the value of the second parameter
		 */
		public T getSecond() {
			return t;
		}
	}
}
