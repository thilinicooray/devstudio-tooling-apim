package org.wso2.developerstudio.eclipse.artifact.apim.compositeapi;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.ui.wizard.APIImportMainWizard;


public class ImportAPIAction implements IActionDelegate {
	IStructuredSelection selection;

	public void run(IAction action) {
		if (selection != null) {
			APIImportMainWizard wizard = new APIImportMainWizard();
			WizardDialog exportWizardDialog =
					new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
			exportWizardDialog.open();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		}

	}

}
