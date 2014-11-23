package core.interpreter;
/**
 * Parse Tree representing a Core program.
 * 
 * @author Simeon Georgiev
 */
public interface ParseTree {

	/**
	 * Sets the number of the current non-terminal node.
	 * 
	 * @param num
	 *            the number corresponding to the non-terminal node
	 */
	void setNT(int num);

	/**
	 * Sets the number of the alternative of the current non-terminal node.
	 * 
	 * @requires 1 <= num <= 6
	 * @param num
	 *            the number corresponding to the alternative
	 */
	void setAlt(int num);

	/**
	 * Adds a child to the current node at the specified index.
	 * 
	 * @requires 1 <= no <= 3
	 * @param no
	 *            the branch index
	 */
	void createBranch(int no);

	/**
	 * Adds an identifier declaration to the parse tree. Should be used during
	 * parsing of a declaration sequence. Ensures there are no duplicates.
	 * 
	 * @param name
	 *            the name of the identifier
	 * @return true if an identifier was added, false if it is a duplicate
	 */
	boolean declId(String name);

	/**
	 * Sets the value of the identifier which is at the current position in the
	 * tree. Should be used during execution to assign values to identifiers.
	 * 
	 * @param num
	 *            the value of the identifier
	 */
	void setIdVal(int num);

	/**
	 * Sets the value of the identifier called name. Should be used during
	 * execution of a read statement to assign values to identifiers whose node
	 * positions are not reachable.
	 * 
	 * @param name
	 *            the name of the identifier whose value is to be set
	 * @param val
	 *            the new value of the identifier
	 */
	void setIdVal(String name, int val);

	/**
	 * Returns the value of the identifier called name. Should be used during
	 * execution of a write statement to retrieve values of identifiers whose
	 * node positions are not reachable.
	 * 
	 * @param name
	 *            the name of the identifier whose value is sought
	 * @return the value of the identifier, or null if the identifier is
	 *         uninitialized
	 */
	Integer getIdVal(String name);

	/**
	 * Sets the name of the current identifier node. Should not be used during
	 * parsing of a declaration sequence. Ensures there are no undeclared
	 * identifiers.
	 * 
	 * @param name
	 *            the name of the identifier
	 * @return true if the identifier was previously declared, false otherwise
	 */
	boolean setIdName(String name);

	/**
	 * Sets the value of the current integer node.
	 * 
	 * @param num
	 *            the value of the integer
	 */
	void setInt(int num);

	/**
	 * Returns the value of current non-terminal node.
	 * 
	 * @return the number corresponding to the current non-terminal node
	 */
	int currNT();

	/**
	 * Returns the alternative of the current non-terminal node.
	 * 
	 * @return the number corresponding to the alternative
	 */
	int currAlt();

	/**
	 * Moves the cursor to the child node at the specified index.
	 * 
	 * @requires 1 <= no <= 3
	 * @param no
	 *            the branch index
	 */
	void goDown(int no);

	/**
	 * Moves the cursor to the parent of the current node, unless the current
	 * node is the top node.
	 */
	void goUp();

	/**
	 * Returns the value of the current identifier node.
	 * 
	 * @return the value of the identifier, or null if the identifier is
	 *         uninitialized
	 */
	Integer currIdVal();

	/**
	 * Returns the name of the current identifier node.
	 * 
	 * @return the name of the identifier
	 */
	String currIdName();

	/**
	 * Returns the value of the current integer node.
	 * 
	 * @return the value of the current integer
	 */
	int currInt();
}
