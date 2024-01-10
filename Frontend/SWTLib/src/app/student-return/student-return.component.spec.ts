import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StudentReturnComponent } from './student-return.component';

describe('StudentReturnComponent', () => {
  let component: StudentReturnComponent;
  let fixture: ComponentFixture<StudentReturnComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [StudentReturnComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StudentReturnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
