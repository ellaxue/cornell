
package physicalOperator;
import java.io.IOException;

import net.sf.jsqlparser.expression.Expression;
import project.Tuple;
/**
 * Abstract class for all the possible operators.
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */

public abstract class Operator {
		/**
		 * Method to obtain the next tuple 
		 */
		public abstract Tuple getNextTuple() throws IOException;
		
		/**
		 * Method to reset the operator
		 */
		public abstract void reset() throws IOException; 
		
		/**
		 * Method to dump the result and give the output of the current operator 
		 */
        public abstract void dump() throws IOException;
        
        public abstract void setLeftChild(Operator child) throws IOException;
        
        public abstract void setRightChild(Operator child);
        
        public abstract Operator getLeftChild();
        
        public abstract Operator getRightChild();
        
        public abstract Expression getExpression();
}
