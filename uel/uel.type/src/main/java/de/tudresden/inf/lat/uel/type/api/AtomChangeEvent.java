package de.tudresden.inf.lat.uel.type.api;

/**
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public class AtomChangeEvent {

	private final Atom atom;
	private final AtomChangeType type;

	public AtomChangeEvent(Atom atom, AtomChangeType type) {
		this.atom = atom;
		this.type = type;
	}

	public Atom getAtom() {
		return this.atom;
	}

	public AtomChangeType getChangeType() {
		return this.type;
	}

}
