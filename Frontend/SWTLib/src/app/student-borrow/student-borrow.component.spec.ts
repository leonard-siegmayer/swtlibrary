import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StudentBorrowComponent } from './student-borrow.component';

describe('StudentBorrowComponent', () => {
  let component: StudentBorrowComponent;
  let fixture: ComponentFixture<StudentBorrowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [StudentBorrowComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StudentBorrowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
