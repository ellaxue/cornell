package project2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
 *  WhereExpressionEvaluator.java
 *  This class find the join condition and selection condition for each table
 *  Created on: 03/06/2017
 *      Author: Ella Xue
 */
public class WhereExpressionEvaluator implements ExpressionVisitor {

	HashMap<String, HashMap<String, Integer>> schema;
	Tuple tuple;
	private Column leftColumn;
	private Column rightColumn;
	HashMap<String, Expression> joinCondition;
	HashMap<String, ArrayList<Expression>> selectCondition;
	
	/**
	 * Constructor 
	 */
	public WhereExpressionEvaluator(){
		joinCondition = new HashMap<String, Expression>();
		selectCondition = new HashMap<String, ArrayList<Expression>>();
	}
	
	/**
	 * @return the join condition for some tables
	 */
	public HashMap<String, Expression>  getJoinCondition(){
		return joinCondition;
	}
	
	/**
	 * @return the selection condition for some tables
	 */
	public HashMap<String, ArrayList<Expression>> getSelectCondition(){
		return selectCondition;
	}
	
	@Override
	public void visit(LongValue longValue) {

	}
	/**
	 *  Get the conjunction expression
	 */
	@Override
	public void visit(AndExpression andExpression) {
		Expression leftExp = andExpression.getLeftExpression();
		Expression rightExp = andExpression.getRightExpression();
		
		leftExp.accept(this);
		rightExp.accept(this);		
	}
	
	/**
	 *  Equal expression of two table's specified columns
	 */
	@Override
	public void visit(EqualsTo equalsTo) {
		// TODO Auto-generated method stub
		Expression leftExp = equalsTo.getLeftExpression();
		Expression rightExp = equalsTo.getRightExpression();
		String tableName = "";
		if(leftExp instanceof Column && rightExp instanceof Column
				&& !((Column) leftExp).getTable().getName().equals(((Column)rightExp).getTable().getName())){
			leftColumn = (Column)leftExp;
			rightColumn = (Column)rightExp;
			
			//join table name combined A+B
			joinCondition.put(leftColumn.getTable().getName()+rightColumn.getTable().getName(),equalsTo);
			joinCondition.put(rightColumn.getTable().getName()+leftColumn.getTable().getName(),equalsTo);
		}
		else{ 
			if(leftExp instanceof Column){
				tableName = ((Column)leftExp).getTable().getName();
			}
			else if(rightExp instanceof Column){
				tableName = ((Column)rightExp).getTable().getName();
			}
			ArrayList<Expression> list;
			if(selectCondition.containsKey(tableName)){
				list = selectCondition.get(tableName);
			}
			else{
				list = new ArrayList<Expression>();
			}
			list.add(equalsTo);
			selectCondition.put( tableName, list);
		}
	}

	/**
	 * greaterThan expression of two table's specified columns
	 */
	@Override
	public void visit(GreaterThan greaterThan) {
		// TODO Auto-generated method stub
		Expression leftExp = greaterThan.getLeftExpression();
		Expression rightExp = greaterThan.getRightExpression();
		String tableName = "";
		if(leftExp instanceof Column && rightExp instanceof Column
				&& !((Column) leftExp).getTable().getName().equals(((Column)rightExp).getTable().getName())){
			leftColumn = (Column)leftExp;
			rightColumn = (Column)rightExp;
			
			//join table name combined A+B
			joinCondition.put(leftColumn.getTable().getName()+rightColumn.getTable().getName(),greaterThan);
			joinCondition.put(rightColumn.getTable().getName()+leftColumn.getTable().getName(),greaterThan);
		}
		else{ 
			if(leftExp instanceof Column){
				tableName = ((Column)leftExp).getTable().getName();
			}
			else if(rightExp instanceof Column){
				tableName = ((Column)rightExp).getTable().getName();
			}
			ArrayList<Expression> list;
			if(selectCondition.containsKey(tableName)){
				list = selectCondition.get(tableName);
			}
			else{
				list = new ArrayList<Expression>();
			}
			list.add(greaterThan);
			selectCondition.put( tableName, list);
		}
	}

	/**
	 * greaterThanEquals expression of two table's specified columns
	 */
	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		Expression leftExp = greaterThanEquals.getLeftExpression();
		Expression rightExp = greaterThanEquals.getRightExpression();
		String tableName = "";
		if(leftExp instanceof Column && rightExp instanceof Column 
				&& !((Column) leftExp).getTable().getName().equals(((Column)rightExp).getTable().getName())){
			leftColumn = (Column)leftExp;
			rightColumn = (Column)rightExp;
			
			//join table name combined A+B
			joinCondition.put(leftColumn.getTable().getName()+rightColumn.getTable().getName(),greaterThanEquals);
			joinCondition.put(rightColumn.getTable().getName()+leftColumn.getTable().getName(),greaterThanEquals);
		}
		else{
				if(leftExp instanceof Column){
					tableName = ((Column)leftExp).getTable().getName();
				}
				else if(rightExp instanceof Column){
					tableName = ((Column)rightExp).getTable().getName();
				}
				ArrayList<Expression> list;
				if(selectCondition.containsKey(tableName)){
					list = selectCondition.get(tableName);
				}
				else{
					list = new ArrayList<Expression>();
				}
				list.add(greaterThanEquals);
				selectCondition.put( tableName, list);
			}
	}


	/**
	 * minorThan expression of two table's specified columns
	 */
	@Override
	public void visit(MinorThan minorThan) {
		Expression leftExp = minorThan.getLeftExpression();
		Expression rightExp = minorThan.getRightExpression();
		String tableName = "";
		if(leftExp instanceof Column && rightExp instanceof Column
				&& !((Column) leftExp).getTable().getName().equals(((Column)rightExp).getTable().getName())){
			leftColumn = (Column)leftExp;
			rightColumn = (Column)rightExp;
			
			//join table name combined A+B = AB
			joinCondition.put(leftColumn.getTable().getName()+rightColumn.getTable().getName(),minorThan);
			joinCondition.put(rightColumn.getTable().getName()+leftColumn.getTable().getName(),minorThan);
		}
		else{
			if(leftExp instanceof Column){
				tableName = ((Column)leftExp).getTable().getName();
			}
			else if(rightExp instanceof Column){
				tableName = ((Column)rightExp).getTable().getName();
			}
			ArrayList<Expression> list;
			if(selectCondition.containsKey(tableName)){
				list = selectCondition.get(tableName);
			}
			else{
				list = new ArrayList<Expression>();
			}
			list.add(minorThan);
			selectCondition.put( tableName, list);
		}
	}
	
	/**
	 * minorThanEquals expression of two table's specified columns:
	 */
	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		Expression leftExp = minorThanEquals.getLeftExpression();
		Expression rightExp = minorThanEquals.getRightExpression();
		String tableName = "";
		if(leftExp instanceof Column && rightExp instanceof Column
				&& !((Column) leftExp).getTable().getName().equals(((Column)rightExp).getTable().getName())){
			leftColumn = (Column)leftExp;
			rightColumn = (Column)rightExp;
			
			//join table name combined A+B
			joinCondition.put(leftColumn.getTable().getName()+rightColumn.getTable().getName(),minorThanEquals);
			joinCondition.put(rightColumn.getTable().getName()+leftColumn.getTable().getName(),minorThanEquals);
		}
		else{
			if(leftExp instanceof Column){
				tableName = ((Column)leftExp).getTable().getName();
			}
			else if(rightExp instanceof Column){
				tableName = ((Column)rightExp).getTable().getName();
			}
			ArrayList<Expression> list;
			if(selectCondition.containsKey(tableName)){
				list = selectCondition.get(tableName);
			}
			else{
				list = new ArrayList<Expression>();
			}
			list.add(minorThanEquals);
			selectCondition.put( tableName, list);
		}
	}

	/**
	 * notEqualsTo expression of two table's specified columns::
	 */
	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		Expression leftExp = notEqualsTo.getLeftExpression();
		Expression rightExp = notEqualsTo.getRightExpression();
		String tableName = "";
		if(leftExp instanceof Column && rightExp instanceof Column
				&& !((Column) leftExp).getTable().getName().equals(((Column)rightExp).getTable().getName())){
			leftColumn = (Column)leftExp;
			rightColumn = (Column)rightExp;
			
			//join table name combined A+B
			joinCondition.put(leftColumn.getTable().getName()+rightColumn.getTable().getName(),notEqualsTo);
			joinCondition.put(rightColumn.getTable().getName()+leftColumn.getTable().getName(),notEqualsTo);
		}
		else{
			if(leftExp instanceof Column){
				tableName = ((Column)leftExp).getTable().getName();
			}
			else if(rightExp instanceof Column){
				tableName = ((Column)rightExp).getTable().getName();
			}
			ArrayList<Expression> list;
			if(selectCondition.containsKey(tableName)){
				list = selectCondition.get(tableName);
			}
			else{
				list = new ArrayList<Expression>();
			}
			list.add(notEqualsTo);
			selectCondition.put( tableName, list);
		}
	}

	/**
	 * TODO:
	 */
	@Override
	public void visit(Column tableColumn) {

	}
	

	@Override
	public void visit(SubSelect subSelect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CaseExpression caseExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhenClause whenClause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Concat concat) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Matches matches) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InExpression inExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LikeExpression likeExpression) {
	}
	
	@Override
	public void visit(OrExpression orExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Between between) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DateValue dateValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimeValue timeValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StringValue stringValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Addition addition) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Division division) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Multiplication multiplication) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Subtraction subtraction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NullValue nullValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Function function) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InverseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleValue doubleValue) {
		// TODO Auto-generated method stub
		
	}

}
