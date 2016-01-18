package de.tudresden.inf.lat.uel.rule.rules;

import de.tudresden.inf.lat.uel.rule.Assignment;
import de.tudresden.inf.lat.uel.rule.FlatSubsumption;
import de.tudresden.inf.lat.uel.rule.Result;
import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * This class implements the rule 'Extension' of the rule-based algorithm for
 * unification in EL.
 * 
 * @author Stefan Borgwardt
 */
public final class ExtensionRule extends Rule {

	@Override
	public Application getFirstApplication(FlatSubsumption sub, Assignment assign) {
		for (Atom at : sub.getBody()) {
			if (at.isVariable()) {
				return new Application(at);
			}
		}
		return null;
	}

	@Override
	public Application getNextApplication(FlatSubsumption sub, Assignment assign, Rule.Application previous) {
		if (!(previous instanceof Application)) {
			throw new IllegalArgumentException("Expected rule application of type ExtensionRule.Application.");
		}
		Application appl = (Application) previous;
		for (int i = sub.getBody().indexOf(appl.at) + 1; i < sub.getBody().size(); i++) {
			if (sub.getBody().get(i).isVariable()) {
				appl.at = sub.getBody().get(i);
				return appl;
			}
		}
		return null;
	}

	@Override
	public Result apply(FlatSubsumption sub, Assignment assign, Rule.Application application) {
		if (!(application instanceof Application)) {
			throw new IllegalArgumentException("Expected rule application of type ExtensionRule.Application.");
		}
		Application appl = (Application) application;
		if (assign.makesCyclic(appl.at, sub.getHead())) {
			return new Result(sub, application, false);
		}
		Result res = new Result(sub, application);
		res.getNewSubsumers().add(appl.at, sub.getHead());
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
