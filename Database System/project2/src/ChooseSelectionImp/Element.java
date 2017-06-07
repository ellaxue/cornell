package ChooseSelectionImp;

import java.util.HashSet;
import net.sf.jsqlparser.schema.Column;
/**
 * This class stores union find element information
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class Element {
	HashSet<Column> attributes;
	Long lowerBound;
	Long upperBound;
	Long equalityConstraint;

	/**
	 * Constructor
	 * @param col the attribute
	 */
	public Element(Column col){
		attributes = new HashSet<Column>();
		attributes.add(col);
	}

	/**
	 * Add a new attribute to the attribute set
	 * @param attr the attribute to be added
	 */
	public void addAttribute(Column attr){
		attributes.add(attr);
	}

	/**
	 * Returns attribute set
	 * @return the attribute set contains all columns that are in the union find element
	 */
	public HashSet<Column> getAttributes(){
		return attributes;
	}

	/**
	 * Sets equality for all the attributes in the set
	 * @param value the value all the attributes equal to
	 */
	public void setNumericConstarints(Long value){
		this.lowerBound = value;
		this.upperBound = value;
		this.equalityConstraint = value;
	}

	/**
	 * sets lower bound value for all the attributes
	 * @param value lower bound attribute value
	 */
	public void setLowerBound(Long value){
		//in case of Sailor.A > 100 AND Sailor.A > 90
		if(lowerBound != null && value < lowerBound) value = lowerBound;
		lowerBound = equalityConstraint == null? value : equalityConstraint;
	}
	
	/**
	 * sets upper bound value for all the attributes
	 * @param value upper bound attribute value
	 */
	public void setUpperBound(Long value){
		//in case of Sailor.A < 100 AND Sailor.A < 110
		if(upperBound != null && value > upperBound) value = upperBound;
		upperBound = equalityConstraint == null? value : equalityConstraint;
	}

	/**
	 * 
	 * @return  lower bound attribute value
	 */
	public Long getLowerBound(){
		return lowerBound;
	}

	/**
	 * 
	 * @return attributes equality value
	 */
	public Long getEqualityConstraint() {
		return equalityConstraint;
	}

	/**
	 * 
	 * @return upper bound attribute value
	 */
	public Long getUpperBound(){
		return upperBound;
	}

	/**
	 * 
	 * @return true if all three constraints are null
	 */
	public boolean allNull() {
		return lowerBound == null && upperBound == null && equalityConstraint == null;
	}

	/**
	 * pretty Print 
	 */
	@Override
	public String toString(){ 
		StringBuilder sb = new StringBuilder();
		sb.append("[[");
		int count = attributes.size();
		for(Column attr:attributes){
			if(--count == 0){
				sb.append(attr).append("],");
			}else sb.append(attr).append(",");
		}
		sb.append(" equals ").append(equalityConstraint).append(", min ").
		append(lowerBound).append(", max ").append(upperBound).append("]\n");
		return sb.toString();
	}


}
