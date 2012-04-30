package de.tudresden.inf.lat.uel.rule;

import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * This class implements the rule 'Extension' of the rule-based algorithm for
 * unification in EL.
 * 
 * @author Stefan Borgwardt
 */
final class ExtensionRule extends Rule {

	@Override
	Application getFirstApplication(Subsumption sub, Assignment assign) {
		for (Atom at : sub.getBody()) {
			if (at.isVariable()) {
				return new Application(at);
			}
		}
		return null;
	}

	@Override
	Application getNextApplication(Subsumption sub, Assignment assign,
			Rule.Application previous) {
		if (!(previous instanceof Application)) {
			throw new IllegalArgumentException(
					"Expected rule application of type ExtensionRule.Application.");
		}
		Application appl = (Application) previous;
		for (int i = sub.getBody().indexOf(appl.at) + 1; i < sub.getBody()
				.size(); i++) {
			if (sub.getBody().get(i).isVariable()) {
				appl.at = sub.getBody().get(i);
				return appl;
			}
		}
		return null;
	}

	@Override
	Result apply(Subsumption sub, Assignment assign,
			Rule.Application application) {
		if (!(application instanceof Application)) {
			throw new IllegalArgumentException(
					"Expected rule application of type ExtensionRule.Application.");
		}
		Application appl = (Application) application;
		if (assign.makesCyclic(appl.at.getConceptNameId(), sub.getHead())) {
			return new Result(sub, application, false);
		}
		Result res = new Result(sub, application);
		res.getNewSubsumers().add(appl.at.getConceptNameId(), sub.getHead());
		return res;
	}

	@Override
	public String shortcut() {
		return "Ext";
	}

	private final class Application extends Rule.Application {

		protected Atom at;

		protected Application(Atom at) {
			this.at = at;
		}

		@Override
		public String toString() {
			return "Ext/" + at;
		}

	}

}
