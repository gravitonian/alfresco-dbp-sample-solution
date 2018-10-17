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
package com.activiti.extension.bean;

import com.activiti.domain.integration.AlfrescoEndpoint;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.service.api.AlfrescoEndpointService;
import com.activiti.service.runtime.RelatedContentService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.alfresco.repository.remote.client.ApiClient;
import org.alfresco.repository.remote.client.api.QueriesApi;
import org.alfresco.repository.remote.client.model.*;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.alfresco.repository.remote.client.ApiException;
import org.alfresco.repository.remote.client.api.NodesApi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.activiti.alfrescoconnector.AlfrescoConnectorConstants.ON_PREM_API_LOCATION;

/**
 * A Service Task delegate implemented as a Spring Bean that uses the ACS ReST API Java Client
 * to set a type, aspect, and properties on a file and move it to a different folder.
 * <p/>
 * This requires you to first clone https://github.com/gravitonian/acs-rest-api-java-client
 * and then run mvn install so you have this lib in your local maven repo.
 *
 * @author martin.bergljung@alfresco.com
 */
@Component("setContractTypeJavaDelegate")
public class SetContractTypeJavaDelegate implements JavaDelegate {
  private static Logger logger = LoggerFactory.getLogger(SetContractTypeJavaDelegate.class);

  private static final String CONTRACT_FILE_VAR_NAME = "contracttobeapproved";

  @Autowired
  protected AlfrescoEndpointService alfrescoEndpointService;

  @Autowired
  RelatedContentService relatedContentService;

  // Service Task fields
  private Expression alfrescoRepoId;
  private Expression contractId;
  private Expression contractName;
  private Expression securityClassification;

  /**
   * Service Task class field injection
   */
  public void setAlfrescoRepoId(Expression alfrescoRepoId) {
    this.alfrescoRepoId = alfrescoRepoId;
  }
  public void setContractName(Expression contractName) { this.contractName = contractName; }
  public void setContractId(Expression contractId) { this.contractId = contractId; }
  public void setSecurityClassification(Expression securityClassification) { this.securityClassification = securityClassification; }

  /**
   * Service Task implementation
   */
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String alfrescoRepoIdText = (String) alfrescoRepoId.getValue(execution);
    String contractIdText = (String) contractId.getValue(execution);
    String contractNameText = (String) contractName.getValue(execution);
    String securityClassificationText = (String) securityClassification.getValue(execution);

    logger.info("Alfresco Repo ID, should match what's configured in Tenant section: [ID=" + alfrescoRepoIdText + "]");

    // Format as Service ID
    String repoId = "alfresco-" + alfrescoRepoIdText;

    // Get the Alfresco Repository Endpoint configuration from APS
    AlfrescoEndpoint endpoint = alfrescoEndpointService.getById(
      alfrescoEndpointService.getEndpointIdForServiceId(repoId));
    if (endpoint != null) {
      logger.info("Alfresco Repo endpoint: [endpointRepoUrl=" + endpoint.getRepositoryUrl() +
        "][secret=" + endpoint.getSecret() + "]");

      // https://content:8080/alfresco/api/-default-/public/alfresco/versions/1
      String url = endpoint.getRepositoryUrl() + "/" + ON_PREM_API_LOCATION.substring(0, ON_PREM_API_LOCATION.length()-1);
      logger.info("ACS ReST API base URL: " + url);

      // Generic API client that can be used by the specific API clients
      ApiClient client = new ApiClient();
      client.setBasePath(url);                              // TODO: Fix these hard coded credentials...
      client.addDefaultHeader("Authorization", getCredential("admin", "admin"));

      // Create/Get folder for where to store approved contract
      Node monthFolderNode = createFolderStructure(client);

      // Move contract file into month folder
      String contractFileNodeId = getContractNodeId(execution);
      boolean moveSuccessful = moveNode(client, contractFileNodeId, monthFolderNode.getId());

      // Set Contract Type and associated properties
      if (moveSuccessful) {
        setContractType(client, contractFileNodeId, contractIdText, contractNameText, securityClassificationText);
      }
    }
  }

  /**
   * Create the folder structure for where we will store the approved contracts:
   *
   * Company Home
   *    Contract Management
   *      2018
   *        OCT
   *
   * @param client
   * @return
   * @throws Exception
   */
  private Node createFolderStructure(ApiClient client) throws Exception {
    // Create/Get the main Contract Management folder
    String contractManagementFolderName = "Contract Management";
    Node contractManagementFolderNode =  findNode(client, contractManagementFolderName, "-root-");
    if (contractManagementFolderNode == null) {
      contractManagementFolderNode = createFolder(client, contractManagementFolderName,
              "Management of Contracts",
              "All files related to approved contracts",
              "");
    }

    // Create/Get the year folder
    Integer year = Calendar.getInstance().get(Calendar.YEAR);
    Node yearFolderNode =  findNode(client, year.toString(), contractManagementFolderNode.getId());
    if (yearFolderNode == null) {
      yearFolderNode = createFolder(client, year.toString(),
              "Contracts for a Year",
              "All files related to approved contracts for year " + year,
              contractManagementFolderName);
    }

    // Create a month folder under year folder
    Calendar cal = Calendar.getInstance();
    String currentMonth = new SimpleDateFormat("MMM").format(cal.getTime());
    Node monthFolderNode =  findNode(client, currentMonth, yearFolderNode.getId());
    if (monthFolderNode == null) {
      monthFolderNode = createFolder(client, currentMonth,
              "Contracts for 1 month",
              "All contract files for month " + currentMonth,
              contractManagementFolderName + "/" + year.toString());
    }

    return monthFolderNode;
  }

  /**
   * Make the remote call to create a folder in ACS, if it does not exist.
   *
   * For more info: https://github.com/gravitonian/acs-rest-api-java-client/blob/master/docs/NodesApi.md#createNode
   *
   * @param client - the API client to use with base URL and auth
   * @param folderName the name of the folder
   * @param folderTitle the title of the folder
   * @param folderDescription the description of the folder
   * @param relativeFolderPath path relative to /Company Home
   * @return a node object for the newly created node, contains the ID,
   * such as e859588c-ae81-4c5e-a3b6-4c6109b6c905
   */
  private Node createFolder(ApiClient client,
                            String folderName,
                            String folderTitle,
                            String folderDescription,
                            String relativeFolderPath) throws IOException {
    // Specific API client for working with Repository Nodes
    NodesApi apiInstance = new NodesApi(client);

    String rootPath = "-root-";       // /Company Home
    String folderType = "cm:folder";  // Standard out-of-the-box folder type

    List<String> folderAspects = new ArrayList<String>();
    folderAspects.add("cm:titled");
    Map<String, String> folderProps = new HashMap<>();
    folderProps.put("cm:title", folderTitle);
    folderProps.put("cm:description", folderDescription);

    // The identifier of a node. You can also use one of these well-known aliases: * -my- * -shared- * -root-
    String nodeId = rootPath;
    NodeBodyCreate nodeBodyCreate = new NodeBodyCreate();
    nodeBodyCreate.setName(folderName);
    nodeBodyCreate.setNodeType(folderType);
    nodeBodyCreate.setAspectNames(folderAspects);
    nodeBodyCreate.setProperties(folderProps);
    nodeBodyCreate.setRelativePath(relativeFolderPath);

    // If true, then  a name clash will cause an attempt to auto rename by
    // finding a unique name using an integer suffix.
    Boolean autoRename = true;
    // Returns additional information about the node.
    // The following optional fields can be requested:
    // * allowableOperations
    // * association
    // * isLink
    // * isLocked
    // * path
    // * permissions
    List<String> include = null;
    // A list of field names.
    // You can use this parameter to restrict the fields returned within a response if, for example,
    // you want to save on overall bandwidth. The list applies to a returned individual entity or entries
    // within a collection.  If the API method also supports the **include** parameter, then the fields specified in
    // the **include** parameter are returned in addition to those specified in the **fields** parameter.
    List<String> fields = null;

    NodeEntry newNode = null;
    try {
      newNode = apiInstance.createNode(nodeId, nodeBodyCreate, autoRename, include, fields);
      logger.info("Created new folder [name="+ folderName + "]" + newNode);
    } catch (ApiException e) {
      logger.error("Exception when calling NodesApi#createNode [responseCode=" +
        e.getCode() + "][responseBody=" + e.getResponseBody() + "]");
      e.printStackTrace();
    }

    return newNode != null ? newNode.getEntry() : null;
  }

  /**
   * Find a node based on name.
   * Search term must be at least 3 characters.
   *
   * For more info: https://github.com/gravitonian/acs-rest-api-java-client/blob/master/docs/QueriesApi.md#findNodes
   *
   * @param client - the API client to use with base URL and auth
   * @param nodeName the name of the node (i.e. folder or file) that we are looking for, min 3 characters
   * @param parentNodeId the parent node under which we expect to find the node
   * @return a node object for the found node, or null if not found
   */
  private Node findNode(ApiClient client,
                            String nodeName,
                            String parentNodeId) {
    // Specific API client for working with Queries/Search
    QueriesApi apiInstance = new QueriesApi(client);

    String term = nodeName;           // The term to search for.
    String rootNodeId = parentNodeId; // The id of the node to start the search from.  Supports the aliases -my-, -root- and -shared-.
    Integer skipCount = 0;            // The number of entities that exist in the collection before those included in this list.  If not supplied then the default value is 0.
    Integer maxItems = 100;           // The maximum number of items to return in the list.  If not supplied then the default value is 100.
    String nodeType = null;           // Restrict the returned results to only those of the given node type and its sub-types

    // Returns additional information about the node.
    // The following optional fields can be requested:
    // * allowableOperations
    // * aspectNames
    // * isLink
    // * isLocked
    // * path
    // * properties
    List<String> include = null;
    // A string to control the order of the entities returned in a list.
    // You can use the **orderBy** parameter to sort the list by one or more fields.
    // Each field has a default sort order, which is normally ascending order.
    // Read the API method implementation notes above to check if any fields used in this
    // method have a descending default search order.
    // To sort the entities in a specific order, you can use the **ASC** and **DESC** keywords for any field.
    List<String> orderBy = null;
    // A list of field names.
    // You can use this parameter to restrict the fields returned within a response if, for example,
    // you want to save on overall bandwidth. The list applies to a returned individual entity or entries
    // within a collection. If the API method also supports the **include** parameter, then the fields specified in
    // the **include** parameter are returned in addition to those specified in the **fields** parameter.
    List<String> fields = null;

    NodeEntry nodeEntry = null;
    try {
      NodePaging result = apiInstance.findNodes(
              term, rootNodeId, skipCount, maxItems, nodeType, include, orderBy, fields);
      if (result.getList().getEntries().isEmpty() == false) {
        nodeEntry = result.getList().getEntries().get(0);
        logger.info("Found node [name=" + nodeEntry.getEntry().getName() + "]" + nodeEntry);
        return nodeEntry.getEntry();
      }
    } catch (ApiException e) {
      logger.error("Exception when calling QueriesApi#findNodes [responseCode=" +
              e.getCode() + "][responseBody=" + e.getResponseBody() + "]");
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Get the Alfresco Node ID for the contract file that we are reviewing and approving/rejecting
   * with the process instance.
   *
   * @param execution execution context
   * @return the Alfresco Node ID for contract file, or null if could not be extracted
   */
  private String getContractNodeId(DelegateExecution execution) {
    List<RelatedContent> contentList = relatedContentService.getFieldContentForProcessInstance(
            execution.getProcessInstanceId(), CONTRACT_FILE_VAR_NAME, 1, 0).getContent();

    if (contentList != null) {
      for (RelatedContent relCon : contentList) {
        logger.info("ACS file: " + relCon.getName() + ", created: " + relCon.getCreated());
        String acsNodeRef = relCon.getSourceId();
        return acsNodeRef;

        // ContentObject co = relatedContentService.getContentStorage().getContentObject(relCon.getContentStoreId());
        // Get the InputStream and do stuff with the file
        // co.getContent()
      }
    }

    return null;
  }

  /**
   * Move a node (file/folder) to a new parent folder.
   *
   * For more info: https://github.com/gravitonian/acs-rest-api-java-client/blob/master/docs/NodesApi.md#moveNode
   *
   * @param client - the API client to use with base URL and auth
   * @param nodeId the id of the node (i.e. folder or file) that we are moving
   * @param newParentNodeId the parent node id where we are moving node
   * @return true if node was moved successfully, otherwise false
   */
  private boolean moveNode(ApiClient client,
                           String nodeId,
                           String newParentNodeId) {
    // Specific API client for working with Repository Nodes
    NodesApi apiInstance = new NodesApi(client);

    // The targetParentId and, optionally, a new name which should include the file extension.
    NodeBodyMove nodeBodyMove = new NodeBodyMove();
    nodeBodyMove.setTargetParentId(newParentNodeId);

    // Returns additional information about the node. The following optional fields can be requested:
    // * allowableOperations
    // * association
    // * isLink
    // * isLocked
    // * path
    // * permissions
    List<String> include = null;
    // A list of field names.
    // You can use this parameter to restrict the fields returned within a response if, for example,
    // you want to save on overall bandwidth. The list applies to a returned individual entity or
    // entries within a collection. If the API method also supports the **include** parameter,
    // then the fields specified in the **include** parameter are returned in addition to
    // those specified in the **fields** parameter.
    List<String> fields = null;

    try {
      NodeEntry result = apiInstance.moveNode(nodeId, nodeBodyMove, include, fields);
      logger.info("Moved node [name=" + result.getEntry().getName() + "]" + result);
      return true;
    } catch (ApiException e) {
      logger.error("Exception when calling NodesApi#moveNode [responseCode=" +
              e.getCode() + "][responseBody=" + e.getResponseBody() + "]");
      e.printStackTrace();
    }

    return false;
  }

  /**
   * Set the content type on passed in contract file to 'Contract Type' and set also associated properties.
   * Set also aspect security classified.
   *
   * @param client - the API client to use with base URL and auth
   * @param contractFileNodeId - the Alfresco Node ID for the contract file
   * @param contractIdText - the contract ID that we want to set
   * @param contractNameText - the contract name that we want to set
   * @param securityClassificationText - the security classification that we want to set
   */
  private void setContractType(ApiClient client,
                               String contractFileNodeId,
                               String contractIdText,
                               String contractNameText,
                               String securityClassificationText) {
    // Specific API client for working with Repository Nodes
    NodesApi apiInstance = new NodesApi(client);

    // Get the contract file node first, so we can get to the aspects that are currently set
    Node contractFileNode = getNode(client, contractFileNodeId);

    if (contractFileNode != null) {
      Map<String, String> properties = new HashMap<>();
      properties.put("acme:documentId", "D" + getRandomDocNumber());
      properties.put("acme:contractId", contractIdText);
      properties.put("acme:contractName", contractNameText);
      properties.put("acme:securityClassification", securityClassificationText);
      List<String> aspectNames = contractFileNode.getAspectNames();
      aspectNames.add("acme:securityClassified");

      NodeBodyUpdate nodeBodyUpdate = new NodeBodyUpdate(); // The node information to update.
      nodeBodyUpdate.setNodeType("acme:contract");
      nodeBodyUpdate.setAspectNames(aspectNames);
      nodeBodyUpdate.setProperties(properties);

      // Returns additional information about the node. The following optional fields can be requested:
      // * allowableOperations
      // * association
      // * isLink
      // * isLocked
      // * path
      // * permissions
      List<String> include = null;
      // A list of field names.
      // You can use this parameter to restrict the fields returned within a response if, for example,
      // you want to save on overall bandwidth. The list applies to a returned individual entity or entries within
      // a collection. If the API method also supports the **include** parameter, then the fields specified in
      // the **include** parameter are returned in addition to those specified in the **fields** parameter.
      List<String> fields = null;
      try {
        NodeEntry result = apiInstance.updateNode(contractFileNodeId, nodeBodyUpdate, include, fields);
        System.out.println(result);
      } catch (ApiException e) {
        logger.error("Exception when calling NodesApi#updateNode [responseCode=" +
                e.getCode() + "][responseBody=" + e.getResponseBody() + "]");
        e.printStackTrace();
      }
    } else {
      logger.error("Failed to set Contract Type, file not found");
    }
  }

  /**
   * Get a random number between 1 and 999
   *
   * @return
   */
  private int getRandomDocNumber() {
    Random r = new Random();
    int  n = r.nextInt(998) + 1;
    return n;
  }

  /**
   * Get a node (file/folder).
   *
   * For more info: https://github.com/gravitonian/acs-rest-api-java-client/blob/master/docs/NodesApi.md#getNode
   *
   * @param client - the API client to use with base URL and auth
   * @param nodeId the id of the node (i.e. folder or file) that we want to get
   * @return Node object if exist, or null if does not exist
   */
  private Node getNode(ApiClient client,
                          String nodeId) {
    // Specific API client for working with Repository Nodes
    NodesApi apiInstance = new NodesApi(client);

    // A path relative to the **nodeId**. If you set this,  information is returned on the node resolved by this path.
    String relativePath = null;
    // Returns additional information about the node. The following optional fields can be requested:
    // * allowableOperations
    // * association
    // * isLink
    // * isLocked
    // * path
    // * permissions
    List<String> include = null;
    // A list of field names.
    // You can use this parameter to restrict the fields returned within a response if, for example,
    // you want to save on overall bandwidth. The list applies to a returned individual entity or
    // entries within a collection. If the API method also supports the **include** parameter,
    // then the fields specified in the **include** parameter are returned in addition to
    // those specified in the **fields** parameter.
    List<String> fields = null;

    try {
      NodeEntry result = apiInstance.getNode(nodeId, include, relativePath, fields);
      logger.info("Got node [name=" + result.getEntry().getName() + "]" + result);
      return result.getEntry();
    } catch (ApiException e) {
      logger.error("Exception when calling NodesApi#getNode [responseCode=" +
              e.getCode() + "][responseBody=" + e.getResponseBody() + "]");
      e.printStackTrace();
    }

    return null;
  }


  /**
   * Helper method to construct the value of the Basic Auth header
   *
   * @return
   */
  private String getCredential(String username, String pwd) {
    String auth = username + ":" + pwd;
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
    String authHeader = "Basic " + new String(encodedAuth);
    return authHeader;
  }


}
