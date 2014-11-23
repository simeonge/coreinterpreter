package core.interpreter;
/**
 * A parser that produces a parse tree representation of the program in the file
 * whose name is passed to the constructor.
 * 
 * @author Simeon Georgiev
 */
public interface Parser {

	/**
	 * Parses the given program and produces a parse tree.
	 * 
	 * @param p
	 *            the parse tree (initially empty) that represents the program
	 * @ensures The program is checked for syntax errors and a parse tree
	 *          representing the program is produced.
	 */
	void parse(ParseTree p);
}
