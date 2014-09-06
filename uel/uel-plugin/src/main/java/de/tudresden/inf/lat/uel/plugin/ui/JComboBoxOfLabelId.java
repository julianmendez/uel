package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * This is a combo box that contains pairs label-identifier. This class is
 * mainly used to keep compatibility with Java 1.6, since Java 1.7 contains
 * already a parameterized combo box.
 *
 * @author Julian Mendez
 * @see javax.swing.JComboBox
 */
public class JComboBoxOfLabelId extends JComboBox {

	private static final long serialVersionUID = -1589168297784841281L;

	private final List<LabelId> list = new ArrayList<LabelId>();

	public JComboBoxOfLabelId() {
		super();
		addActionListener(this);
		setEditable(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String lastText = event.getActionCommand();
		if (!lastText.equals(getActionCommand())) {
			int itemIndex = binarySearch(this.list, lastText);
			this.setSelectedIndex(itemIndex);
		}
	}

	/**
	 * This method is not supported.
	 *
	 * @throws UnsupportedOperationException
	 *             always because this method is not supported
	 */
	@Override
	public void addItem(Object anObject) {
		throw new UnsupportedOperationException();
	}

	private int binarySearch(List<LabelId> list, String key) {
		int left = 0;
		int right = list.size();
		int mid = left;
		while (left < (right - 1)) {
			mid = (left + right) / 2;
			LabelId current = list.get(mid);
			if (current.getLabel().equals(key)) {
				left = mid;
				right = mid;
			} else if (current.getLabel().compareTo(key) < 0) {
				left = mid;
			} else if (current.getLabel().compareTo(key) > 0) {
				right = mid;
			}
		}
		int ret = (left < (list.size() - 1))
				&& (list.get(left).getLabel().compareTo(key) < 0) ? left + 1
				: left;
		return ret;
	}

	public LabelId getSelectedElement() {
		return this.list.get(getSelectedIndex());
	}

	public void setItemList(List<LabelId> origList) {
		this.list.clear();
		this.list.addAll(origList);
		Collections.sort(this.list);
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (LabelId label : this.list) {
			model.addElement(label.getLabel());
		}
		super.setModel(model);
	}

}
