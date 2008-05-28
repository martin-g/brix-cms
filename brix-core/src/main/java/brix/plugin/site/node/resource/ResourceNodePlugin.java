package brix.plugin.site.node.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.RequestParameters;

import brix.Brix;
import brix.jcr.wrapper.BrixNode;
import brix.plugin.site.NodeConverter;
import brix.plugin.site.SiteNodePlugin;
import brix.plugin.site.node.resource.admin.ResourceManagerPanel;
import brix.plugin.site.node.resource.admin.UploadResourcesPanel;
import brix.plugin.site.node.resource.managers.ImageResourceManager;
import brix.plugin.site.node.resource.managers.TextResourceManager;
import brix.web.admin.navigation.NavigationAwarePanel;

public class ResourceNodePlugin implements SiteNodePlugin
{

    public static final String TYPE = Brix.NS_PREFIX + "resource";

    public ResourceNodePlugin()
    {
        registerBuiltInManagers();
        registerDefaultMimeTypes();
    }

    public String getNodeType()
    {
        return TYPE;
    }

    public String getName()
    {
        return "Resource";
    }

    public NavigationAwarePanel newManageNodePanel(String id, IModel<BrixNode> nodeModel)
    {
        return new ResourceManagerPanel(id, nodeModel);
    }

    public IRequestTarget respond(IModel<BrixNode> nodeModel, RequestParameters requestParameters)
    {
//    	IRequestTarget switchTarget = SwitchProtocolRequestTarget.requireProtocol(Protocol.HTTP);
//    	if (switchTarget != null) 
//    	{
//    		return switchTarget;
//    	} 
//    	else 
//    	{
    		return new ResourceRequestTarget(nodeModel);
//    	}
    }

    public NavigationAwarePanel newCreateNodePanel(String id, IModel<BrixNode> parentNode)
    {
        return new UploadResourcesPanel(id, parentNode);
    }

    public NodeConverter getConverterForNode(BrixNode node)
    {
        return null;
    }

    public String resolveMimeTypeFromFileName(String fileName)
    {
        int last = fileName.lastIndexOf(".");
        if (last != -1)
        {
            String ext = fileName.substring(last + 1).toLowerCase();
            return mimeTypeFromExtension(ext);
        }
        return null;
    }

    private String mimeTypeFromExtension(String ext)
    {
        return mimeTypes.get(ext);
    }

    private Map<String /* extension */, String /* mime-type */> mimeTypes = new ConcurrentHashMap<String, String>();

    public void registerMimeType(String mimeType, String... extensions)
    {
        for (String s : extensions)
        {
            mimeTypes.put(s, mimeType);
        }
    }

    private void registerDefaultMimeTypes()
    {
        registerMimeType("application/xml", "xml");
        registerMimeType("text/html", "html", "htm");
        registerMimeType("text/plain", "txt");
        registerMimeType("text/css", "css");
        registerMimeType("text/javascript", "js");
        registerMimeType("image/jpeg", "jpg", "jpeg");
        registerMimeType("image/png", "png");
        registerMimeType("image/gif", "gif");
    }

    private void registerBuiltInManagers()
    {
        registerResourceManager(new ImageResourceManager());
        registerResourceManager(new TextResourceManager());
    }

    public ResourceManager getResourceManagerForMimeType(String mimeType)
    {
        if (mimeType != null)
        {
            for (ResourceManager manager : resourceManagers)
            {
                if (manager.handles(mimeType))
                {
                    return manager;
                }
            }
        }
        return null;
    }

    public void registerResourceManager(ResourceManager type)
    {
        resourceManagers.add(0, type);
    }

    private final List<ResourceManager> resourceManagers = new ArrayList<ResourceManager>();

}
