package de.tudresden.inf.lat.uel.rule;

final class EagerExtensionRule extends EagerRule {

	@Override
	public Application getFirstApplication(Subsumption sub, Assignment assign) {
		// extract a variable from the body of sub
		// if there is more than one such variable, this rule does not apply
		Integer var = -1;
		for (FlatAtom at : sub.getBody()) {
			if (at.isVariable()) {
				if ((var > -1) && (var != at.getConceptName())) {
					return null;
				}
				var = at.getConceptName();
			}
		}
		if (var == -1) {
			return null;
		}
		// check whether the rest of the body is contained in the assignment of the variable
		for (FlatAtom at : sub.getBody()) {
			if (!at.isVariable() && !assign.getSubsumers(var).contains(at)) {
				return null;
			}
		}
		return new Application(var);
	}

	@Override
	public Result apply(Subsumption sub, Assignment assign, Rule.Application application) {
		if (!(application instanceof Application)) {
			throw new IllegalArgumentException("Expected rule application of type EagerExtensionRule.Application.");
		}
		Application appl = (Application) application;
		if (assign.makesCyclic(appl.var, sub.getHead())) {
			return new Result(sub, application, false);
		}
		Result res = new Result(sub, application);
		res.getNewSubsumers().add(appl.var, sub.getHead());
		return res;
	}
	
	@Override
	public String shortcut() {
		return "EEx";
	}
	
	private final class Application extends Rule.Application {
		
		protected Integer var;
		
		protected Application(Integer var) {
			this.var = var;
		}
		
		@Override
		public String toString() {
			return "EEx/" + var;
		}
		
	}

}
