package core.interpreter;
/**
 * Executor that executes a program given a parse tree representing it. The
 * program reads input from a file whose name is passed to the constructor.
 * 
 * @author Simeon Georgiev
 */
public interface Executor {

	/**
	 * Takes a parse tree representing a Core program, and executes the program.
	 * 
	 * @param p
	 *            the parse tree representing the Core program
	 */
	void execute(ParseTree p);
}
