package de.tudresden.inf.lat.uel.plugin.main;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.semanticweb.owlapi.apibinding.OWLManager;

/**
 * This is used to start UEL without Protege.
 * 
 * @author Julian Mendez
 */
public class UelStandalone {

	public static void main(String[] args) {
		(new UelStandalone()).run();
	}

	public void run() {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		new UelStarter(frame.getContentPane(), OWLManager.createOWLOntologyManager(), null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(new Dimension(1024, 400));
		frame.setVisible(true);
	}

}
