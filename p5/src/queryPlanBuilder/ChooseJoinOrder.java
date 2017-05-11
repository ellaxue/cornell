package queryPlanBuilder;

import java.awt.List;
import java.lang.reflect.Array;
import java.security.KeyStore.PrivateKeyEntry;
import java.time.chrono.MinguoChronology;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.swing.tree.TreeNode;

import ChooseSelectionImp.Element;
import ChooseSelectionImp.ExtractColumnFromExpression;
import ChooseSelectionImp.RelationInfo;
import logicalOperator.LogicalJoinOperator;
import logicalOperator.LogicalSelectOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.schema.Column;
import physicalOperator.JoinOperator;
import physicalOperator.SelectOperator;
import project.catalog;

public class ChooseJoinOrder {

	private catalog cl = catalog.getInstance();
	private HashMap<String, RelationInfo> relationInfoMap=cl.getRelationMap();
	private ArrayList<HashMap<String, Integer>> vValue=  new ArrayList<>();
	private ArrayList<HashMap<String, Integer>> TableSize=  new ArrayList<>();
	private HashMap<String, Double> SingleTableReductionFactor= new HashMap<>(); // total ReductionFactor of single table
	private ArrayList<HashMap<String, Integer>> cost=  new ArrayList<>();
	private HashMap<String, ArrayList<String>> TableColumns = new HashMap<>();
	private String FinalOrder;


	public ChooseJoinOrder(LogicalJoinOperator joinOperator,LogicalPlanBuilder lPlanBuilder,Expression ex) {
		ArrayList<logicalOperator.TreeNode> joinchild= joinOperator.getChildren();
		EquijoinCondition equiJoin= new EquijoinCondition(ex);
		HashMap<String,ArrayList<String>> equalColumn= equiJoin.equiColumn;
		System.out.println("equalColumn"+  equalColumn);

		HashMap<String, Integer> oneTableVvalue=new HashMap<>();
		HashMap<String, Integer> oneTableSize=new HashMap<>();
		HashMap <String,Integer>oneTableCost= new HashMap<>();
		for (int i=0;i<joinchild.size();i++) {
			LogicalSelectOperator table= (LogicalSelectOperator)joinchild.get(i);

			ArrayList<String> columns= new ArrayList<String>(Arrays.asList(relationInfoMap.get(table.getTable().getWholeTableName()).getAttributeNames()));
			TableColumns.put(i+"", columns);
			// if the table is a base table
			if (table.getExpressoin()==null) {
				String tableName = table.getTable().getWholeTableName();
				RelationInfo stats= relationInfoMap.get(tableName);
				oneTableSize.put(i+"", stats.getTotalTupleInRelation());
				oneTableCost.put(i+"", 0);
				for (String s:stats.getAttributeNames()) {
					oneTableVvalue.put(i+s, stats.getMaxValOfAttr(s)-stats.getMinValOfAttr(s)+1);
				}
			}

			// if the table is a selection on a base table
			else {
				Double totalReductionFactor=1.0;
				String tableName = table.getTable().getWholeTableName();
				RelationInfo stats= relationInfoMap.get(tableName);
				ExtractColumnFromExpression ext = new ExtractColumnFromExpression();
				table.getExpressoin().accept(ext);
				HashSet<Column> colSet = ext.getColumnResult();
				for(Column col: colSet){
					String colName = col.getColumnName();
					Element element = LogicalPlanBuilder.unionFindConditions.findElement(colName);
					double reductionFactor = PhysicalPlanBuilder.computeReductionFactor(element, stats,colName);
					System.out.println(reductionFactor+" reduction");
					totalReductionFactor=totalReductionFactor*reductionFactor;
					SingleTableReductionFactor.put(colName, reductionFactor);
				}
				int size= (int)(stats.getTotalTupleInRelation()*totalReductionFactor);
				oneTableSize.put(i+"", size);
				oneTableCost.put(i+"", 0);
				for (String s:stats.getAttributeNames()) {
					int factor=1;
					if (SingleTableReductionFactor.get(s)!=null) factor=(int)(factor*SingleTableReductionFactor.get(s));
					int value= (int)((stats.getMaxValOfAttr(s)-stats.getMinValOfAttr(s)+1)*factor);
					if (value<size) oneTableVvalue.put(i+s,value);
					else oneTableVvalue.put(i+s, size);
				}
			}
			//		System.out.println("vValue"+vValue);
			//		System.out.println("tablesize"+TableSize);
			//		System.out.println("reductionfactor"+SingleTableReductionFactor);
			
		}
		
		TableSize.add(oneTableSize);
		vValue.add(oneTableVvalue);
		cost.add(oneTableCost);
		
		if(joinchild.size()==2) {
			System.out.println(TableSize.get(0).get("0"));
			if (TableSize.get(0).get("0") > TableSize.get(0).get("1")) FinalOrder="01";
			else FinalOrder="10";
		}

		else {
			for (int i=2;i<joinchild.size();i++) {
				HashMap<String, Integer> iTableSize=new HashMap<>();
				HashMap<String, Integer> iVvalue=new HashMap<>();
				HashMap<String, Integer> iCost=new HashMap<>();
				HashMap<String, Integer> previousTableSize= TableSize.get(i-2);
				HashMap<String, Integer> previousVvalue= vValue.get(i-2);
				HashMap<String, Integer> previousCost= cost.get(i-2);
				HashMap<String, Integer> firstTableSize= TableSize.get(0);
				HashMap<String, Integer> firstTableVvalue= vValue.get(0);

				Set<String> keySet = previousTableSize.keySet();
				for(String leftRelation: keySet) {
					ArrayList<String> leftColumn= TableColumns.get(leftRelation);
					Set<String> singleSet = firstTableSize.keySet();
					for (String rightRelation: singleSet) {
						if (!leftRelation.contains(rightRelation)) {
						// First compute the new relation size
						Integer leftSize= previousTableSize.get(leftRelation);
						Integer rightSize= firstTableSize.get(rightRelation);
						Integer tempSize= leftSize*rightSize;
						ArrayList<String> newTableColumns= new ArrayList<>(leftColumn);
						String[] rightColumns=relationInfoMap.get(((LogicalSelectOperator)joinchild.get(Integer.parseInt(rightRelation))).getTable().getWholeTableName()).getAttributeNames();
						for(String rightColumn:rightColumns) {
							newTableColumns.add(rightColumn);
							ArrayList<String> leftRightEqualColumn = equalColumn.get(rightColumn);
							if(leftRightEqualColumn!=null) { for(String leftEqual:leftRightEqualColumn) {
								if (leftColumn.contains(leftEqual)) {
									Integer Vleft= iVvalue.get(leftEqual);
									Integer Vright= iVvalue.get(rightColumn);
									tempSize=tempSize/Math.max(Vleft,Vright);
								}
							}
							}
						}
						String newTable=leftRelation+rightRelation;
						TableColumns.put(newTable, newTableColumns);
						iTableSize.put(newTable, tempSize);
						iCost.put(newTable, previousCost.get(leftRelation)+tempSize);

						// Update Vvalue of new relation
						for (String previousLeftColumn:leftColumn) {
							iVvalue.put(newTable+previousLeftColumn, previousVvalue.get(previousLeftColumn));
						}
						for (String rightColumn:relationInfoMap.get(((LogicalSelectOperator)joinchild.get(Integer.parseInt(rightRelation))).getTable().getWholeTableName()).getAttributeNames()) {
							ArrayList<String> leftRightEqualColumn = equalColumn.get(rightColumn);
							boolean rightColumnInTheCondition= false;
							if (leftRightEqualColumn!=null) { for(String leftEqual:leftRightEqualColumn) {
								if(leftColumn.contains(leftEqual)) {
									rightColumnInTheCondition= true;
									if (iVvalue.get(newTable+rightColumn)==null) {
										ArrayList<String> equalColumns= new ArrayList<String>();
										equalColumns.add(rightColumn);
										ArrayList<String> rightEqualColumns= equalColumn.get(leftEqual);
										for (String rightEqual:rightEqualColumns) {
											if (Arrays.asList(rightColumns).contains(rightEqual)) equalColumns.add(rightEqual);
										}
										Integer minVvalue= Math.min(tempSize,previousVvalue.get(leftRelation+leftEqual));
										for (String col:equalColumns) {
											minVvalue=Math.min(minVvalue,firstTableVvalue.get(rightRelation+col));
										}
										iVvalue.put(newTable+leftEqual, minVvalue);
										for (String col:equalColumns) {
											iVvalue.put(newTable+col, minVvalue);
										}
									}
								}
							}
						}
							if(!rightColumnInTheCondition) {
								iVvalue.put(newTable+rightColumn, Math.min(tempSize,firstTableVvalue.get(rightRelation+rightColumn)));
							}
						}
					}
					}
				}
				TableSize.add(iTableSize);
				cost.add(iCost);
				vValue.add(iVvalue);
			}
			// find min cost and final join order
			HashMap<String, Integer> finalcost= cost.get(joinchild.size()-2);
			
			Set<String> finalSet = finalcost.keySet();
			boolean firstRelation=true;
			for(String relation:finalSet) {
				if(firstRelation) {FinalOrder=relation; firstRelation=false;}
				else {
					if(finalcost.get(relation)<finalcost.get(FinalOrder)) FinalOrder=relation;
				}
			}
			Set<String> singleSet = cost.get(0).keySet();
			for (String s:singleSet) {
				if (!FinalOrder.contains(s)) FinalOrder=FinalOrder+s;
			}
			System.out.println("finalOrder is   "+FinalOrder);
		}
	}
}
	