package brix.web.nodepage;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;

import brix.auth.Action;
import brix.jcr.wrapper.BrixNode;
import brix.plugin.site.auth.SiteNodeAction;
import brix.web.generic.IGenericComponent;

public class BrixNodeWebPage extends WebPage implements IGenericComponent<BrixNode>
{

    public BrixNodeWebPage(IModel<BrixNode> nodeModel)
    {
        super(nodeModel);
    }
    
    public BrixNodeWebPage(IModel<BrixNode> nodeModel, BrixPageParameters pageParameters)
    {
        super(nodeModel);
        this.pageParameters = pageParameters;
    }

    @Override
    protected void onBeforeRender()
    {
        checkAccess();
        super.onBeforeRender();
    }

    protected void checkAccess()
    {
        BrixNode node = getModelObject();
        Action action = new SiteNodeAction(Action.Context.PRESENTATION,
                SiteNodeAction.Type.NODE_VIEW, node);
        if (!node.getBrix().getAuthorizationStrategy().isActionAuthorized(action))
        {
            throw new RestartResponseException(ForbiddenPage.class);
        }
    }

    public BrixPageParameters getBrixPageParameters()
    {
        if (pageParameters == null)
        {
            pageParameters = new BrixPageParameters();
        }
        return pageParameters;
    }
    
    @SuppressWarnings("unchecked")
	public IModel<BrixNode> getModel()
    {
    	return (IModel<BrixNode>) getDefaultModel();
    }

    @Override
    public boolean isBookmarkable()
    {
        return true;
    }

    private BrixPageParameters pageParameters;

    public boolean initialRedirect()
    {
        return false;
    }

	public BrixNode getModelObject()
	{
		return (BrixNode) getDefaultModelObject();
	}

	public void setModel(IModel<BrixNode> model)
	{
		setDefaultModel(model);
	}

	public void setModelObject(BrixNode object)
	{
		setDefaultModelObject(object);
	}
}
