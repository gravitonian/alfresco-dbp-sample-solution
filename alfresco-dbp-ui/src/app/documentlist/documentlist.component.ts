import { Component, ViewChild, Input, OnInit } from '@angular/core';
import { NotificationService } from '@alfresco/adf-core';
import { DocumentListComponent } from '@alfresco/adf-content-services';
import { PreviewService } from '../services/preview.service';
import { MinimalNodeEntryEntity } from 'alfresco-js-api';
import { ActivatedRoute, Router } from '@angular/router';
import { map } from 'rxjs/internal/operators';

@Component({
  selector: 'app-documentlist',
  templateUrl: './documentlist.component.html',
  styleUrls: ['./documentlist.component.css']
})
export class DocumentlistComponent implements OnInit {
  currentFolderId = '-root-'; // By default display /Company Home

  @Input()
  showViewer = false;

  nodeId: string = null;

  @ViewChild('documentList')
  documentList: DocumentListComponent;

  constructor(private notificationService: NotificationService,
              private preview: PreviewService,
              protected activatedRoute: ActivatedRoute,
              protected router: Router) {
  }

  ngOnInit(): void {
    // Check if we should display some other folder than root
    const currentFolderIdObservable = this.activatedRoute
      .queryParamMap
      .pipe(map(params => params.get('current_folder_id')));
    currentFolderIdObservable.subscribe((id: string) => {
      if (id) {
        this.currentFolderId = id;
        this.documentList.loadFolderByNodeId(this.currentFolderId);
      }
    });
  }

  uploadSuccess(event: any) {
    this.notificationService.openSnackMessage('File uploaded');
    this.documentList.reload();
  }

  showPreview(event) {
    const entry = event.value.entry;
    if (entry && entry.isFile) {
      this.preview.showResource(entry.id);
    }
  }

  onGoBack(event: any) {
    this.showViewer = false;
    this.nodeId = null;
  }

  getDocumentListCurrentFolderId() {
    return this.currentFolderId;
  }

  onDocumentDetails(event: any) {
    const entry: MinimalNodeEntryEntity = event.value.entry;
    console.log('DocumentlistComponent: Navigating to details page for document: ' + entry.name);
    this.router.navigate(['/documentdetails', entry.id]);
  }
}
