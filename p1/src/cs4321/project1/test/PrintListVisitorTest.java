package cs4321.project1.test;

import static org.junit.Assert.*;

import cs4321.project1.PrintListVisitor;
import cs4321.project1.list.*;

import org.junit.Test;

public class PrintListVisitorTest {

	@Test
	public void testSingleNumberNode() {
		ListNode n1 = new NumberListNode(1.0);
		PrintListVisitor pv1 = new PrintListVisitor();
		n1.accept(pv1);
		assertEquals("1.0", pv1.getResult());
	}
	
	@Test
	public void testAdditionSimplePrefix() {
		ListNode n1 = new NumberListNode(1.0);
		ListNode n2 = new NumberListNode(2.0);
		ListNode n3 = new AdditionListNode();
		n3.setNext(n2);
		n2.setNext(n1);
		PrintListVisitor pv1 = new PrintListVisitor();
		n3.accept(pv1);
		assertEquals("+ 2.0 1.0", pv1.getResult());
	}
	
	@Test
	public void testAdditionSimplePostfix() {
		ListNode n1 = new NumberListNode(1.0);
		ListNode n2 = new NumberListNode(2.0);
		ListNode n3 = new AdditionListNode();
		n1.setNext(n2);
		n2.setNext(n3);
		PrintListVisitor pv1 = new PrintListVisitor();
		n1.accept(pv1);
		assertEquals("1.0 2.0 +", pv1.getResult());
	}
	
	// New test
	@Test
	public void testComplexSimplePrefix() {
		ListNode n1 = new MultiplicationListNode();
		ListNode n2 = new AdditionListNode();
		ListNode n3 = new UnaryMinusListNode();
		ListNode n4 = new NumberListNode(1.0);
		ListNode n5 = new NumberListNode(4.0);
		ListNode n6 = new DivisionListNode();
		ListNode n7 = new NumberListNode(4.0);
		ListNode n8 = new NumberListNode(2.0);
		n1.setNext(n2);
		n2.setNext(n3);
		n3.setNext(n4);
		n4.setNext(n5);
		n5.setNext(n6);
		n6.setNext(n7);
		n7.setNext(n8);
		PrintListVisitor pv1 = new PrintListVisitor();
		n1.accept(pv1);
		assertEquals("* + ~ 1.0 4.0 / 4.0 2.0", pv1.getResult());
	}
	
	// New test
	@Test
	public void testComplexSimplePostfix() {
		ListNode n1 = new NumberListNode(1.0);
		ListNode n2 = new UnaryMinusListNode();
		ListNode n3 = new NumberListNode(4.0);
		ListNode n4 = new AdditionListNode();
		ListNode n5 = new NumberListNode(4.0);
		ListNode n6 = new NumberListNode(2.0);
		ListNode n7 = new DivisionListNode();
		ListNode n8 = new MultiplicationListNode();
		
		n1.setNext(n2);
		n2.setNext(n3);
		n3.setNext(n4);
		n4.setNext(n5);
		n5.setNext(n6);
		n6.setNext(n7);
		n7.setNext(n8);
		PrintListVisitor pv1 = new PrintListVisitor();
		n1.accept(pv1);
		assertEquals("1.0 ~ 4.0 + 4.0 2.0 / *", pv1.getResult());
	}
}
