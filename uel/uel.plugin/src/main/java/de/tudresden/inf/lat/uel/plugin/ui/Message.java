package de.tudresden.inf.lat.uel.plugin.ui;

/**
 * This interface provides messages for the user interface.
 * 
 * @author Julian Mendez
 */
public interface Message {

	public static final String buttonAcceptVar = "OK";
	public static final String buttonFirst = "|<";
	public static final String buttonLast = ">|";
	public static final String buttonMakeCons = "<<<";
	public static final String buttonMakeVar = ">>>";
	public static final String buttonNext = ">";
	public static final String buttonOpen = "Open";
	public static final String buttonPrevious = "<";
	public static final String buttonRejectVar = "Cancel";
	public static final String buttonSave = "Save";
	public static final String buttonSelectVariables = "Vars";
	public static final String titleError = "Error";
	public static final String tooltipAcceptVar = "use the selected entities as variables";
	public static final String tooltipFirst = "shows the first unifier";
	public static final String tooltipLast = "computes all unifiers and shows the last unifier";
	public static final String tooltipMakeCons = "defines the selected items as constants";
	public static final String tooltipMakeVar = "defines the selected items as variables";
	public static final String tooltipNext = "shows the next unifier, it unifies the chosen class names using the selected variables";
	public static final String tooltipOpen = "opens an ontology";
	public static final String tooltipPrevious = "shows the previous unifier";
	public static final String tooltipRejectVar = "cancels this selection of variables";
	public static final String tooltipSave = "saves the current unifier into a file";
	public static final String tooltipSelectVariables = "gets candidates for variables";
	public static final String tooltipUnifier = "unifier";
	public static final String tooltipUnifierId = "unifier id";

}
