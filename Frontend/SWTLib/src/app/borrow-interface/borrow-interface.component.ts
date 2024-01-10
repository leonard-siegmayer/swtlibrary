import { Component, OnInit, Input } from '@angular/core';
import { BorrowingStatus } from '../borrowing-status';
import { BorrowService } from '../borrow.service';
import { Role } from '../role';
import { LoginComponent } from '../login/login.component';
import { User } from '../user';
import { FormControl, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { Config } from '../config';
import { environment } from 'src/environments/environment';
import { NgbModal, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';
import { Reservation } from '../reservation';
import { PhysicalBook } from '../physicalBook';

@Component({
  selector: 'app-borrow-interface',
  templateUrl: './borrow-interface.component.html',
  styleUrls: ['./borrow-interface.component.scss']
})
export class BorrowInterfaceComponent implements OnInit {

  closeResult: string;

  @Input() id: number;
  @Input() user: User;
  @Input() reservation: Reservation;

  numberOfReservations: number;
  resFormGroup: FormGroup;

  role: typeof Role = Role;

  datePickerForm: FormControl = new FormControl('', [
    Validators.required,
    // check if type is date as safari does not suport input type date
    Validators.pattern(/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/)
  ]);

  availableItems: any[];
  requestedItems: any[];
  borrowedItems: any[];

  currentDate: Date;
  maxDate: Date;
  maxDays: number;

  selectedItemId: number | null;
  errorRes: boolean = false;
  successRes: boolean = false;

  selectedPhysical: PhysicalBook;

  // in enviroment you can Change and add new Prioritys
  priorities: string[] = Object.assign([], environment.priority);

  constructor(
    private borrowService: BorrowService,
    private loginComp: LoginComponent,
    private modalService: NgbModal,
    private fb: FormBuilder,
  ) { }

  ngOnInit() {
    this.priorities.forEach((p, i) => this.priorities[i] = p.substring(2));
    this.getItems();
    this.getConfig();
    this.selectedItemId = null;
    this.numberOfReservations = 0;//this.borrowService.; //hier weiter machen


    this.resFormGroup = this.fb.group({

      purpose: '',
      priority: '',
      requiredTime: '',
    });
  }

  getConfig() {
    this.borrowService.getConfig((config: Config) => this.maxDays = config.MAX_DAYS);
  }

  getItems(): void {
    // reset current items
    this.availableItems = [];
    this.requestedItems = [];
    this.borrowedItems = [];

    // fetch items from rest API
    let items;
    this.borrowService.get(this.id).subscribe(
      data => {
        items = data;
        items.forEach((item) => {
          item.borrowings.forEach((borrowing) => borrowing.dueDate = new Date(borrowing.dueDate));
        });
        //sort items
        this.classifyByStatus(items);
        this.sortById(this.availableItems);
        this.sortById(this.requestedItems);
        this.sortByAvailability();

      }
    );
  }

  classifyByStatus(items) {
    items.forEach(item => {
      switch (true) {
        case this.containsStatus(item, BorrowingStatus.BORROWED):
          this.borrowedItems.push(item);
          break;

        case this.containsStatus(item, BorrowingStatus.REQUESTED):
          this.requestedItems.push(item);
          break;

        default:
          this.availableItems.push(item);
      }
    });
  }

  private containsStatus(item, status) {
    return item.borrowings.some((borrowing) => borrowing.status === status);
  }

  sortById(items) {
    items.sort((itemA, itemB) => itemA.physical.id - itemB.physical.id);
  }

  sortByAvailability() {
    // for each item bring borrowed entity to top of array (array only contains only one borrowed element)
    this.borrowedItems.forEach(item => {
      item.borrowings.sort((borrowingA, borrowingB) => {
        if (borrowingA.status === BorrowingStatus.BORROWED) {
          return -1;
        } else {
          return 1;
        }
      });
    });
    // sort all borrowed items by dueDate; comparison of top elements in array works because of above code snippet
    this.borrowedItems.sort((itemA, itemB) => itemA.borrowings[0].dueDate.getTime() - itemB.borrowings[0].dueDate.getTime());
  }

  isItemRequested(item) {
    if (!this.user || this.user.role !== Role.ROLE_STUDENT) {
      return false;
    }
    return item.borrowings.some((borrowing) =>
      borrowing.borrower.id == this.user.id && borrowing.status === BorrowingStatus.REQUESTED);
  }

  isItemBorrowed(item) {
    if (!this.user) {
      return false;
    }
    return item.borrowings.some((borrowing) =>
      borrowing.borrower.id == this.user.id && borrowing.status === BorrowingStatus.BORROWED);
  }

  startBorrowProcess(id: number) {
    // set current date + max date
    this.currentDate = new Date();
    this.maxDate = new Date();
    this.maxDate = new Date(this.maxDate.setDate(this.currentDate.getDate() + this.maxDays))

    // reset server response variables
    this.errorRes = false;
    this.successRes = false;

    // set selected item id
    this.selectedItemId = id;
  }

  borrow() {
    if (this.datePickerForm.invalid)
      return;

    let date = this.datePickerForm.value;
    this.datePickerForm.reset();

    this.borrowService.borrow(date, this.selectedItemId).subscribe(
      (response) => {
        if (response["status"] === 500) {
          this.errorRes = true;
        } else {
          this.successRes = true;
        }
      },
      (err) => {
        console.error(err);
        this.errorRes = true;
      },
      () => {
        this.selectedItemId = null;
        // update view by retrieving updated data about physicals
        this.getItems();
      }
    );
  }

  login() {
    this.loginComp.login();

    // avoid redirect by href
    return false;
  }

  // for the PopupWindow with Purpose and Priority 
  open(content) {
    this.modalService.open(content, { ariaLabelledBy: 'modal-basic-title' }).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });


  }
  // Gets the Purpose and Priority for the new Reservation and adds them to the reservation
  newReservation() {
    let reservation: Reservation = new Reservation();
    // converts the priority name to the corresponding number
    reservation.priority = Number.parseInt(environment.priority.find(p => p.substring(2) == this.resFormGroup.value.priority).substring(0, 1));
    reservation.physicalID = this.selectedPhysical.id;
    reservation.requiredTime = this.resFormGroup.value.requiredTime;
    reservation.purpose = this.resFormGroup.value.purpose;
    reservation.user = this.user;
    reservation.logicalID = this.selectedPhysical.mediumId;
    // checks if there is a Purpose and limits the number of Days for reservation
    if (reservation.purpose == "") {
      alert("Pls give a purpose and set a numbers of days ");
    }
    else if (reservation.requiredTime > this.maxDays || reservation.requiredTime <= 0) {
      alert("Numbers of days musst be bettween 1-" + this.maxDays);
    } else {
      this.borrowService.saveReservation(reservation).subscribe(r => {
        alert("Reservation successful!");
      },
        error => { alert("Error"); });
    }
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }
}
