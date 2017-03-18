package cs4321.project1;

import cs4321.project1.tree.*;

/**
 * Class for a parser that can parse a string and produce an expression tree. To
 * keep the code simple, this does no input checking whatsoever so it only works
 * on correct input.
 * 
 * An expression is one or more terms separated by + or - signs. A term is one
 * or more factors separated by * or / signs. A factor is an expression in
 * parentheses (), a factor with a unary - before it, or a number.
 * 
 * The deeper the depth of a expression, the more priority it has.
 * 
 * @author Lucja Kot
 * @author Chengcheng Ji (cj368), Chengjie Yao (cy437) and Pei Xu(px29)
 */
public class Parser {

	private String[] tokens;
	private int currentToken; // pointer to next input token to be processed

	/**
	 * @precondition input represents a valid expression with all tokens
	 *               separated by spaces, e.g. "3.0 - ( 1.0 + 2.0 ) / - 5.0. All
	 *               tokens must be either numbers that parse to Double, or one
	 *               of the symbols +, -, /, *, ( or ), and all parentheses must
	 *               be matched and properly nested.
	 */
	public Parser(String input) {
		this.tokens = input.split("\\s+");
		currentToken = 0;
	}

	/**
	 * Parse the input and build the expression tree
	 * 
	 * @return the (root node of) the resulting tree
	 */
	public TreeNode parse() {
		return expression();
	}

	/**
	 * Parse the remaining input as far as needed to get the next factor
	 * "- number" or number
	 * @return the (root node of) the resulting subtree
	 */
	private TreeNode factor() {
		String s = tokens[currentToken];
		if(currentToken < tokens.length-1)	currentToken++;
		TreeNode result = null;
		try{
			double d = Double.parseDouble(s);
			
			return new LeafTreeNode(d);
		}catch(NumberFormatException e){
			if(s.equals("-")){
				return new UnaryMinusTreeNode(factor());
			}
			if(s.equals("(")){
				System.out.println(currentToken + "  1  ");
				result = expression();
			}
			s = tokens[currentToken];
			if(s.equals(")")){
				if(currentToken < tokens.length-1)	currentToken++;
			}
		}
		return result;
	}

	/**
	 * Parse the remaining input as far as needed to get the next term
	 * "+" or "-"
	 * @return the (root node of) the resulting subtree
	 */
	private TreeNode term() {
		TreeNode result1 = factor();
		String s = tokens[currentToken];
		while(currentToken < tokens.length-1 && (s.equals("*") || s.equals("/"))){
			currentToken++;
			TreeNode result2 = factor();
			if(s.equals("*")){
				result1 = new MultiplicationTreeNode(result1, result2);
			}else{
				result1 = new DivisionTreeNode(result1, result2);
			}
			s = tokens[currentToken];
		}
		return result1;
	}

	/**
	 * Parse the remaining input as far as needed to get the next expression
	 * "*" or "/"
	 * @return the (root node of) the resulting subtree
	 */
	private TreeNode expression() {
		TreeNode result3 = term();
		System.out.println(currentToken);
		String s = tokens[currentToken];
		while(currentToken < tokens.length-1 && (s.equals("+") || s.equals("-"))){
			currentToken++;
			TreeNode result4 = term();
			if(s.equals("+")){
				result3 = new AdditionTreeNode(result3, result4);
			}else{
				result3 = new SubtractionTreeNode(result3, result4);
			}
			s = tokens[currentToken];
		}
		return result3;
	}
}
