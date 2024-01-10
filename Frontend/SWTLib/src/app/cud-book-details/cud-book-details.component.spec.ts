import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CUDBookDetailsComponent } from './cud-book-details.component';

describe('CUDBookDetailsComponent', () => {
  let component: CUDBookDetailsComponent;
  let fixture: ComponentFixture<CUDBookDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CUDBookDetailsComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CUDBookDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
