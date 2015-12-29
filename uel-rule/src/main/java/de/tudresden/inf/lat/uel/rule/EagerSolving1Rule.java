package de.tudresden.inf.lat.uel.rule;

import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * This class implements the first part of the rule 'Eager Solving' of the
 * rule-based algorithm for unification in EL.
 * 
 * @author Stefan Borgwardt
 */
final class EagerSolving1Rule extends EagerRule {

	@Override
	Application getFirstApplication(FlatSubsumption sub, Assignment assign) {
		Atom head = sub.getHead();
		for (Atom at : sub.getBody()) {
			if (at.equals(head)) {
				return new Application();
			}
		}
		return null;
	}

	@Override
	Result apply(FlatSubsumption sub, Assignment assign, Application application) {
		return new Result(sub, application);
	}

	@Override
	public String shortcut() {
		return "ES1";
	}

}
