package ChooseSelectionImp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;
import project.catalog;

public class UnionFind {
	private Map<Integer, Element> disjointSet;
	private Map<String, Integer> attributeMap;
	private int disjointSetCount;
	
	private HashMap<String, Expression> unionFindSelectExpMap;
//	private HashMap<String, Expression> unionFindJoinExpMap;
	private ArrayList<Element> unionFindJoinExpList;
	
	
	public UnionFind(){
		disjointSet = new HashMap<Integer, Element>();
		attributeMap = new HashMap<String, Integer>();
		disjointSetCount = 0;
	}

	/**
	 * @return the disjointSet
	 */
	public Map<Integer, Element> getDisjointSet() {
		return disjointSet;
	}

	/**
	 * @param disjointSet the disjointSet to set
	 */
	public void setDisjointSet(Map<Integer, Element> disjointSet) {
		this.disjointSet = disjointSet;
	}

	/**
	 * @return the attributeMap
	 */
	public Map<String, Integer> getAttributeMap() {
		return attributeMap;
	}

	/**
	 * @param attributeMap the attributeMap to set
	 */
	public void setAttributeMap(Map<String, Integer> attributeMap) {
		this.attributeMap = attributeMap;
	} 
    public int getNumberofDisjointSets()
    {
        return disjointSet.size();
    }
	
	public void mergerElements(Column A, Column B){
		Integer KeyA = attributeMap.get(A.getColumnName());
		Integer KeyB = attributeMap.get(B.getColumnName());
		
//		Integer key = KeyA != null? KeyA : (KeyB != null)? KeyB : null;
		if(KeyA == null && KeyB == null){
			disjointSetCount++;	
			attributeMap.put(A.getColumnName(),disjointSetCount);
			attributeMap.put(B.getColumnName(),disjointSetCount);
			disjointSet.put(disjointSetCount, new Element(A));
			disjointSet.get(disjointSetCount).addAttribute(B);
		}
		//Both columns are in the map, but not in the same element set yet. combine them into one element set
		else if(KeyA != null && KeyB != null){
			if(KeyA != KeyB){
				attributeMap.put(B.getColumnName(), KeyA);
				disjointSet.get(KeyA).addAttribute(B);
					disjointSet.remove(KeyB);
			}
		}
		else if(KeyA == null){
			attributeMap.put(A.getColumnName(),KeyB);
			disjointSet.get(KeyB).addAttribute(A);
		}
		else if(KeyB == null){
			attributeMap.put(B.getColumnName(),KeyA);
			disjointSet.get(KeyA).addAttribute(B);
		}		    
	}
	
	public Element findElement(Column col){
		Integer key = attributeMap.get(col.getColumnName());
		if(key == null){
			disjointSetCount++;	
			attributeMap.put(col.getColumnName(),disjointSetCount);
			disjointSet.put(disjointSetCount, new Element(col));
			key = disjointSetCount;
		}
		
		//return the newly added element or existing element.(Need to update constraints)
		return disjointSet.get(key);
	}
	
	public Element findElement(String columnName){
		Integer key = attributeMap.get(columnName);
		if(key == null){
			return null;
		}
		
		return disjointSet.get(key);
	}
	
	/**
	 * Store all unionFind selection expression to hashMap.
	 */
	public void setUnionFindExpressionMap(){
		unionFindSelectExpMap = new HashMap<String, Expression>();
		unionFindJoinExpList = new ArrayList<Element>();
		for(Map.Entry<Integer, Element> entry:disjointSet.entrySet()){
			
			unionFindJoinExpList.add(entry.getValue());
		
			for(Column col: entry.getValue().getAttributes()){
				//stores alias name as key for the map
				String tableName = col.getTable().getName();	
				BinaryExpression exp = null;
				if(entry.getValue().getEqualityConstraint() != null){
					exp = new EqualsTo() ;
					storeExpressionToMAP(tableName, exp,col,new LongValue(entry.getValue().getEqualityConstraint()));
					continue;
				}
				else{
					if(entry.getValue().getLowerBound() != null){			
						exp = new GreaterThanEquals();
						storeExpressionToMAP(tableName, exp,col,new LongValue(entry.getValue().getLowerBound()));
					}
					if(entry.getValue().getUpperBound() != null){
						exp = new MinorThanEquals();
						storeExpressionToMAP(tableName, exp,col,new LongValue(entry.getValue().getUpperBound()));
					}
				}
			}
		}		
	}
	public HashMap<String, Expression> getUnionFindSelectExpMap(){
		return unionFindSelectExpMap;
	}
	
	public ArrayList<Element> getUnionFindJoinExpList(){
		return unionFindJoinExpList;
	}
	private void storeExpressionToMAP(String tableName, BinaryExpression exp, Column col, LongValue longValue){
		exp.setLeftExpression(col);
		exp.setRightExpression(longValue);
//		System.out.println("exp ======> " + tableName + " exp "+ exp);
		if(!unionFindSelectExpMap.containsKey(tableName)){
			unionFindSelectExpMap.put(tableName, exp);
		}
		else{
			unionFindSelectExpMap.put(tableName, new AndExpression(unionFindSelectExpMap.get(tableName),exp));
		}	
	}
}
