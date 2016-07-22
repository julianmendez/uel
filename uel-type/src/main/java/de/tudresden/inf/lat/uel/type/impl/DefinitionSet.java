/**
 * 
 */
package de.tudresden.inf.lat.uel.type.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import de.tudresden.inf.lat.uel.type.api.Definition;

/**
 * @author Stefan Borgwardt
 *
 */
public class DefinitionSet extends HashMap<Integer, Definition> implements Iterable<Definition> {

	private static final long serialVersionUID = -5104296756025334343L;

	public static DefinitionSet EMPTY_SET = new DefinitionSet(0);

	public DefinitionSet() {
		super();
	}

	public DefinitionSet(int initialSize) {
		super(initialSize);
	}

	public DefinitionSet(Collection<Definition> definitions) {
		super(definitions.size());
		definitions.forEach(this::add);
	}

	public DefinitionSet(DefinitionSet definitions) {
		super(definitions);
	}

	public boolean add(Definition def) {
		return super.put(def.getDefiniendum(), def) == null;
	}

	public Definition getDefinition(Integer atomId) {
		return super.get(atomId);
	}

	public Set<Integer> getDefiniens(Integer atomId) {
		Definition def = super.get(atomId);
		if (def != null) {
			return def.getDefiniens();
		} else {
			return null;
		}
	}

	@Override
	public Iterator<Definition> iterator() {
		return values().iterator();
	}

	public Stream<Definition> stream() {
		return values().stream();
	}

	public boolean contains(Definition def) {
		return containsValue(def);
	}
}
