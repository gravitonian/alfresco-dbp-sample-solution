/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.alfresco.reposamples;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Repository action that will set the ACME Contract Type of any file it's applied to.
 *
 * @author martin.bergljung@alfresco.com
 */
public class SetContractTypeRepoAction extends ActionExecuterAbstractBase {
    private static final Logger LOG = LoggerFactory.getLogger(SetContractTypeRepoAction.class);

    public static final String PARAM_DOCID = "docid";
    public static final String PARAM_SECURITY_CLASS = "securityclass";
    public static final String PARAM_CONTRACT_ID = "contractid";
    public static final String PARAM_CONTRACT_NAME = "contractname";

    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        for (String s : new String[]{PARAM_DOCID, PARAM_SECURITY_CLASS, PARAM_CONTRACT_ID, PARAM_CONTRACT_NAME}) {
            paramList.add(new ParameterDefinitionImpl(s, DataTypeDefinition.TEXT, true, getParamDisplayLabel(s)));
        }
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        // Make sure node we are operating on actually exist
        if (serviceRegistry.getNodeService().exists(actionedUponNodeRef) == true) {
            // Get the parameters passed into the action
            String docId = (String) action.getParameterValue(PARAM_DOCID);
            String secClass = (String) action.getParameterValue(PARAM_SECURITY_CLASS);
            String contractId = (String) action.getParameterValue(PARAM_CONTRACT_ID);
            String contractName = (String) action.getParameterValue(PARAM_CONTRACT_NAME);

            String acmeModelURI = "http://www.acme.org/model/content/1.0";

            // Set the type with properties on the node
            serviceRegistry.getNodeService().setType(actionedUponNodeRef,
                    QName.createQName(acmeModelURI, "contract"));
            Map<QName, Serializable> typeProperties = new HashMap<QName, Serializable>();
            typeProperties.put(QName.createQName(acmeModelURI, "documentId"), docId);
            typeProperties.put(QName.createQName(acmeModelURI, "contractId"), contractId);
            typeProperties.put(QName.createQName(acmeModelURI, "contractName"), contractName);
            serviceRegistry.getNodeService().setProperties(actionedUponNodeRef, typeProperties);

            // Apply the Security Classified aspect to the node
            Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
            aspectProperties.put(QName.createQName(acmeModelURI, "securityClassification"), secClass);
            serviceRegistry.getNodeService().addAspect(actionedUponNodeRef,
                    QName.createQName(acmeModelURI, "securityClassified"), aspectProperties);
        }
    }
}

