import { Component, OnInit } from '@angular/core';
import { BorrowService } from '../borrow.service';
import { ResponseBorrowingEnititiesExtended } from '../response-borrowing-entities-extended';
import { Config } from '../config';
import { LoginService } from '../login.service';
import { Role } from '../role';

@Component({
  selector: 'app-student-borrow',
  templateUrl: './student-borrow.component.html',
  styleUrls: ['./student-borrow.component.scss']
})
export class StudentBorrowComponent implements OnInit {

  studentRequests: ResponseBorrowingEnititiesExtended[] = [];
  maxBorrowingDate: Date;
  isAuthorized: boolean;

  constructor(private borrowService: BorrowService, private loginService: LoginService) { }

  ngOnInit() {
    this.loginService.getUser(
      user => {
        this.isAuthorized = (user != null && (user.role === Role.ROLE_ADMIN || user.role === Role.ROLE_STAFF))
        this.buildPage();
      });
  }

  buildPage() {
    this.borrowService.getStudentRequests().subscribe(
      (data: ResponseBorrowingEnititiesExtended[]) => {
        this.studentRequests = data;

        if (this.studentRequests.length == 0) {
          this.studentRequests = null;
        }
      }
    );
    this.getMaxBorrowingDate();
  }

  getPDF(id: number) {
    this.borrowService.getPDF(id).subscribe(
      (data: ArrayBuffer) => {
        let blob = new Blob([data], { type: 'application/pdf' });
        let url = window.URL.createObjectURL(blob);
        window.open(url);
      }
    );
  }

  acceptBorrow(id: number) {
    let date: string = document.forms["form" + id.toString()].elements["date"].value;
    let validDate: RegExp = /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/

    if (!validDate.test(date)) {
      document.forms["form" + id.toString()].elements["date"].classList.add("is-invalid");
      return;
    } else {
      document.forms["form" + id.toString()].elements["date"].classList.remove("is-invalid");
    }

    this.borrowService.acceptStudentRequest(id, date).subscribe(
      data => {
        document.getElementById("accept" + id).style.display = 'block';
        document.getElementById("request" + id).style.display = 'none';
      },
      error => {
        document.getElementById("failAccept" + id).style.display = 'block';
        document.getElementById("request" + id).style.display = 'none';
      }
    );

  }

  declineBorrow(id: number) {
    this.borrowService.declineStudentRequest(id).subscribe(
      data => {
        document.getElementById("decline" + id).style.display = 'block';
        document.getElementById("request" + id).style.display = 'none';
      },
      error => {
        document.getElementById("failDecline" + id).style.display = 'block';
        document.getElementById("request" + id).style.display = 'none';
      }
    );
  }

  // deadline is today + max renting days from server
  getMaxBorrowingDate() {
    let borrowDate = new Date();
    this.borrowService.getConfig((config: Config) => {
      this.maxBorrowingDate = new Date(borrowDate.setDate(borrowDate.getDate() + config.MAX_DAYS))
    });
  }

  getEarlisestReturnDate() {
    return new Date();
  }

}
