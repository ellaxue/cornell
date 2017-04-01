/**
 * Visitor to evaluate the where condition for a tuple. We use a stack to
 * recursively evaluate the expression.
 * For basic comparing condition, if it's evaluated to be true, we push a
 * true boolean into the stack. if not, push a false.
 * For And expression, visit the left expression and the right expression. 
 * Then pop the stack twice to get results of the two expression.
 * If both true, then push a true boolean. Else, push a false. 
 * In the end, pop the final result and store it.
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */

package project;
import java.util.Stack;

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


public class conditionEvaluator implements ExpressionVisitor {
	private Tuple tuple;
	private boolean result=false;
	private Stack<Boolean> stack=new Stack<Boolean>();


	/**
	 * Constructor of the conditionEvaluator. Recursively evaluate the 
	 * expression and pop the result.
	 * @param tuple to be judged
	 * @param condition needed to be evaluated for the tuple
	 */
	public conditionEvaluator(Tuple tu,Expression ex) {
		tuple=tu;
		ex.accept(this);
		result= stack.pop();
	}

	@Override
	public void visit(NullValue arg0) {
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
	public void visit(LongValue arg0) {
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

	/**
	 * visit the and expression
	 * @param and expression
	 */
	@Override
	public void visit(AndExpression arg0) {
		Expression exleft=arg0.getLeftExpression();
		Expression exright=arg0.getRightExpression();
		exleft.accept(this);
		exright.accept(this);
		boolean a= stack.pop();
		boolean b= stack.pop();
		if (a&&b) stack.push(true);
		else stack.push(false);
	}

	@Override
	public void visit(OrExpression arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Between arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * visit the A=B expression. If either side is a column, find the
	 * value corresponding to that volume. Compare the both values.
	 * @param = expression
	 */
	@Override
	public void visit(EqualsTo arg0) {
		Expression left= arg0.getLeftExpression();
		Expression right=arg0.getRightExpression();
		long l; long r; 
		if (left instanceof Column) {
			int indexl=-1;
			String tablename= ((Column)left).getTable().getName();
			String columnname=((Column)left).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexl=tuple.getSchemaList().indexOf(sp);}
			}
			l=Long.parseLong(((tuple.getTuple())[indexl]));
		}		
		else {l=((LongValue)left).getValue();}

		if (right instanceof Column) {
			int indexr=-1;
			String tablename= ((Column)right).getTable().getName();
			String columnname=((Column)right).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexr=tuple.getSchemaList().indexOf(sp);}
			}
			r=Long.parseLong(((tuple.getTuple())[indexr]));
		}
		else {r=((LongValue)right).getValue();}
		if (l==r) {stack.push(true);}
		else {stack.push(false);}		
	}

	/**
	 * visit the A>=B expression. If either side is a column, find the
	 * value corresponding to that volume. Compare the both values.
	 * @param > expression
	 */
	@Override
	public void visit(GreaterThan arg0) {
		Expression left= arg0.getLeftExpression();
		Expression right=arg0.getRightExpression();
		long l; long r; 
		if (left instanceof Column) {
			int indexl=-1;
			String tablename= ((Column)left).getTable().getName();
			String columnname=((Column)left).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexl=tuple.getSchemaList().indexOf(sp);}
			}
			l=Long.parseLong(((tuple.getTuple())[indexl]));
		}		
		else {l=((LongValue)left).getValue();}

		if (right instanceof Column) {
			int indexr=-1;
			String tablename= ((Column)right).getTable().getName();
			String columnname=((Column)right).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexr=tuple.getSchemaList().indexOf(sp);}
			}
			r=Long.parseLong(((tuple.getTuple())[indexr]));
		}
		else {r=((LongValue)right).getValue();}
		if (l>r) {stack.push(true);}
		else {stack.push(false);}		
	}		

	/**
	 * visit the A>B expression. If either side is a column, find the
	 * value corresponding to that volume. Compare the both values.
	 * @param >= expression
	 */
	@Override
	public void visit(GreaterThanEquals arg0) {
		Expression left= arg0.getLeftExpression();
		Expression right=arg0.getRightExpression();
		long l; long r; 
		if (left instanceof Column) {
			int indexl=-1;
			String tablename= ((Column)left).getTable().getName();
			String columnname=((Column)left).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexl=tuple.getSchemaList().indexOf(sp);}
			}
			l=Long.parseLong(((tuple.getTuple())[indexl]));
		}		
		else {l=((LongValue)left).getValue();}

		if (right instanceof Column) {
			int indexr=-1;
			String tablename= ((Column)right).getTable().getName();
			String columnname=((Column)right).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexr=tuple.getSchemaList().indexOf(sp);}
			}
			r=Long.parseLong(((tuple.getTuple())[indexr]));
		}
		else {r=((LongValue)right).getValue();}
		if (l>=r) {stack.push(true);}
		else {stack.push(false);}		
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
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * visit the A<B expression. If either side is a column, find the
	 * value corresponding to that volume. Compare the both values.
	 * @param < expression
	 */
	@Override
	public void visit(MinorThan arg0) {
		Expression left= arg0.getLeftExpression();
		Expression right=arg0.getRightExpression();
		long l; long r; 
		if (left instanceof Column) {
			int indexl=-1;
			String tablename= ((Column)left).getTable().getName();
			String columnname=((Column)left).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexl=tuple.getSchemaList().indexOf(sp);}
			}
			l=Long.parseLong(((tuple.getTuple())[indexl]));
		}		
		else {l=((LongValue)left).getValue();}

		if (right instanceof Column) {
			int indexr=-1;
			String tablename= ((Column)right).getTable().getName();
			String columnname=((Column)right).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexr=tuple.getSchemaList().indexOf(sp);}
			}
			r=Long.parseLong(((tuple.getTuple())[indexr]));
		}
		else {r=((LongValue)right).getValue();}
		if (l<r) {stack.push(true);}
		else {stack.push(false);}	
	}

	/**
	 * visit the A<=B expression. If either side is a column, find the
	 * value corresponding to that volume. Compare the both values.
	 * @param <= expression
	 */
	@Override
	public void visit(MinorThanEquals arg0) {
		Expression left= arg0.getLeftExpression();
		Expression right=arg0.getRightExpression();
		long l; long r; 
		if (left instanceof Column) {
			int indexl=-1;
			String tablename= ((Column)left).getTable().getName();
			String columnname=((Column)left).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexl=tuple.getSchemaList().indexOf(sp);}
			}
			l=Long.parseLong(((tuple.getTuple())[indexl]));
		}		
		else {l=((LongValue)left).getValue();}

		if (right instanceof Column) {
			int indexr=-1;
			String tablename= ((Column)right).getTable().getName();
			String columnname=((Column)right).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexr=tuple.getSchemaList().indexOf(sp);}
			}
			r=Long.parseLong(((tuple.getTuple())[indexr]));
		}
		else {r=((LongValue)right).getValue();}
		if (l<=r) {stack.push(true);}
		else {stack.push(false);}	
	}

	/**
	 * visit the A!=B expression. If either side is a column, find the
	 * value corresponding to that volume. Compare the both values.
	 * @param != expression
	 */
	@Override
	public void visit(NotEqualsTo arg0) {
		Expression left= arg0.getLeftExpression();
		Expression right=arg0.getRightExpression();
		long l; long r; 
		if (left instanceof Column) {
			int indexl=-1;
			String tablename= ((Column)left).getTable().getName();
			String columnname=((Column)left).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexl=tuple.getSchemaList().indexOf(sp);}
			}
			l=Long.parseLong(((tuple.getTuple())[indexl]));
		}		
		else {l=((LongValue)left).getValue();}

		if (right instanceof Column) {
			int indexr=-1;
			String tablename= ((Column)right).getTable().getName();
			String columnname=((Column)right).getColumnName();
			for(SchemaPair sp:tuple.getSchemaList()) {
				if (sp.equalsTo(tablename,columnname)){
					indexr=tuple.getSchemaList().indexOf(sp);}
			}
			r=Long.parseLong(((tuple.getTuple())[indexr]));
		}
		else {r=((LongValue)right).getValue();}
		if (l!=r) {stack.push(true);}
		else {stack.push(false);}	
	}

	@Override
	public void visit(Column arg0) {
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

	public boolean getResult() {
		return result;
	}

}
