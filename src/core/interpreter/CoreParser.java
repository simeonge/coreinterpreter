package core.interpreter;
import java.text.ParseException;

/**
 * Parser for the Core language. The constructor takes the name of the file
 * containing the source program to be parsed.
 * 
 * @author Simeon Georgiev
 */
public class CoreParser implements Parser {
	// fields
	/**
	 * A global tokenizer to read the input file.
	 */
	private Lexer t;

	// private methods
	/**
	 * Parses a declaration sequence.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A declaration sequence is parsed and its representation added to
	 *          the parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseDeclSeq(ParseTree p) throws ParseException {
		p.setNT(2); // decl seq
		p.createBranch(1); // decl
		p.goDown(1);
		parseDecl(p);
		p.goUp();

		if (t.getToken() == 4) { // more than one declaration
			p.createBranch(2);
			p.goDown(2);
			parseDeclSeq(p);
			p.goUp();
			p.setAlt(2);
		} else { // only one declaration
			p.setAlt(1);
		}
	}

	/**
	 * Parses a statement sequence.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A statement sequence is parsed and its representation added to
	 *          the parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseStmtSeq(ParseTree p) throws ParseException {
		p.setNT(3); // stmt seq
		p.createBranch(1); // stmt
		p.goDown(1);
		parseStmt(p);
		p.goUp();

		if (t.getToken() == 5 || t.getToken() == 8 || t.getToken() == 10
				|| t.getToken() == 11 || t.getToken() == 32) {
			p.createBranch(2);
			p.goDown(2);
			parseStmtSeq(p);
			p.goUp();
			p.setAlt(2);
		} else { // only one statement
			p.setAlt(1);
		}
	}

	/**
	 * Parses a declaration.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A declaration is parsed and its representation added to the
	 *          parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseDecl(ParseTree p) throws ParseException {
		if (t.getToken() != 4) { // int
			throw new ParseException("Expecting at least one declaration", -1);
		}
		p.setNT(4); // decl
		p.setAlt(1); // only one alt

		p.createBranch(1); // id list
		p.goDown(1);
		t.skipToken(); // look-ahead
		addDecls(p); // add declarations
		p.goUp();

		if (t.getToken() != 12) { // ;
			throw new ParseException("Expecting \";\" at " + t.getTokenName(),
					0);
		}
		t.skipToken(); // look-ahead
	}

	/**
	 * Parses an identifier list. Called only by parseDecl, this method calls
	 * the declId method of the parse tree
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @throws ParseException
	 *             if a syntax error occurs, or a duplicate variable is
	 *             encountered
	 */
	private void addDecls(ParseTree p) throws ParseException {
		if (t.getToken() != 32) { // id
			throw new ParseException("Expecting an identifier at "
					+ t.getTokenName(), 0);
		}
		p.setNT(5); // id list
		p.createBranch(1); // id
		p.goDown(1);
		p.setNT(18); // id
		boolean b = p.declId(t.idName());
		if (!b) { // duplicate variable
			throw new ParseException("Duplicate variable " + t.idName(), -1);
		}
		p.goUp();

		t.skipToken(); // get , or look-ahead
		if (t.getToken() == 13) { // ,
			p.createBranch(2); // id list
			p.goDown(2);
			t.skipToken();
			addDecls(p);
			p.goUp();
			p.setAlt(2);
		} else { // only one id
			p.setAlt(1);
		}
	}

	/**
	 * Parses an identifier list. Called by any methods that parse non-terminals
	 * that contain an id list, except parseDecl.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures An identifier list is parsed and its representation added to the
	 *          parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered or if an underclared
	 *             variable is encountered
	 */
	private void parseIdList(ParseTree p) throws ParseException {
		if (t.getToken() != 32) { // id
			throw new ParseException("Expecting an identifier at "
					+ t.getTokenName(), 0);
		}
		p.setNT(5); // id list
		p.createBranch(1); // id
		p.goDown(1);
		p.setNT(18); // id terminal
		boolean b = p.setIdName(t.idName());
		if (!b) { // undeclared variable
			throw new ParseException("Undeclared variable " + t.idName(), -1);
		}
		p.goUp();

		t.skipToken(); // get , or look-ahead
		if (t.getToken() == 13) { // ,
			p.createBranch(2); // id list
			p.goDown(2);
			t.skipToken();
			parseIdList(p);
			p.goUp();
			p.setAlt(2);
		} else { // only one id
			p.setAlt(1);
		}
	}

	/**
	 * Parses a statement.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A statement is parsed and its representation added to the parse
	 *          tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseStmt(ParseTree p) throws ParseException {
		p.setNT(6); // stmt

		switch (t.getToken()) {
		case 32: // assign
			p.createBranch(1); // assign
			p.goDown(1);
			parseAssign(p);
			p.goUp();
			p.setAlt(1);
			break;
		case 5: // if
			p.createBranch(1); // if
			p.goDown(1);
			parseIf(p);
			p.goUp();
			p.setAlt(2);
			break;
		case 8: // while
			p.createBranch(1); // while
			p.goDown(1);
			parseLoop(p);
			p.goUp();
			p.setAlt(3);
			break;
		case 10: // read
			p.createBranch(1); // read
			p.goDown(1);
			parseInput(p);
			p.goUp();
			p.setAlt(4);
			break;
		case 11: // write
			p.createBranch(1); // write
			p.goDown(1);
			parseOutput(p);
			p.goUp();
			p.setAlt(5);
			break;
		default: // only runs if no statements
			throw new ParseException("Expecting at least one statement at "
					+ t.getTokenName(), 0);
		}
	}

	/**
	 * Parses an assign statement.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures An assign statement is parsed and its representation added to
	 *          the parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseAssign(ParseTree p) throws ParseException {
		if (t.getToken() != 32) { // id
			throw new ParseException("Expecting an identifier at "
					+ t.getTokenName(), 0);
		}
		p.setNT(7); // assign
		p.setAlt(1);

		p.createBranch(1); // id
		p.goDown(1);
		p.setNT(18); // id terminal
		boolean b = p.setIdName(t.idName());
		if (!b) {
			throw new ParseException("Undeclared variable " + t.idName(), -1);
		}
		p.goUp();

		t.skipToken(); // get =
		if (t.getToken() != 14) { // =
			throw new ParseException("Expecting \"=\" at " + t.getTokenName(),
					0);
		}

		p.createBranch(2); // exp
		p.goDown(2);
		t.skipToken(); // look-ahead
		parseExp(p);
		p.goUp();

		if (t.getToken() != 12) { // ;
			throw new ParseException("Expecting \";\" at " + t.getTokenName(),
					0);
		}
		t.skipToken(); // look-ahead
	}

	/**
	 * Parses an if statement.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures An if statement is parsed and its representation added to the
	 *          parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseIf(ParseTree p) throws ParseException {
		if (t.getToken() != 5) { // if
			throw new ParseException("Expecting \"if\" at " + t.getTokenName(),
					0);
		}
		p.setNT(8); // if

		p.createBranch(1); // cond
		p.goDown(1);
		t.skipToken(); // look-ahead
		parseCond(p);
		p.goUp();

		if (t.getToken() != 6) { // then
			throw new ParseException("Expecting \"then\" at "
					+ t.getTokenName(), 0);
		}
		p.createBranch(2); // stmt seq
		p.goDown(2);
		t.skipToken(); // look-ahead
		parseStmtSeq(p);
		p.goUp();

		if (t.getToken() != 7 && t.getToken() != 3) { // must be else or end
			throw new ParseException("Expecting \"else\" or \"end\" at "
					+ t.getTokenName(), 0);
		}

		if (t.getToken() == 7) { // else
			p.createBranch(3);
			p.goDown(3);
			t.skipToken(); // look-ahead
			parseStmtSeq(p);
			p.goUp();
			p.setAlt(2);
		} else {
			p.setAlt(1);
		}

		if (t.getToken() != 3) { // to check after else clause
			throw new ParseException(
					"Expecting \"end\" at " + t.getTokenName(), 0);
		}
		t.skipToken(); // get ;
		if (t.getToken() != 12) { // ;
			throw new ParseException("Expecting \";\" at " + t.getTokenName(),
					0);
		}
		t.skipToken(); // look-ahead
	}

	/**
	 * Parses a while statement.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A while statement is parsed and its representation added to the
	 *          parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseLoop(ParseTree p) throws ParseException {
		if (t.getToken() != 8) { // while
			throw new ParseException("Expecting \"while\" at "
					+ t.getTokenName(), 0);
		}
		p.setNT(9); // loop
		p.setAlt(1);

		p.createBranch(1); // cond
		p.goDown(1);
		t.skipToken(); // look-ahead
		parseCond(p);
		p.goUp();

		if (t.getToken() != 9) { // loop
			throw new ParseException("Expecting \"loop\" at "
					+ t.getTokenName(), 0);
		}
		p.createBranch(2); // stmt seq
		p.goDown(2);
		t.skipToken(); // look-ahead
		parseStmtSeq(p);
		p.goUp();

		if (t.getToken() != 3) { // end
			throw new ParseException(
					"Expecting \"end\" at " + t.getTokenName(), 0);
		}
		t.skipToken(); // get ;
		if (t.getToken() != 12) { // ;
			throw new ParseException("Expecting \";\" at " + t.getTokenName(),
					0);
		}
		t.skipToken(); // look-ahead
	}

	/**
	 * Parses a read statement.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A read statement is parsed and its representation added to the
	 *          parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseInput(ParseTree p) throws ParseException {
		if (t.getToken() != 10) { // read
			throw new ParseException("Expecting \"read\" at "
					+ t.getTokenName(), 0);
		}
		p.setNT(10); // input
		p.setAlt(1);

		p.createBranch(1); // id list
		p.goDown(1);
		t.skipToken(); // look-ahead
		parseIdList(p);
		p.goUp();

		if (t.getToken() != 12) { // ;
			throw new ParseException("Expecting \";\" at " + t.getTokenName(),
					0);
		}
		t.skipToken(); // look-ahead
	}

	/**
	 * Parses a write statement.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A write statement is parsed and its representation added to the
	 *          parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseOutput(ParseTree p) throws ParseException {
		if (t.getToken() != 11) { // write
			throw new ParseException("Expecting \"write\" at "
					+ t.getTokenName(), 0);
		}
		p.setNT(11); // output
		p.setAlt(1);

		p.createBranch(1); // id list
		p.goDown(1);
		t.skipToken(); // look-ahead
		parseIdList(p);
		p.goUp();

		if (t.getToken() != 12) { // ;
			throw new ParseException("Expecting \";\" at " + t.getTokenName(),
					0);
		}
		t.skipToken(); // look-ahead
	}

	/**
	 * Parses a condition.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A condition is parsed and its representation added to the parse
	 *          tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseCond(ParseTree p) throws ParseException {
		p.setNT(12); // cond

		switch (t.getToken()) {
		case 15: // !
			p.createBranch(1); // cond
			p.goDown(1);
			t.skipToken(); // look-ahead
			parseCond(p);
			p.goUp();
			p.setAlt(2);
			break;
		case 16: // [
			p.createBranch(1); // cond
			p.goDown(1);
			t.skipToken(); // look-ahead
			parseCond(p);
			p.goUp();

			if (t.getToken() != 18 && t.getToken() != 19) { // && or ||
				throw new ParseException("Expecting \"&&\" or \"||\"",
						t.getToken());
			}

			if (t.getToken() == 18) { // &&
				p.createBranch(2); // cond
				p.goDown(2);
				t.skipToken(); // look-ahead
				parseCond(p);
				p.goUp();
				p.setAlt(3);
			} else { // ||
				p.createBranch(2); // cond
				p.goDown(2);
				t.skipToken(); // look-ahead
				parseCond(p);
				p.goUp();
				p.setAlt(4);
			}

			if (t.getToken() != 17) {
				throw new ParseException("Expecting \"]\" at "
						+ t.getTokenName(), 0);
			}
			t.skipToken(); // look-ahead
			break;
		default: // if neither ! nor [, default to <comp>
			p.createBranch(1); // comp
			p.goDown(1);
			parseComp(p);
			p.goUp();
			p.setAlt(1);
		}
	}

	/**
	 * Parses a comparison.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A comparison is parsed and its representation added to the parse
	 *          tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseComp(ParseTree p) throws ParseException {
		if (t.getToken() != 20) { // (
			throw new ParseException("Expecting a comparison condition at "
					+ t.getTokenName(), 0);
		}
		p.setNT(13); // comp
		p.setAlt(1);

		p.createBranch(1); // op
		p.goDown(1);
		t.skipToken(); // look-ahead
		parseOp(p);
		p.goUp();

		p.createBranch(2); // comp op
		p.goDown(2);
		parseCompOp(p);
		p.goUp();

		p.createBranch(3); // op
		p.goDown(3);
		parseOp(p);
		p.goUp();

		if (t.getToken() != 21) { // )
			throw new ParseException("Expecting \")\" at " + t.getTokenName(),
					0);
		}
		t.skipToken(); // look-ahead
	}

	/**
	 * Parses an expression.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures An expression is parsed and its representation added to the
	 *          parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseExp(ParseTree p) throws ParseException {
		p.setNT(14); // exp
		p.createBranch(1); // fac
		p.goDown(1);
		parseFac(p);
		p.goUp();

		if (t.getToken() == 22) { // +
			p.createBranch(2); // exp
			p.goDown(2);
			t.skipToken(); // look-ahead
			parseExp(p);
			p.goUp();
			p.setAlt(2);
		} else if (t.getToken() == 23) { // -
			p.createBranch(2); // exp
			p.goDown(2);
			t.skipToken(); // look-ahead
			parseExp(p);
			p.goUp();
			p.setAlt(3);
		} else {
			p.setAlt(1);
		}
	}

	/**
	 * Parses a factor.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A factor is parsed and its representation added to the parse
	 *          tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseFac(ParseTree p) throws ParseException {
		p.setNT(15); // fac
		p.createBranch(1); // op
		p.goDown(1);
		parseOp(p);
		p.goUp();

		if (t.getToken() == 24) { // *
			p.createBranch(2); // fac
			p.goDown(2);
			t.skipToken(); // look-ahead
			parseFac(p);
			p.goUp();
			p.setAlt(2);
		} else {
			p.setAlt(1);
		}
	}

	/**
	 * Parses an operand.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures An operand is parsed and its representation added to the parse
	 *          tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseOp(ParseTree p) throws ParseException {
		p.setNT(16); // op

		switch (t.getToken()) {
		case 31: // int
			p.createBranch(1);
			p.goDown(1);
			p.setNT(20); // int terminal
			p.setInt(t.intVal());
			p.goUp();
			p.setAlt(1);
			break;
		case 32: // id
			p.createBranch(1);
			p.goDown(1);
			p.setNT(18); // id terminal
			boolean b = p.setIdName(t.idName());
			if (!b) {
				throw new ParseException("Undeclared variable " + t.idName(),
						-1);
			}
			p.goUp();
			p.setAlt(2);
			break;
		case 20: // ( for expression
			p.createBranch(1);
			p.goDown(1);
			t.skipToken(); // look-ahead
			parseExp(p);
			p.goUp();
			if (t.getToken() != 21) { // )
				throw new ParseException("Expecting \")\" at "
						+ t.getTokenName(), 0);
			}
			p.setAlt(3);
			break;
		default:
			throw new ParseException(
					"Expecting an integer, an identifier, or an expression at "
							+ t.getTokenName(), 0);
		}
		t.skipToken(); // look-ahead
	}

	/**
	 * Parses a comparison operator.
	 * 
	 * @param p
	 *            the parse tree that represents the program
	 * @ensures A comparison operator is parsed and its representation added to
	 *          the parse tree.
	 * @throws ParseException
	 *             if a syntax error is encountered
	 */
	private void parseCompOp(ParseTree p) throws ParseException {
		p.setNT(17); // comp op
		switch (t.getToken()) {
		case 25: // !=
			p.setAlt(1);
			break;
		case 26: // ==
			p.setAlt(2);
			break;
		case 27: // <
			p.setAlt(3);
			break;
		case 28: // >
			p.setAlt(4);
			break;
		case 29: // <=
			p.setAlt(5);
			break;
		case 30: // >=
			p.setAlt(6);
			break;
		default:
			throw new ParseException("Expecting a comparison operator at "
					+ t.getTokenName(), 0);
		}
		t.skipToken(); // look-ahead
	}

	// public methods
	/**
	 * Initializes the global tokenizer used for parsing.
	 * 
	 * @param filename
	 *            the name of the file containing the source code for a Core
	 *            program
	 */
	public CoreParser(String filename) {
		t = new Tokenizer(filename); // initialize tokenizer
	}

	@Override
	public void parse(ParseTree p) {
		try { // begin parsing
			if (t.getToken() != 1) { // must be program
				throw new ParseException("Expecting \"program\" at "
						+ t.getTokenName(), 0);
			}
			p.setNT(1); // program
			p.setAlt(1); // only one for program

			p.createBranch(1); // decl seq
			p.goDown(1);
			t.skipToken(); // get look-ahead token
			parseDeclSeq(p);
			p.goUp();

			if (t.getToken() != 2) { // begin
				throw new ParseException("Expecting \"begin\" at "
						+ t.getTokenName(), 0);
			}
			p.createBranch(2); // stmt seq
			p.goDown(2);
			t.skipToken(); // look-ahead token
			parseStmtSeq(p);
			p.goUp();

			if (t.getToken() != 3) { // end
				throw new ParseException("Expecting \"end\" at "
						+ t.getTokenName(), 0);
			}
			t.skipToken(); // get EOF token
			if (t.getToken() != 33) { // EOF
				throw new ParseException("No tokens allowed after program end",
						t.getToken());
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}
	}
}
