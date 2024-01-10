import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from './user';
import { Role } from './role';
import { environment } from 'src/environments/environment';
import { UserSettings } from './userSettings';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  restApi = environment.API_HOST + ":" + environment.API_PORT + "/api/user";

  constructor(
    private http: HttpClient
  ) { }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.restApi + "/getAll", { withCredentials: true })
  }

  setRole(id: number, role: Role) {
    const options = {
      params: {
        "setRole": role.toString()
      },
      withCredentials: true
    }
    return this.http.post(this.restApi + "/" + id, "", options);
  }

  updateUserSettings(id: number, settings: UserSettings) {
    return this.http.post(this.restApi + "/" + id + "/settings", settings, { withCredentials: true });
  }
}
