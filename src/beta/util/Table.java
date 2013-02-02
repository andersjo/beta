/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta.util;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class Table<E> {

	private final List<E> entries;
	private final TObjectIntMap<E> table;

	public Table() {
		this.entries = new ArrayList<E>();
		this.table = new TObjectIntHashMap<E>();
	}

	public int addEntry(E entry) {
		int index = table.get(entry) - 1;
		if (index >= 0) {
			return index;
		} else {
			index = entries.size();
			entries.add(entry);
			table.put(entry, index + 1);
			return index;
		}
	}

	public int getIndex(E entry) {
		return table.get(entry) - 1;
	}

	public int getSize() {
		return entries.size();
	}

	public List<E> getEntries() {
		return entries;
	}

	public E getEntry(int index) {
		return entries.get(index);
	}
}
