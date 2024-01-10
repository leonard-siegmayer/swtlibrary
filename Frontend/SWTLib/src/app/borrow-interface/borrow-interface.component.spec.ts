import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BorrowInterfaceComponent } from './borrow-interface.component';

describe('BorrowInterfaceComponent', () => {
  let component: BorrowInterfaceComponent;
  let fixture: ComponentFixture<BorrowInterfaceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BorrowInterfaceComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BorrowInterfaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
