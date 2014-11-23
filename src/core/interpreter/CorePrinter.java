package core.interpreter;
/**
 * A printer for the Core language.
 * 
 * @author Simeon Georgiev
 */
public class CorePrinter implements Printer {
	// field
	/**
	 * Number of space to print in front of each statement for correct
	 * indentation.
	 */
	public int space;

	// private methods
	/**
	 * Prints spaces.
	 */
	private void printSpaces() {
		for (int i = 0; i < this.space; i++) {
			System.out.print(" ");
		}
	}

	/**
	 * Pretty-prints a declaration sequence.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at a decl seq node
	 */
	private void printDeclSeq(ParseTree p) {
		assert p.currNT() == 2 : "Expecting <decl seq>";
		p.goDown(1); // one decl
		printSpaces(); // indentation
		printDecl(p);
		p.goUp();
		if (p.currAlt() == 2) {
			p.goDown(2); // decl seq
			printDeclSeq(p);
			p.goUp();
		}
	}

	/**
	 * Pretty-prints a statement sequence.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at a stmt seq node
	 */
	private void printStmtSeq(ParseTree p) {
		assert p.currNT() == 3 : "Expecting <stmt seq>";
		p.goDown(1); // one stmt
		printSpaces(); // indentation
		printStmt(p);
		p.goUp();
		if (p.currAlt() == 2) {
			p.goDown(2); // stmt seq
			printStmtSeq(p);
			p.goUp();
		}
	}

	/**
	 * Pretty-prints a declaration.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at a decl node
	 */
	private void printDecl(ParseTree p) {
		assert p.currNT() == 4 : "Expecting <decl>";
		System.out.print("int ");
		p.goDown(1); // id list
		printIdList(p);
		p.goUp();
		System.out.println(";");
	}

	/**
	 * Pretty-prints a list of identifiers.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at an id list node
	 */
	private void printIdList(ParseTree p) {
		assert p.currNT() == 5 : "Expecting <id list>";
		p.goDown(1); // id
		System.out.print(p.currIdName()); // print id name
		p.goUp();

		if (p.currAlt() == 2) {
			System.out.print(", ");
			p.goDown(2); // id list
			printIdList(p);
			p.goUp();
		}
	}

	/**
	 * Pretty-prints a statement.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at a stmt node
	 */
	private void printStmt(ParseTree p) {
		assert p.currNT() == 6 : "Expecting <stmt>";
		switch (p.currAlt()) { // determine alternative
		case 1:
			p.goDown(1);
			printAssign(p);
			p.goUp();
			break;
		case 2:
			p.goDown(1);
			printIf(p);
			p.goUp();
			break;
		case 3:
			p.goDown(1);
			printLoop(p);
			p.goUp();
			break;
		case 4:
			p.goDown(1);
			printInput(p);
			p.goUp();
			break;
		case 5:
			p.goDown(1);
			printOutput(p);
			p.goUp();
			break;
		}
	}

	/**
	 * Pretty-prints an assign statement.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at an assign node
	 */
	private void printAssign(ParseTree p) {
		assert p.currNT() == 7 : "Expecting <assign>";
		p.goDown(1); // id
		System.out.print(p.currIdName()); // id name
		p.goUp();

		System.out.print(" = ");
		p.goDown(2); // expression
		printExp(p);
		p.goUp();

		System.out.println(";");
	}

	/**
	 * Pretty-prints an if statement.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at an if node
	 */
	private void printIf(ParseTree p) {
		assert p.currNT() == 8 : "Expecting <if>";
		System.out.print("if ");
		p.goDown(1); // condition
		printCond(p);
		p.goUp();

		System.out.println(" then");
		this.space = this.space + 4;
		p.goDown(2); // stmt seq
		printStmtSeq(p);
		p.goUp();

		if (p.currAlt() == 2) {
			this.space = this.space - 4;
			printSpaces();
			System.out.println("else");
			this.space = this.space + 4;
			p.goDown(3); // stmt seq
			printStmtSeq(p);
			p.goUp();
		}

		this.space = this.space - 4;
		printSpaces();
		System.out.println("end;");
	}

	/**
	 * Pretty-prints a loop statement.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at a loop node
	 */
	private void printLoop(ParseTree p) {
		assert p.currNT() == 9 : "Expecting <loop>";
		System.out.print("while ");
		p.goDown(1); // condition
		printCond(p);
		p.goUp();

		System.out.println(" loop");
		this.space = this.space + 4;
		p.goDown(2); // stmt seq
		printStmtSeq(p);
		p.goUp();

		this.space = this.space - 4;
		printSpaces();
		System.out.println("end;");
	}

	/**
	 * Pretty-prints an input statement.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at an input node
	 */
	private void printInput(ParseTree p) {
		assert p.currNT() == 10 : "Expecting <input>";
		System.out.print("read ");
		p.goDown(1); // id list
		printIdList(p);
		p.goUp();
		System.out.println(";");
	}

	/**
	 * Pretty-prints an output statement.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at an output node
	 */
	private void printOutput(ParseTree p) {
		assert p.currNT() == 11 : "Expecting <output>";
		System.out.print("write ");
		p.goDown(1); // id list
		printIdList(p);
		p.goUp();
		System.out.println(";");
	}

	/**
	 * Pretty-prints a condition.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at a cond node
	 */
	private void printCond(ParseTree p) {
		assert p.currNT() == 12 : "Expecting <cond>";
		switch (p.currAlt()) {
		case 1:
			p.goDown(1); // comp
			printComp(p);
			p.goUp();
			break;
		case 2:
			System.out.print("!");
			p.goDown(1); // cond
			printCond(p);
			p.goUp();
			break;
		case 3:
			System.out.print("[");
			p.goDown(1); // first cond
			printCond(p);
			p.goUp();

			System.out.print(" && ");
			p.goDown(2); // second cond
			printCond(p);
			p.goUp();

			System.out.print("]");
			break;
		case 4:
			System.out.print("[");
			p.goDown(1); // first cond
			printCond(p);
			p.goUp();

			System.out.print(" || ");
			p.goDown(2); // second cond
			printCond(p);
			p.goUp();

			System.out.print("]");
			break;
		}
	}

	/**
	 * Pretty-prints a comparison.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at a comp node
	 */
	private void printComp(ParseTree p) {
		assert p.currNT() == 13 : "Expecting <comp>";
		System.out.print("(");
		p.goDown(1); // op
		printOp(p);
		p.goUp();

		p.goDown(2); // comp op
		printCompOp(p);
		p.goUp();

		p.goDown(3); // op
		printOp(p);
		p.goUp();
		
		System.out.print(")");
	}

	/**
	 * Pretty-prints an expression.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at an exp node
	 */
	private void printExp(ParseTree p) {
		assert p.currNT() == 14 : "Expecting <exp>";
		p.goDown(1); // fac
		printFac(p);
		p.goUp();

		if (p.currAlt() == 2) {
			System.out.print(" + ");
			p.goDown(2); // exp
			printExp(p);
			p.goUp();
		} else if (p.currAlt() == 3) {
			System.out.print(" - ");
			p.goDown(2); // exp
			printExp(p);
			p.goUp();
		}
	}

	/**
	 * Pretty-prints a factor.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at a fac node
	 */
	private void printFac(ParseTree p) {
		assert p.currNT() == 15 : "Expecting <fac>";
		p.goDown(1); // op
		printOp(p);
		p.goUp();
		if (p.currAlt() == 2) {
			System.out.print(" * ");
			p.goDown(2); // fac
			printFac(p);
			p.goUp();
		}
	}

	/**
	 * Pretty-prints an operand.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at an op node
	 */
	private void printOp(ParseTree p) {
		assert p.currNT() == 16 : "Expecting <op>";
		switch (p.currAlt()) {
		case 1:
			p.goDown(1);
			System.out.print(p.currInt());
			p.goUp();
			break;
		case 2:
			p.goDown(1); // id
			System.out.print(p.currIdName()); // id name
			p.goUp();
			break;
		case 3:
			System.out.print("(");
			p.goDown(1); // exp
			printExp(p);
			p.goUp();
			System.out.print(")");
			break;
		}
	}

	/**
	 * Pretty-prints a comparison operator.
	 * 
	 * @param p
	 *            the parse tree representing the Core program, with the cursor
	 *            at a comp op node
	 */
	private void printCompOp(ParseTree p) {
		assert p.currNT() == 17 : "Expecting <comp op>";
		switch (p.currAlt()) {
		case 1:
			System.out.print(" != ");
			break;
		case 2:
			System.out.print(" == ");
			break;
		case 3:
			System.out.print(" < ");
			break;
		case 4:
			System.out.print(" > ");
			break;
		case 5:
			System.out.print(" <= ");
			break;
		case 6:
			System.out.print(" >= ");
			break;
		}
	}

	// public method
	@Override
	public void print(ParseTree p) {
		assert p.currNT() == 1 : "Expecting <prog>";
		System.out.println("program");
		this.space = 4;
		p.goDown(1);
		printDeclSeq(p); // print declarations
		p.goUp();
		System.out.println("begin");
		p.goDown(2);
		printStmtSeq(p); // print statements
		p.goUp();
		System.out.println("end\n");
	}
}
