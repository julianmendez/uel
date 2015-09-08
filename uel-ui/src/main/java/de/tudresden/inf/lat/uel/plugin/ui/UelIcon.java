package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * This manages the icons.
 * 
 * @author Julian Mendez
 *
 */
public class UelIcon {

	public static final int DEFAULT_ICON_SIZE = 18;

	public static final String PATH_BACK = "icons/back.png";
	public static final String PATH_FAST_FORWARD = "icons/fastforward.png";
	public static final String PATH_FORWARD = "icons/forward.png";
	public static final String PATH_OPEN = "icons/openfolder.png";
	public static final String PATH_REWIND = "icons/rewind.png";
	public static final String PATH_SAVE = "icons/floppydisk.png";
	public static final String PATH_STATISTICS = "icons/statistics.png";
	public static final String PATH_STEP_BACK = "icons/stepback.png";
	public static final String PATH_STEP_FORWARD = "icons/stepforward.png";

	public static final ImageIcon ICON_BACK = createIcon(PATH_BACK);
	public static final ImageIcon ICON_FAST_FORWARD = createIcon(PATH_FAST_FORWARD);
	public static final ImageIcon ICON_FORWARD = createIcon(PATH_FORWARD);
	public static final ImageIcon ICON_OPEN = createIcon(PATH_OPEN);
	public static final ImageIcon ICON_REWIND = createIcon(PATH_REWIND);
	public static final ImageIcon ICON_SAVE = createIcon(PATH_SAVE);
	public static final ImageIcon ICON_STATISTICS = createIcon(PATH_STATISTICS);
	public static final ImageIcon ICON_STEP_BACK = createIcon(PATH_STEP_BACK);
	public static final ImageIcon ICON_STEP_FORWARD = createIcon(PATH_STEP_FORWARD);

	/**
	 * Returns an icon created with the default size for the given path.
	 * 
	 * @path path of icon
	 */
	public static ImageIcon createIcon(String path) {
		return createIcon(path, DEFAULT_ICON_SIZE);
	}

	/**
	 * Creates an icon with the given size. If the path is invalid, this method
	 * returns <code>null</code>.
	 * 
	 * @param path
	 *            path of icon
	 * @param size
	 *            size of icon
	 * @return an icon with the given size, or <code>null</code> if the path is
	 *         invalid
	 */
	public static ImageIcon createIcon(String path, int size) {
		ImageIcon ret = null;
		try {
			URL url = UelIcon.class.getClassLoader().getResource(path);
			if (url == null) {
				try {
					throw new IllegalArgumentException(
							"Icon has an invalid path: '" + path + "'.");
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}

			} else {
				BufferedImage img = ImageIO.read(url);
				ret = new ImageIcon(img.getScaledInstance(size, size,
						Image.SCALE_SMOOTH));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static void setBorder(JButton button) {
		Border line = new LineBorder(new Color(150, 150, 150));
		Border margin = new EmptyBorder(0, 4, 0, 4);
		Border compound = new CompoundBorder(line, margin);
		button.setBorder(compound);
	}

}
