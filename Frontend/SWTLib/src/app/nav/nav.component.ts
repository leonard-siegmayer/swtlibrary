import { Component, OnInit } from '@angular/core';
import { LoginService } from '../login.service';
import { Role } from '../role';

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.scss']
})
export class NavComponent implements OnInit {

  staffOrAdmin = false;
  admin = false;
  student = false;

  constructor(private loginService: LoginService) { }

  ngOnInit() {
    this.loginService.getUser(
      user => {
        if (user && (user.role == Role.ROLE_ADMIN || user.role == Role.ROLE_STAFF)) {
          this.staffOrAdmin = true;
        }
        if (user && (user.role == Role.ROLE_ADMIN)) {
          this.admin = true;
        }
        if (user && (user.role == Role.ROLE_STUDENT)) {
          this.student = true;
        }
      }
    )

  }

}
