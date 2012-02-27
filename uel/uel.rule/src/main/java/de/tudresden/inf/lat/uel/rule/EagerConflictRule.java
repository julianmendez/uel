package de.tudresden.inf.lat.uel.rule;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

final class EagerConflictRule extends EagerRule {

	@Override
	public Application getFirstApplication(Subsumption sub, Assignment assign) {
		if (sub.getHead().isConstant()) {
			// check if the constant appears again in the body of the subsumption
			for (Atom at : sub.getBody()) {
				if (at.isVariable()) {
					return null;
				}
				if (at.isConstant()) {
					if (sub.getHead().getConceptNameId().equals(at.getConceptNameId())) {
						return null;
					}
				}
			}
		}
		
		if (sub.getHead().isExistentialRestriction()) {
			// check if the role name appears again in the body of the subsumption
			Integer role = ((ExistentialRestriction) sub.getHead()).getRoleId();
			for (Atom at : sub.getBody()) {
				if (at.isVariable()) {
					return null;
				}
				if (at.isExistentialRestriction()) {
					if (role.equals(((ExistentialRestriction) at).getRoleId())) {
						return null;
					}
				}
			}
		}
		
		return new Application();
	}

	@Override
	public Result apply(Subsumption sub, Assignment assign, Application application) {
		return new Result(sub, application, false);
	}

	@Override
	public String shortcut() {
		return "ECo";
	}

}
