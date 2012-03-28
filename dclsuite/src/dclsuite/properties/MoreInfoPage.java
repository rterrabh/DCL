package dclsuite.properties;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.views.markers.MarkerItem;

import dclsuite.enums.ViolationProperties;

public class MoreInfoPage extends PropertyPage {

	private static final int TEXT_FIELD_WIDTH = 60;

	private void addFirstSection(Composite parent) throws CoreException {
		final Composite composite = createDefaultComposite(parent);
		final IMarker marker = ((MarkerItem) getElement()).getMarker();

		for (ViolationProperties vp : ViolationProperties.values()) {
			this.add(composite, marker, vp);
		}
	}

	private void add(Composite composite, IMarker marker, ViolationProperties vp) throws CoreException {
		if (marker.getAttribute(vp.getKey()) != null) {
			Label label = new Label(composite, SWT.NONE);
			label.setText(vp.getLabel() + ":");

			String strValue = marker.getAttribute(vp.getKey()).toString();

			if (strValue.toString().length() < 60) {
				Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
				text.setEditable(false);
				text.setText(strValue);
			} else {
				Text text = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
				text.setEditable(false);
				GridData gd = new GridData();
				gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
				text.setLayoutData(gd);
				text.setText(strValue);
			}
		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		try {
			addFirstSection(composite);
		} catch (CoreException e) {
		}

		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	public boolean performOk() {
		return true;
	}

}