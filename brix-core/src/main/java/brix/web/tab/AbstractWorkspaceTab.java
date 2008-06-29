package brix.web.tab;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import brix.workspace.Workspace;
import brix.workspace.WorkspaceModel;

public abstract class AbstractWorkspaceTab extends CachingAbstractTab implements IDetachable
{

    private final IModel<Workspace> workspaceModel;

    public void detach()
    {
        workspaceModel.detach();
    }

    public AbstractWorkspaceTab(IModel<String> title, Workspace workspace)
    {
        this(title, new WorkspaceModel(workspace));
    }

    public AbstractWorkspaceTab(IModel<String> title, IModel<Workspace> workspaceModel)
    {
        super(title);
        this.workspaceModel = workspaceModel;
    }


    public IModel<Workspace> getWorkspaceModel()
    {
        return workspaceModel;
    }

    @Override
    public Panel newPanel(String panelId)
    {
        return newPanel(panelId, workspaceModel);
    }

    public abstract Panel newPanel(String panelId, IModel<Workspace> workspaceModel);

}
