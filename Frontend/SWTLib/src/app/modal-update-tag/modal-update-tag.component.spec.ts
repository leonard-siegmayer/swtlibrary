import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalUpdateTagComponent } from './modal-update-tag.component';

describe('ModalUpdateTagComponent', () => {
  let component: ModalUpdateTagComponent;
  let fixture: ComponentFixture<ModalUpdateTagComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModalUpdateTagComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModalUpdateTagComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
