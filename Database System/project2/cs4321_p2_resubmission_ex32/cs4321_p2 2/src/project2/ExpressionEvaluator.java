package project2;
import java.util.HashMap;

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
 *  ExpressionEvaluator.java
 *  This class evaluate comparison expression of the columns
 *  Created on: 03/06/2017
 *      Author: Ella Xue (ex32)
 */
public class ExpressionEvaluator implements ExpressionVisitor{
	private boolean result;
	private int leftVal;
	private int rightVal;
	private String columnName;
	private long isLongValue;
	private String tableName;
	private int flag;
	HashMap<String, Column[]> schema =  ParserExample.schema;
	Tuple leftTuple;
	Tuple rightTuple;
	String rightTableName;
	String leftTableName;
	Tuple tuple;
	/**
	 *  Constructor for selection operator
	 * @param tuple
	 */
	public ExpressionEvaluator(Tuple tuple){
		this.tuple = tuple;
		leftVal = 0;
		rightVal = 0;
		columnName = "";
		isLongValue = 0;
		result = true;
		flag = 0;
	}
	/**
	 *  Constructor for join operator
	 * @param A joining table a
	 * @param B joining table b
	 * @param leftTableName name of table a
	 * @param rightTableName name of table b
	 */
	public ExpressionEvaluator(Tuple A, Tuple B ,String leftTableName, String rightTableName){
		leftTuple = A;
		rightTuple = B;
		leftVal = 0;
		rightVal = 0;
		columnName = "";
		isLongValue = 0;
		result = true;
		this.leftTableName = leftTableName;
		this.rightTableName = rightTableName;
		flag = 1;
	}

	/**
	 * @return the evaluation result
	 */
	public boolean result(){
		return result;
	}
	
	/** 
	 *  get the numerical value for a specific tuple at a specific column index
	 * @param colName the column name of the tuple
	 * @return the numerical value of the tuple at that column
	 */
	public int getNumericVal(String colName){
		Column[] columnNames = schema.get(tableName);
		int columnIndex = 0;
		for(int i = 0 ; i < columnNames.length; i++){
			if(columnNames[i].getColumnName().equals(colName)){
				columnIndex = i;
			}
		}
		int columnVal = Integer.parseInt(tuple.getData()[columnIndex]);
		return columnVal;	
	}
	
	/** 
	 *  get the numerical value for a specific tuple at a specific column index
	 */
	public int getNumericVal(){
		String theTableName = "";
		Tuple theTuple = null;
		if(tableName.equals(rightTableName)){
			theTableName = rightTableName;
			theTuple = rightTuple;
		}
		else{
			theTableName = leftTableName;
			theTuple = leftTuple;
		}
		
		Column[] columnNames = schema.get(theTableName);
		int columnIndex = 0;
		for(int i = 0 ; i < columnNames.length; i++){
			if(columnNames[i].getColumnName().equals(columnName) && columnNames[i].getTable().getName().equals(tableName)){
				columnIndex = i;
			}
		}
		int columnVal = Integer.parseInt(theTuple.getData()[columnIndex]);
		return columnVal;	
	}
	
	/**
	 * convert to long value type
	 */
	@Override
	public void visit(LongValue longValue) {
		isLongValue = longValue.toLong();
	}

	/**
	 * andExpression expression to be evaluated::::
	 */
	@Override
	public void visit(AndExpression andExpression) {
		Expression leftExp = andExpression.getLeftExpression();
		Expression rightExp = andExpression.getRightExpression();
		
		leftExp.accept(this);
		rightExp.accept(this);		
	}
	/**
	 * EqualsTo expression to be evaluated::::
	 */
	@Override
	public void visit(EqualsTo equalsTo) {
		// TODO Auto-generated method stub
		Expression leftExp = equalsTo.getLeftExpression();
		Expression rightExp = equalsTo.getRightExpression();
		
		leftExp.accept(this);
		if(leftExp instanceof Column){
			leftVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(leftExp instanceof LongValue){
			leftVal = (int)isLongValue;
		}
		rightExp.accept(this);
		if(rightExp instanceof Column){
			rightVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(rightExp instanceof LongValue){
			rightVal = (int)isLongValue;
		}
		result = (leftVal == rightVal) && (result == true);
	}

	/**
	 * greaterThan expression to be evaluated::::
	 */
	@Override
	public void visit(GreaterThan greaterThan) {
		// TODO Auto-generated method stub
		Expression leftExp = greaterThan.getLeftExpression();
		Expression rightExp = greaterThan.getRightExpression();
		
		leftExp.accept(this);
		if(leftExp instanceof Column){
			leftVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(leftExp instanceof LongValue){
			leftVal = (int)isLongValue;
		}
		rightExp.accept(this);
		if(rightExp instanceof Column){
			rightVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(rightExp instanceof LongValue){
			rightVal = (int)isLongValue;
		}
		result = (leftVal > rightVal) && (result == true);
	}

	/**
	 * greaterThanEquals expression to be evaluated:::
	 */
	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		Expression leftExp = greaterThanEquals.getLeftExpression();
		Expression rightExp = greaterThanEquals.getRightExpression();
		
		leftExp.accept(this);
		if(leftExp instanceof Column){
			leftVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(leftExp instanceof LongValue){
			leftVal = (int)isLongValue;
		}
		rightExp.accept(this);
		if(rightExp instanceof Column){
			rightVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(rightExp instanceof LongValue){
			rightVal = (int)isLongValue;
		}
		result = ((leftVal > rightVal) || (leftVal == rightVal)) && (result == true);
	}


	/**
	 *  MinorThan expression to be evaluated
	 */
	@Override
	public void visit(MinorThan minorThan) {
		
		Expression leftExp = minorThan.getLeftExpression();
		Expression rightExp = minorThan.getRightExpression();
		
		leftExp.accept(this);
		if(leftExp instanceof Column){
			leftVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(leftExp instanceof LongValue){
			leftVal = (int)isLongValue;
		}
		rightExp.accept(this);
		if(rightExp instanceof Column){
			rightVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(rightExp instanceof LongValue){
			rightVal = (int)isLongValue;
		}
		result = (leftVal < rightVal) && (result == true);
	}
	
	/**
	 * minorThanEquals expression to be evaluated:
	 */
	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		// TODO Auto-generated method stub
		Expression leftExp = minorThanEquals.getLeftExpression();
		Expression rightExp = minorThanEquals.getRightExpression();
		
		leftExp.accept(this);
		if(leftExp instanceof Column){
			leftVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(leftExp instanceof LongValue){
			leftVal = (int)isLongValue;
		}
		rightExp.accept(this);
		if(rightExp instanceof Column){
			rightVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(rightExp instanceof LongValue){
			rightVal = (int)isLongValue;
		}
		result = ((leftVal < rightVal) || (leftVal == rightVal)) && (result == true);
	}

	/**
	 * notEqualsTo expression to be evaluated::
	 */
	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		// TODO Auto-generated method stub
		Expression leftExp = notEqualsTo.getLeftExpression();
		Expression rightExp = notEqualsTo.getRightExpression();
		
		leftExp.accept(this);
		if(leftExp instanceof Column){
			leftVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(leftExp instanceof LongValue){
			leftVal = (int)isLongValue;
		}
		rightExp.accept(this);
		if(rightExp instanceof Column){
			rightVal = (flag == 0) ? getNumericVal(columnName) :getNumericVal();
		}
		else if(rightExp instanceof LongValue){
			rightVal = (int)isLongValue;
		}
		result = (leftVal != rightVal) && (result == true);		
	}

	/**
	 * Column to be evaluated:
	 */
	@Override
	public void visit(Column tableColumn) {
		columnName = tableColumn.getColumnName();
		tableName = tableColumn.getTable().getName();
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
