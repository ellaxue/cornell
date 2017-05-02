package project;

import java.util.HashMap;

import BPlusTree.IndexInfo;
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
 * Separate condition of indexscan and fullscan
 * @author anorakj
 *
 */
public class IndexScanConditionExtration implements ExpressionVisitor {
	
	private Integer lowKey=null;
	private Integer highKey=null;
	private Expression FullScan=null;
	private String indexColumn=null;

	
	public IndexScanConditionExtration(Expression ex, IndexInfo index) throws Exception {
		indexColumn = index.getIndexCol();
		if (ex!=null) ex.accept(this);
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

	@Override
	public void visit(AndExpression arg0) {
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
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
	public void visit(EqualsTo arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String lcolumn = null;
		String rcolumn = null;
		Integer leftValue= null;
		Integer rightValue= null;
		if (left instanceof LongValue) {
			leftValue= (int)((LongValue) left).getValue();
		}
		else lcolumn=((Column)left).getColumnName();
		if (right instanceof LongValue) {
			rightValue= (int)((LongValue) right).getValue();
		}
		else rcolumn=((Column)right).getColumnName();
		if((lcolumn!=null && rcolumn!=null) || (!indexColumn.equals(lcolumn) && !indexColumn.equals(rcolumn))) {
			if (getFullScan()==null) setFullScan(arg0);
			else setFullScan(new AndExpression(arg0, getFullScan()));
		}
		else {
			if (leftValue!=null) {
				setLowKey(leftValue); setHighKey(leftValue);
			}
			else {
				setLowKey(rightValue); setHighKey(rightValue);
			}
		}
	}

	@Override
	public void visit(GreaterThan arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String lcolumn = null;
		String rcolumn = null;
		Integer leftValue= null;
		Integer rightValue= null;
		if (left instanceof LongValue) {
			leftValue= (int)((LongValue) left).getValue();
		}
		else lcolumn=((Column)left).getColumnName();
		if (right instanceof LongValue) {
			rightValue= (int)((LongValue) right).getValue();
		}
		else rcolumn=((Column)right).getColumnName();
		if((lcolumn!=null && rcolumn!=null) || (!indexColumn.equals(lcolumn) && !indexColumn.equals(rcolumn))) {
			if (getFullScan()==null) setFullScan(arg0);
			else setFullScan(new AndExpression(arg0, getFullScan()));
		}
		else {
			if (leftValue!=null) {
				if (getHighKey()==null) setHighKey(leftValue-1);
				else if (getHighKey()>leftValue-1) setHighKey(leftValue-1);
			}
			else {
				if (getLowKey()==null) setLowKey(rightValue+1);
				else if (getLowKey()<rightValue+1) setLowKey(rightValue+1);}
		}		
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String lcolumn = null;
		String rcolumn = null;
		Integer leftValue= null;
		Integer rightValue= null;
		if (left instanceof LongValue) {
			leftValue= (int)((LongValue) left).getValue();
		}
		else lcolumn=((Column)left).getColumnName();
		if (right instanceof LongValue) {
			rightValue= (int)((LongValue) right).getValue();
		}
		else rcolumn=((Column)right).getColumnName();
		if((lcolumn!=null && rcolumn!=null) || (!indexColumn.equals(lcolumn) && !indexColumn.equals(rcolumn))) {
			if (getFullScan()==null) setFullScan(arg0);
			else setFullScan(new AndExpression(arg0, getFullScan()));
		}
		else {
			if (leftValue!=null) {
				if (getHighKey()==null) setHighKey(leftValue);
				else if (getHighKey()>leftValue) setHighKey(leftValue);
			}
			else {
				if (getLowKey()==null) setLowKey(rightValue);
				else if (getLowKey()<rightValue) setLowKey(rightValue);}
		}			
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

	@Override
	public void visit(MinorThan arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String lcolumn = null;
		String rcolumn = null;
		Integer leftValue= null;
		Integer rightValue= null;
		if (left instanceof LongValue) {
			leftValue= (int)((LongValue) left).getValue();
		}
		else lcolumn=((Column)left).getColumnName();
		if (right instanceof LongValue) {
			rightValue= (int)((LongValue) right).getValue();
		}
		else rcolumn=((Column)right).getColumnName();
		if((lcolumn!=null && rcolumn!=null) || (!indexColumn.equals(lcolumn) && !indexColumn.equals(rcolumn))) {
			if (getFullScan()==null) setFullScan(arg0);
			else setFullScan(new AndExpression(arg0, getFullScan()));
		}
		else {
			if (leftValue!=null) {
				if (getLowKey()==null) setLowKey(leftValue+1);
				else if (getLowKey()<leftValue+1) setLowKey(leftValue+1);
			}
			else {
				if (getHighKey()==null) setHighKey(rightValue-1);
				else if (getHighKey()>rightValue-1) setHighKey(rightValue-1);}
		}			
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String lcolumn = null;
		String rcolumn = null;
		Integer leftValue= null;
		Integer rightValue= null;
		if (left instanceof LongValue) {
			leftValue= (int)((LongValue) left).getValue();
		}
		else lcolumn=((Column)left).getColumnName();
		if (right instanceof LongValue) {
			rightValue= (int)((LongValue) right).getValue();
		}
		else rcolumn=((Column)right).getColumnName();
		if((lcolumn!=null && rcolumn!=null) || (!indexColumn.equals(lcolumn) && !indexColumn.equals(rcolumn))) {
			if (getFullScan()==null) setFullScan(arg0);
			else setFullScan(new AndExpression(arg0, getFullScan()));
		}
		else {
			if (leftValue!=null) {
				if (getLowKey()==null) setLowKey(leftValue);
				else if (getLowKey()<leftValue) setLowKey(leftValue);
			}
			else {
				if (getHighKey()==null) setHighKey(rightValue);
				else if (getHighKey()>rightValue) setHighKey(rightValue);}
		}			
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		if (getFullScan()==null) setFullScan(arg0);
		else setFullScan(new AndExpression(arg0, getFullScan()));	
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
	public Integer getLowKey() {
		return lowKey;
	}
	public void setLowKey(Integer lowKey) {
		this.lowKey = lowKey;
	}
	public Integer getHighKey() {
		return highKey;
	}
	public void setHighKey(Integer highKey) {
		this.highKey = highKey;
	}
	public Expression getFullScan() {
		return FullScan;
	}
	public void setFullScan(Expression fullScan) {
		FullScan = fullScan;
	}

}
