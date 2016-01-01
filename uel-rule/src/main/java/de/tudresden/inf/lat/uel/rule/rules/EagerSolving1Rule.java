package de.tudresden.inf.lat.uel.rule.rules;

import de.tudresden.inf.lat.uel.rule.Assignment;
import de.tudresden.inf.lat.uel.rule.FlatSubsumption;
import de.tudresden.inf.lat.uel.rule.Result;
import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * This class implements the first part of the rule 'Eager Solving' of the
 * rule-based algorithm for unification in EL.
 * 
 * @author Stefan Borgwardt
 */
public final class EagerSolving1Rule extends EagerRule {

	@Override
	public Application getFirstApplication(FlatSubsumption sub, Assignment assign) {
		Atom head = sub.getHead();
		for (Atom at : sub.getBody()) {
			if (at.equals(head)) {
				return new Application();
			}
		}
		return null;
	}

	@Override
	public Result apply(FlatSubsumption sub, Assignment assign, Application application) {
		return new Result(sub, application);
	}

	@Override
	public String shortcut() {
		return "ES1";
	}

}
