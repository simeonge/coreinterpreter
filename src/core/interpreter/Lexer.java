package core.interpreter;
/**
 * Interface for the Tokenizer.
 * 
 * @author Simeon Georgiev
 */
public interface Lexer {

	/**
	 * Returns the current token.
	 * 
	 * @return the token number of the current token
	 */
	int getToken();

	/**
	 * Returns the current token name, or "EOF" for EOF.
	 * 
	 * @return the text of the current token
	 */
	String getTokenName();

	/**
	 * Skips current token, unless current token is EOF, in which case nothing
	 * happens.
	 */
	void skipToken();

	/**
	 * Returns the value of the current token if it is an integer, -1 otherwise.
	 * 
	 * @return the value of the current integer token
	 */
	int intVal();

	/**
	 * Returns the name of the current token if it is an identifier, null
	 * otherwise.
	 * 
	 * @return the name of the current identifier token
	 */
	String idName();
}
