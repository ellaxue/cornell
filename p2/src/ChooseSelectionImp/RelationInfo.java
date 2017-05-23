package ChooseSelectionImp;

import java.util.ArrayList;

import project.SchemaPair;
import project.catalog;

/**
 * This class a relation table information
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class RelationInfo {
	
	private int totalTupleInRelation;
	private String tableName;
	private int[] attributeMin;
	private int[] attributeMax;
	private int totalAttribute;
	private String attributeNames[];
	
	/**
	 * Constructor
	 * @param attrMin attributes min value
	 * @param attrMax attributes max value
	 * @param totalTuple total number of tuples in the relation
	 * @param tablename table name of the relation
	 * @param attriName all the attribute names in the relation
	 */
	public RelationInfo(int[] attrMin, int[] attrMax, int totalTuple, String tablename, String[] attriName){
		this.tableName = tablename;
		this.totalTupleInRelation = totalTuple;
		this.attributeMax = attrMax;
		this.attributeMin = attrMin;
		this.totalAttribute = attrMin.length;
		this.setAttributeNames(attriName);
	}
	
	/**
	 * 
	 * @return number of attribute of this relation
	 */
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

	/**
	 * 
	 * @param colName attribute name
	 * @return min value of this attribute
	 */
	public Integer getMinValOfAttr(String colName) {
		for(int i = 0; i< getAttributeNames().length;i++){
			if(getAttributeNames()[i].equals(colName)){
				return attributeMin[i];
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param colName attribute name
	 * @return max value of this attribute
	 */
	public Integer getMaxValOfAttr(String colName) {
		for(int i = 0; i< getAttributeNames().length;i++){
			if(getAttributeNames()[i].equals(colName)){
				return attributeMax[i];
			}
		}
		return null;
	}

	/**
	 * 
	 * @return return all attribute name in this relation
	 */
	public String[] getAttributeNames() {
		return attributeNames;
	}

	/**
	 * set attribute names in this relation
	 * @param attributeNames attribute names in this relation
	 */
	public void setAttributeNames(String attributeNames[]) {
		this.attributeNames = attributeNames;
	}
}
