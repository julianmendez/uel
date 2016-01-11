package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;

public class KRSSRenderer extends StringRenderer {

	protected KRSSRenderer(AtomManager atomManager, Map<String, String> shortFormMap, Set<Definition> background) {
		super(atomManager, shortFormMap, background);
	}

	@Override
	protected String translateDefinition(Definition definition) {
		sb.append(KRSSKeyword.open);
		if (definition.isPrimitive()) {
			sb.append(KRSSKeyword.define_primitive_concept);
		} else {
			sb.append(KRSSKeyword.define_concept);
		}
		sb.append(KRSSKeyword.space);
		translateAtom(definition.getDefiniendum());
		sb.append(KRSSKeyword.space);
		translateConjunction(definition.getRight());
		sb.append(KRSSKeyword.close);
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		return "";
	}

	@Override
	protected String translateExistentialRestriction(String roleName, Integer childId) {
		sb.append(KRSSKeyword.open);
		sb.append(KRSSKeyword.some);
		sb.append(KRSSKeyword.space);
		sb.append(roleName);
		sb.append(KRSSKeyword.space);
		translateChild(childId);
		sb.append(KRSSKeyword.close);
		return "";
	}

	@Override
	protected String translateTop() {
		sb.append(KRSSKeyword.top);
		return "";
	}
	
	@Override
	protected String translateTrueConjunction(Set<Integer> atomIds) {
		sb.append(KRSSKeyword.open);
		sb.append(KRSSKeyword.and);
		sb.append(KRSSKeyword.space);

		for (Integer atomId : atomIds) {
			translateAtom(atomId);
			sb.append(KRSSKeyword.space);
		}

		sb.setLength(sb.length() - KRSSKeyword.space.length());
		sb.append(KRSSKeyword.close);
		return "";
	}

}
