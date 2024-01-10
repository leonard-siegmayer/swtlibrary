import { TestBed } from '@angular/core/testing';

import { BorrowService } from './borrow.service';

describe('BorrowService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: BorrowService = TestBed.get(BorrowService);
    expect(service).toBeTruthy();
  });
});
