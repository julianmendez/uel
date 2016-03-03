package de.tudresden.inf.lat.uel.plugin.main;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

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
		new UelStarter(frame.getContentPane());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(new Dimension(640, 480));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
