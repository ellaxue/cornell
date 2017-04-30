
package physicalOperator;
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
		public abstract Tuple getNextTuple() throws Exception;
		
		/**
		 * Method to reset the operator
		 */
		public abstract void reset() throws Exception; 
		
		/**
		 *  reset the operator to specific tuple index
		 */
		public abstract void reset(int index) throws Exception;
		
		/**
		 * Method to dump the result and give the output of the current operator 
		 */
        public abstract void dump() throws Exception;
        
        public abstract void setLeftChild(Operator child) throws Exception;
        
        public abstract void setRightChild(Operator child) throws Exception;
        
        public abstract Operator getLeftChild();
        
        public abstract Operator getRightChild();
        
        public abstract Expression getExpression();
        
}
