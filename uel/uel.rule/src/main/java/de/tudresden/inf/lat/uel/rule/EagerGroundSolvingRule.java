package de.tudresden.inf.lat.uel.rule;



final class EagerGroundSolvingRule extends EagerRule {

	@Override
	public Application getFirstApplication(Subsumption sub, Assignment assign) {
		if (sub.isGround()) {
			return new Application();
		}
		return null;
	}

	@Override
	public Result apply(Subsumption sub, Assignment assign, Application application) {
		return new Result(sub, application, sub.getBody().contains(sub.getHead()));
	}
	
	@Override
	public String shortcut() {
		return "EGr";
	}

}
