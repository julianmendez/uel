package de.tudresden.inf.lat.uel.sat.type;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * An object of this class is an output of a SAT solver. In its string
 * representation it has a line containing either SAT (satisfiable) or UNSAT
 * (unsatisfiable), and, if satisfiable, a model.
 * 
 * @author Julian Mendez
 */
public class SatOutput {

	private Set<Integer> clause = new TreeSet<>();
	private boolean satisfiable = false;

	/**
	 * Constructs a new SAT output
	 * 
	 * @param satisf
	 *            <code>true</code> if and only if the SAT is satisfiable
	 * @param c
	 *            in case the SAT problem is satisfiable, the set of literals
	 *            satisfying the problem, otherwise this argument is ignored
	 */
	public SatOutput(boolean satisf, Collection<Integer> c) {
		if (c == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.satisfiable = satisf;
		if (satisf) {
			this.clause.addAll(c);
			this.clause.remove(SatSolver.END_OF_CLAUSE);
		}
	}

	/**
	 * Retrieve the model.
	 * 
	 * @return a set of literal identifiers
	 */
	public Set<Integer> getOutput() {
		return Collections.unmodifiableSet(this.clause);
	}

	/**
	 * @return a value indicating whether the problem is satisfiable
	 */
	public boolean isSatisfiable() {
		return this.satisfiable;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append((this.satisfiable ? SatSolver.SAT : SatSolver.UNSAT));
		sbuf.append(SatSolver.NEWLINE);
		for (Integer e : this.clause) {
			sbuf.append(e);
			sbuf.append(SatSolver.SPACE);
		}
		sbuf.append(SatSolver.END_OF_CLAUSE);
		sbuf.append(SatSolver.NEWLINE);
		return sbuf.toString();
	}

}
