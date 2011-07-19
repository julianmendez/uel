package de.tudresden.inf.lat.uel.sattranslator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * An object of this class represents a SAT input file.
 * 
 * @author Julian Mendez
 * 
 */
public class SatInput {

	private static final String P_CNF = "p cnf ";
	private Set<Set<Integer>> clauses = new HashSet<Set<Integer>>();
	private Integer lastId = 0;

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
			ret = this.clauses.add(newSet);
			if (ret) {
				updateLastId(newSet);
			}
		}
		return ret;
	}

	/**
	 * Adds a new clause parsing a string containing integer numbers.
	 * 
	 * @param clause
	 *            line representing the clause
	 */
	public boolean add(String clause) {
		StringTokenizer stok = new StringTokenizer(clause);
		Set<Integer> litSet = new HashSet<Integer>();
		while (stok.hasMoreTokens()) {
			litSet.add(Integer.parseInt(stok.nextToken()));
		}
		return add(litSet);
	}

	/**
	 * Clears the clause set.
	 */
	public void clear() {
		this.clauses.clear();
		this.lastId = 0;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(P_CNF);
		sbuf.append(this.lastId);
		sbuf.append(" ");
		sbuf.append(this.clauses.size());
		sbuf.append("\n");
		for (Set<Integer> clause : this.clauses) {
			for (Integer literal : clause) {
				sbuf.append(literal);
				sbuf.append(" ");
			}
			sbuf.append(0);
			sbuf.append("\n");
		}
		return sbuf.toString();
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
