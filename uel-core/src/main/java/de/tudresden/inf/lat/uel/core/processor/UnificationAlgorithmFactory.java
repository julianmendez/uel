package de.tudresden.inf.lat.uel.core.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tudresden.inf.lat.uel.asp.solver.AspUnificationAlgorithm;
import de.tudresden.inf.lat.uel.rule.RuleBasedUnificationAlgorithm;
import de.tudresden.inf.lat.uel.sat.solver.SatUnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.UnificationAlgorithm;

/**
 * This factory constructs algorithms referred by name.
 * 
 * @author Julian Mendez
 */
public class UnificationAlgorithmFactory {

	public static final String RULE_BASED_ALGORITHM = "Rule-based algorithm";
	public static final String SAT_BASED_ALGORITHM = "SAT-based algorithm";
	public static final String SAT_BASED_ALGORITHM_MINIMAL = "SAT-based algorithm (minimal assignments)";
	public static final String ASP_BASED_ALGORITHM = "ASP-based algorithm";
	public static final String ASP_BASED_ALGORITHM_MINIMAL = "ASP-based algorithm (minimal assignments)";

	/**
	 * Creates an algorithm with a given UEL input.
	 * 
	 * @param name
	 *            name of algorithm
	 * @param input
	 *            UEL input
	 * @return a new algorithm with a given UEL input
	 */
	public static UnificationAlgorithm instantiateAlgorithm(String name, Goal input) {
		UnificationAlgorithm ret;
		if (name.equals(RULE_BASED_ALGORITHM)) {
			ret = new RuleBasedUnificationAlgorithm(input);
		} else if (name.equals(SAT_BASED_ALGORITHM)) {
			ret = new SatUnificationAlgorithm(input, false);
		} else if (name.equals(SAT_BASED_ALGORITHM_MINIMAL)) {
			ret = new SatUnificationAlgorithm(input, true);
		} else if (name.equals(ASP_BASED_ALGORITHM)) {
			ret = new AspUnificationAlgorithm(input, false);
		} else if (name.equals(ASP_BASED_ALGORITHM_MINIMAL)) {
			ret = new AspUnificationAlgorithm(input, true);
		} else {
			throw new IllegalArgumentException("Unknown algorithm : '" + name + "'.");
		}
		return ret;
	}

	/**
	 * Returns all the algorithms that this factory can construct.
	 * 
	 * @return all the algorithms that this factory can construct
	 */
	public static List<String> getAlgorithmNames() {
		List<String> ret = new ArrayList<>();
		ret.add(SAT_BASED_ALGORITHM);
		ret.add(SAT_BASED_ALGORITHM_MINIMAL);
		ret.add(RULE_BASED_ALGORITHM);
		ret.add(ASP_BASED_ALGORITHM);
		ret.add(ASP_BASED_ALGORITHM_MINIMAL);
		return Collections.unmodifiableList(ret);
	}

	public static Object shortString(String unificationAlgorithmName) {
		return unificationAlgorithmName.replace("-based algorithm", "").replace(" assignments", "");
	}

}
