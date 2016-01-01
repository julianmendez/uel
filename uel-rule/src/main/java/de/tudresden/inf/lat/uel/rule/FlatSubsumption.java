package de.tudresden.inf.lat.uel.rule;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * This is a class representing a subsumption between a conjunction of flat
 * atoms (body) and a flat atom (head).
 * 
 * @author Stefan Borgwardt
 */
public class FlatSubsumption {

	private final List<Atom> body;
	private final Atom head;
	private boolean solved;
	private final int hashCode;

	/**
	 * Construct a new subsumption from the given atoms.
	 * 
	 * @param body
	 *            the body of the new subsumption
	 * @param head
	 *            the head of the new subsumption
	 */
	public FlatSubsumption(List<Atom> body, Atom head) {
		if ((body == null) || (head == null)) {
			throw new IllegalArgumentException("Body and head cannot be null.");
		}
		this.body = body;
		this.head = head;
		this.solved = false;
		this.hashCode = body.hashCode() * 31 + head.hashCode();
	}

	/**
	 * Construct a new subsumption with a single-atom body.
	 * 
	 * @param body
	 *            the body of the new subsumption
	 * @param head
	 *            the head of the new subsumption
	 */
	public FlatSubsumption(Atom body, Atom head) {
		if ((body == null) || (head == null)) {
			throw new IllegalArgumentException("Body and head cannot be null.");
		}
		this.body = Arrays.asList(new Atom[] { body });
		this.head = head;
		this.solved = false;
		this.hashCode = body.hashCode() * 31 + head.hashCode();
	}

	/**
	 * Retrieve the body of this subsumption.
	 * 
	 * @return a list containing the atoms of the body of this subsumption
	 */
	public List<Atom> getBody() {
		return body;
	}

	/**
	 * Retrieve the head of this subsumption.
	 * 
	 * @return the atom that is the head of this subsumption
	 */
	public Atom getHead() {
		return head;
	}

	/**
	 * Check whether this subsumption is already solved.
	 * 
	 * @return true iff this subsumption is solved
	 */
	boolean isSolved() {
		return solved;
	}

	/**
	 * Set the 'solved' status of this subsumption.
	 * 
	 * @param solved
	 *            a flag indicating whether this subsumption is solved
	 */
	void setSolved(boolean solved) {
		this.solved = solved;
	}

	/**
	 * Check whether this subsumption is ground.
	 * 
	 * @return true iff body and head are both ground
	 */
	public boolean isGround() {
		for (Atom at : body) {
			if (!at.isGround())
				return false;
		}
		if (!head.isGround())
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof FlatSubsumption))
			return false;

		FlatSubsumption other = (FlatSubsumption) obj;
		if (!body.containsAll(other.body))
			return false;
		if (!other.body.containsAll(body))
			return false;
		if (!head.equals(other.head))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (body.isEmpty()) {
			buf.append("top");
		} else {
			Iterator<Atom> iter = body.iterator();
			buf.append(iter.next());
			while (iter.hasNext()) {
				buf.append(",");
				buf.append(iter.next());
			}
		}
		buf.append(" < ");
		buf.append(head);
		if (solved) {
			buf.append("[s]");
		}
		return buf.toString();

	}

}
