import { User } from './user';
import { BorrowingStatus } from './borrowing-status';
import { PhysicalBook } from './physicalBook';

export class BorrowingEntity {

    id: number;
    borrower: User;
    medium: PhysicalBook;
    borrowDate: string;
    dueDate: string;
    returnDate: string;
    status: BorrowingStatus;
    responsibleStaff: User;

}