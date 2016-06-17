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
		 * All UNDEF names are marked as user variables.
		 */
		USER_VARIABLES,
		/**
		 * All UNDEF names are marked as definition variables.
		 */
		INTERNAL_VARIABLES,
		/**
		 * All UNDEF names are marked as constants.
		 */
		CONSTANTS
	};

	public enum Verbosity {
		/**
		 * No output.
		 */
		SILENT,
		/**
		 * Standard output.
		 */
		NORMAL,
		/**
		 * Detailed output.
		 */
		FULL
	}

	/**
	 * Indicates whether 'SNOMED mode' is active. If yes, then certain
	 * syntactical restrictions are enabled, e.g., type compatibility and the
	 * number of occurrences of restrictions over the same role in one variable.
	 * 
	 * Default: false.
	 */
	public boolean snomedMode = false;

	/**
	 * Indicates how many RoleGroups to allow in the same substitution set
	 * (modulo subsumption). Only relevant for SNOMED.
	 * 
	 * Default: 0 (unlimited).
	 */
	public int numberOfRoleGroups = 0;

	/**
	 * Indicates whether UNDEF names are restricted to occur only in the context
	 * of their original definition.
	 * 
	 * Default: false.
	 */
	public boolean restrictUndefContext = false;

	/**
	 * Indicates whether to expand simple primitive definitions like A ⊑ B and
	 * introduce the auxiliary name A_UNDEF ('true'), or to simply make A a
	 * constant and not further expand the definition ('false').
	 * 
	 * Default: true.
	 */
	public boolean expandPrimitiveDefinitions = true;

	/**
	 * Indicates how much information about the unification process is printed
	 * to System.out.
	 * 
	 * Default: SILENT.
	 */
	public Verbosity verbosity = Verbosity.SILENT;

	/**
	 * Designates an alias to be used to express 'owl:Thing', e.g., 'SNOMED CT
	 * Concept'.
	 * 
	 * Default: null (no alias).
	 */
	public OWLClass owlThingAlias = null;

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
	 * Indicates whether unifiers should be minimized w.r.t. the background
	 * ontology as a post-processing step.
	 * 
	 * Default: false
	 */
	public boolean minimize = false;

	/**
	 * Indicates whether the computed solutions should be checked for
	 * equivalence (w.r.t. the background ontology) and no two equivalent
	 * solutions should be returned.
	 * 
	 * Default: false
	 */
	public boolean noEquivalentSolutions = false;

	/**
	 * Limits the number of siblings extracted from the background ontology (-1
	 * = unlimited).
	 * 
	 * Default: -1
	 */
	public int numberOfSiblings = -1;
}
