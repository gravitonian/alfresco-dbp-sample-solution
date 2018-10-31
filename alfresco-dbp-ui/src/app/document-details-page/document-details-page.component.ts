import { Component, OnInit } from '@angular/core';
import { MinimalNodeEntryEntity, NodeBody } from 'alfresco-js-api';
import { ContentService, FormModel, FormService, FormValues, NodesApiService, NotificationService } from '@alfresco/adf-core';
import { ActivatedRoute, Router } from '@angular/router';
import { RepositoryContentModel } from '../repository/repository-content.model';
import { RepositoryFormFieldModel } from '../repository/repository-formfield.model';
import { AlfrescoNodeForm } from '../repository/alfresco-node-form';
import { ContractNodeForm } from '../repository/contract-node-form';

@Component({
  selector: 'app-document-details-page',
  templateUrl: './document-details-page.component.html',
  styleUrls: ['./document-details-page.component.scss']
})
export class DocumentDetailsPageComponent implements OnInit {
  node: MinimalNodeEntryEntity;
  parentFolder: MinimalNodeEntryEntity;

  form: FormModel;
  originalFormData: FormValues = {};

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private nodeService: NodesApiService,
              private contentService: ContentService,
              private formService: FormService,
              protected notificationService: NotificationService) { }

  ngOnInit() {
    const nodeId = this.activatedRoute.snapshot.params['node-id'];
    this.nodeService.getNode(nodeId).subscribe((entry: MinimalNodeEntryEntity) => {
      this.node = entry;

      this.nodeService.getNode(this.node.parentId).subscribe((parentNode: MinimalNodeEntryEntity) => {
        this.parentFolder = parentNode;
      });

      this.setupFormData(this.node);
    });
  }

  private setupFormData(node: MinimalNodeEntryEntity) {
    console.log('setupFormData: ', node.id);

    // Content file specific props
    let size = 'N/A';
    let mimetype = 'N/A';
    if (node.isFile) {
      size = '' + node.content.sizeInBytes;
      mimetype = node.content.mimeTypeName;
    }

    // Aspect properties
    let title = '';
    let desc = '';
    if (node.aspectNames.indexOf(RepositoryContentModel.TITLED_ASPECT_QNAME) > -1) {
      title = node.properties[RepositoryContentModel.TITLE_PROP_QNAME];
      desc = node.properties[RepositoryContentModel.DESC_PROP_QNAME];
    }
    let securityClassification = '';
    if (node.aspectNames.indexOf(RepositoryContentModel.ACME_SECURITY_CLASSIFIED_ASPECT_QNAME) > -1) {
      securityClassification = node.properties[RepositoryContentModel.ACME_SECURITY_CLASSIFICATION_PROP_QNAME];
    }

    // Author can be available if extracted during ingestion of content
    let author = '';
    if (node.properties && node.properties[RepositoryContentModel.AUTHOR_PROP_QNAME]) {
      author = node.properties[RepositoryContentModel.AUTHOR_PROP_QNAME];
    }

    // If this is a ACME contract file, then fish out those properties
    let contractid = '';
    let contractName = '';
    if (node.nodeType === RepositoryContentModel.ACME_CONTRACT_TYPE_QNAME) {
      contractid = node.properties[RepositoryContentModel.ACME_CONTRACT_ID_PROP_QNAME];
      contractName = node.properties[RepositoryContentModel.ACME_CONTRACT_NAME_PROP_QNAME];
    }

    this.originalFormData = {
      'id': node.id,
      'type': node.nodeType,
      'secondarytypes': node.aspectNames,
      'creator': node.createdByUser.displayName,
      'created': node.createdAt,
      'modifier': node.modifiedByUser.displayName,
      'modified': node.modifiedAt,
      'sizebytes': size,
      'mimetype': mimetype,
      'title': title,
      'description': desc,
      'author': author,
      'contractid': contractid,
      'contractname': contractName,
      'securityclassification': securityClassification
    };

    let formDefinitionJSON;
    if (node.nodeType === RepositoryContentModel.ACME_CONTRACT_TYPE_QNAME) {
      formDefinitionJSON = ContractNodeForm.getDefinition();
    } else {
      formDefinitionJSON = AlfrescoNodeForm.getDefinition();
    }
    // Read and parse the form that we will use to display the node
    const readOnly = false;
    this.form = this.formService.parseForm(formDefinitionJSON, this.originalFormData, readOnly);
  }

  onGoBack($event: Event) {
    this.navigateBack2DocList();
  }

  onDownload($event: Event) {
    const url = this.contentService.getContentUrl(this.node.id, true);
    const fileName = this.node.name;
    this.download(url, fileName);
  }

  onDelete($event: Event) {
    this.nodeService.deleteNode(this.node.id).subscribe(() => {
      this.navigateBack2DocList();
    });
  }

  onSave(form: FormModel) {
    const titleChanged = this.form.values[RepositoryFormFieldModel.TITLE_FIELD_NAME] &&
      (this.form.values[RepositoryFormFieldModel.TITLE_FIELD_NAME] !==
        this.originalFormData[RepositoryFormFieldModel.TITLE_FIELD_NAME]);
    const descriptionChanged = this.form.values[RepositoryFormFieldModel.DESC_FIELD_NAME] &&
      (this.form.values[RepositoryFormFieldModel.DESC_FIELD_NAME] !==
        this.originalFormData[RepositoryFormFieldModel.DESC_FIELD_NAME]);
    if (titleChanged || descriptionChanged) {
      // We got some non-readonly metadata that has been updated

      console.log('Updating [cm:title = ' + this.form.values[RepositoryFormFieldModel.TITLE_FIELD_NAME] + ']');
      console.log('Updating [cm:description = ' + this.form.values[RepositoryFormFieldModel.DESC_FIELD_NAME] + ']');

      // Set up the properties that should be updated
      const nodeBody = <NodeBody> {};
      nodeBody[RepositoryContentModel.NODE_BODY_PROPERTIES_KEY] = {};
      nodeBody[RepositoryContentModel.NODE_BODY_PROPERTIES_KEY][RepositoryContentModel.TITLE_PROP_QNAME] = this.form.values['title'];
      nodeBody[RepositoryContentModel.NODE_BODY_PROPERTIES_KEY][RepositoryContentModel.DESC_PROP_QNAME] = this.form.values['description'];

      // Make the call to Alfresco Repo and update props
      this.nodeService.updateNode(this.node.id, nodeBody).subscribe(
        () => {
          this.notificationService.openSnackMessage(
            `Updated properties for '${this.node.name}' successfully`,
            4000);
        }
      );
    } else {
      this.notificationService.openSnackMessage(
        `Node '${this.node.name}' was NOT saved, nothing has been changed!`,
        4000);
    }
  }

  private navigateBack2DocList() {
    this.router.navigate(['/documentlist'],
      {
        queryParams: {current_folder_id: this.parentFolder.id},
        relativeTo: this.activatedRoute
      });
  }

  private download(url: string, fileName: string) {
    if (url && fileName) {
      const link = document.createElement('a');

      link.style.display = 'none';
      link.download = fileName;
      link.href = url;

      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }
}
