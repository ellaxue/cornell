package cs4321.project1;

import static org.junit.Assert.*;

import org.junit.Test;

import cs4321.project1.tree.*;

public class EvaluateTreeVisitorTest {

	private static final double DELTA = 1e-15;

	@Test
	public void testSingleLeafNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
        EvaluateTreeVisitor v1 = new EvaluateTreeVisitor();
		n1.accept(v1);
		System.out.println(v1.getResult());
		assertEquals(1.0, v1.getResult(), DELTA);
	}
	
	@Test
	public void testAdditionNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
		TreeNode n2 = new LeafTreeNode(2.0);
		TreeNode n3 = new AdditionTreeNode(n1, n2);
		TreeNode n4 = new AdditionTreeNode(n2, n1);
        EvaluateTreeVisitor v1 = new EvaluateTreeVisitor();
		n3.accept(v1);
		assertEquals(3.0, v1.getResult(), DELTA);
        EvaluateTreeVisitor v2 = new EvaluateTreeVisitor();
		n4.accept(v2);
		assertEquals(3.0, v2.getResult(), DELTA);
	}
	
	@Test
	public void testMultiplicationNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
		TreeNode n2 = new LeafTreeNode(2.0);
		TreeNode n3 = new MultiplicationTreeNode(n1, n2);
		TreeNode n4 = new MultiplicationTreeNode(n2, n1);
        EvaluateTreeVisitor v1 = new EvaluateTreeVisitor();
		n3.accept(v1);
		assertEquals(2.0, v1.getResult(), DELTA);
        EvaluateTreeVisitor v2 = new EvaluateTreeVisitor();
		n4.accept(v2);
		assertEquals(2.0, v2.getResult(), DELTA);
	}
	
	// New test
	@Test
	public void testDivisionNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
		TreeNode n2 = new LeafTreeNode(2.0);
		TreeNode n3 = new DivisionTreeNode(n1, n2);
		TreeNode n4 = new DivisionTreeNode(n2, n1);
        EvaluateTreeVisitor v1 = new EvaluateTreeVisitor();
		n3.accept(v1);
		assertEquals(0.5, v1.getResult(), DELTA);
        EvaluateTreeVisitor v2 = new EvaluateTreeVisitor();
		n4.accept(v2);
		assertEquals(2.0, v2.getResult(), DELTA);
	}
	
	// New test
	@Test
	public void testSubtractionNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
		TreeNode n2 = new LeafTreeNode(2.0);
		TreeNode n3 = new SubtractionTreeNode(n1, n2);
		TreeNode n4 = new SubtractionTreeNode(n2, n1);
        EvaluateTreeVisitor v1 = new EvaluateTreeVisitor();
		n3.accept(v1);
		assertEquals(-1, v1.getResult(), DELTA);
        EvaluateTreeVisitor v2 = new EvaluateTreeVisitor();
		n4.accept(v2);
		assertEquals(1, v2.getResult(), DELTA);
	}
	
	// New test
	@Test
	public void testUnaryMinusNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
		TreeNode n2 = new UnaryMinusTreeNode(n1);
		TreeNode n3 = new UnaryMinusTreeNode(n2);
        EvaluateTreeVisitor v1 = new EvaluateTreeVisitor();
		n2.accept(v1);
		assertEquals(-1, v1.getResult(), DELTA);
        EvaluateTreeVisitor v2 = new EvaluateTreeVisitor();
		n3.accept(v2);
		assertEquals(1, v2.getResult(), DELTA);
	}
	
	// New test
	@Test
	public void testMultipleNode() {
		TreeNode n1 = new LeafTreeNode(1.0);
		TreeNode n2 = new LeafTreeNode(4.0);
		TreeNode n3 = new LeafTreeNode(4.0);
		TreeNode n4 = new LeafTreeNode(2.0);
		TreeNode n5 = new UnaryMinusTreeNode(n1);
		TreeNode n6 = new AdditionTreeNode(n2,n5);
		TreeNode n7 = new DivisionTreeNode(n3,n4);
		TreeNode n8 = new MultiplicationTreeNode(n6,n7);
		TreeNode n9 = new MultiplicationTreeNode(n7,n6);
        EvaluateTreeVisitor v1 = new EvaluateTreeVisitor();
		n8.accept(v1);
		assertEquals(6, v1.getResult(), DELTA);
        EvaluateTreeVisitor v2 = new EvaluateTreeVisitor();
		n9.accept(v2);
		assertEquals(6, v2.getResult(), DELTA);
	}

}
