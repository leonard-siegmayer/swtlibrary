import { Role } from './role';
import { UserSettings } from './userSettings';

export class User {
  id: number;
  name: string;
  email: string;
  role: Role;
  profilePicture: string;
  settings: UserSettings;
}