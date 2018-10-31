import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DocumentDetailsPageComponent } from './document-details-page.component';

describe('DocumentDetailsPageComponent', () => {
  let component: DocumentDetailsPageComponent;
  let fixture: ComponentFixture<DocumentDetailsPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DocumentDetailsPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DocumentDetailsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
