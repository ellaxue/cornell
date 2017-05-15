package ChooseSelectionImp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * This class process where expression for finding union find elements
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class WhereProcessForUnionFind implements ExpressionVisitor {
	private UnionFind unionFind;
	private ArrayList<Expression> residualJoinExpression;
	private HashMap<String, Expression> residualSelectExpression;
	
	/**
	 * Constructor
	 * @param exp
	 */
	public WhereProcessForUnionFind(Expression exp){
		unionFind = new UnionFind();
		residualJoinExpression = new ArrayList<Expression>();
		residualSelectExpression= new HashMap<String, Expression>();
		if(exp != null) exp.accept(this);
	}
	
	/**
	 * 
	 * @return getResidualJoinExpression
	 */
	public ArrayList<Expression> getResidualJoinExpression(){
		return this.residualJoinExpression;
	}
	
	/**
	 * 
	 * @return unionFind
	 */
	public UnionFind getUnionFindResult(){
		return this.unionFind;
	}
	
	/**
	 * 
	 * @return residualSelectExpression
	 */
	public HashMap<String, Expression> getResidualSelectExpression(){
		return residualSelectExpression;
	}
	
	/**
	 * Expression visitor for finding union find elements
	 */
	@Override
	public void visit(EqualsTo arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		
		if(left instanceof Column && right instanceof Column){
			unionFind.mergerElements((Column)left, (Column)right);
		}
		else if(left instanceof Column && right instanceof LongValue){
			Element element = unionFind.findElement((Column)left);
			element.setNumericConstarints(((LongValue)right).toLong());
		}
		else if(right instanceof Column && left instanceof LongValue){
			Element element = unionFind.findElement((Column)right);
			element.setNumericConstarints(((LongValue)left).toLong());
		}
		else{
			//residual comparison
			residualJoinExpression.add(arg0);
		}
	}
	
	/**
	 * Expression visitor for finding union find elements
	 */
	@Override
	public void visit(GreaterThan arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		
		if(left instanceof Column && right instanceof LongValue){
			Element element = unionFind.findElement((Column)left);
			element.setLowerBound(((LongValue)right).toLong()+1);
		}
		else if(right instanceof Column && left instanceof LongValue){
			Element element = unionFind.findElement((Column)right);
			element.setLowerBound(((LongValue)left).toLong()+1);
		}
		else{
			//residual comparison
			residualJoinExpression.add(arg0);
		}
	}

	/**
	 * Expression visitor for finding union find elements
	 */
	@Override
	public void visit(GreaterThanEquals arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		if(left instanceof Column && right instanceof LongValue){
			Element element = unionFind.findElement((Column)left);
			element.setLowerBound(((LongValue)right).toLong());
		}
		else if(right instanceof Column && left instanceof LongValue){
			Element element = unionFind.findElement((Column)right);
			element.setLowerBound(((LongValue)left).toLong());
		}
		else{
			//residual comparison
			residualJoinExpression.add(arg0);
		}
	}

	/**
	 * Expression visitor for finding union find elements
	 */
	@Override
	public void visit(MinorThan arg0) {
		// TODO Auto-generated method stub
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		if(left instanceof Column && right instanceof LongValue){
			Element element = unionFind.findElement((Column)left);
			element.setUpperBound(((LongValue)right).toLong()-1);
		}
		else if(right instanceof Column && left instanceof LongValue){
			Element element = unionFind.findElement((Column)right);
			element.setLowerBound(((LongValue)left).toLong()-1);
		}
		else{
			//residual comparison
			residualJoinExpression.add(arg0);
		}
	}

	/**
	 * Expression visitor for finding union find elements
	 */
	@Override
	public void visit(MinorThanEquals arg0) {
		// TODO Auto-generated method stub
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		if(left instanceof Column && right instanceof LongValue){
			Element element = unionFind.findElement((Column)left);
			element.setUpperBound(((LongValue)right).toLong());
		}
		else if(right instanceof Column && left instanceof LongValue){
			Element element = unionFind.findElement((Column)right);
			element.setLowerBound(((LongValue)left).toLong());
		}
		else{
			//residual comparison
			residualJoinExpression.add(arg0);
		}
	}
	
	/**
	 * Expression visitor for finding union find elements
	 */
	@Override
	public void visit(AndExpression arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		left.accept(this);
		right.accept(this);	
	}

	/**
	 * Expression visitor for finding union find elements
	 */
	@Override
	public void visit(NotEqualsTo arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		if(left instanceof Column && right instanceof Column){
			residualJoinExpression.add(arg0);
		}
		else if(left instanceof Column && right instanceof LongValue){
			String tableName = ((Column)left).getTable().getName();
			if(!residualSelectExpression.containsKey(tableName)){
				residualSelectExpression.put(tableName, arg0);
			}
			else{
				residualSelectExpression.put(tableName, new AndExpression(residualSelectExpression.get(tableName),arg0));
			}	
		}
		else if(right instanceof Column && left instanceof LongValue){
			String tableName = ((Column)right).getTable().getName();
			if(!residualSelectExpression.containsKey(tableName)){
				residualSelectExpression.put(tableName, arg0);
			}
			else{
				residualSelectExpression.put(tableName, new AndExpression(residualSelectExpression.get(tableName),arg0));
			}	
		}
	}

	@Override
	public void visit(Column arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LongValue arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void visit(OrExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Between arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CaseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhenClause arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExistsExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Concat arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void visit(NullValue arg0) {
		
	}
	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Addition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Division arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Multiplication arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Subtraction arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseOr arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseXor arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void visit(Function arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InverseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleValue arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimeValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimestampValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Parenthesis arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub
		
	}
}
