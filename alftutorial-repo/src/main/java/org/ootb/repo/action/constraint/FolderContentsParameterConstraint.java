package org.ootb.repo.action.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.constraint.BaseParameterConstraint;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

public class FolderContentsParameterConstraint extends BaseParameterConstraint {
	private NodeService nodeService;
	private SearchService searchService;   
    private DictionaryService dictionaryService;   
    private NamespaceService namespaceService;   
    private Repository repository;   
    private List<String> searchPath = Collections.emptyList();
    private List<String> nodeInclusionFilter = Collections.emptyList();
    
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	/**
	 * This property has been changed from the default class so that 
	 * it can accept multiple path values
	 * 
	 * @param searchPath A list of folder paths to search for templates
	 */
	public void setSearchPath(List<String> searchPath) {
		this.searchPath = searchPath;
	}

	/**
	 * This optional property defines a list of file extensions which should be included in the result set from
	 * this class. By implication, all other file extensions will be excluded. (The dot should not be specified
	 * i.e. "txt" not ".txt").
	 * If present, the cm:name of each candidate node will be checked against the values in this list and
	 * only those nodes whose cm:name ends with one of these file extensions will be included.
	 * <p/>
	 * If the property is not set then no inclusion filter is specified and all file extensions will
	 * be included.
	 * 
	 * @param nodeInclusionFilter A list of file extensions
	 * @since 3.5
	 */
	public void setNodeInclusionFilter(List<String> nodeInclusionFilter) {
		this.nodeInclusionFilter = new ArrayList<String>(nodeInclusionFilter.size());
		for(String extension : nodeInclusionFilter){
			 StringBuilder dotExt = new StringBuilder().append(".").append(extension);
			 this.nodeInclusionFilter.add(dotExt.toString());
		}
	}
	
	@Override
	protected Map<String, String> getAllowableValuesImpl() {
		Map<String, String> result = new HashMap<String, String>(23);
		for(String path : searchPath){
			List<NodeRef> nodeRefs = searchService.selectNodes(repository.getRootHome(),path,null,this.namespaceService,false);
			NodeRef rootFolder = null;
	        if (nodeRefs.size() == 0)
	        {
	            throw new AlfrescoRuntimeException("The path '" + searchPath + "' did not return any results.");
	        }
	        else
	        {
	            rootFolder = nodeRefs.get(0);
	        }
	        buildMap(result, rootFolder);
		}
		Map<String,String> sortedResult = sortByName(result);
		return sortedResult;
	}

	private void buildMap(Map<String, String> result, NodeRef folderNodeRef)
    {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(folderNodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef assoc : assocs)
        {
            NodeRef nodeRef = assoc.getChildRef();
            QName className = nodeService.getType(nodeRef);
            if (dictionaryService.isSubClass(className, ContentModel.TYPE_CONTENT) == true)
            {
                if(isCmNameAcceptable(nodeRef))
                {
                    String title = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
                    if (title != null && title.length() > 0)
                    {
                        result.put(nodeRef.toString(), title);
                    }
                    else
                    {
                        result.put(nodeRef.toString(), (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
                    }
                }
            }
            else if (dictionaryService.isSubClass(className, ContentModel.TYPE_FOLDER) == true)
            {
                buildMap(result, nodeRef);
            }
        }
    }
	
    /**
     * Folder contents as returned by this class can be filtered based on the cm:name of the
     * contained content nodes. If no file extensions are included, then all content NodeRefs
     * will be included in the result set.
     * If however, there are any file extensions specified, then the cm:name must match one of
     * those file extensions to be included in the result set.
     * 
     * @param nodeRef the node whose cm:name is to be checked.
     * @return <code>true</code> if cm:name is acceptable, else <code>false</code>.
     */
    private boolean isCmNameAcceptable(NodeRef nodeRef)
    {
        boolean result = true;
        
        if (!nodeInclusionFilter.isEmpty())
        {
            result = false;
            String cmName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            
            for (String extension : nodeInclusionFilter)
            {
                if (cmName.endsWith(extension))
                {
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * This method has been added to sort the list of templates displayed in  
     * the dropdown while creating a rule
     * 
     * @param result unsorted map containing list of templates
     * @return <code>sortedMap</code> sorted map containing list of templates
     */
    private Map<String,String> sortByName(Map<String,String> result){
    	//Convert the Map to List
    	List<Map.Entry<String, String>> nodeRefList = new LinkedList<Map.Entry<String,String>>(result.entrySet());
    	
    	//Sort list with comparator, to compare the Map values
    	Collections.sort(nodeRefList, new Comparator<Map.Entry<String, String>>() {
			public int compare(Entry<String, String> o1,
					Entry<String, String> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
    	
    	//Convert sorted list to Map
    	Map<String,String> sortedMap = new LinkedHashMap<String, String>();
    	for(Iterator<Map.Entry<String, String>> i = nodeRefList.iterator();i.hasNext();){
    		Map.Entry<String, String> entry = i.next();
    		sortedMap.put(entry.getKey(), entry.getValue());
    	}
		return sortedMap;
    }
    
}
