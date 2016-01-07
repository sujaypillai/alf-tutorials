package org.ootb.repo.model;

import org.alfresco.service.namespace.QName;

public interface MyCompanyModel {
	public static final String MYCOMPANY_MODEL_URI    = "http://www.mycompany.com/model/content/1.0";
    public static final String MYCOMPANY_MODEL_PREFIX = "myc";
    
    static final QName ASPECT_EVUNIQUEIDENTIFIER = QName.createQName(MYCOMPANY_MODEL_URI, "uniqueid");
    static final QName PROP_EVID   = QName.createQName(MYCOMPANY_MODEL_URI, "id");
}
