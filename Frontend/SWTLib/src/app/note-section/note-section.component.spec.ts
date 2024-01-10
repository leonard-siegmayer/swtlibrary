import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NoteSectionComponent } from './note-section.component';

describe('NoteSectionComponent', () => {
  let component: NoteSectionComponent;
  let fixture: ComponentFixture<NoteSectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NoteSectionComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NoteSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
