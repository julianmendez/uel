package de.tudresden.inf.lat.uel.rule;

import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.rule.rules.Rule.Application;

/**
 * Instances of this class describe the result of applying a rule of the
 * rule-based unification algorithm for EL to a subsumption. In particular, they
 * specify newly created subsumptions and new assignments.
 * 
 * @author Stefan Borgwardt
 */
public final class Result {

	private final FlatSubsumption subsumption;
	private final Application application;
	private final Set<FlatSubsumption> newUnsolvedSubsumptions = new HashSet<>();
	private final Set<FlatSubsumption> newSolvedSubsumptions = new HashSet<>();
	private final Set<FlatSubsumption> solvedSubsumptions = new HashSet<>();
	private final Assignment newSubsumers = new Assignment();
	private boolean successful;

	/**
	 * Construct a new rule application result.
	 * 
	 * @param subsumption
	 *            the subsumption that triggered the rule application
	 * @param application
	 *            the rule application
	 * @param successful
	 *            a flag indicating whether the rule application was successful
	 */
	public Result(FlatSubsumption subsumption, Application application, boolean successful) {
		this.subsumption = subsumption;
		this.application = application;
		this.successful = successful;
	}

	/**
	 * Construct a new rule application result, assuming that the application
	 * was successful.
	 * 
	 * @param subsumption
	 *            the subsumption that triggered the rule application
	 * @param application
	 *            the rule application
	 */
	public Result(FlatSubsumption subsumption, Application application) {
		this(subsumption, application, true);
	}

	/**
	 * Adds the given result to this instance by appropriately merging the sets
	 * of new subsumptions and the new assignments.
	 * 
	 * @param res
	 *            the result that is to be added to the current result
	 */
	void amend(Result res) {
		/*
		 * the result of (committed) eager rule applications should be added to
		 * the (committed) main result
		 */
		if (res.subsumption != null) {
			solveSubsumption(res.subsumption);
		}

		newUnsolvedSubsumptions.addAll(res.newUnsolvedSubsumptions);
		newSolvedSubsumptions.addAll(res.newSolvedSubsumptions);
		for (FlatSubsumption sub : res.solvedSubsumptions) {
			solveSubsumption(sub);
		}
		newSubsumers.addAll(res.newSubsumers);
	}

	private void solveSubsumption(FlatSubsumption sub) {
		if (newUnsolvedSubsumptions.remove(sub)) {
			newSolvedSubsumptions.add(sub);
		} else {
			solvedSubsumptions.add(sub);
		}
	}

	/**
	 * Return the subsumption that triggered the rule application.
	 * 
	 * @return the triggering subsumption
	 */
	FlatSubsumption getSubsumption() {
		return subsumption;
	}

	/**
	 * Return the rule application that led to this result.
	 * 
	 * @return the rule application
	 */
	Application getApplication() {
		return application;
	}

	/**
	 * Return a flag indicating whether the rule application was successful.
	 * 
	 * @return 'false' iff the rule application failed
	 */
	boolean wasSuccessful() {
		return successful;
	}

	/**
	 * Set the success status of this rule application result.
	 * 
	 * @param value
	 *            a flag indicating whether the rule application was successful
	 */
	void setSuccessful(boolean value) {
		successful = value;
	}

	/**
	 * Retrieve the new assignments that resulted from the rule application or
	 * subsequent applications of eager rules.
	 * 
	 * @return an object specifying new non-variable atoms that were assigned to
	 *         variables
	 */
	public Assignment getNewSubsumers() {
		return newSubsumers;
	}

	/**
	 * Retrieve the new unsolved subsumptions that resulted from the rule
	 * application or subsequent applications of eager rules.
	 * 
	 * @return a set of new unsolved subsumptions
	 */
	public Set<FlatSubsumption> getNewUnsolvedSubsumptions() {
		return newUnsolvedSubsumptions;
	}

	/**
	 * Retrieve the new unsolved subsumptions that resulted from the rule
	 * application or subsequent applications of eager rules.
	 * 
	 * @return a set of new unsolved subsumptions
	 */
	Set<FlatSubsumption> getNewSolvedSubsumptions() {
		return newSolvedSubsumptions;
	}

	/**
	 * Retrieve the solved subsumptions that resulted from the rule application
	 * or subsequent applications of eager rules.
	 * 
	 * @return a set of solved subsumptions
	 */
	Set<FlatSubsumption> getSolvedSubsumptions() {
		return solvedSubsumptions;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{");
		buf.append(subsumption);
		buf.append(",");
		buf.append(application);
		buf.append(",");
		buf.append(successful);
		buf.append(",");
		buf.append(newUnsolvedSubsumptions);
		buf.append(",");
		buf.append(newSolvedSubsumptions);
		buf.append(",");
		buf.append(solvedSubsumptions);
		buf.append(",");
		buf.append(newSubsumers);
		buf.append("}");
		return buf.toString();
	}

}
