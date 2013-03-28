package de.tudresden.inf.lat.uel.sat.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An object of this class represents a SAT input file.
 * 
 * @author Julian Mendez
 */
public class SatInput {

	/**
	 * Converts a given clause into an array of integers.
	 * 
	 * @param clause
	 *            the clause
	 * @return an array containing exactly the literal identifiers of the clause
	 */
	public static int[] toArray(Set<Integer> clause) {
		int[] ret = new int[clause.size()];
		int index = 0;
		for (Integer var : clause) {
			ret[index] = var;
			index++;
		}
		return ret;
	}

	/**
	 * Formats a clause as "hard" clause in WCNF format.
	 * 
	 * @param clause
	 *            the clause
	 * @param weight
	 *            the weight for the clause
	 * @return a string containing the clause in WCNF format
	 */
	public static String toWCNF(Set<Integer> clause, int weight) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(weight);
		sbuf.append(Solver.SPACE);
		for (Integer literal : clause) {
			sbuf.append(literal);
			sbuf.append(Solver.SPACE);
		}
		sbuf.append(Solver.END_OF_CLAUSE);
		sbuf.append(Solver.NEWLINE);
		return sbuf.toString();
	}

	/**
	 * Produce the first line of a WCNF file.
	 * 
	 * @param nbVars
	 *            the maximal number of variables of the problem
	 * @param nbClauses
	 *            the number of clauses of the problem
	 * @param maxWeight
	 *            the weight for the "hard" clauses
	 * @return the "p wcnf ..." line starting a WCNF file
	 */
	public static String WCNFline(int nbVars, int nbClauses, int maxWeight) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(Solver.P_WCNF);
		sbuf.append(Solver.SPACE);
		sbuf.append(nbVars);
		sbuf.append(Solver.SPACE);
		sbuf.append(nbClauses);
		sbuf.append(Solver.SPACE);
		sbuf.append(maxWeight);
		sbuf.append(Solver.NEWLINE);
		return sbuf.toString();
	}

	private Collection<Set<Integer>> clauses = new ArrayList<Set<Integer>>();

	private Integer lastId = 0;

	private Set<Integer> minimizeLiterals = new HashSet<Integer>();

	/**
	 * Constructs a new SAT input.
	 */
	public SatInput() {
	}

	/**
	 * Adds a new non-empty clause.
	 * 
	 * @param clause
	 *            new non-empty clause
	 * @return a value indicating whether the SatInput was changed
	 * 
	 */
	public boolean add(Set<Integer> clause) {
		if (clause == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (clause.isEmpty()) {
			throw new IllegalArgumentException("Clause cannot be empty.");
		}
		if (clause.contains(Solver.END_OF_CLAUSE)) {
			throw new IllegalArgumentException("Clause cannot contain "
					+ Solver.END_OF_CLAUSE + ".");
		}

		boolean ret = this.clauses.add(Collections.unmodifiableSet(clause));
		if (ret) {
			updateLastId(clause);
		}
		return ret;
	}

	/**
	 * Adds a set of new non-empty clauses.
	 * 
	 * @param clauses
	 *            set of new non-empty clauses
	 * @return a value indicating whether the SatInput was changed
	 */
	public boolean addAll(Collection<? extends Set<Integer>> clauses) {
		if (clauses == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = false;
		for (Set<Integer> clause : clauses) {
			boolean changed = add(clause);
			ret = ret || changed;
		}
		return ret;
	}

	/**
	 * Adds a literal to the set of literals that are to be minimized.
	 * 
	 * @param literal
	 *            the literal identifier
	 * @return true iff the set changed as a result of this operation
	 */
	public boolean addMinimizeLiteral(Integer literal) {
		return this.minimizeLiterals.add(literal);
	}

	/**
	 * Adds several literals to the set of literals that are to be minimized.
	 * 
	 * @param literals
	 *            a set of literal identifiers
	 * @return true iff the set changed as a result of this operation
	 */
	public boolean addMinimizeLiterals(Set<Integer> literals) {
		return this.minimizeLiterals.addAll(literals);
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
	public Collection<Set<Integer>> getClauses() {
		return Collections.unmodifiableCollection(this.clauses);
	}

	/**
	 * Returns the greatest propositional variable identifier.
	 * 
	 * @return the greatest propositional variable identifier
	 */
	public Integer getLastId() {
		return this.lastId;
	}

	/**
	 * Retrieve the set of literals that are to be minimized.
	 * 
	 * @return the literals to be minimized
	 */
	public Set<Integer> getMinimizeLiterals() {
		return Collections.unmodifiableSet(minimizeLiterals);
	}

	@Override
	public int hashCode() {
		return this.clauses.hashCode();
	}

	/**
	 * Returns this SAT input in DIMACS CNF format.
	 * 
	 * @return a string in DIMACS CNF format
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

	/**
	 * Returns this MaxSAT input in WCNF format.
	 * 
	 * @param maxWeight
	 *            the weight for the "hard" clauses
	 * @return a string in WNCF format
	 */
	public String toWCNF(int maxWeight) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(WCNFline(this.lastId, this.clauses.size()
				+ this.minimizeLiterals.size(), maxWeight));
		for (Set<Integer> clause : this.clauses) {
			sbuf.append(toWCNF(clause, maxWeight));
		}
		for (Integer lit : this.minimizeLiterals) {
			sbuf.append(1);
			sbuf.append(Solver.SPACE);
			sbuf.append(-lit);
			sbuf.append(Solver.SPACE);
			sbuf.append(Solver.END_OF_CLAUSE);
			sbuf.append(Solver.NEWLINE);
		}
		return sbuf.toString();
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
