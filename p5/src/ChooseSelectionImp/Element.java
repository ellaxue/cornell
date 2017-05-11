package ChooseSelectionImp;

import java.util.HashSet;
import net.sf.jsqlparser.schema.Column;

public class Element {
	HashSet<Column> attributes;
	Long lowerBound;
	Long upperBound;
	Long equalityConstraint;

	public Element(Column col){
		attributes = new HashSet<Column>();
		attributes.add(col);
	}

	public void addAttribute(Column attr){
		attributes.add(attr);
	}

	public HashSet<Column> getAttributes(){
		return attributes;
	}

	public void setNumericConstarints(Long value){
		this.lowerBound = value;
		this.upperBound = value;
		this.equalityConstraint = value;
	}

	public void setLowerBound(Long value){
		//in case of Sailor.A > 100 AND Sailor.A > 90
		if(lowerBound != null && value < lowerBound) value = lowerBound;
		lowerBound = equalityConstraint == null? value : equalityConstraint;
	}

	public void setUpperBound(Long value){
		//in case of Sailor.A < 100 AND Sailor.A < 110
		if(upperBound != null && value > upperBound) value = upperBound;
		upperBound = equalityConstraint == null? value : equalityConstraint;
	}

	public Long getLowerBound(){
		return lowerBound;
	}

	public Long upperBound(){
		return upperBound;
	}

	public Long getEqualityConstraint() {
		return equalityConstraint;
	}

	public Long getUpperBound(){
		return upperBound;
	}

	public boolean allNull() {
		return lowerBound == null && upperBound == null && equalityConstraint == null;
	}

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
