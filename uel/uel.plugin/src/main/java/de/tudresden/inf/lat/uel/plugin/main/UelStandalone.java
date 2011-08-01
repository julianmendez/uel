package de.tudresden.inf.lat.uel.plugin.main;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;
import de.tudresden.inf.lat.uel.plugin.ui.UelController;
import de.tudresden.inf.lat.uel.plugin.ui.UelView;

public class UelStandalone {

	public static void main(String[] args) throws IOException,
			OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager
				.createOWLOntologyManager();
		UelController controller = new UelController(new UelView(
				new UelProcessor()), ontologyManager);

		JFrame frame = new JFrame();
		frame.add(controller.getView());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(new Dimension(1024, 400));
		frame.setVisible(true);
	}

}
