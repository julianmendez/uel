package de.tudresden.inf.lat.uel.rule;

/**
 * This interface describes a rule of the rule-based unification algorithm for EL. It is possible
 * to query for the first and subsequent possible rule applications for a subsumption.
 * 
 * Once a rule application has been determined, it can be applied to a subsumption.
 * 
 * @author Stefan Borgwardt
 */
abstract class Rule {

	/**
	 * Returns the first possible application of this rule to the given subsumption.
	 * 
	 * @param sub the subsumption this rule shall be applied to
	 * @param assign the current assignment
	 * @return a rule application describing the arguments needed to apply this rule or 'null' if
	 * the rule is not applicable 
	 */
	public abstract Application getFirstApplication(Subsumption sub, Assignment assign);
	
	/**
	 * Returns the next application of this rule to the given subsumption. It is important
	 * that the parameters 'sub' and 'assign' are the same as those used to obtain 'previous'.
	 * 
	 * @param sub the subsumption this rule shall be applied to
	 * @param assign the current assignment
	 * @param previous the previous rule application returned by 'getFirstApplication' or
	 * 'getNextApplication'
	 * @return a rule application describing the arguments needed to apply this rule or 'null' if
	 * there are no more ways to apply this rule
	 */
	public abstract Application getNextApplication(Subsumption sub, Assignment assign, Application previous);

	/**
	 * Applies this rule to the given subsumption using the arguments stored in 'application' and
	 * returns the resulting change to the set of subsumptions and the current assignment.  It is
	 * important that the parameters 'sub' and 'assign' are the same as those used to obtain
	 * 'application'.
	 *
	 * @param sub the subsumption this rule shall be applied to
	 * @param assign the current assignment
	 * @param application
	 * @return the result of the application
	 */
	public abstract Result apply(Subsumption sub, Assignment assign, Application application); 

	public abstract String shortcut();
	
	class Application {

		final Rule rule() {
			return Rule.this;
		}
		
		@Override
		public String toString() {
			return rule().shortcut();
		}

	}
	
}
