import { BorrowingEntity } from './borrowing-entity';
import { PhysicalBook } from './physicalBook';
import { Book } from './book';

export class ResponsePhysicalsExtended {
    physical: PhysicalBook;
    borrowings: BorrowingEntity[];
    media: Book;
}