package core.interpreter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizer for the Core language.
 * 
 * @author Simeon Georgiev
 */
public class Tokenizer implements Lexer {
	// Fields
	/**
	 * Array used to store the token numbers.
	 */
	private ArrayList<Integer> tokens = new ArrayList<Integer>();

	/**
	 * Array used to store the token names.
	 */
	private ArrayList<String> tokenNames = new ArrayList<String>();

	/**
	 * Current token.
	 */
	private int index;

	/**
	 * The number of tokens currently read.
	 */
	private int numTokens;

	/**
	 * Map of word/symbol to token number, containing legal keywords and
	 * symbols.
	 */
	private Map<String, Integer> wordSym = new HashMap<String, Integer>();

	/**
	 * Map of index to value, containing the values of integer tokens in the
	 * program.
	 */
	private Map<Integer, Integer> intValues = new HashMap<Integer, Integer>();

	/**
	 * Map of index to name, containing the names of identifier tokens in the
	 * program.
	 */
	private Map<Integer, String> idNames = new HashMap<Integer, String>();

	/**
	 * Buffer for token being read currently.
	 */
	private StringBuffer buffer = new StringBuffer("");

	/**
	 * Reader used to read the input file.
	 */
	private BufferedReader in;

	// private methods
	/**
	 * Builds map of tokens and their token numbers.
	 */
	private void buildTokenMap() {
		// add keywords
		this.wordSym.put("program", 1);
		this.wordSym.put("begin", 2);
		this.wordSym.put("end", 3);
		this.wordSym.put("int", 4);
		this.wordSym.put("if", 5);
		this.wordSym.put("then", 6);
		this.wordSym.put("else", 7);
		this.wordSym.put("while", 8);
		this.wordSym.put("loop", 9);
		this.wordSym.put("read", 10);
		this.wordSym.put("write", 11);

		// add symbols
		this.wordSym.put(";", 12);
		this.wordSym.put(",", 13);
		this.wordSym.put("=", 14);
		this.wordSym.put("!", 15);
		this.wordSym.put("[", 16);
		this.wordSym.put("]", 17);
		this.wordSym.put("&&", 18);
		this.wordSym.put("||", 19);
		this.wordSym.put("(", 20);
		this.wordSym.put(")", 21);
		this.wordSym.put("+", 22);
		this.wordSym.put("-", 23);
		this.wordSym.put("*", 24);
		this.wordSym.put("!=", 25);
		this.wordSym.put("==", 26);
		this.wordSym.put("<", 27);
		this.wordSym.put(">", 28);
		this.wordSym.put("<=", 29);
		this.wordSym.put(">=", 30);

		// 31 is for integers, 32 for identifiers, 33 for EOF
	}

	/**
	 * Reads stream to get keyword token.
	 * 
	 * @requires Buffer contains a lower-case character.
	 * @ensures A valid keyword token is put in tokens array and buffer contains
	 *          the first character after current token.
	 * @throws IOException
	 *             if an IO error occurs
	 * @throws ParseException
	 *             if token is not valid
	 */
	private void getWordToken() throws IOException, ParseException {
		Pattern wordChar = Pattern.compile("[a-z]"); // lowercase char

		int ch = this.in.read(); // read char
		Matcher wordMatcher = wordChar.matcher("" + (char) ch); // match
		while (wordMatcher.matches()) {
			this.buffer.append((char) ch); // add to buffer
			ch = this.in.read(); // read next
			wordMatcher = wordChar.matcher("" + (char) ch); // match
		}

		// find keyword in map
		if (this.wordSym.containsKey(this.buffer.toString())) {
			this.tokens.add(this.wordSym.get(this.buffer.toString())); // add
			this.tokenNames.add(this.buffer.toString()); // add name
			this.buffer = new StringBuffer(""); // clear buffer
			if (ch != -1) {
				this.buffer.append((char) ch); // update buffer
			}
		} else { // no valid keyword was found
			throw new ParseException("Invalid token: " + this.buffer,
					this.numTokens);
		}
	}

	/**
	 * Reads stream to get symbol token.
	 * 
	 * @requires Buffer contains a punctuation character.
	 * @ensures A valid symbol token is put in tokens array and buffer contains
	 *          the first character after current token.
	 * @throws IOException
	 *             if an IO error occurs
	 * @throws ParseException
	 *             if token is not valid
	 */
	private void getSymbolToken() throws IOException, ParseException {
		// for symbols that are two characters long
		int ch = this.in.read();
		Pattern symChar = Pattern.compile("\\p{Punct}");
		Matcher symMatcher = symChar.matcher("" + (char) ch);
		String buffer2 = "";
		buffer2 = buffer2 + this.buffer.charAt(0);
		if (symMatcher.matches()) { // must be punctuation
			buffer2 = buffer2 + (char) ch;
		}

		// first attempt to find a two-char symbol, then one-char
		if (buffer2.length() == 2 && this.wordSym.containsKey(buffer2)) {
			this.tokens.add(this.wordSym.get(buffer2)); // add
			this.tokenNames.add(buffer2); // add name
			ch = this.in.read(); // read new character
			this.buffer = new StringBuffer("");
			if (ch != -1) {
				this.buffer.append((char) ch);
			}
		} else if (this.wordSym.containsKey(this.buffer.toString())) {
			this.tokens.add(this.wordSym.get(this.buffer.toString())); // add
			this.tokenNames.add(this.buffer.toString()); // add string
			this.buffer = new StringBuffer("");
			if (ch != -1) {
				this.buffer.append((char) ch); // first new char
			}
		} else { // no valid symbol was found
			throw new ParseException("Invalid token: " + this.buffer,
					this.numTokens);
		}
	}

	/**
	 * Reads stream to get integer token.
	 * 
	 * @requires Buffer contains the first (valid) character of this integer
	 *           token.
	 * @ensures A valid integer token is put in tokens array and the value of
	 *          the integer is put in intValues map and buffer contains the
	 *          first character after current token.
	 * @throws IOException
	 *             if an IO error occurs
	 */
	private void getIntToken() throws IOException {
		Pattern intChar = Pattern.compile("\\d"); // for digits

		int ch = this.in.read(); // first char
		Matcher intMatcher = intChar.matcher("" + (char) ch); // match
		while (intMatcher.matches()) {
			this.buffer.append((char) ch); // add to buffer
			ch = this.in.read(); // read next
			intMatcher = intChar.matcher("" + (char) ch); // match
		}

		// after ensuring each character is a digit, no need to match buffer

		// put integer in map
		this.intValues.put(this.numTokens,
				Integer.parseInt(this.buffer.toString()));
		this.tokens.add(31); // 31 for integers
		this.tokenNames.add(this.buffer.toString()); // add string
		this.buffer = new StringBuffer("");
		if (ch != -1) {
			this.buffer.append((char) ch); // update buffer
		}
	}

	/**
	 * Reads stream to get identifier token.
	 * 
	 * @requires Buffer contains the first (valid) character of this identifier
	 *           token.
	 * @ensures A valid identifier token is put in tokens array and the name of
	 *          the identifiers is put in the idNames map and buffer contains
	 *          the first character after current token.
	 * @throws IOException
	 *             if an IO error occurs
	 * @throws ParseException
	 *             if token is not valid
	 */
	private void getIdToken() throws IOException, ParseException {
		Pattern idChar = Pattern.compile("[A-Z]|\\d"); // for valid id chars

		int ch = this.in.read(); // read char
		Matcher charMatcher = idChar.matcher("" + (char) ch); // match
		while (charMatcher.matches()) {
			this.buffer.append((char) ch); // add to buffer
			ch = this.in.read(); // next char
			charMatcher = idChar.matcher("" + (char) ch); // match
		}

		// match whole token
		Pattern idPat = Pattern.compile("[A-Z]*\\d*"); // for valid ids
		Matcher idMatcher = idPat.matcher(this.buffer);
		if (idMatcher.matches()) {
			// put name in map
			this.idNames.put(this.numTokens, this.buffer.toString());
			// add to tokens array
			this.tokens.add(32); // 32 for identifiers
			this.tokenNames.add(this.buffer.toString()); // add string
			this.buffer = new StringBuffer("");
			if (ch != -1) {
				this.buffer.append((char) ch); // update buffer
			}
		} else { // token not valid
			throw new ParseException("Invalid token: " + this.buffer,
					this.numTokens);
		}
	}

	/**
	 * Reads the input file using the buffered reader and produces one token
	 * from the source code in the file. If the end of file is reached, the EOF
	 * token is produced, and the reader is closed.
	 * 
	 * @ensures One token is read and put into the array of tokens.
	 */
	private void produceToken() {
		// patterns
		Pattern lowerChar = Pattern.compile("[a-z]"); // for keywords
		Pattern punctChar = Pattern.compile("\\p{Punct}"); // for symbols
		Pattern digitChar = Pattern.compile("\\d"); // for integers
		Pattern upperChar = Pattern.compile("[A-Z]"); // for identifiers
		Pattern whiteChar = Pattern.compile("\\s"); // for whitespace

		int ch = 0;

		try {
			// get rid of any whitespaces
			while (whiteChar.matcher(this.buffer).matches()) {
				ch = this.in.read(); // read through all whitespaces
				this.buffer = new StringBuffer("");
				if (ch != -1) {
					this.buffer.append((char) ch);
				}
			}

			if (this.buffer.length() != 0) {
				// determine which token ch starts
				if (lowerChar.matcher(this.buffer).matches()) { // keyword
					this.getWordToken(); // keyword token
					// if next token is not a symbol, there must be whitespace
					if (this.buffer.length() != 0
							&& !punctChar.matcher(this.buffer).matches()) {
						if (!whiteChar.matcher(this.buffer).matches()) {
							throw new ParseException(
									"Whitespace required after "
											+ this.tokenNames.get(this.numTokens)
											+ " token.", this.numTokens);
						}
					}
				} else if (punctChar.matcher(this.buffer).matches()) { // symbol
					this.getSymbolToken(); // symbol token
				} else if (digitChar.matcher(this.buffer).matches()) { // integer
					this.getIntToken(); // integer token
					// if next token is not a symbol, there must be whitespace
					if (this.buffer.length() != 0
							&& !punctChar.matcher(this.buffer).matches()) {
						if (!whiteChar.matcher(this.buffer).matches()) {
							throw new ParseException(
									"Whitespace required after "
											+ this.tokenNames.get(this.numTokens)
											+ " token.", this.numTokens);
						}
					}
				} else if (upperChar.matcher(this.buffer).matches()) { // id
					this.getIdToken(); // identifier token
					// if next token is not a symbol, there must be whitespace
					if (this.buffer.length() != 0
							&& !punctChar.matcher(this.buffer).matches()) {
						if (!whiteChar.matcher(this.buffer).matches()) {
							throw new ParseException(
									"Whitespace required after "
											+ this.tokenNames.get(this.numTokens)
											+ " token.", this.numTokens);
						}
					}
				} else { // invalid character encountered
					throw new ParseException("Invalid character: " + ch, ch);
				}
				// token is produces so update index
				this.numTokens++;
			} else { // if buffer is empty then entire file is read
				this.tokens.add(33); // add EOF token
				this.tokenNames.add("EOF");
				if (this.in != null) {
					this.in.close(); // close file
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading from file.");
			System.exit(0); // terminate program
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(0); // terminate program
		}
	}

	// public methods
	/**
	 * This constructor builds a map of string to integer that contains all the
	 * legal tokens and their corresponding numbers. Then it opens the file
	 * called filename and produces the first token.
	 * 
	 * @param filename
	 *            the name of the input file containing the source Core program
	 */
	public Tokenizer(String filename) {
		this.buildTokenMap(); // build map of legal tokens

		BufferedReader reader = null;
		try {
			// create reader to read from file
			reader = new BufferedReader(new FileReader(filename));
			this.in = reader;

			// begin reading
			int ch = this.in.read();
			// use buffer
			if (ch != -1) {
				this.buffer.append((char) ch);
			}

			this.produceToken(); // get one token
		} catch (FileNotFoundException e) {
			System.err.println("Error opening file.");
			System.exit(0); // terminate program
		} catch (IOException e) {
			System.err.println("Error reading from file.");
			System.exit(0); // terminate program
		}
	}

	@Override
	public int getToken() {
		return this.tokens.get(this.index);
	}
	
	@Override
	public String getTokenName() {
		return this.tokenNames.get(this.index);
	}

	@Override
	public void skipToken() {
		if (this.tokens.get(this.index) != 33) { // if not EOF
			this.produceToken(); // get new token
			this.index++; // move cursor to it
		} // else nothing happens
	}

	@Override
	public int intVal() {
		// if current token is integer token
		if (this.tokens.get(this.index) == 31) { // 31 is the token # for ints
			return this.intValues.get(this.index);
		} else {
			return -1;
		}
	}

	@Override
	public String idName() {
		// if current token is id token
		if (this.tokens.get(this.index) == 32) { // 32 is the token # for ids
			return this.idNames.get(this.index);
		} else {
			return null;
		}
	}
}
