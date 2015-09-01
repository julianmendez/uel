package de.tudresden.inf.lat.uel.plugin.ui;

/**
 * This interface provides messages for the user interface.
 * 
 * @author Julian Mendez
 */
public interface Message {

	String buttonAcceptVar = ">";
	String buttonFirst = "|<";
	String buttonLast = ">|";
	String buttonMakeCons = "<<<";
	String buttonMakeVar = ">>>";
	String buttonNext = ">";
	String buttonOpen = "Open";
	String buttonPrevious = "<";
	String buttonRejectVar = "<";
	String buttonSave = "Save";
	String buttonSaveGoal = "Save";
	String buttonSelectVariables = ">";
	String buttonShowStatInfo = "Stat";
	String iconBack = "icons/back-16x16.png";
	String iconFastForward = "icons/fastforward-16x16.png";
	String iconForward = "icons/forward-16x16.png";
	String iconHistory = "toolbarButtonGraphics/general/History16.gif";
	String iconOpen = "icons/openfolder-16x16.png";
	String iconRewind = "icons/rewind-16x16.png";
	String iconSave = "toolbarButtonGraphics/general/Save16.gif";
	String iconStepBack = "icons/stepback-16x16.png";
	String iconStepForward = "icons/stepforward-16x16.png";
	String titleError = "Error";
	String textOntologyBg00 = "First background ontology:";
	String textOntologyBg01 = "Second background ontology:";
	String textOntologyPos = "Goal subsumptions and equations:";
	String textOntologyNeg = "Goal dissubsumptions and disequations:";
	String tooltipAcceptVar = "use the selected entities as variables";
	String tooltipComboBoxOntologyBg00 = "select first background ontology";
	String tooltipComboBoxOntologyBg01 = "select second background ontology";
	String tooltipComboBoxOntologyPos = "select ontology containing subsumptions and equations";
	String tooltipComboBoxOntologyNeg = "select ontology containing dissubsumptions and disequations";
	String tooltipFirst = "shows the first unifier";
	String tooltipGoal = "goal";
	String tooltipLast = "computes all unifiers and shows the last unifier";
	String tooltipMakeCons = "defines the selected items as constants";
	String tooltipMakeVar = "defines the selected items as variables";
	String tooltipNext = "shows the next unifier, it unifies the chosen class names using the selected variables";
	String tooltipOpen = "opens an ontology";
	String tooltipPrevious = "shows the previous unifier";
	String tooltipRejectVar = "cancels this selection of variables";
	String tooltipSave = "saves the current unifier into a file";
	String tooltipSaveGoal = "saves the goal to a file";
	String tooltipSelectProcessor = "selects the processor";
	String tooltipSelectVariables = "gets candidates for variables";
	String tooltipShowStatInfo = "shows statistical information before computing the first unifier";
	String tooltipTextInfo = "statistical information given by the processor";
	String tooltipUnifier = "unifier";
	String tooltipUnifierId = "unifier id";

}
