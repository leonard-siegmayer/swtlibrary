import { User } from './user';
import { PhysicalStatus } from './physical-status';

export class PhysicalBook {
  id: number;
  displayId: number;
  mediumId: number;
  creationDate: Date;
  handapparat: boolean;
  rvkSignature: string;
  location: string;
  department: string;
  room: string;
  owner: User;
  status: PhysicalStatus;
  resCount: number;
}