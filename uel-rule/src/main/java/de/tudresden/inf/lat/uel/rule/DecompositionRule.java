package de.tudresden.inf.lat.uel.rule;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

/**
 * This class implements the rule 'Decomposition' of the rule-based algorithm
 * for unification in EL.
 * 
 * @author Stefan Borgwardt
 */
final class DecompositionRule extends Rule {

	@Override
	Application getFirstApplication(Subsumption sub, Assignment assign) {
		if (!sub.getHead().isExistentialRestriction()) {
			return null;
		}
		Integer role = ((ExistentialRestriction) sub.getHead()).getRoleId();
		for (Atom at : sub.getBody()) {
			if (at.isExistentialRestriction()) {
				if (((ExistentialRestriction) at).getRoleId().equals(role)) {
					return new Application(role, at);
				}
			}
		}
		return null;
	}

	@Override
	Application getNextApplication(Subsumption sub, Assignment assign, Rule.Application previous) {
		if (!(previous instanceof Application)) {
			throw new IllegalArgumentException("Expected rule application of type DecompositionRule.Application.");
		}
		Application appl = (Application) previous;
		for (int i = sub.getBody().indexOf(appl.at) + 1; i < sub.getBody().size(); i++) {
			Atom at = sub.getBody().get(i);
			if (at.isExistentialRestriction()) {
				if (((ExistentialRestriction) at).getRoleId().equals(appl.role)) {
					appl.at = sub.getBody().get(i);
					return appl;
				}
			}
		}
		return null;
	}

	@Override
	Result apply(Subsumption sub, Assignment assign, Rule.Application application) {
		if (!(application instanceof Application)) {
			throw new IllegalArgumentException("Expected rule application of type DecompositionRule.Application.");
		}
		Result res = new Result(sub, application);
		ConceptName head = sub.getHead().getConceptName();
		ConceptName body = ((Application) application).at.getConceptName();
		res.getNewUnsolvedSubsumptions().add(new Subsumption(body, head));
		return res;
	}

	@Override
	public String shortcut() {
		return "Dec";
	}

	private final class Application extends Rule.Application {

		protected Integer role;
		protected Atom at;

		protected Application(Integer role, Atom at) {
			this.role = role;
			this.at = at;
		}

		@Override
		public String toString() {
			return "Dec/" + role + "/" + at;
		}

	}
}
