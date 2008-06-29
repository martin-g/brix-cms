package brix.web.tile.pagetile;

import org.apache.wicket.model.IModel;

import brix.BrixNodeModel;
import brix.jcr.wrapper.BrixNode;
import brix.plugin.site.page.PageSiteNodePlugin;
import brix.plugin.site.page.tile.admin.GenericTileEditorPanel;
import brix.plugin.site.picker.node.SiteNodePickerPanel;
import brix.web.picker.node.NodePickerPanel;
import brix.web.picker.node.NodeTypeFilter;
import brix.web.tree.NodeFilter;

public class PageTileEditorPanel extends GenericTileEditorPanel<BrixNode>
{

    public PageTileEditorPanel(String id, IModel<BrixNode> tileContainerNode)
    {
        super(id, tileContainerNode);

        NodeFilter filter = new NodeTypeFilter(PageSiteNodePlugin.TYPE); 
        NodePickerPanel picker = new SiteNodePickerPanel("nodePicker", targetNodeModel, tileContainerNode.getObject().getSession().getWorkspace().getName(), filter);
        picker.setRequired(true);
        add(picker);
    }

    private IModel<BrixNode> targetNodeModel = new BrixNodeModel();

    @Override
    public void load(BrixNode node)
    {
        if (node.hasProperty("pageNode"))
        {
            BrixNode pageNode = (BrixNode) node.getProperty("pageNode").getNode();
            targetNodeModel.setObject(pageNode);
        }
    }

    @Override
    public void save(BrixNode node)
    {        
        node.setProperty("pageNode", targetNodeModel.getObject());
    }

}
