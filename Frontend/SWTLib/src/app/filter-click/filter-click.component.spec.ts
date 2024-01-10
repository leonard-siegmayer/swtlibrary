import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterClickComponent } from './filter-click.component';

describe('FilterClickComponent', () => {
  let component: FilterClickComponent;
  let fixture: ComponentFixture<FilterClickComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [FilterClickComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterClickComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
