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
 * 
 * @author Julian Mendez
 */
public class UelProcessorFactory {

	public static final String RULE_BASED_PROCESSOR = "Rule-based processor";
	public static final String SAT_PROCESSOR = "SAT processor";

	public static UelProcessor createProcessor(String name, UelInput input) {
		UelProcessor ret;
		if (name.equals(RULE_BASED_PROCESSOR)) {
			ret = new RuleProcessor(input);
		} else if (name.equals(SAT_PROCESSOR)) {
			ret = new SatProcessor(input);
		} else {
			throw new IllegalArgumentException("Unknown processor : '" + name
					+ "'.");
		}
		return ret;
	}

	public static Collection<String> getProcessorNames() {
		List<String> ret = new ArrayList<String>();
		ret.add(SAT_PROCESSOR);
		ret.add(RULE_BASED_PROCESSOR);
		return Collections.unmodifiableCollection(ret);
	}

}
