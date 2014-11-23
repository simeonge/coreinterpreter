package core.interpreter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Executor for the Core language.
 * 
 * @author Simeon Georgiev
 */
public class CoreExecutor implements Executor {
	// fields
	/**
	 * Enumeration containing the six comparison operators.
	 * 
	 * @author Simeon Georgiev
	 */
	private static enum Comparators {
		NOTEQUAL, EQUAL, LESS, GREATER, GREATEROREQUAL, LESSOREQUAL;
	}

	/**
	 * Scanner used to read the input file.
	 */
	private Scanner scan;

	// private methods
	/**
	 * Executes a statement sequence.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 */
	private void execStmtSeq(ParseTree p) {
		assert p.currNT() == 3 : "Expecting <stmt seq>";
		p.goDown(1); // stmt
		execStmt(p);
		p.goUp();
		if (p.currAlt() == 2) {
			p.goDown(2); // stmt seq
			execStmtSeq(p);
			p.goUp();
		}
	}

	/**
	 * Evaluates a list of identifiers and returns this list.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @return the list of identifiers
	 */
	private ArrayList<String> evalIdList(ParseTree p) {
		assert p.currNT() == 5 : "Expecting <id list>";
		ArrayList<String> list = new ArrayList<String>();

		p.goDown(1); // id
		list.add(p.currIdName()); // add id to list
		p.goUp();

		ArrayList<String> rest = null;
		if (p.currAlt() == 2) { // more ids
			p.goDown(2); // id list
			rest = evalIdList(p);
			p.goUp();
			list.addAll(rest); // add rest after the one id
		}
		return list;
	}

	/**
	 * Executes a statement.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 */
	private void execStmt(ParseTree p) {
		assert p.currNT() == 6 : "Expecting <stmt>";
		switch (p.currAlt()) { // determine alternative
		case 1:
			p.goDown(1);
			execAssign(p);
			p.goUp();
			break;
		case 2:
			p.goDown(1);
			execIf(p);
			p.goUp();
			break;
		case 3:
			p.goDown(1);
			execLoop(p);
			p.goUp();
			break;
		case 4:
			p.goDown(1);
			execInput(p);
			p.goUp();
			break;
		case 5:
			p.goDown(1);
			execOutput(p);
			p.goUp();
			break;
		}
	}

	/**
	 * Executes an assign statement.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 */
	private void execAssign(ParseTree p) {
		assert p.currNT() == 7 : "Expecting <assign>";
		p.goDown(2); // exp
		int val = evalExp(p); // get expression
		p.goUp();

		p.goDown(1); // id
		p.setIdVal(val); // assign value to current id
		p.goUp();
	}

	/**
	 * Executes a if statement.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 */
	private void execIf(ParseTree p) {
		assert p.currNT() == 8 : "Expecting <if>";
		p.goDown(1); // cond
		boolean cond = evalCond(p); // evaluate condition
		p.goUp();

		if (cond) {
			p.goDown(2); // stmt seq
			execStmtSeq(p);
			p.goUp();
		} else { // else clause, only exec'd if alt is 2
			if (p.currAlt() == 2) {
				p.goDown(3); // stmt seq
				execStmtSeq(p);
				p.goUp();
			}
		}
	}

	/**
	 * Executes a while statement.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 */
	private void execLoop(ParseTree p) {
		assert p.currNT() == 9 : "Expecting <loop>";
		p.goDown(1); // cond
		boolean cond = evalCond(p); // get condition
		p.goUp();

		while (cond) {
			p.goDown(2);
			execStmtSeq(p);
			p.goUp();

			p.goDown(1); // cond
			cond = evalCond(p); // get cond again
			p.goUp();
		}
	}

	/**
	 * Executes an input statement. Reads integers from a file using a scanner.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 */
	private void execInput(ParseTree p) {
		assert p.currNT() == 10 : "Expecting <input>";
		p.goDown(1); // id list
		ArrayList<String> list = evalIdList(p); // get ids
		p.goUp();

		Iterator<String> iter = list.iterator(); // get iterator
		try {
			while (iter.hasNext()) {
				String id = iter.next(); // get id
				int val = this.scan.nextInt(); // get value
				p.setIdVal(id, val);
			}
		} catch (InputMismatchException e) {
			System.err.println("Input is not an integer.");
			System.exit(0);
		} catch (NoSuchElementException e) {
			System.err.println("Input is empty.");
			System.exit(0);
		}
	}

	/**
	 * Executes an output statement. Outputs to stdout.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 */
	private void execOutput(ParseTree p) {
		assert p.currNT() == 11 : "Expecting <output>";
		p.goDown(1); // id list
		ArrayList<String> list = evalIdList(p); // get ids
		p.goUp();

		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			String name = iter.next(); // get id
			if (p.getIdVal(name) == null) {
				try {
					throw new RuntimeException("Uninitialized variable " + name);
				} catch (RuntimeException e) {
					System.err.println(e.getMessage());
					System.exit(0);
				}

			}
			System.out.println(name + " = " + p.getIdVal(name));
		}
	}

	/**
	 * Evaluates a condition.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @return true if the condition is true, false otherwise
	 */
	private boolean evalCond(ParseTree p) {
		assert p.currNT() == 12 : "Expecting <cond>";
		boolean cond = false;

		switch (p.currAlt()) {
		case 1:
			p.goDown(1); // comp
			cond = evalComp(p); // get comparison
			p.goUp();
			break;
		case 2:
			p.goDown(1); // cond
			cond = !evalCond(p); // take the not
			p.goUp();
			break;
		case 3:
			boolean b1 = false;
			p.goDown(1); // first cond
			b1 = evalCond(p);
			p.goUp();

			boolean b2 = false;
			p.goDown(2); // second cond
			b2 = evalCond(p);
			p.goUp();

			cond = b1 && b2;
			break;
		case 4:
			boolean c1 = false;
			p.goDown(1); // first cond
			c1 = evalCond(p);
			p.goUp();

			boolean c2 = false;
			p.goDown(2); // second cond
			c2 = evalCond(p);
			p.goUp();

			cond = c1 || c2;
			break;
		}
		return cond;
	}

	/**
	 * Evaluates a comparison between two operands.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @return true if the comparison evaluates to true, false otherwise
	 */
	private boolean evalComp(ParseTree p) {
		assert p.currNT() == 13 : "Expecting <comp>";
		boolean comp = false;

		p.goDown(1); // op
		int op1 = evalOp(p);
		p.goUp();

		p.goDown(2); // comp op
		Comparators c = evalCompOp(p); // get comparator
		p.goUp();

		p.goDown(3); // op
		int op2 = evalOp(p);
		p.goUp();

		switch (c) { // determine operation
		case NOTEQUAL:
			comp = (op1 != op2);
			break;
		case EQUAL:
			comp = (op1 == op2);
			break;
		case LESS:
			comp = (op1 < op2);
			break;
		case GREATER:
			comp = (op1 > op2);
			break;
		case LESSOREQUAL:
			comp = (op1 <= op2);
			break;
		case GREATEROREQUAL:
			comp = (op1 >= op2);
			break;
		}
		return comp;
	}

	/**
	 * Evaluates an expression.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @return the value of the expression
	 */
	private int evalExp(ParseTree p) {
		assert p.currNT() == 14 : "Expecting <exp>";
		int exp = 0;

		p.goDown(1); // fac
		exp = evalFac(p);
		p.goUp();

		if (p.currAlt() == 2) { // +
			p.goDown(2); // exp
			exp = exp + evalExp(p);
			p.goUp();
		} else if (p.currAlt() == 3) { // -
			p.goDown(2); // exp
			exp = exp - evalExp(p);
			p.goUp();
		}
		return exp;
	}

	/**
	 * Evaluates a factor.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @return the value of the factor
	 */
	private int evalFac(ParseTree p) {
		assert p.currNT() == 15 : "Expecting <fac>";
		p.goDown(1); // op
		int fop = evalOp(p);
		p.goUp();

		if (p.currAlt() == 2) {
			p.goDown(2); // fac
			fop = fop * evalFac(p);
			p.goUp();
		}
		return fop;
	}

	/**
	 * Evaluates an operand.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @return the value of the operand
	 */
	private int evalOp(ParseTree p) {
		assert p.currNT() == 16 : "Expecting <op>";
		int op = 0;

		switch (p.currAlt()) {
		case 1: // int
			p.goDown(1); // int
			op = p.currInt();
			p.goUp();
			break;
		case 2: // id
			p.goDown(1); // id
			if (p.currIdVal() == null) {
				try {
					throw new RuntimeException("Uninitialized variable "
							+ p.currIdName());
				} catch (RuntimeException e) {
					System.err.println(e.getMessage());
					System.exit(0);
				}
			}
			op = p.currIdVal();
			p.goUp();
			break;
		case 3: // exp
			p.goDown(1); // exp
			op = evalExp(p);
			p.goUp();
			break;
		}
		return op;
	}

	/**
	 * Evaluates a comparison operator.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @return the comparator
	 */
	private Comparators evalCompOp(ParseTree p) {
		assert p.currNT() == 17 : "Expecting <comp op>";
		switch (p.currAlt()) {
		case 1:
			return Comparators.NOTEQUAL;
		case 2:
			return Comparators.EQUAL;
		case 3:
			return Comparators.LESS;
		case 4:
			return Comparators.GREATER;
		case 5:
			return Comparators.LESSOREQUAL;
		case 6:
			return Comparators.GREATEROREQUAL;
		default:
			return null;
		}
	}

	// public methods
	/**
	 * Initializes the file and scanner fields.
	 * 
	 * @param filename
	 *            the name of the file the Core program reads from.
	 */
	public CoreExecutor(String filename) {
		try { // open scanner
			this.scan = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			System.err.println("Input file not found.");
			System.exit(0);
		}
	}

	@Override
	public void execute(ParseTree p) {
		assert p.currNT() == 1 : "Expecting <prog>";
		// no need to execute declarations; all ids are already in parse tree

		p.goDown(2); // stmt seq
		execStmtSeq(p);
		p.goUp();

		if (this.scan != null) {
			this.scan.close(); // close scanner after done using it
		}
	}
}
