package de.tudresden.inf.lat.uel.rule;

import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.rule.Rule.Application;


/**
 * Instances of this class describe the result of applying a rule of the rule-based unification
 * algorithm for EL to a subsumption. In particular, they specify newly created subsumptions and
 * new assignments. 
 * 
 * @author Stefan Borgwardt
 */
final class Result {
	
	private final Subsumption subsumption;
	private final Application application;
	private final Set<Subsumption> newUnsolvedSubsumptions = new HashSet<Subsumption>();
	private final Set<Subsumption> newSolvedSubsumptions = new HashSet<Subsumption>();
	private final Set<Subsumption> solvedSubsumptions = new HashSet<Subsumption>();
	private final Assignment newSubsumers = new Assignment();
	private boolean successful;
	
	public Result(Subsumption subsumption, Application application, boolean successful) {
		this.subsumption = subsumption;
		this.application = application;
		this.successful = successful;
	}
	
	public Result(Subsumption subsumption, Application application) {
		this(subsumption, application, true);
	}

	public void amend(Result res) {
		/* the result of (committed) eager rule applications should be added to the (committed)
		 * main result
		 */
		if (res.subsumption != null) {
			solveSubsumption(res.subsumption);
		}
		
		newUnsolvedSubsumptions.addAll(res.newUnsolvedSubsumptions);
		newSolvedSubsumptions.addAll(res.newSolvedSubsumptions);
		for (Subsumption sub : res.solvedSubsumptions) {
			solveSubsumption(sub);
		}
		newSubsumers.addAll(res.newSubsumers);
	}

	private void solveSubsumption(Subsumption sub) {
		if (newUnsolvedSubsumptions.remove(sub)) {
			newSolvedSubsumptions.add(sub);
		} else {
			solvedSubsumptions.add(sub);
		}
	}
	
	public Subsumption getSubsumption() {
		return subsumption;
	}
	
	public Application getApplication() {
		return application;
	}
	
	public boolean wasSuccessful() {
		return successful;
	}
	
	public void setSuccessful(boolean value) {
		successful = value;
	}

	public Assignment getNewSubsumers() {
		return newSubsumers;
	}

	public Set<Subsumption> getNewUnsolvedSubsumptions() {
		return newUnsolvedSubsumptions;
	}

	public Set<Subsumption> getNewSolvedSubsumptions() {
		return newSolvedSubsumptions;
	}

	public Set<Subsumption> getSolvedSubsumptions() {
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
