package dclsuite.editor;

import org.eclipse.ui.editors.text.TextEditor;

public class DCLEditor extends TextEditor {

	public DCLEditor() {
		super();
		setSourceViewerConfiguration(new DCLSourceViewerConfiguration());
		setDocumentProvider(new DCLDocumentProvider());
	}
	
}
