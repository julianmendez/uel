package de.tudresden.inf.lat.uel.core.sat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * An object of this class represents a SAT input file.
 * 
 * @author Julian Mendez
 */
public class SatInput {

	private static final String newLine = "\n";
	private static final String P_CNF = "p cnf";
	private static final String space = " ";

	private Set<Set<Integer>> clauses = new HashSet<Set<Integer>>();
	private Integer lastId = 0;

	/**
	 * Constructs a new SAT input.
	 */
	public SatInput() {
	}

	/**
	 * Adds a new clause. Empty clauses are ignored.
	 * 
	 * @param clause
	 *            new clause
	 */
	public boolean add(Collection<Integer> clause) {
		boolean ret = false;
		Set<Integer> newSet = new TreeSet<Integer>();
		newSet.addAll(clause);
		newSet.remove(0);
		if (!newSet.isEmpty()) {
			ret = this.clauses.add(Collections.unmodifiableSet(newSet));
			if (ret) {
				updateLastId(newSet);
			}
		}
		return ret;
	}

	/**
	 * Clears the set of clauses.
	 */
	public void clear() {
		this.clauses.clear();
		this.lastId = 0;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof SatInput) {
			SatInput other = (SatInput) o;
			ret = this.clauses.equals(other.clauses)
					&& this.lastId == other.lastId;
		}
		return ret;
	}

	/**
	 * Returns the clauses.
	 * 
	 * @return the clauses
	 */
	public Set<Set<Integer>> getClauses() {
		return Collections.unmodifiableSet(this.clauses);
	}

	@Override
	public int hashCode() {
		return this.clauses.hashCode();
	}

	/**
	 * Returns a string that has the DIMACS CNF format.
	 * 
	 * @return a string that has the DIMACS CNF format
	 */
	public String toCNF() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(P_CNF);
		sbuf.append(space);
		sbuf.append(this.lastId);
		sbuf.append(space);
		sbuf.append(this.clauses.size());
		sbuf.append(newLine);
		for (Set<Integer> clause : this.clauses) {
			for (Integer literal : clause) {
				sbuf.append(literal);
				sbuf.append(space);
			}
			sbuf.append(0);
			sbuf.append(newLine);
		}
		return sbuf.toString();
	}

	@Override
	public String toString() {
		return toCNF();
	}

	private boolean updateLastId(Set<Integer> newSet) {
		boolean ret = false;
		for (Integer elem : newSet) {
			Integer absElem = elem < 0 ? (-1) * elem : elem;
			if (absElem > this.lastId) {
				this.lastId = absElem;
				ret = true;
			}
		}
		return ret;
	}

}
