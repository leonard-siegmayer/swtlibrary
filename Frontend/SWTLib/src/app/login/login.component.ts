import { Component, OnInit } from '@angular/core';
import { LoginService } from '../login.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  username: string;
  loggedIn: boolean = false;

  constructor(private loginService: LoginService, private router: Router) { }

  ngOnInit() {
    this.loginService.getUser(
      user => {
        if (user != null) {
          this.username = user.name;
          this.loggedIn = true;
        }
      }
    )
  }

  login() {
    this.loginService.login();
  }

  logout() {
    this.loginService.logout();
  }

}
