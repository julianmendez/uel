package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.tudresden.inf.lat.uel.rule.RuleProcessor;
import de.tudresden.inf.lat.uel.sat.solver.SatProcessor;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;

/**
 * A UEL processor factory constructs processors.
 * 
 * @author Julian Mendez
 */
public class UelProcessorFactory {

	public static final String RULE_BASED_ALGORITHM = "Rule-based algorithm";
	public static final String SAT_BASED_ALGORITHM = "SAT-based algorithm";

	/**
	 * Creates a processor with a given UEL input.
	 * 
	 * @param name
	 *            name of processor
	 * @param input
	 *            UEL input
	 * @return a new processor with a given UEL input
	 */
	public static UelProcessor createProcessor(String name, UelInput input) {
		UelProcessor ret;
		if (name.equals(RULE_BASED_ALGORITHM)) {
			ret = new RuleProcessor(input);
		} else if (name.equals(SAT_BASED_ALGORITHM)) {
			ret = new SatProcessor(input, true);
		} else {
			throw new IllegalArgumentException("Unknown processor : '" + name
					+ "'.");
		}
		return ret;
	}

	/**
	 * Returns all the processors that this factory can construct.
	 * 
	 * @return all the processors that this factory can construct
	 */
	public static Collection<String> getProcessorNames() {
		List<String> ret = new ArrayList<String>();
		ret.add(SAT_BASED_ALGORITHM);
		ret.add(RULE_BASED_ALGORITHM);
		return Collections.unmodifiableCollection(ret);
	}

}
