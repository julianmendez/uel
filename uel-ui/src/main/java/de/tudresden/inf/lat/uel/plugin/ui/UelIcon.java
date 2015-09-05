package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * This manages the icons.
 * 
 * @author Julian Mendez
 *
 */
public class UelIcon {

	public static final String iconBack = "icons/back.png";
	public static final String iconFastForward = "icons/fastforward.png";
	public static final String iconForward = "icons/forward.png";
	public static final String iconHistory = "icons/statistics.png";
	public static final String iconOpen = "icons/openfolder.png";
	public static final String iconRewind = "icons/rewind.png";
	public static final String iconSave = "icons/floppydisk.png";
	public static final String iconStepBack = "icons/stepback.png";
	public static final String iconStepForward = "icons/stepforward.png";

	public static final int DEFAULT_ICON_SIZE = 16;

	public static final ImageIcon ICON_BACK = createIcon(iconBack);
	public static final ImageIcon ICON_FAST_FORWARD = createIcon(iconFastForward);
	public static final ImageIcon ICON_FORWARD = createIcon(iconForward);
	public static final ImageIcon ICON_HISTORY = createIcon(iconHistory);
	public static final ImageIcon ICON_OPEN = createIcon(iconOpen);
	public static final ImageIcon ICON_REWIND = createIcon(iconRewind);
	public static final ImageIcon ICON_SAVE = createIcon(iconSave);
	public static final ImageIcon ICON_STEP_BACK = createIcon(iconStepBack);
	public static final ImageIcon ICON_STEP_FORWARD = createIcon(iconStepForward);

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
					throw new IllegalArgumentException("Icon has an invalid path: '" + path + "'.");
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}

			} else {
				BufferedImage img = ImageIO.read(url);
				ret = new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Returns an icon created with the default size for the given path.
	 * 
	 * @path path of icon
	 */
	public static ImageIcon createIcon(String path) {
		return createIcon(path, DEFAULT_ICON_SIZE);
	}

}
