package de.tudresden.inf.lat.uel.rule;

/**
 * This class implements the rule 'Eager Ground Solving' of the rule-based
 * algorithm for unification in EL.
 * 
 * @author Stefan Borgwardt
 */
final class EagerGroundSolvingRule extends EagerRule {

	@Override
	Application getFirstApplication(Subsumption sub, Assignment assign) {
		if (sub.isGround()) {
			return new Application();
		}
		return null;
	}

	@Override
	Result apply(Subsumption sub, Assignment assign, Application application) {
		return new Result(sub, application, sub.getBody().contains(
				sub.getHead()));
	}

	@Override
	public String shortcut() {
		return "EGr";
	}

}
