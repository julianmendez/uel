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
public class JComboBoxOfLabelId extends JComboBox<LabelId> {

	private static final long serialVersionUID = -1589168297784841281L;

	public JComboBoxOfLabelId() {
		super();
		addActionListener(this);
		setEditable(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String lastText = event.getActionCommand();
		if (!lastText.equals(getActionCommand())) {
			int itemIndex = binarySearch(lastText);
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
	public void addItem(LabelId s) {
		throw new UnsupportedOperationException();
	}

	private int binarySearch(String key) {
		int left = 0;
		int right = getItemCount();
		int mid = left;
		while (left < (right - 1)) {
			mid = (left + right) / 2;
			LabelId current = getItemAt(mid);
			if (current.getLabel().equals(key)) {
				left = mid;
				right = mid;
			} else if (current.getLabel().compareTo(key) < 0) {
				left = mid;
			} else if (current.getLabel().compareTo(key) > 0) {
				right = mid;
			}
		}
		int ret = (left < (getItemCount() - 1))
				&& (getItemAt(left).getLabel().compareTo(key) < 0) ? left + 1
				: left;
		return ret;
	}

	public LabelId getSelectedElement() {
		return getItemAt(getSelectedIndex());
	}

	public void setItemList(List<LabelId> origList) {
		List<LabelId> list = new ArrayList<LabelId>();
		list.addAll(origList);
		Collections.sort(list);

		LabelId[] array = list.toArray(new LabelId[list.size()]);
		DefaultComboBoxModel<LabelId> model = new DefaultComboBoxModel<LabelId>(array);
		super.setModel(model);
	}

}
