import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalDeleteTagComponent } from './modal-delete-tag.component';

describe('ModalDeleteTagComponent', () => {
  let component: ModalDeleteTagComponent;
  let fixture: ComponentFixture<ModalDeleteTagComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModalDeleteTagComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModalDeleteTagComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
