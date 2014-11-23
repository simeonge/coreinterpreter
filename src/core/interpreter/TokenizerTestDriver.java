package core.interpreter;
/**
 * Test driver for the Tokenizer class.
 * 
 * @author Simeon Georgiev
 */
public final class TokenizerTestDriver {

	/**
	 * Takes a file, converts the source code to tokens, and outputs the token
	 * numbers, one per line.
	 * 
	 * @param args
	 *            contains file name of the file to be read from
	 */
	public static void main(String[] args) {
		assert (args.length == 1) : "args must contain file name.";

		// create tokenizer
		Lexer t = new Tokenizer(args[0]); // pass name of file

		while (t.getToken() != 33) { // 33 for EOF
			System.out.println(t.getToken());
			/*if (t.getToken() == 31) {
				System.out.println("int: " + t.intVal());
			}
			if (t.getToken() == 32) {
				System.out.println("id: " + t.idName());
			}*/
			t.skipToken(); // move to next token
		}
		System.out.println(t.getToken()); // output EOF token
	}
}
