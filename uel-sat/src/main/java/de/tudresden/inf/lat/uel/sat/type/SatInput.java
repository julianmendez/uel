package de.tudresden.inf.lat.uel.sat.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An object of this class represents a SAT input file.
 * 
 * @author Julian Mendez
 */
public class SatInput {

	public static void appendCNFClause(StringBuffer sbuf, Collection<Integer> clause) {
		for (Integer literal : clause) {
			sbuf.append(literal);
			sbuf.append(SatSolver.SPACE);
		}
		sbuf.append(SatSolver.END_OF_CLAUSE);
		sbuf.append(SatSolver.NEWLINE);
	}

	public static void appendCNFLine(StringBuffer sbuf, int nbVars, int nbClauses) {
		sbuf.append(SatSolver.P_CNF);
		sbuf.append(SatSolver.SPACE);
		sbuf.append(nbVars);
		sbuf.append(SatSolver.SPACE);
		sbuf.append(nbClauses);
		sbuf.append(SatSolver.NEWLINE);
	}

	public static void appendWCNFClause(StringBuffer sbuf, Set<Integer> clause, int weight) {
		sbuf.append(weight);
		sbuf.append(SatSolver.SPACE);
		appendCNFClause(sbuf, clause);
	}

	public static void appendWCNFLine(StringBuffer sbuf, int nbVars, int nbClauses, int maxWeight) {
		sbuf.append(SatSolver.P_WCNF);
		sbuf.append(SatSolver.SPACE);
		sbuf.append(nbVars);
		sbuf.append(SatSolver.SPACE);
		sbuf.append(nbClauses);
		sbuf.append(SatSolver.SPACE);
		sbuf.append(maxWeight);
		sbuf.append(SatSolver.NEWLINE);
	}

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

	private Collection<Set<Integer>> clauses = new ArrayList<>();
	private Integer lastId = 0;
	private Set<Integer> minimizeLiterals = new HashSet<Integer>();
	private Collection<Set<Integer>> softClauses = new ArrayList<Set<Integer>>();

	/**
	 * Constructs a new SAT input.
	 */
	public SatInput() {
	}

	/**
	 * Adds a new unit clause.
	 * 
	 * @param literal
	 *            the only literal of the unit clause
	 */
	public void add(Integer literal) {
		add(Collections.singleton(literal));
	}

	/**
	 * Adds a new non-empty clause.
	 * 
	 * @param clause
	 *            new non-empty clause
	 */
	public void add(Set<Integer> clause) {
		if (clause == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (clause.isEmpty()) {
			throw new IllegalArgumentException("Clause cannot be empty.");
		}
		if (clause.contains(SatSolver.END_OF_CLAUSE)) {
			throw new IllegalArgumentException("Clause cannot contain " + SatSolver.END_OF_CLAUSE + ".");
		}

		clauses.add(clause);
		updateLastId(clause);
	}

	public void addImplication(Integer head, Integer... body) {
		addImplication(new HashSet<Integer>(Arrays.asList(head)), body);
	}

	public void addImplication(Set<Integer> head, Integer... body) {
		Arrays.stream(body).forEach(l -> head.add(-l));
		add(head);
	}

	/**
	 * Adds a literal to the set of literals that are to be minimized.
	 * 
	 * @param literal
	 *            the literal identifier
	 */
	public void addMinimizeLiteral(Integer literal) {
		addMinimizeLiterals(Collections.singleton(literal));
	}

	/**
	 * Adds several literals to the set of literals that are to be minimized.
	 * 
	 * @param literals
	 *            a set of literal identifiers
	 */
	public void addMinimizeLiterals(Set<Integer> literals) {
		minimizeLiterals.addAll(literals);
		updateLastId(literals);
	}

	public void addNegativeClause(Integer... body) {
		addImplication(new HashSet<Integer>(), body);
	}

	public void addNegativeSoftClause(Integer... body) {
		addSoftClause(Arrays.stream(body).map(l -> -l).collect(Collectors.toSet()));
	}

	public void addSoftClause(Set<Integer> clause) {
		softClauses.add(clause);
		updateLastId(clause);
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
			ret = this.clauses.equals(other.clauses) && this.lastId == other.lastId;
		}
		return ret;
	}

	/**
	 * Returns the clauses.
	 * 
	 * @return the clauses
	 */
	public Collection<Set<Integer>> getClauses() {
		return Collections.unmodifiableCollection(clauses);
	}

	public Collection<Set<Integer>> getSoftClauses() {
		return Collections.unmodifiableCollection(softClauses);
	}

	/**
	 * Returns the greatest propositional variable identifier.
	 * 
	 * @return the greatest propositional variable identifier
	 */
	public Integer getLastId() {
		return lastId;
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
		appendCNFLine(sbuf, lastId, clauses.size());
		for (Collection<Integer> clause : clauses) {
			appendCNFClause(sbuf, clause);
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
		appendWCNFLine(sbuf, lastId, clauses.size() + minimizeLiterals.size(), maxWeight);
		for (Set<Integer> clause : clauses) {
			appendWCNFClause(sbuf, clause, maxWeight);
		}
		for (Integer lit : minimizeLiterals) {
			appendWCNFClause(sbuf, Collections.singleton(-lit), 1);
		}
		return sbuf.toString();
	}

	private void updateLastId(Collection<Integer> newSet) {
		lastId = newSet.stream().map(Math::abs).reduce(lastId, Math::max);
	}

}
