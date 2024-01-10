import { User } from './user';

export class Note {
    id: number;
    user: User;
    note: string;
    date: Date;
}