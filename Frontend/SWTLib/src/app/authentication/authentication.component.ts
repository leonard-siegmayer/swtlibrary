import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { LoginService } from '../login.service';


@Component({
  selector: 'app-authentication',
  templateUrl: './authentication.component.html',
  styleUrls: ['./authentication.component.scss']
})


export class AuthenticationComponent implements OnInit {

  //if authentication fails, view shows login option
  success: boolean = true;

  constructor(private route: ActivatedRoute, private http: HttpClient, private router: Router, private loginService: LoginService) {

  }

  // component sends recieved token to backend for validation
  ngOnInit() {
    let code = this.route.snapshot.queryParamMap.get('code');
    this.loginService.authenticate(code).subscribe(
      (data) => {
        // on success the whoami endpoint is asked for user info
        this.loginService.whoAmICallback(
          () => {
            // now user is signed in and gets redirected to last page
            const prevUrl = this.loginService.getPreviousUrl();
            this.router.navigateByUrl(prevUrl);
          }
        );
      },
      error => {
        this.success = false;
      }
    );

  }



}
