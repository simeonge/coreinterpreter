package core.interpreter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Parse Tree representing a Core program.
 * 
 * @author Simeon Georgiev
 */
public class CoreParseTree implements ParseTree {
	// fields
	/**
	 * A two-dimensional array representing the parse tree. Each row has five
	 * indices: The first index is the value of the current non-terminal, the
	 * second is the alternative in the production rule that is used, and the
	 * next tree are the row numbers of the children nodes. There can be at most
	 * 1000 rows.
	 */
	private int[][] arrRep = new int[5][1000];

	/**
	 * Cursor that keeps track of the current position in the array-tree.
	 * Initially 0, it points to the first node (row). Cannot be greater than
	 * 999.
	 */
	private int cursor;

	/**
	 * Keeps track of the parents of nodes as the tree is descended. When goDown
	 * is called, the parent is added to the stack. When goUp is called, the
	 * most cursor goes back to the most recent parent, and that parent is
	 * removed from the stack.
	 */
	private Stack<Integer> parents = new Stack<Integer>();

	/**
	 * A map of row number to value containing all the integer tokens in the
	 * Core program.
	 */
	private Map<Integer, Integer> ints = new HashMap<Integer, Integer>();

	/**
	 * A map containing all the identifiers in the Core program (may contain
	 * duplicates) and their positions. The positions of the identifier node are
	 * mapped to the identifier names. Used during printing.
	 */
	private Map<Integer, String> idNames = new HashMap<Integer, String>();

	/**
	 * A map containing all the unique identifiers and their values. Identifier
	 * names are mapped to integers that are their values. Used during
	 * execution, and the values are null until then.
	 */
	private Map<String, Integer> ids = new HashMap<String, Integer>();

	// private method
	/**
	 * Finds the next empty row the array-tree. Iterates through the rows from
	 * the current position to row 999, and finds the index of the first empty
	 * row.
	 * 
	 * @return The index of the first empty row in arrRep, -1 if all rows are
	 *         full.
	 */
	private int getNewRow() {
		for (int i = this.cursor; i < 1000; i++) {
			// check if empty
			if (this.arrRep[0][i] == 0 && this.arrRep[1][i] == 0
					&& this.arrRep[2][i] == 0 && this.arrRep[3][i] == 0
					&& this.arrRep[4][i] == 0) {
				return i;
			}
		}
		return -1;
	}

	// public methods
	@Override
	public void setNT(int num) {
		// first index at current position set to num
		this.arrRep[0][this.cursor] = num;
	}

	@Override
	public void setAlt(int num) {
		// second index at current position set to num
		this.arrRep[1][this.cursor] = num;
	}

	@Override
	public void createBranch(int no) {
		int newRow = this.getNewRow(); // get empty row
		assert newRow != -1 : "Parse Tree is out of memory."; // check if full
		this.arrRep[0][newRow] = -1; // mark as non-empty to avoid errors
		this.arrRep[no + 1][this.cursor] = newRow;
	}

	@Override
	public boolean declId(String name) {
		assert this.arrRep[0][this.cursor] == 18 : "Expecting <id> node.";
		this.idNames.put(this.cursor, name); // add name

		if (!this.ids.containsKey(name)) { // check for duplicates
			this.ids.put(name, null);
			return true;
		} else { // id not added; return error
			return false;
		}
	}

	@Override
	public void setIdVal(int num) {
		assert this.arrRep[0][this.cursor] == 18 : "Expecting <id> node.";
		assert this.idNames.get(this.cursor) != null : "Null name id idNames rep.";
		this.ids.put(this.idNames.get(this.cursor), num);
	}

	@Override
	public void setIdVal(String name, int val) {
		assert this.ids.containsKey(name) : "Name not in ids.";
		this.ids.put(name, val);		
	}

	@Override
	public Integer getIdVal(String name) {
		return this.ids.get(name);
	}

	@Override
	public boolean setIdName(String name) {
		assert this.arrRep[0][this.cursor] == 18 : "Expecting <id> node.";
		if (this.ids.containsKey(name)) {
			this.idNames.put(this.cursor, name);
			return true;
		} else { // undeclared variable encountered
			return false;
		}
	}

	@Override
	public void setInt(int num) {
		assert this.arrRep[0][this.cursor] == 20 : "Expecting <int> node.";
		this.ints.put(this.cursor, num);
	}

	@Override
	public int currNT() {
		return this.arrRep[0][this.cursor];
	}

	@Override
	public int currAlt() {
		return this.arrRep[1][this.cursor];
	}

	@Override
	public void goDown(int no) {
		this.parents.push(this.cursor);
		this.cursor = this.arrRep[no + 1][this.parents.peek()];
	}

	@Override
	public void goUp() {
		assert !this.parents.isEmpty() : "Empty parents stack";
		this.cursor = this.parents.pop();
	}

	@Override
	public Integer currIdVal() {
		assert this.arrRep[0][this.cursor] == 18 : "Expecting <id> node.";
		assert this.idNames.get(this.cursor) != null : "Null name in idNames rep.";
		return this.ids.get(this.idNames.get(this.cursor));
	}

	@Override
	public String currIdName() {
		assert this.arrRep[0][this.cursor] == 18 : "Expecting <id> node.";
		assert this.idNames.get(this.cursor) != null : "Null idname in idNames rep.";
		return this.idNames.get(this.cursor);
	}

	@Override
	public int currInt() {
		assert this.arrRep[0][this.cursor] == 20 : "Expecting <int> node.";
		assert this.ints.get(this.cursor) != null : "Null int in ints rep.";
		return this.ints.get(this.cursor);
	}
}
