/*
 * SonarQube Eclipse
 * Copyright (C) 2010-2015 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.ide.eclipse.ui.internal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.sonar.ide.eclipse.core.internal.SonarCorePlugin;
import org.sonar.ide.eclipse.core.resources.ISonarResource;
import org.sonar.ide.eclipse.ui.internal.views.ResourceWebView;

/**
 * Open the internal web browser to show the page of the sonar server corresponding to the selection.
 *
 * @author Jérémie Lagarde
 */
public class OpenInBrowserAction implements IObjectActionDelegate {

  private IStructuredSelection selection;

  public OpenInBrowserAction() {
    super();
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    // Nothing to do
  }

  @Override
  public void run(IAction action) {
    Object element = selection.getFirstElement();
    if (element instanceof ISonarResource) {
      openBrowser((ISonarResource) element);
    }
  }

  protected void openBrowser(ISonarResource sonarResource) {
    try {
      ResourceWebView view = (ResourceWebView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ResourceWebView.ID);
      view.setInput(sonarResource);
    } catch (PartInitException e) {
      SonarCorePlugin.getDefault().error("Unable to open Web View", e);
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      this.selection = (IStructuredSelection) selection;
    }
  }
}