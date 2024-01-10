import { User } from './user';
import { Book } from './book';

export class Reservation {
    id: number;
    purpose: string;
    priority: number;
    user: User;
    physicalID: number;
    logicalID: number;
    date: Date;
    borrowDate: Date;
    rank: string;
    readyToBorrow: boolean;
    requiredTime: number;
    // only in frontend
    logicalBook: Book;
    borrowed: boolean;
}
