package dclsuite.view;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import dclsuite.Activator;

/**
 * DCL2 Class responsible to provide a view to architectural drifts
 * @author Ricardo Terra
 */
public class DriftsView extends ViewPart {
	public static final String ID = "dclcheck.view1";

	private TableViewer viewer;
	private Action action1;
	private Action action2;
	//private Action action3;
	private Table t;
	private Label text;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return new Object[] { new String[] { "", "", "", "" } };
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(((Object[]) obj)[index]);
		}

		public Image getColumnImage(Object obj, int index) {
			if (index == 0) {
				return getImage(obj);
			}
			return null;
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		}
	}

	class NameSorter extends ViewerSorter {

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Object[] v1 = (Object[]) e1;
			Object[] v2 = (Object[]) e2;
			if (v1[0].toString().compareTo(v2[0].toString()) == 0) {
				return v1[1].toString().compareTo(v2[1].toString());
			} else {
				return v1[0].toString().compareTo(v2[0].toString());
			}
		}
	}

	/**
	 * The constructor.
	 */
	public DriftsView() {

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());

		text = new Label(parent, SWT.NONE);
		text.setSize(new Point(10, 10));
		text.setText("0 architectural drifts");
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		t = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		t.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableColumn c = new TableColumn(t, SWT.LEFT);
		c.setWidth(100);
		c.setText("Project Name");

		c = new TableColumn(t, SWT.LEFT);
		c.setWidth(300);
		c.setText("Dependency Constraint");

		c = new TableColumn(t, SWT.LEFT);
		c.setWidth(300);
		c.setText("Class Name");

		c = new TableColumn(t, SWT.LEFT);
		c.setWidth(100);
		c.setText("Location");

		viewer = new TableViewer(t);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());

		this.makeActions();
		this.hookContextMenu();
		this.hookDoubleClickAction();
		this.contributeToActionBars();

	}

	public void clear() {
		viewer.getTable().removeAll();
		this.updateCount(0);
	}

	public void addArchitecturalDrift(Object[] o) {
		viewer.add(new Object[] { o });
	}

	public void updateCount(int count) {
		text.setText(count + " architectural drifts");
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DriftsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
		manager.add(new Separator());
		//manager.add(action3);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		//manager.add(action3);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		//manager.add(action3);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object[] array = (Object[]) ((IStructuredSelection) selection)
						.getFirstElement();
				try {
					IDE.openEditor(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage(),
							(IMarker) array[5]);
				} catch (PartInitException e) {
					MessageDialog.openError(
							new Shell(),
							Activator.PLUGIN_ID,
							"Problem opening the marker. Cause: "
									+ e.getMessage());
					e.printStackTrace();
				}
			}
		};
		action1.setText("Go to and see possible fixes...");
		action1.setToolTipText("Go to the line in the specific class");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));

		action2 = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage(((Object[]) obj)[4].toString());
			}
		};
		action2.setText("View Details");
		action2.setToolTipText("View details of the selected architectural drift");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
//		action3 = new Action() {
//			public void run() {
//				showMessage("eita");
//			}
//		};
//		action3.setText("Look for Possible Fixes");
//		action3.setToolTipText("Look for refactoring recommendations");
//		action3.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
//				.getImageDescriptor(ISharedImages.IMG_LCL_LINKTO_HELP));
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				action1.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"dclcheck", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		
		viewer.getControl().setFocus();
	}
}