/**
 * 
 */
package de.tudresden.inf.lat.uel.core.processor;

import org.semanticweb.owlapi.model.OWLClass;

/**
 * This class bundles all options to UEL.
 * 
 * @author Stefan Borgwardt
 */
public class UelOptions {

	/**
	 * Possible treatments for UNDEF names.
	 */
	public enum UndefBehavior {
		/**
		 * All UNDEF names are marked as constants.
		 */
		CONSTANTS,
		/**
		 * All UNDEF names are marked as definition variables.
		 */
		INTERNAL_VARIABLES,
		/**
		 * All UNDEF names are marked as user variables.
		 */
		USER_VARIABLES
	};

	public enum Verbosity {
		/**
		 * Detailed output.
		 */
		FULL(3),
		/**
		 * Standard output.
		 */
		NORMAL(2),
		/**
		 * Short output.
		 */
		SHORT(1),
		/**
		 * No output.
		 */
		SILENT(0);

		public int level;

		Verbosity(int level) {
			this.level = level;
		}
	}

	/**
	 * Indicates whether to expand simple primitive definitions like A ⊑ B and
	 * introduce the auxiliary name A_UNDEF ('true'), or to simply make A a
	 * constant and not further expand the definition ('false').
	 * 
	 * Default: true.
	 */
	public boolean expandPrimitiveDefinitions = true;

	/**
	 * Indicates whether solutions should be minimized w.r.t. the background
	 * ontology as a post-processing step.
	 * 
	 * Default: false
	 */
	public boolean minimizeSolutions = false;

	/**
	 * Indicates whether the computed solutions should be checked for
	 * equivalence (w.r.t. the background ontology) and no two equivalent
	 * solutions should be returned.
	 * 
	 * Default: false
	 */
	public boolean noEquivalentSolutions = false;

	/**
	 * Indicates how many RoleGroups to allow in the same substitution set
	 * (modulo subsumption). Only relevant for SNOMED.
	 * 
	 * Default: 0 (unlimited).
	 */
	public int numberOfRoleGroups = 0;

	/**
	 * Limits the number of siblings extracted from the background ontology (-1
	 * = unlimited). Leaves with more siblings will be ignored.
	 * 
	 * Default: -1
	 */
	public int numberOfSiblings = -1;

	/**
	 * Designates an alias to be used to express 'owl:Thing', e.g., 'SNOMED CT
	 * Concept'.
	 * 
	 * Default: null (no alias).
	 */
	public OWLClass owlThingAlias = null;

	/**
	 * Indicates whether UNDEF names are restricted to occur only in the context
	 * of their original definition.
	 * 
	 * Default: false.
	 */
	public boolean restrictUndefContext = false;

	/**
	 * Indicates whether 'SNOMED mode' is active. If yes, then certain
	 * syntactical restrictions are enabled, e.g., type compatibility and the
	 * number of occurrences of restrictions over the same role in one variable.
	 * 
	 * Default: false.
	 */
	public boolean snomedMode = false;

	/**
	 * Indicates how the UNDEF names should be treated.
	 * 
	 * Default: USER_VARIABLES.
	 */
	public UndefBehavior undefBehavior = UndefBehavior.USER_VARIABLES;

	/**
	 * Indicates which algorithm should be used for unification.
	 * 
	 * Default: SAT_BASED_ALGORITHM.
	 */
	public String unificationAlgorithmName = UnificationAlgorithmFactory.SAT_BASED_ALGORITHM;

	/**
	 * Indicates how much information about the unification process is printed
	 * to System.out.
	 * 
	 * Default: SILENT.
	 */
	public Verbosity verbosity = Verbosity.SILENT;

	/**
	 * Contains the URI for the 'RoleGroup' in SNOMED CT.
	 * 
	 * Default: 'http://snomed.info/id/609096000'
	 */
	public String snomedRoleGroupUri = "http://snomed.info/id/609096000";

	/**
	 * Contains the URI for the 'SNOMED CT Concept' in SNOMED CT.
	 * 
	 * Default: 'http://snomed.info/id/138875005'
	 */
	public String snomedCtConceptUri = "http://snomed.info/id/138875005";

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Expand primitive definitions: ");
		sb.append(expandPrimitiveDefinitions);
		sb.append(System.lineSeparator());

		sb.append("Minimize solutions: ");
		sb.append(minimizeSolutions);
		sb.append(System.lineSeparator());

		sb.append("Skip equivalent solutions: ");
		sb.append(noEquivalentSolutions);
		sb.append(System.lineSeparator());

		sb.append("Number of role groups: ");
		sb.append(numberOfRoleGroups);
		sb.append(System.lineSeparator());

		sb.append("Number of extracted siblings: ");
		sb.append(numberOfSiblings);
		sb.append(System.lineSeparator());

		sb.append("owl:Thing alias: ");
		sb.append(owlThingAlias);
		sb.append(System.lineSeparator());

		sb.append("Restrict UNDEF names to their original context: ");
		sb.append(restrictUndefContext);
		sb.append(System.lineSeparator());

		sb.append("SNOMED mode: ");
		sb.append(snomedMode);
		sb.append(System.lineSeparator());

		sb.append("Treat UNDEF names as: ");
		sb.append(undefBehavior);
		sb.append(System.lineSeparator());

		sb.append("Unification algorithm: ");
		sb.append(unificationAlgorithmName);
		sb.append(System.lineSeparator());

		sb.append("Verbosity: ");
		sb.append(verbosity);
		sb.append(System.lineSeparator());

		return sb.toString();
	}
}
