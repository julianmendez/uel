package de.tudresden.inf.lat.uel.sat.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * An object of this class represents a SAT input file.
 * 
 * @author Julian Mendez
 */
public class SatInput {

	private Set<Collection<Integer>> clauses = new HashSet<Collection<Integer>>();
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
	 * @return a value indicating whether the SatInput was changed
	 */
	public boolean add(Collection<Integer> clause) {
		if (clause == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = false;
		Set<Integer> newSet = new TreeSet<Integer>();
		newSet.addAll(clause);
		newSet.remove(Solver.END_OF_CLAUSE);
		if (!newSet.isEmpty()) {
			List<Integer> newList = new ArrayList<Integer>();
			newList.addAll(newSet);
			ret = this.clauses.add(Collections.unmodifiableCollection(newList));
			if (ret) {
				updateLastId(newList);
			}
		}
		return ret;
	}

	/**
	 * Adds a set of new clauses. Empty clauses are ignored.
	 * 
	 * @param clauses
	 *            set of new clauses
	 * @return a value indicating whether the SatInput was changed
	 */
	public boolean addAll(Set<? extends Collection<Integer>> clauses) {
		if (clauses == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = false;
		for (Collection<Integer> clause : clauses) {
			boolean changed = add(clause);
			ret = ret || changed;
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
		boolean ret = (this == o);
		if (!ret && o instanceof SatInput) {
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
	public Set<Collection<Integer>> getClauses() {
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
		sbuf.append(Solver.P_CNF);
		sbuf.append(Solver.SPACE);
		sbuf.append(this.lastId);
		sbuf.append(Solver.SPACE);
		sbuf.append(this.clauses.size());
		sbuf.append(Solver.NEWLINE);
		for (Collection<Integer> clause : this.clauses) {
			for (Integer literal : clause) {
				sbuf.append(literal);
				sbuf.append(Solver.SPACE);
			}
			sbuf.append(Solver.END_OF_CLAUSE);
			sbuf.append(Solver.NEWLINE);
		}
		return sbuf.toString();
	}

	@Override
	public String toString() {
		return toCNF();
	}

	private boolean updateLastId(Collection<Integer> newSet) {
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
