import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { User } from './user';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  restApi = environment.API_HOST + ":" + environment.API_PORT;
  redirectURL = environment.REDIRECT_HOST + ":" + environment.REDIRECT_PORT + "/auth";

  currentUser: User = null;
  loggedIn = false;

  constructor(private http: HttpClient, private router: Router) {
  }


  login() {
    this.setPreviousUrl();

    let client_id = environment.oAuth_client_id;
    let url = environment.oAuth_url;
    let response_type = environment.oAuth_response_type;
    let scope = environment.oAuth_scope;

    window.location.href = (url + '?client_id=' + client_id + '&redirect_uri=' + this.redirectURL + '&response_type=' + response_type + '&scope=' + scope)
  }

  authenticate(code: string) {
    const formData = new FormData();
    formData.append('code', code);
    formData.append('redirect_uri', this.redirectURL);
    return this.http.post(this.restApi + "/auth/login", formData, { withCredentials: true });
  }

  logout() {
    this.http.post(this.restApi + '/auth/logout', "", { withCredentials: true }).subscribe(
      success => {
        sessionStorage.removeItem("user");
        this.loggedIn = false;
        this.currentUser = null;
        location.href = "/"
      },
      error => {
        sessionStorage.removeItem("user");
        this.loggedIn = false;
        this.currentUser = null;
        location.href = "/"
      }
    );

  }

  getUser(success) {
    if (sessionStorage.getItem("user")) {
      this.currentUser = JSON.parse(sessionStorage.getItem("user"));
      this.loggedIn = true;
      success(this.currentUser);
    } else {
      this.whoAmICallback(success(this.currentUser))
    }
  }

  whoAmICallback(success) {
    this.http.get(this.restApi + "/auth/whoami", { withCredentials: true }).subscribe(
      (user: User) => {
        this.setUser(user);
        this.loggedIn = true;
        success(this.currentUser);
      },
      error => {

      });
  }

  setUser(user: User) {
    this.currentUser = user;
    sessionStorage.setItem("user", JSON.stringify(this.currentUser));
  }

  private setPreviousUrl() {
    sessionStorage.setItem("previousUrl", this.router.url);
  }

  getPreviousUrl() {
    return sessionStorage.getItem("previousUrl") || "/";
  }
}
