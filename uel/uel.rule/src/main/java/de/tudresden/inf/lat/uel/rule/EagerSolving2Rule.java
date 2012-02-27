package de.tudresden.inf.lat.uel.rule;

final class EagerSolving2Rule extends EagerRule {

	@Override
	public Application getFirstApplication(Subsumption sub, Assignment assign) {
		FlatAtom head = sub.getHead();
		for (FlatAtom at : sub.getBody()) {
			if (at.isVariable()) {
				if (assign.getSubsumers(at.getConceptName()).contains(head)) {
					return new Application();
				}
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
