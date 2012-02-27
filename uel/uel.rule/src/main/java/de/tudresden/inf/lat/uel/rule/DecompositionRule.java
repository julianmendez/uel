package de.tudresden.inf.lat.uel.rule;

final class DecompositionRule extends Rule {

	@Override
	public Application getFirstApplication(Subsumption sub, Assignment assign) {
		if (!sub.getHead().isExistentialRestriction()) {
			return null;
		}
		Integer role = sub.getHead().getRole();
		for (FlatAtom at : sub.getBody()) {
			if (at.getRole() == role) {
				return new Application(role, at);
			}
		}
		return null;
	}

	@Override
	public Application getNextApplication(Subsumption sub, Assignment assign, Rule.Application previous) {
		if (!(previous instanceof Application)) {
			throw new IllegalArgumentException("Expected rule application of type DecompositionRule.Application.");
		}
		Application appl = (Application) previous;
		for (int i=sub.getBody().indexOf(appl.at)+1; i<sub.getBody().size(); i++) {
			if (sub.getBody().get(i).getRole() == appl.role) {
				appl.at = sub.getBody().get(i);
				return appl;
			}
		}
		return null;
	}

	@Override
	public Result apply(Subsumption sub, Assignment assign, Rule.Application application) {
		if (!(application instanceof Application)) {
			throw new IllegalArgumentException("Expected rule application of type DecompositionRule.Application.");
		}
		Result res = new Result(sub, application);
		res.getNewUnsolvedSubsumptions().add(new Subsumption(((Application) application).at.getChild(), sub.getHead().getChild()));
		return res;
	}
	
	@Override
	public String shortcut() {
		return "Dec";
	}

	private final class Application extends Rule.Application {
		
		protected Integer role;
		protected FlatAtom at;
		
		protected Application(Integer role, FlatAtom at) {
			this.role = role;
			this.at = at;
		}
		
		@Override
		public String toString() {
			return "Dec/" + role + "/" + at;
		}
		
	}
}
