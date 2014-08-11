package gui;

import gui.models.CustIdItem;
import gui.models.ModelUpdater;
import gui.models.Models;
import gui.models.TableLine;
import gui.models.TableSorter;
import gui.models.VectorContentProvider;
import gui.models.VectorLabelProvider;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.system.DeviceEntry;
import org.system.OS;
import org.system.URLDownloader;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.iagucool.xperifirm.CDFInfoLoader;

public class DeviceUpdates extends Dialog {

	protected Object result;
	protected Shell shlDeviceUpdateChecker;
	protected CTabFolder tabFolder;
	protected DeviceEntry _entry;
	protected Label lblInfo;
	//protected CTabItem tabItem;
	//private Table tableDevice;
	//private TableViewer tableViewer;
	protected Models models;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DeviceUpdates(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(DeviceEntry entry) {
		_entry = entry;
		createContents();
		shlDeviceUpdateChecker.open();
		shlDeviceUpdateChecker.layout();
		Display display = getParent().getDisplay();
		while (!shlDeviceUpdateChecker.isDisposed()) {
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
		shlDeviceUpdateChecker = new Shell(getParent(), getStyle());
		shlDeviceUpdateChecker.setSize(450, 300);
		shlDeviceUpdateChecker.setText("Device Update Checker");
		
		tabFolder = new CTabFolder(shlDeviceUpdateChecker, SWT.BORDER);
		tabFolder.setBounds(11, 10, 423, 223);
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));				
		
		Button btnNewButton = new Button(shlDeviceUpdateChecker, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlDeviceUpdateChecker.dispose();
			}
		});
		btnNewButton.setBounds(359, 239, 75, 25);
		btnNewButton.setText("Close");
		
		lblInfo = new Label(shlDeviceUpdateChecker, SWT.NONE);
		lblInfo.setBounds(11, 244, 342, 15);

		fillMap();
		FillJob fj = new FillJob("Update Search");
		fj.schedule();
	}

	public void fillMap() {
		models = _entry.getUpdatableModels(false);
	}

	public void addTab(final String tabtitle) {
		Display.getDefault().asyncExec(
				new Runnable() {
					public void run() {
						Vector<TableLine> result = new Vector<TableLine>();
						ModelUpdater mu = (ModelUpdater)models.get(tabtitle);
						CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
						tabItem.setText(tabtitle.length()>0?tabtitle:_entry.getId());
						TableViewer tableViewer = new TableViewer(tabFolder,SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);						
						tableViewer.setContentProvider(new VectorContentProvider());
						tableViewer.setLabelProvider(new VectorLabelProvider());

						// Create the popup menu
						  MenuManager menuMgr = new MenuManager();
						  Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
						  menuMgr.addMenuListener(new IMenuListener() {
						    @Override
						    public void menuAboutToShow(IMenuManager manager) {
						    	TableLine tl = (TableLine)tableViewer.getTable().getSelection()[0].getData();
						    	if (tl.getValueOf(2).length()==0) {
							    	manager.add(new Action("Check release") {
							            public void run() {
							            		tl.setValueOf(2, mu.getReleaseOf(tl.getValueOf(0)));
								            	tableViewer.refresh();
							            }
							        });
						    	}
						    	else {
							    	manager.add(new Action("Download") {
							            public void run() {
							            	DownloadJob dj = new DownloadJob("Download FW");
							            	dj.setCDF(tl.getValueOf(0));
							            	String path = OS.getWorkDir()+File.separator+"firmwares"+File.separator+"Downloads"+File.separator+mu.getModel()+"_"+tl.getValueOf(1).replaceAll(" ","_") + "_" + mu.getReleaseOf(tl.getValueOf(0));
							            	dj.setPath(path);
							            	dj.setUpdater(mu);
							            	dj.schedule();
								            tableViewer.refresh();
							            	}
							        });
						    	}
						    }
						  });

						menuMgr.setRemoveAllWhenShown(true);
						tableViewer.getControl().setMenu(menu);

						
						TableColumn[] columns = new TableColumn[3];
						columns[0] = new TableColumn(tableViewer.getTable(), SWT.NONE);
						columns[0].setText("Id");
						columns[1] = new TableColumn(tableViewer.getTable(), SWT.NONE);
						columns[1].setText("Branding");
						columns[2] = new TableColumn(tableViewer.getTable(), SWT.NONE);
						columns[2].setText("Version");
						tableViewer.getTable().setHeaderVisible(true);
						tableViewer.getTable().setLinesVisible(true);
						TableSorter sort = new TableSorter(tableViewer);
						tableViewer.getTable().setSortColumn(tableViewer.getTable().getColumn(0));
						tableViewer.getTable().setSortDirection(SWT.UP);
						tableViewer.setInput(result);
						tableViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
						      public void handleEvent(Event e) {
						    	  TableLine tl = (TableLine)tableViewer.getTable().getSelection()[0].getData();
							    	if (tl.getValueOf(2).length()==0) {
							    		tl.setValueOf(2, mu.getReleaseOf(tl.getValueOf(0)));
									    tableViewer.refresh();
								     }
							    	else {
						            	DownloadJob dj = new DownloadJob("Download FW");
						            	dj.setCDF(tl.getValueOf(0));
						            	String path = OS.getWorkDir()+File.separator+"firmwares"+File.separator+"Downloads"+File.separator+mu.getModel()+"_"+tl.getValueOf(1).replaceAll(" ","_") + "_" + mu.getReleaseOf(tl.getValueOf(0));
						            	dj.setPath(path);
						            	dj.setUpdater(mu);
						            	dj.schedule();
							            tableViewer.refresh();
							    	}
						      }
						    });

						Iterator cdflist = mu.getCustIds().getProperties().keySet().iterator();
						while (cdflist.hasNext()) {
							String id = (String)cdflist.next();
							TableLine line1 = new TableLine();
							line1.add(id);
							line1.add(mu.getCustIds().getProperty(id));
							line1.add("");
							result.add(line1);
							tableViewer.refresh();
						}
										tableViewer.setInput(result);
										for (int i = 0, n = tableViewer.getTable().getColumnCount(); i < n; i++) {
											tableViewer.getTable().getColumn(i).pack();
										}
										tableViewer.getTable().pack();
										tableViewer.refresh();
										tabItem.setControl(tableViewer.getTable());

					}
				}
		);
		
	}
	
	public void fillTab() {
		Iterator imodels = models.keySet().iterator();
		while (imodels.hasNext()) {
			addTab((String)imodels.next());
		}
		Display.getDefault().asyncExec(
				new Runnable() {
					public void run() {
						tabFolder.redraw();
						tabFolder.setSelection(0);
					}
				}
		);
	}

	class FillJob extends Job {

		boolean canceled = false;

		public FillJob(String name) {
			super(name);
		}
		
		public void stopSearch() {
			canceled=true;
		}
		
	    protected IStatus run(IProgressMonitor monitor) {
			    while (!canceled) {
					Display.getDefault().asyncExec(
							new Runnable() {
								public void run() {
									lblInfo.setText("Searching for updates. Please wait");
								}
							}
					);
					fillTab();
					Display.getDefault().asyncExec(
							new Runnable() {
								public void run() {
									lblInfo.setText("");
								}
							}
					);
					return Status.OK_STATUS;
			    }
			    return Status.CANCEL_STATUS;
	    }
	}

	class DownloadJob extends Job {

		boolean canceled = false;
		String cdfval;
		ModelUpdater mu=null;
		String _path = "";

		public DownloadJob(String name) {
			super(name);
		}
		
		public void stopSearch() {
			canceled=true;
		}
		
		public void setUpdater(ModelUpdater pmu) {
			mu=pmu;
		}
		
		public void setCDF(String cdf) {
			cdfval = cdf;
		}
		
		public void setPath(String path) {
			_path = path;
		}
		
	    protected IStatus run(IProgressMonitor monitor) {
			    while (!canceled) {
	            	Vector<String> v = mu.getFilesOf(cdfval);
	            	try {
	            		for (int i=0;i<v.size();i++) {
	            			URLDownloader ud = new URLDownloader();
	            			new File(_path).mkdirs();
	            			String f = v.get(i).substring(v.get(i).lastIndexOf("/"));
	            			ud.Download(v.get(i),_path+File.separator+f);
	            		}
	            		
	            	} catch (IOException ioe) {}
	            }
			    return Status.OK_STATUS;
	    }
	}
}