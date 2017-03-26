package project;

import operator.*;
import java.io.*;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.expression.*;
import java.util.*;

/**
 * Main program to get the input, build the query plan tree and dump the output.
 * We first parse the query and extract various elements in the select body. We
 * use nested if clause to determine what operators we need and build the query
 * tree. For the join, we use a left deep join tree and always evaluate
 * selection condition first and then join condition.
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
public class QueryPlan {
	private static int queryCount = 1;
	private static Distinct distinct;
	private static ArrayList<SchemaPair> schema_pair_order;
	private static ArrayList<SchemaPair> schema_pair;

	/**
	 * count the query completed
	 */
	public static void nextQuery() {
		queryCount++;
	}

	/**
	 * get the query number being dealt with
	 * 
	 * @return query number
	 */
	public static int getCount() {
		return queryCount;
	}

	/**
	 * main program to parse the query, build the query plan and output the
	 * result
	 * 
	 * @param input
	 *            directory and out put directory
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		catalog cl = catalog.getInstance();

		cl.setOutputdir(args[1]);
		String inputdir = args[0];
		String schemadr = args[0] + File.separator + "db" + File.separator + "schema.txt";
		String database = args[0] + File.separator + "db" + File.separator + "data";

		// store database information
		BufferedReader schemaReader = new BufferedReader(new FileReader(schemadr));
		String line = schemaReader.readLine();
		while (line != null) {
			String tableName = line.substring(0, line.indexOf(' '));
			ArrayList<String> schema = new ArrayList<String>();
			String[] schemaSt = line.substring(line.indexOf(' ') + 1).split(" ");
			for (String s : schemaSt) {
				schema.add(s);
			}
			cl.storeTableInfo(tableName, database + File.separator + tableName, schema);
			line = schemaReader.readLine();
		}
		schemaReader.close();

		// parse the query and output results
		try {
			CCJSqlParser parser = new CCJSqlParser(new FileReader(inputdir + File.separator + "queries.sql"));
			Statement statement;
			while ((statement = parser.Statement()) != null) {
				System.out.println("Read statement: " + statement);
				Select select = (Select) statement;
				System.out.println("Select body is " + select);

				// store alias information
				Table firstTable = ((Table) (((PlainSelect) select.getSelectBody()).getFromItem()));
				if (firstTable.getAlias() != null)
					cl.setUseAlias(true);
				cl.storeAlias(firstTable.getAlias(), firstTable.getName());
				@SuppressWarnings("unchecked")
				List<Join> join = ((PlainSelect) select.getSelectBody()).getJoins();
				if (join != null) {
					for (Join ta : join) {
						Table table = (Table) ta.getRightItem();
						if (table.getAlias() != null) {
							cl.storeAlias(table.getAlias(), table.getName());
						}
					}
				}

				// separate where clause into joinExpression and
				// selectExpression
				Expression ex = ((PlainSelect) select.getSelectBody()).getWhere();
				HashMap<String, Expression> JoinEx;
				HashMap<String, Expression> SelectEx;

				ArrayList<String> tableList = new ArrayList<String>();
				if (!cl.UseAlias()) {
					tableList.add(firstTable.getName());
				} else {
					tableList.add(firstTable.getAlias());
				}
				if (join != null) {
					for (Join j : join) {
						String name = ((Table) j.getRightItem()).getName();
						if (cl.UseAlias()) {
							name = ((Table) j.getRightItem()).getAlias();
						}
						tableList.add(name);
					}
					processWHERE pw = new processWHERE(ex, tableList);
					JoinEx = pw.getJoinEx();
					SelectEx = pw.getSelectEx();
				} else {
					JoinEx = null;
					SelectEx = null;
				}

				// get selectItems from select clause
				@SuppressWarnings("unchecked")
				List<SelectItem> selectItem = ((PlainSelect) select.getSelectBody()).getSelectItems();
				schema_pair = new ArrayList<SchemaPair>();
				for (SelectItem s : selectItem) {
					if (s instanceof SelectExpressionItem) {
						Column c = (Column) ((SelectExpressionItem) s).getExpression();
						String tablename = c.getTable().getName();
						String columnName = c.getColumnName();
						schema_pair.add(new SchemaPair(tablename, columnName));
					}
				}

				// get order by Items from order by
				@SuppressWarnings("unchecked")
				List<OrderByElement> ex_order = ((PlainSelect) select.getSelectBody()).getOrderByElements();
				schema_pair_order = new ArrayList<SchemaPair>();
				if (ex_order != null && ex_order.size() > 0) {
					for (OrderByElement o : ex_order) {
						if (o instanceof OrderByElement) {
							Column col = (Column) ((OrderByElement) o).getExpression();
							String table_name = col.getTable().getName();
							String column_name = col.getColumnName();
							schema_pair_order.add(new SchemaPair(table_name, column_name));
						}
					}
				}

				// whether distinct
				distinct = ((PlainSelect) select.getSelectBody()).getDistinct();

				// Condition with no join
				if (join == null) {
					ScanOperator sc;
					if (cl.UseAlias()) {
						sc = new ScanOperator(firstTable.getAlias());
					} else {
						sc = new ScanOperator(firstTable.getName());
					}

					// without selection
					if (ex == null) {
						QueryPlan.Projection_Sort_Distinct(selectItem, sc);
						// with selection
					} else {
						SelectOperator se = new SelectOperator(sc, ex);
						QueryPlan.Projection_Sort_Distinct(selectItem, se);
					}
				}

				// Condition with join
				else {
					Table secondTable = (Table) join.get(0).getRightItem();
					Operator leftchild;
					Operator rightchild;
					String firstTableName;
					String secondTableName;
					
					// join first two tables
					if (cl.UseAlias()) {
						firstTableName = firstTable.getAlias();
						secondTableName = secondTable.getAlias();
					} else {
						firstTableName = firstTable.getName();
						secondTableName = secondTable.getName();
					}
					if (SelectEx.get(firstTableName) != null) {
						leftchild = new SelectOperator(new ScanOperator(firstTableName), SelectEx.get(firstTableName));
					} else {

						leftchild = new ScanOperator(firstTableName);
					}
					if (SelectEx.get(secondTableName) != null) {

						rightchild = new SelectOperator(new ScanOperator(secondTableName),
								SelectEx.get(secondTableName));
					} else {
						rightchild = new ScanOperator(secondTableName);
					}

					JoinOperator jo = new JoinOperator(leftchild, rightchild, JoinEx.get(secondTableName));
					// join other tables and make selections
					for (Join j : join) {
						if (j != join.get(0)) {
							Table joinTable = (Table) j.getRightItem();
							String joinTableName;
							if (cl.UseAlias()) {
								joinTableName = joinTable.getAlias();
							} else {
								joinTableName = joinTable.getName();
							}
							if (SelectEx.get(joinTableName) != null) {
								rightchild = new SelectOperator(new ScanOperator(joinTableName),
										SelectEx.get(joinTableName));
							} else {
								rightchild = new ScanOperator(joinTableName);
							}
							jo = new JoinOperator(jo, rightchild, JoinEx.get(joinTableName));
						}
					}
					QueryPlan.Projection_Sort_Distinct(selectItem, jo);
				}
				cl.setUseAlias(false);
			}
		} catch (
		Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}

	}

	/**
	 * build the query plan for the projection, sort and distinct
	 */
	public static void Projection_Sort_Distinct(List<SelectItem> selectItems, Operator sc) throws Exception {
		// without projection
		if (selectItems.get(0) instanceof AllColumns) {
			// with order by
			if (schema_pair_order != null && schema_pair_order.size() > 0) {
				SortOperator so = new SortOperator(sc, schema_pair_order);
				// with distinct
				if (distinct != null) {
					DuplicateEliminationOperator du = new DuplicateEliminationOperator(so);
					du.dump();
					// without distinct
				} else {
					so.dump();
				}
				// without order by
			} else {
				// with distinct
				if (distinct != null) {
					SortOperator so = new SortOperator(sc, null);
					DuplicateEliminationOperator du = new DuplicateEliminationOperator(so);
					du.dump();
					// without distinct
				} else {
					sc.dump();
				}
			}
		}
		// with projection
		else {
			ProjectOperator pr = new ProjectOperator(sc, schema_pair);
			// with order by
			if (schema_pair_order != null && schema_pair_order.size() > 0) {
				SortOperator so = new SortOperator(pr, schema_pair_order);
				// with distinct
				if (distinct != null) {
					DuplicateEliminationOperator du = new DuplicateEliminationOperator(so, schema_pair);
					du.dump();
					// without distinct
				} else {
					so.dump();
				}
			}
			// without order by
			else {
				// with distinct
				if (distinct != null) {
					SortOperator so = new SortOperator(pr, schema_pair);
					DuplicateEliminationOperator du = new DuplicateEliminationOperator(so, schema_pair);
					du.dump();
					// withtout distinct
				} else {
					pr.dump();
				}
			}
		}
	}
}
