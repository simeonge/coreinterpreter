package core.interpreter;
/**
 * Printer that pretty-prints a program given in the parse tree.
 * 
 * @author Simeon Georgiev
 */
public interface Printer {

	/**
	 * Pretty-prints the program given in the parse tree.
	 * 
	 * @param p
	 *            the parse tree representing the program to be printed
	 */
	void print(ParseTree p);
}
