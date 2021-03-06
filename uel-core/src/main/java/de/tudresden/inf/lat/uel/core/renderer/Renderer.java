package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Set;

import de.tudresden.inf.lat.uel.core.processor.ShortFormProvider;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

abstract class Renderer<ExpressionType, AxiomsType> {

	private final AtomManager atomManager;
	private final ShortFormProvider provider;
	private final Set<Definition> background;
	private final boolean restrictToUserVariables;

	protected Renderer(AtomManager atomManager, ShortFormProvider provider, Set<Definition> background) {
		this.atomManager = atomManager;
		this.provider = provider;
		this.background = background;
		this.restrictToUserVariables = (background != null);
	}

	protected abstract AxiomsType finalizeAxioms();

	protected abstract ExpressionType finalizeExpression();

	private Set<Integer> getDefinition(Integer atomId) {
		for (Definition definition : background) {
			if (definition.getDefiniendum().equals(atomId)) {
				return definition.getRight();
			}
		}
		throw new IllegalArgumentException("Atom has no definition.");
	}

	private String getShortForm(String id) {
		if (provider == null) {
			return id;
		}

		String label = id;
		if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
			label = id.substring(0, id.length() - AtomManager.UNDEF_SUFFIX.length());
		}
		String str = provider.getShortForm(label);
		if (str != null) {
			label = str;
		}
		if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
			label += AtomManager.UNDEF_SUFFIX;
		}

		return "<" + label + ">";
	}

	protected abstract void initialize();

	public ExpressionType renderAtom(Integer atomId) {
		initialize();
		translateAtom(atomId);
		return finalizeExpression();
	}

	public AxiomsType renderAxioms(Set<? extends Axiom> axioms) {
		initialize();
		translateAxioms(axioms);
		return finalizeAxioms();
	}

	public ExpressionType renderConjunction(Set<Integer> atomIds) {
		initialize();
		translateConjunction(atomIds);
		return finalizeExpression();
	}

	public AxiomsType renderGoal(Goal input) {
		initialize();
		translateAxioms(input.getDefinitions());
		translateAxioms(input.getEquations());
		translateAxioms(input.getSubsumptions());
		translateAxioms(input.getDisequations());
		translateAxioms(input.getDissubsumptions());
		return finalizeAxioms();
	}

	public String renderName(Integer atomId) {
		return getShortForm(atomManager.printConceptName(atomId));
	}

	public String renderRole(Integer roleId) {
		return getShortForm(atomManager.getRoleName(roleId));
	}

	public ExpressionType renderTop() {
		initialize();
		translateTop();
		return finalizeExpression();
	}

	public AxiomsType renderUnifier(Unifier unifier) {
		initialize();
		for (Definition definition : unifier.getDefinitions()) {
			if (!restrictToUserVariables || atomManager.getUserVariables().contains(definition.getDefiniendum())) {
				translateAxiom(definition);
			}
		}
		return finalizeAxioms();
	}

	protected ExpressionType translateAtom(Integer atomId) {
		if (atomManager.getExistentialRestrictions().contains(atomId)) {
			Integer childId = atomManager.getChild(atomId);
			String roleName = renderRole(atomManager.getExistentialRestriction(atomId).getRoleId());
			return translateExistentialRestriction(roleName, childId);
		} else {
			return translateName(atomId);
		}
	}

	protected abstract AxiomsType translateAxiom(Axiom axiom);

	public AxiomsType translateAxioms(Set<? extends Axiom> axioms) {
		AxiomsType ret = null;
		for (Axiom axiom : axioms) {
			ret = translateAxiom(axiom);
		}
		return ret;
	}

	protected ExpressionType translateChild(Integer childId) {
		if (restrictToUserVariables && atomManager.getFlatteningVariables().contains(childId)) {
			return translateConjunction(getDefinition(childId));
		} else {
			return translateName(childId);
		}
	}

	protected ExpressionType translateConjunction(Set<Integer> atomIds) {
		if (atomIds.isEmpty()) {
			return translateTop();
		} else if (atomIds.size() == 1) {
			return translateAtom(atomIds.iterator().next());
		} else {
			return translateTrueConjunction(atomIds);
		}
	}

	protected abstract ExpressionType translateExistentialRestriction(String roleName, Integer childId);

	protected abstract ExpressionType translateName(Integer atomId);

	protected abstract ExpressionType translateTop();

	protected abstract ExpressionType translateTrueConjunction(Set<Integer> atomIds);

}
