package de.tudresden.inf.lat.uel.rule;

import de.tudresden.inf.lat.uel.type.api.Atom;

final class EagerSolving1Rule extends EagerRule {

	@Override
	public Application getFirstApplication(Subsumption sub, Assignment assign) {
		Atom head = sub.getHead();
		for (Atom at : sub.getBody()) {
			if (at.equals(head)) {
				return new Application();
			}
		}
		return null;
	}
	
	@Override
	public Result apply(Subsumption sub, Assignment assign, Application application) {
		return new Result(sub, application); 
	}
	
	@Override
	public String shortcut() {
		return "ES1";
	}

}
