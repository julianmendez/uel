package de.tudresden.inf.lat.uel.rule;

final class EagerSolving1Rule extends EagerRule {

	@Override
	public Application getFirstApplication(Subsumption sub, Assignment assign) {
		FlatAtom head = sub.getHead();
		for (FlatAtom at : sub.getBody()) {
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
