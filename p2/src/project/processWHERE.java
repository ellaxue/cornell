package project;
import java.util.ArrayList;
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
 * Visitor to separate the where condition into two kinds of conditions.
 * One is JoinCondition and the other is select condition. 
 * Build two hashmaps that pointing from tablename to the condition
 * which should be applied to the correspoding table 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */

public class processWHERE implements ExpressionVisitor {
	private HashMap<String, Expression> JoinEx = new HashMap<String, Expression>();
	private HashMap<String, Expression> SelectEx = new HashMap<String, Expression>();
	private ArrayList<String> JoinOrder;
	private HashMap<String, Boolean> UseSMJ = new HashMap<String,Boolean>();

	/**
	 * Constructor that recursively visit the expression
	 * @param the where expression needed to be separated
	 * @param the table list in the from clause from left to right 
	 */
	public processWHERE(Expression ex, ArrayList<String> tableList) {
		JoinOrder = tableList;
		if(ex!=null) ex.accept(this);
	}

	@Override
	public void visit(NullValue arg0) {

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
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		left.accept(this);
		right.accept(this);
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
	 * visit the and expression
	 * @param = expression
	 */
	@Override
	public void visit(EqualsTo arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String ltable = "";
		String rtable = "";
		if (left instanceof Column) {
			ltable = ((Column) left).getTable().getName();
		}
		if (right instanceof Column) {
			rtable = ((Column) right).getTable().getName();
		}
		// both are value. Make it into first table's selection conditon
		if (ltable=="" && rtable =="") {
			ltable=JoinOrder.get(0);
			if (!SelectEx.containsKey(ltable))
				SelectEx.put(ltable, arg0);
			else
				SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));}

		// one is value and another is column or two columns are the same
		else if (ltable == "" || rtable == "" || ltable.equals(rtable)) {
			if (ltable != "") {
				if (!SelectEx.containsKey(ltable))
					SelectEx.put(ltable, arg0);
				else
					SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));
			} else {
				if (!SelectEx.containsKey(rtable))
					SelectEx.put(rtable, arg0);
				else
					SelectEx.put(rtable, new AndExpression(SelectEx.get(rtable), arg0));
			}
		}

		// different columns(join condition)
		else {
			int index1 = JoinOrder.indexOf(ltable);
			int index2 = JoinOrder.indexOf(rtable);
			if (index1 > index2) {
				if (JoinEx.putIfAbsent(ltable, arg0) != null) {
					JoinEx.put(ltable, new AndExpression(JoinEx.get(ltable), arg0));
				}
				if(UseSMJ.get(ltable)!=false) UseSMJ.put(ltable, true);
			} else {
				if (JoinEx.putIfAbsent(rtable, arg0) != null) {
					JoinEx.put(rtable, new AndExpression(JoinEx.get(rtable), arg0));
				}
				if(UseSMJ.get(rtable)!=false) UseSMJ.put(rtable, true);
			}
		}
	}

	/**
	 * visit the and expression
	 * @param > expression
	 */
	@Override
	public void visit(GreaterThan arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String ltable = "";
		String rtable = "";
		if (left instanceof Column) {
			ltable = ((Column) left).getTable().getName();
		}
		if (right instanceof Column) {
			rtable = ((Column) right).getTable().getName();
		}
		// both are value. Make it into first table's selection conditon
		if (ltable=="" && rtable =="") {
			ltable=JoinOrder.get(0);
			if (!SelectEx.containsKey(ltable))
				SelectEx.put(ltable, arg0);
			else
				SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));}

		// one is value and another is column or two columns are the same
		else if (ltable == "" || rtable == "" || ltable.equals(rtable)) {
			if (ltable != "") {
				if (!SelectEx.containsKey(ltable))
					SelectEx.put(ltable, arg0);
				else
					SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));
			} else {
				if (!SelectEx.containsKey(rtable))
					SelectEx.put(rtable, arg0);
				else
					SelectEx.put(rtable, new AndExpression(SelectEx.get(rtable), arg0));
			}
		}

		// different columns(join condition)
		else {
			int index1 = JoinOrder.indexOf(ltable);
			int index2 = JoinOrder.indexOf(rtable);
			if (index1 > index2) {
				if (JoinEx.putIfAbsent(ltable, arg0) != null) {
					JoinEx.put(ltable, new AndExpression(JoinEx.get(ltable), arg0));
				}
				if(UseSMJ.get(ltable)!=false) UseSMJ.put(ltable, false);

			} else {
				if (JoinEx.putIfAbsent(rtable, arg0) != null) {
					JoinEx.put(rtable, new AndExpression(JoinEx.get(rtable), arg0));
				}
				if(UseSMJ.get(rtable)!=false) UseSMJ.put(rtable, false);

			}
		}
	}

	/**
	 * visit the and expression
	 * @param >= expression
	 */
	@Override
	public void visit(GreaterThanEquals arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String ltable = "";
		String rtable = "";
		if (left instanceof Column) {
			ltable = ((Column) left).getTable().getName();
		}
		if (right instanceof Column) {
			rtable = ((Column) right).getTable().getName();
		}
		// both are value. Make it into first table's selection conditon
		if (ltable=="" && rtable =="") {
			ltable=JoinOrder.get(0);
			if (!SelectEx.containsKey(ltable))
				SelectEx.put(ltable, arg0);
			else
				SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));}

		// one is value and another is column or two columns are the same
		else if (ltable == "" || rtable == "" || ltable.equals(rtable)) {
			if (ltable != "") {
				if (!SelectEx.containsKey(ltable))
					SelectEx.put(ltable, arg0);
				else
					SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));
			} else {
				if (!SelectEx.containsKey(rtable))
					SelectEx.put(rtable, arg0);
				else
					SelectEx.put(rtable, new AndExpression(SelectEx.get(rtable), arg0));
			}
		}

		// different columns(join condition)
		else {
			int index1 = JoinOrder.indexOf(ltable);
			int index2 = JoinOrder.indexOf(rtable);
			if (index1 > index2) {
				if (JoinEx.putIfAbsent(ltable, arg0) != null) {
					JoinEx.put(ltable, new AndExpression(JoinEx.get(ltable), arg0));
				}
				if(UseSMJ.get(ltable)!=false) UseSMJ.put(ltable, false);

			} else {
				if (JoinEx.putIfAbsent(rtable, arg0) != null) {
					JoinEx.put(rtable, new AndExpression(JoinEx.get(rtable), arg0));
				}
				if(UseSMJ.get(rtable)!=false) UseSMJ.put(rtable, false);

			}
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

	/**
	 * visit the and expression
	 * @param < expression
	 */
	@Override
	public void visit(MinorThan arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String ltable = "";
		String rtable = "";
		if (left instanceof Column) {
			ltable = ((Column) left).getTable().getName();
		}
		if (right instanceof Column) {
			rtable = ((Column) right).getTable().getName();
		}
		// both are value. Make it into first table's selection conditon
		if (ltable=="" && rtable =="") {
			ltable=JoinOrder.get(0);
			if (!SelectEx.containsKey(ltable))
				SelectEx.put(ltable, arg0);
			else
				SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));}

		// one is value and another is column or two columns are the same
		else if (ltable == "" || rtable == "" || ltable.equals(rtable)) {
			if (ltable != "") {
				if (!SelectEx.containsKey(ltable))
					SelectEx.put(ltable, arg0);
				else
					SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));
			} else {
				if (!SelectEx.containsKey(rtable))
					SelectEx.put(rtable, arg0);
				else
					SelectEx.put(rtable, new AndExpression(SelectEx.get(rtable), arg0));
			}
		}

		// different columns(join condition)
		else {
			int index1 = JoinOrder.indexOf(ltable);
			int index2 = JoinOrder.indexOf(rtable);
			if (index1 > index2) {
				if (JoinEx.putIfAbsent(ltable, arg0) != null) {
					JoinEx.put(ltable, new AndExpression(JoinEx.get(ltable), arg0));
				}
				if(UseSMJ.get(ltable)!=false) UseSMJ.put(ltable, false);

			} else {
				if (JoinEx.putIfAbsent(rtable, arg0) != null) {
					JoinEx.put(rtable, new AndExpression(JoinEx.get(rtable), arg0));
				}
				if(UseSMJ.get(rtable)!=false) UseSMJ.put(rtable, false);
			}
		}
	}

	/**
	 * visit the and expression
	 * @param <= expression
	 */
	@Override
	public void visit(MinorThanEquals arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String ltable = "";
		String rtable = "";
		if (left instanceof Column) {
			ltable = ((Column) left).getTable().getName();
		}
		if (right instanceof Column) {
			rtable = ((Column) right).getTable().getName();
		}
		// both are value. Make it into first table's selection conditon
		if (ltable=="" && rtable =="") {
			ltable=JoinOrder.get(0);
			if (!SelectEx.containsKey(ltable))
				SelectEx.put(ltable, arg0);
			else
				SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));}

		// one is value and another is column or two columns are the same
		else if (ltable == "" || rtable == "" || ltable.equals(rtable)) {
			if (ltable != "") {
				if (!SelectEx.containsKey(ltable))
					SelectEx.put(ltable, arg0);
				else
					SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));
			} else {
				if (!SelectEx.containsKey(rtable))
					SelectEx.put(rtable, arg0);
				else
					SelectEx.put(rtable, new AndExpression(SelectEx.get(rtable), arg0));
			}
		}

		// different columns(join condition)
		else {
			int index1 = JoinOrder.indexOf(ltable);
			int index2 = JoinOrder.indexOf(rtable);
			if (index1 > index2) {
				if (JoinEx.putIfAbsent(ltable, arg0) != null) {
					JoinEx.put(ltable, new AndExpression(JoinEx.get(ltable), arg0));
				}
				if(UseSMJ.get(ltable)!=false) UseSMJ.put(ltable, false);
			} else {
				if (JoinEx.putIfAbsent(rtable, arg0) != null) {
					JoinEx.put(rtable, new AndExpression(JoinEx.get(rtable), arg0));
				}
				if(UseSMJ.get(rtable)!=false) UseSMJ.put(rtable, false);
			}
		}
	}

	/**
	 * visit the and expression
	 * @param != expression
	 */
	@Override
	public void visit(NotEqualsTo arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		String ltable = "";
		String rtable = "";
		if (left instanceof Column) {
			ltable = ((Column) left).getTable().getName();
		}
		if (right instanceof Column) {
			rtable = ((Column) right).getTable().getName();
		}
		// both are value. Make it into first table's selection conditon
		if (ltable=="" && rtable =="") {
			ltable=JoinOrder.get(0);
			if (!SelectEx.containsKey(ltable))
				SelectEx.put(ltable, arg0);
			else
				SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));}

		// one is value and another is column or two columns are the same
		else if (ltable == "" || rtable == "" || ltable.equals(rtable)) {
			if (ltable != "") {
				if (!SelectEx.containsKey(ltable))
					SelectEx.put(ltable, arg0);
				else
					SelectEx.put(ltable, new AndExpression(SelectEx.get(ltable), arg0));
			} else {
				if (!SelectEx.containsKey(rtable))
					SelectEx.put(rtable, arg0);
				else
					SelectEx.put(rtable, new AndExpression(SelectEx.get(rtable), arg0));
			}
		}

		// different columns(join condition)
		else {
			int index1 = JoinOrder.indexOf(ltable);
			int index2 = JoinOrder.indexOf(rtable);
			if (index1 > index2) {
				if (JoinEx.putIfAbsent(ltable, arg0) != null) {
					JoinEx.put(ltable, new AndExpression(JoinEx.get(ltable), arg0));
				}
				if(UseSMJ.get(ltable)!=false) UseSMJ.put(ltable, false);
			} else {
				if (JoinEx.putIfAbsent(rtable, arg0) != null) {
					JoinEx.put(rtable, new AndExpression(JoinEx.get(rtable), arg0));
				}
				if(UseSMJ.get(rtable)!=false) UseSMJ.put(rtable, false);
			}
		}
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

	/**
	 * return the hashmap pointing from table to applied join expression
	 * @return hashmap
	 */
	public HashMap<String, Expression> getJoinEx() {
		return JoinEx;
	}

	/**
	 * return the hashmap pointing from table to applied select expression
	 * @return hashmap
	 */
	public HashMap<String, Expression> getSelectEx() {
		return SelectEx;
	}

	/**
	 * return the table list in the from clause 
	 * @return table list
	 */
	public ArrayList<String> getJoinOrder() {
		return JoinOrder;
	}

}
