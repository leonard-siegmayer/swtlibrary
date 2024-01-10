import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalDeletePhysicalComponent } from './modal-delete-physical.component';

describe('ModalDeletePhysicalComponent', () => {
  let component: ModalDeletePhysicalComponent;
  let fixture: ComponentFixture<ModalDeletePhysicalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModalDeletePhysicalComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModalDeletePhysicalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
