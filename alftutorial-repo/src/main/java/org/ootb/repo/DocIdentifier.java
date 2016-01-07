package org.ootb.repo;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ootb.repo.model.MyCompanyModel;

public class DocIdentifier implements OnCreateNodePolicy {
	Log logger = LogFactory.getLog(DocIdentifier.class);
	private NodeService nodeService;
	private Behaviour onCreateNode;
	private PolicyComponent policyComponent;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}	

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void init(){
		onCreateNode = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, onCreateNode);
	}
	
	public void onCreateNode(ChildAssociationRef childAssocRef) {	
		String docId = "OOTB-"+nodeService.getProperty(childAssocRef.getChildRef(), ContentModel.PROP_NODE_DBID);
        nodeService.setProperty(childAssocRef.getChildRef(), MyCompanyModel.PROP_EVID, docId);
	}
}
