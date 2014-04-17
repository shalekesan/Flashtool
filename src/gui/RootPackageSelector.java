package gui;

import gui.tools.WidgetsTool;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class RootPackageSelector extends Dialog {

	protected Object result;
	protected Shell shell;
	Button btnSuperuser;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public RootPackageSelector(Shell parent, int style) {
		super(parent, style);
		setText("Root Package chooser");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		WidgetsTool.setSize(shell);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
		    	  result = "";
		    	  event.doit = true;
		      }
		    });
		shell.setSize(229, 128);
		shell.setText(getText());
		
		btnSuperuser = new Button(shell, SWT.RADIO);
		btnSuperuser.setBounds(42, 10, 171, 16);
		btnSuperuser.setText("Superuser");
		btnSuperuser.setSelection(true);
		
		Button btnSuperSU = new Button(shell, SWT.RADIO);
		btnSuperSU.setBounds(42, 32, 171, 16);
		btnSuperSU.setText("SuperSU");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = "";
				shell.dispose();
			}
		});
		btnCancel.setBounds(138, 64, 75, 25);
		btnCancel.setText("Cancel");
		
		Button btnOK = new Button(shell, SWT.NONE);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnSuperuser.getSelection()) result="Superuser";
				else result="Supersu";
				shell.dispose();
			}
		});
		btnOK.setBounds(57, 64, 75, 25);
		btnOK.setText("Ok");

	}
}
