import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserlistEntryComponent } from './userlist-entry.component';

describe('UserlistEntryComponent', () => {
  let component: UserlistEntryComponent;
  let fixture: ComponentFixture<UserlistEntryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserlistEntryComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserlistEntryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
