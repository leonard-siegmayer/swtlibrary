import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalUpdateRankComponent } from './modal-update-rank.component';

describe('ModalUpdateRankComponent', () => {
  let component: ModalUpdateRankComponent;
  let fixture: ComponentFixture<ModalUpdateRankComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModalUpdateRankComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModalUpdateRankComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
