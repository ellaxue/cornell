package queryPlanBuilder;

import java.awt.List;
import java.lang.reflect.Array;
import java.security.KeyStore.PrivateKeyEntry;
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
	private String FinalOrder;


	public ChooseJoinOrder(LogicalJoinOperator joinOperator,LogicalPlanBuilder lPlanBuilder,Expression ex) {
		ArrayList<logicalOperator.TreeNode> joinchild= joinOperator.getChildren();
		EquijoinCondition equiJoin= new EquijoinCondition(ex);
		HashMap equalColumn= equiJoin.equiColumn;
		System.out.println("equalColumn"+  equalColumn);
		
		HashMap<String, Integer> oneTableVvalue=new HashMap<>();
		HashMap<String, Integer> oneTableSize=new HashMap<>();
		HashMap <String,Integer>oneTableCost= new HashMap<>();
		for (int i=0;i<joinchild.size();i++) {
			LogicalSelectOperator table= (LogicalSelectOperator)joinchild.get(i);

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
		TableSize.add(oneTableSize);
		vValue.add(oneTableVvalue);
	}
		
		if(joinchild.size()==2) {
			if (TableSize.get(0).get(0) > TableSize.get(0).get(1)) FinalOrder="01";
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
					List leftColumn= Arrays.asList(relationInfoMap.get(leftRelation).getAttributeNames());
					Set<String> singleSet = firstTableSize.keySet();
					for (String rightRelation: singleSet) {
						Integer leftSize= previousTableSize.get(leftRelation);
						Integer rightSize= firstTableSize.get(rightRelation);
						Integer tempSize= leftSize*rightSize;
						
					}
				}
			}
		}
		
}
}