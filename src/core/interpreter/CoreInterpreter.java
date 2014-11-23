package core.interpreter;
/**
 * Interpreter for the Core language.
 * 
 * @author Simeon Georgiev
 */
public final class CoreInterpreter {

	/**
	 * Takes a file containing the source code for a Core program, parses it,
	 * creating a parse tree representation of the program, then if no syntax
	 * errors are found pretty-prints the program, then executes the code.
	 * 
	 * @param args
	 *            args[0] - the name of the file containing the source code for
	 *            a Core program; args[1] - the name of the data file from which
	 *            the Core program reads;
	 */
	public static void main(String[] args) {
		assert args.length == 2 : "The interpreter takes two parameters.";

		ParseTree p = new CoreParseTree(); // create parse tree

		new CoreParser(args[0]).parse(p); // pass source file and tree
		new CorePrinter().print(p); // pass tree
		new CoreExecutor(args[1]).execute(p); // pass tree and data file
	}
}
