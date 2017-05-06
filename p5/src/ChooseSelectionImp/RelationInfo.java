package ChooseSelectionImp;

import java.util.ArrayList;

import project.SchemaPair;
import project.catalog;

public class RelationInfo {
	
	private int totalTupleInRelation;
	private String tableName;
	private int[] attributeMin;
	private int[] attributeMax;
	private int totalAttribute;
	private catalog cl = catalog.getInstance();
	private ArrayList<String> attributeNameList;
	public RelationInfo(int[] attrMin, int[] attrMax, int totalTuple, String name){
		this.tableName = name;
		this.totalTupleInRelation = totalTuple;
		this.attributeMax = attrMax;
		this.attributeMin = attrMin;
		this.totalAttribute = attrMin.length;
		if (cl.UseAlias()){
			this.attributeNameList = cl.getTableSchema().get(cl.getAlias().get(tableName));
		}
		else{
			this.attributeNameList = cl.getTableSchema().get(tableName);
		}
	}
	
	public int getNumOfAttribute(){
		return this.totalAttribute;
	}
	/**
	 * @return the totalTupleInRelation
	 */
	public int getTotalTupleInRelation() {
		return totalTupleInRelation;
	}

	/**
	 * @param totalTupleInRelation the totalTupleInRelation to set
	 */
	public void setTotalTupleInRelation(int totalTupleInRelation) {
		this.totalTupleInRelation = totalTupleInRelation;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the attributeMin
	 */
	public int[] getAttributeMin() {
		return attributeMin;
	}

	/**
	 * @param attributeMin the attributeMin to set
	 */
	public void setAttributeMin(int[] attributeMin) {
		this.attributeMin = attributeMin;
	}

	/**
	 * @return the attributeMax
	 */
	public int[] getAttributeMax() {
		return attributeMax;
	}

	/**
	 * @param attributeMax the attributeMax to set
	 */
	public void setAttributeMax(int[] attributeMax) {
		this.attributeMax = attributeMax;
	}

	public Integer getMinValOfAttr(String colName) {
		for(int i = 0; i< attributeNameList.size();i++){
			if(attributeNameList.get(i).equals(colName)){
				return attributeMin[i];
			}
		}
		return null;
	}
	
	public Integer getMaxValOfAttr(String colName) {
		for(int i = 0; i< attributeNameList.size();i++){
			if(attributeNameList.get(i).equals(colName)){
				return attributeMax[i];
			}
		}
		return null;
	}
}
