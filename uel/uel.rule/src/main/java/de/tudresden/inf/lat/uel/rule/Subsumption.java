package de.tudresden.inf.lat.uel.rule;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A class representing a subsumption between a conjunction of flat atoms (body) and a flat atom
 * (head).
 * 
 * @author Stefan Borgwardt
 */
class Subsumption {

	private final List<FlatAtom> body;
	private final FlatAtom head;
	private boolean solved;
	
	/**
	 * Construct a new subsumption from the given atoms.
	 * 
	 * @param body the body of the new subsumption
	 * @param head the head of the new subsumption
	 */
	public Subsumption(List<FlatAtom> body, FlatAtom head) {
		if ((body == null) || (head == null)) {
			throw new IllegalArgumentException("Body and head cannot be null.");
		}
		this.body = body;
		this.head = head;
		this.solved = false;
	}
	
	/**
	 * Construct a new subsumption with a single-atom body.
	 * 
	 * @param body the body of the new subsumption
	 * @param head the head of the new subsumption
	 */
	public Subsumption(FlatAtom body, FlatAtom head) {
		if ((body == null) || (head == null)) {
			throw new IllegalArgumentException("Body and head cannot be null.");
		}
		this.body = Arrays.asList(new FlatAtom[]{body});
		this.head = head;
		this.solved = false;
	}
	
	List<FlatAtom> getBody() {
		return body;
	}
	
	FlatAtom getHead() {
		return head;
	}
	
	boolean isSolved() {
		return solved;
	}
	
	void setSolved(boolean solved) {
		this.solved = solved;
	}
	
	boolean isGround() {
		for (FlatAtom at : body) {
			if (!at.isGround()) return false;
		}
		if (!head.isGround()) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + body.hashCode();
		result = prime * result + head.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Subsumption)) return false;
		
		Subsumption other = (Subsumption) obj;
		if (!body.containsAll(other.body)) return false;
		if (!other.body.containsAll(body)) return false;
		if (!head.equals(other.head)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (body.isEmpty()) {
			buf.append("top");
		} else {
			Iterator<FlatAtom> iter = body.iterator();
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
