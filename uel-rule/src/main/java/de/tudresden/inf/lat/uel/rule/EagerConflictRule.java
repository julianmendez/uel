package de.tudresden.inf.lat.uel.rule;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

/**
 * This class implements an additional rule that detects whether the atom on the
 * right-hand side of the subsumption is structurally compatible with any atom
 * of the left-hand side.
 * 
 * @author Stefan Borgwardt
 */
final class EagerConflictRule extends EagerRule {

	@Override
	Application getFirstApplication(FlatSubsumption sub, Assignment assign) {
		if (sub.getHead().isConstant()) {
			// check if the constant appears again in the body of the
			// subsumption
			for (Atom at : sub.getBody()) {
				if (at.isVariable()) {
					return null;
				}
				if (at.isConstant()) {
					if (sub.getHead().equals(at)) {
						return null;
					}
				}
			}
		}

		if (sub.getHead().isExistentialRestriction()) {
			// check if the role name appears again in the body of the
			// subsumption
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
	Result apply(FlatSubsumption sub, Assignment assign, Application application) {
		return new Result(sub, application, false);
	}

	@Override
	public String shortcut() {
		return "ECo";
	}

}
