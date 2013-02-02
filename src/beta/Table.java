/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * A table.
 *
 * <p>A table is essentially a list of elements. The major difference between a
 * table and a list is that each element can occur at most once.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class Table<E> {

	/**
	 * The index that is used for non-elements.
	 */
	private static final int NO_ENTRY_VALUE = -1;
	/**
	 * The list of entries in this table.
	 */
	private final List<E> entries;
	/**
	 * The mapping from indexes to entries.
	 */
	private final TObjectIntMap<E> table;

	/**
	 * Creates a new table.
	 */
	public Table() {
		this.entries = new ArrayList<E>();
		this.table = new TObjectIntHashMap<E>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY_VALUE);
	}

	/**
	 * Inserts the specified element into this table and returns its index. If
	 * the element already is contained in this table, then just the index is
	 * returned.
	 *
	 * @param entry the element to be added to this table
	 * @return the index of the specified element
	 */
	public int addEntry(E entry) {
		int index = table.get(entry);
		if (index != NO_ENTRY_VALUE) {
			return index;
		} else {
			index = entries.size();
			entries.add(entry);
			table.put(entry, index);
			return index;
		}
	}

	/**
	 * Returns the index of the specified element in this table.
	 *
	 * @param entry the element to search for
	 * @return the index of the specified element in this table, or -1 if this
	 * table does not contain the element
	 */
	public int getIndex(E entry) {
		return table.get(entry);
	}

	/**
	 * Returns the number of elements in this table.
	 *
	 * @return the number of elements in this table
	 */
	public int getSize() {
		return entries.size();
	}

	/**
	 * Returns the list of elements in this table.
	 *
	 * @return the list of elements in this table
	 */
	public List<E> getEntries() {
		return entries;
	}

	/**
	 * Returns the element at the specified position in this table.
	 *
	 * @param index index of the element to return
	 * @return the element at the specified position in this table
	 * @throws IndexOutOfBoundsException if the index is out of range
	 * (<code>index < 0 || index >= getSize()</code>)
	 */
	public E getEntry(int index) throws IndexOutOfBoundsException {
		return entries.get(index);
	}
}
