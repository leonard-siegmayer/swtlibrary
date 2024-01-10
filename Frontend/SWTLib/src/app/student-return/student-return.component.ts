import { Component, OnInit } from '@angular/core';
import { ResponseBorrowingEnititiesExtended } from '../response-borrowing-entities-extended';
import { BorrowService } from '../borrow.service';
import { Config } from '../config';
import { LoginService } from '../login.service';
import { Role } from '../role';

@Component({
  selector: 'app-student-return',
  templateUrl: './student-return.component.html',
  styleUrls: ['./student-return.component.scss']
})
export class StudentReturnComponent implements OnInit {

  borrowedMedia: ResponseBorrowingEnititiesExtended[] = [];
  expiredMedia: ResponseBorrowingEnititiesExtended[] = [];
  maxBorrowingDays: number;
  isAuthorized: boolean;

  constructor(private borrowService: BorrowService, private loginService: LoginService) { }

  ngOnInit() {
    this.loginService.getUser(
      user => {
        this.isAuthorized = (user != null && (user.role === Role.ROLE_ADMIN || user.role === Role.ROLE_STAFF))
      }
    );

    this.borrowService.getStudentBorrowings().subscribe(
      (data: ResponseBorrowingEnititiesExtended[]) => {
        data.forEach(borrowing => {
          if (this.getDueDate(borrowing.borrowingEntity.dueDate) < this.getToday()) {
            this.expiredMedia.push(borrowing);
          } else {
            this.borrowedMedia.push(borrowing);
          }
        });
        // do not display anything in case of empty data
        if (this.expiredMedia.length == 0) {
          this.expiredMedia = null;
        }
        if (this.borrowedMedia.length == 0) {
          this.borrowedMedia = null;
        }
      }
    );
    // get server configuration for date selection
    this.getMaxBorrowingDays();
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


  extendBorrow(id: number) {
    let date: string = document.forms["form" + id.toString()].elements["date"].value;
    let validDate: RegExp = /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/

    if (!validDate.test(date)) {
      document.forms["form" + id.toString()].elements["date"].classList.add("is-invalid");
      return;
    } else {
      document.forms["form" + id.toString()].elements["date"].classList.remove("is-invalid");
    }

    this.borrowService.extendStudentBorrow(id, date).subscribe(
      (data: any) => {
        if (data.status == 200) {
          document.getElementById("extend" + id).style.display = 'block';
          document.getElementById("extend" + id).firstChild.textContent = data.message;
          document.getElementById("request" + id).style.display = 'none';
        } else {
          document.getElementById("failExtend" + id).style.display = 'block';
          document.getElementById("failExtend" + id).firstChild.textContent = data.message;
          document.getElementById("request" + id).style.display = 'none';
        }
      },
      error => {
        document.getElementById("failExtend" + id).style.display = 'block';
        document.getElementById("request" + id).style.display = 'none';
      }
    );

  }

  returnBorrow(id: number) {
    this.borrowService.returnStudentBorrow(id).subscribe(
      data => {
        document.getElementById("return" + id).style.display = 'block';
        document.getElementById("request" + id).style.display = 'none';
      },
      error => {
        document.getElementById("failReturn" + id).style.display = 'block';
        document.getElementById("request" + id).style.display = 'none';
      }
    );

  }

  getMaxBorrowingDays() {
    this.borrowService.getConfig((config: Config) => {
      this.maxBorrowingDays = config.MAX_DAYS;
    });
  }

  getMaxExtensionDate(borrowDate: string) {
    let date = new Date(borrowDate);
    return date.setDate(date.getDate() + this.maxBorrowingDays)
  }

  getDueDate(dueDate: string) {
    return new Date(dueDate);
  }

  getToday() {
    return new Date();
  }

}
