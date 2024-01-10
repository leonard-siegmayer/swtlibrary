import { Component, OnInit } from '@angular/core';
import { LoginService } from '../login.service';
import { BorrowService } from '../borrow.service';
import { ResponseBorrowingEnititiesExtended } from '../response-borrowing-entities-extended';
import { ResponsePhysicalsExtended } from '../response-physicals-extended';
import { BooksService } from '../books.service';
import { Role } from '../role';
import { Config } from '../config';
import { BorrowingStatus } from '../borrowing-status';
import { User } from '../user';
import { BorrowingEntity } from '../borrowing-entity';
import { Book } from '../book';
import { Reservation } from '../reservation';
import { environment } from 'src/environments/environment';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalUpdateRankComponent } from '../modal-update-rank/modal-update-rank.component';
import { PhysicalBook } from '../physicalBook';
import { PhysicalStatus } from '../physical-status';



@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  username: string;
  returnPrivilege = false;
  borrowedMedia: ResponseBorrowingEnititiesExtended[] = [];
  expiredMedia: ResponseBorrowingEnititiesExtended[] = [];
  requestedMedia: ResponseBorrowingEnititiesExtended[] = [];
  ownMedia: ResponsePhysicalsExtended[] = null;
  reservations: Reservation[] = [];

  borrowedMediaFiltered: ResponseBorrowingEnititiesExtended[] = [];
  expiredMediaFiltered: ResponseBorrowingEnititiesExtended[] = [];
  requestedMediaFiltered: ResponseBorrowingEnititiesExtended[] = [];
  ownMediaFiltered: ResponsePhysicalsExtended[] = null;
  reservationsFiltered: Reservation[] = [];

  ownMediaSortOrder: number = -1;
  borrowingOrder: number = -1;
  requestedOrder: number = -1;
  expiredOrder: number = -1;
  reservationsOrder: number = -1;
  currentOwnBooksOrder: string = "title";
  currentBorrowingOrder: string = "title";
  currentExpiredOrder: string = "title";
  currentRequestedOrder: string = "title";
  currentReservationsOrder: string = "readyToBorrow";

  hideAvailable: boolean;
  hideBorrowed: boolean;

  maxBorrowingDays: number;
  maxExtensionDays: number;

  user: User;
  historyTitle: String;
  reservationTitle: String;
  dtOptions: DataTables.Settings = {};
  table: DataTables.Api;
  waitingTable: DataTables.Api;
  availableRanks: number = 0;
  displayedWaitingList: Reservation[] = [];

  constructor(private modalService: NgbModal, private loginService: LoginService, private borrowService: BorrowService, private bookService: BooksService) {

  }

  ngOnInit() {
    this.loginService.getUser(
      user => {
        if (user === null) {
          return;
        }
        this.user = user;
        this.buildPage();
      }
    );

    this.hideAvailable = false;
    this.hideBorrowed = false;

    // initialize the history table
    this.table = $('#historyTable').DataTable({
      "scrollX": true,
      scrollY: '50vh',
      scrollCollapse: true
    });

    // initialize the waitingListTable table
    this.waitingTable = $('#waitingListTable').DataTable({
      "scrollX": true,
      scrollY: '50vh',
      scrollCollapse: true,
      ordering: false,
      // clicking a row will open a modal to edit the reservation
      rowCallback: (row: Node, data: any[] | Object, index: number) => {
        $("#waitingListTable tbody tr td:nth-child(1)").css("color", "rgb(17, 65, 170)").css("cursor", "pointer");
        $($('td', row)[0]).off("click");
        $($('td', row)[0]).on("click", event => {
          if (data[1] == "Ready") {
            return;
          }
          const modalRef = this.modalService.open(ModalUpdateRankComponent);

          (data[1] == "/") ? modalRef.componentInstance.ranks = this.availableRanks : modalRef.componentInstance.ranks = this.availableRanks - 1;
          modalRef.componentInstance.selectedRank.subscribe(rank => {
            if (rank) {
              const reservationId = data[0];
              const reservation = this.displayedWaitingList.find(r => r.id == reservationId);
              if (rank == 0 && !reservation.rank) { return; }
              this.borrowService.updateReservationRank(reservationId, rank).subscribe(res => {
                this.hideWaitingList();
                this.showWaitingList(reservation.physicalID);
              });
            }
            modalRef.close();
          })
        })
        return row;
      }
    });
  }

  //only continue if user is present
  buildPage() {
    this.username = this.user.name;

    if (this.user.role === Role.ROLE_ADMIN || this.user.role === Role.ROLE_STAFF) {
      this.returnPrivilege = true;
    }

    this.borrowService.getBorrowedMediaByUserID(this.user.id).subscribe(
      (data: ResponseBorrowingEnititiesExtended[]) => {
        if (data.length > 0) {
          // sort data into either expired/borrowed or requested
          data.forEach(borrowing => {
            switch (borrowing.borrowingEntity.status) {
              case BorrowingStatus.BORROWED:
                if (this.getDueDate(borrowing.borrowingEntity.dueDate) < this.getToday()) {
                  this.expiredMedia.push(borrowing);
                } else {
                  this.borrowedMedia.push(borrowing);
                }
                break;
              case BorrowingStatus.REQUESTED:
                this.requestedMedia.push(borrowing);
                break;
              default:
                break;
            }
          });
        }
        if (this.expiredMedia.length == 0) {
          this.expiredMedia = null;
        }
        if (this.borrowedMedia.length == 0) {
          this.borrowedMedia = null;
        }
        if (this.requestedMedia.length == 0) {
          this.requestedMedia = null;
        }

        this.expiredMediaFiltered = this.expiredMedia;
        this.borrowedMediaFiltered = this.borrowedMedia;
        this.requestedMediaFiltered = this.requestedMedia;
      });

    // fetch info for each media individually and append to results
    this.borrowService.getOwnMediaByUserID(this.user.id).subscribe(
      (data: ResponsePhysicalsExtended[]) => {
        if (data.length > 0) {
          this.ownMedia = data;
          this.ownMediaFiltered = this.ownMedia;
        }
        for (let i = 0; i < data.length; i++) {
          this.bookService.getBook(data[i].physical.mediumId).subscribe(
            (book) => {
              this.ownMedia[i]["media"] = book;
            },
          );
        }
      });

    this.borrowService.getReservationsByUser(this.user.id).subscribe(
      async (data: Reservation[]) => {
        this.reservations = data;
        for (let r of this.reservations) {
          r.logicalBook = await this.bookService.getBook(r.logicalID).toPromise();
        }
        if (this.reservations.length == 0) {
          this.reservations = null;
        }
        this.reservationsFiltered = this.reservations;
        if (this.reservations != null) this.sortReservationsBy(this.currentReservationsOrder);
      }
    )
    this.getMaxBorrowingDays();
  }

  returnBorrow(id: number) {
    this.borrowService.returnBorrowStaff(id).subscribe(
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

  extendBorrow(id: number) {
    let date: string = document.forms["form" + id.toString()].elements["date"].value;
    let validDate: RegExp = /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/

    if (!validDate.test(date)) {
      document.forms["form" + id.toString()].elements["date"].classList.add("is-invalid");
      return;
    } else {
      document.forms["form" + id.toString()].elements["date"].classList.remove("is-invalid");
    }

    this.borrowService.extendBorrowStaff(id, date).subscribe(
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

  getMaxBorrowingDays() {
    let borrowDate = new Date();
    this.borrowService.getConfig((config: Config) => {
      this.maxBorrowingDays = config.MAX_DAYS;
      this.maxExtensionDays = config.EXT_DAYS;
    });
  }

  getMaxExtensionDate(borrowDate: string) {
    let date = new Date(borrowDate);
    return date.setDate(date.getDate() + this.maxBorrowingDays + this.maxExtensionDays)
  }

  getDueDate(dueDate: string) {
    return new Date(dueDate);
  }

  getToday() {
    return new Date();
  }

  showHistory(id: number) {
    this.hideHistory();
    var mediaId = this.ownMedia.find((m) => { return m.physical.id == id }).physical.mediumId;
    this.bookService.getBook(mediaId).subscribe((book) => {
      this.historyTitle = book.booktitle;
      // cutting the title if it has too many characters to display
      if (this.historyTitle.length > 50) {
        this.historyTitle = this.historyTitle.slice(0, 50) + "...";
      }
    });
    this.table.column(1).visible(false);

    this.addBorrowingsToHistory(id);
  }

  showFullHistory() {
    this.hideHistory();
    $("#borrowHistory h3").empty();
    this.table.column(1).visible(true);
    this.ownMedia.forEach((media) => {
      var id = media.physical.id;
      this.bookService.getBook(media.physical.mediumId).subscribe((book) => {
        this.addBorrowingsToHistory(id, book.booktitle);
      });
    });
  }

  addBorrowingsToHistory(id: number, title?: string) {
    this.borrowService.getBorrowingsFromPhysical(id).subscribe(
      (data: BorrowingEntity[]) => {
        document.getElementById("borrowHistory").style.visibility = "visible";
        if (title == null) {
          title = "";
        } else if (title.length > 55) {
          // cutting the title if it has too many characters to display
          title = title.slice(0, 55) + "...";
        }
        // add the data to the table
        data.forEach((d) => {
          this.table.row.add([
            d.id, title, d.borrower.name, d.borrower.email, d.borrowDate, d.dueDate, d.returnDate
          ]).draw();
        });
        this.table.columns.adjust().draw();
      },
      error => {
        document.getElementById("failReturn" + id).style.display = 'block';
        document.getElementById("request" + id).style.display = 'none';
      }
    );
  }

  hideHistory() {
    document.getElementById("borrowHistory").style.visibility = "hidden";
    this.table.clear().draw();
  }


  /**
   * Sorts the 'ownMediaFiltered' array by the given field
   * @param type the field determining the order
   */
  sortOwnMediaBy(type: string) {
    // changes the order direction
    this.ownMediaSortOrder *= -1;
    this.currentOwnBooksOrder = type;

    this.ownMediaFiltered.sort((a, b) => {
      switch (type) {
        case "title":
          let aTitle = a.media.booktitle.toLowerCase();
          let bTitle = b.media.booktitle.toLowerCase();
          return (aTitle < bTitle ? -1 : 1) * this.ownMediaSortOrder;
        case "borrower":
          if (a.borrowings.length == 0) {
            // a isn't borrowed
            return -1 * this.ownMediaSortOrder;
          } else if (b.borrowings.length != 0) {
            // a and b are borrowed
            return (a.borrowings[0].borrower.name.toLowerCase() < b.borrowings[0].borrower.name.toLowerCase() ? -1 : 1) * this.ownMediaSortOrder;
          } else {
            // neither a nor b is borrowed
            return -1;
          }
        case "location":
          let aLocation = a.physical.location.toLowerCase();
          let bLocation = b.physical.location.toLowerCase();
          return (aLocation < bLocation ? -1 : 1) * this.ownMediaSortOrder;
        case "status":
          return (a.borrowings.length < b.borrowings.length ? -1 : 1) * this.ownMediaSortOrder;
        case "dueDate":
          if (a.borrowings.length == 0) {
            return -1 * this.ownMediaSortOrder;
          } else if (b.borrowings.length != 0) {
            return (a.borrowings[0].dueDate < b.borrowings[0].dueDate ? -1 : 1) * this.ownMediaSortOrder;
          } else {
            return -1;
          }
        default:
          return -1;
      }
    })
  }

  /**
   * Sorts the 'borrowedMediaFilterted' array by the given field
   * @param type the field determining the order
   */
  sortBorrowingsBy(type: string) {
    this.currentBorrowingOrder = type;
    this.borrowingOrder *= -1;
    this.sort(type, this.borrowedMediaFiltered, this.borrowingOrder);
  }

  /**
   * Sorts the 'requestedMediaFiltered' array by the given field
   * @param type the field determining the order
   */
  sortRequestedBy(type: string) {
    this.currentRequestedOrder = type;
    this.requestedOrder *= -1;
    this.sort(type, this.requestedMediaFiltered, this.requestedOrder);
  }

  /**
  * Sorts the 'expiredMediaFiltered' array by the given field
  * @param type the field determining the order
  */
  sortExpiredBy(type: string) {
    this.currentExpiredOrder = type;
    this.expiredOrder *= -1;
    this.sort(type, this.expiredMediaFiltered, this.expiredOrder);
  }

  /**
  * Sorts the 'reservationsFiltered' array by the given field
  * @param type the field determining the order
  */
  sortReservationsBy(type: string) {
    this.currentReservationsOrder = type;
    this.reservationsOrder *= -1;
    this.sortReservations(type, this.reservationsFiltered, this.reservationsOrder);
  }

  /**
   * A helper function for sorting arrays of type 'ResponseBorrowingEntitiesExtended'. 
   * Can't be used for function sortOwnMediaBy()
   * @param type the field determining the order. Can be 'title', 'dueDate' and 'borrowDate'
   * @param toSort the array to be sorted
   * @param sortOrder 1 for ascending, -1 for decending order
   */
  private sort(type: string, toSort: ResponseBorrowingEnititiesExtended[], sortOrder: number) {
    toSort.sort((a, b) => {
      switch (type) {
        case "title":
          let aTitle = a.medium.booktitle.toLowerCase();
          let bTitle = b.medium.booktitle.toLowerCase();
          return (aTitle < bTitle ? -1 : 1) * sortOrder;
        case "dueDate":
          return (a.borrowingEntity.dueDate < b.borrowingEntity.dueDate ? -1 : 1) * sortOrder;
        case "borrowDate":
          return (a.borrowingEntity.borrowDate < b.borrowingEntity.borrowDate ? -1 : 1) * sortOrder;
        default:
          return -1;
      }
    })
  }

  /**
   * A helper function for sorting arrays of type 'Reservation[]'.
   * @param type the field determining the order. Can be 'priority', 'readyToBorrow' and 'date'
   * @param toSort the array to be sorted
   * @param sortOrder 1 for ascending, -1 for decending order
   */
  private async sortReservations(type: string, toSort: Reservation[], sortOrder: number) {
    toSort.sort((a, b) => {
      switch (type) {
        case "date":
          return (a.date < b.date ? -1 : 1) * sortOrder;
        case "priority":
          return (a.priority > b.priority ? -1 : 1) * sortOrder;
        case "readyToBorrow":
          return ((a.readyToBorrow || !b.readyToBorrow) ? -1 : 1) * sortOrder;
        default:
          return -1;
      }
    })
  }

  searchOwnMedia() {
    let searchTerm = (<HTMLInputElement>document.getElementById("ownMediaSearch")).value.toLowerCase();
    this.ownMediaFiltered = this.ownMedia.filter((m) => {
      // search in dueDate
      if (m.borrowings.length > 0) {
        if (m.borrowings[0].dueDate.includes(searchTerm)) {
          return true;
        }
      }
      // searchTerm is included in booktitle, ...
      return (m.media.booktitle.toLowerCase().includes(searchTerm) ||
        // ... location, ...
        m.physical.location.toLowerCase().includes(searchTerm) ||
        // ... a borrowers name ...
        m.borrowings.find(b => b.borrower.name.toLowerCase().includes(searchTerm)) ||
        // ... or an authors name
        m.media.authors.find(a => a.name.toLowerCase().includes(searchTerm)) != null);
    });

    // restore order
    this.ownMediaSortOrder *= -1;
    this.sortOwnMediaBy(this.currentOwnBooksOrder);
  }

  searchBorrowings() {
    let searchTerm = (<HTMLInputElement>document.getElementById("borrowingSearch")).value.toLowerCase();
    this.borrowedMediaFiltered = this.borrowedMedia.filter(m => this.filter(m, searchTerm));

    // restore order
    this.borrowingOrder *= -1;
    this.sortBorrowingsBy(this.currentBorrowingOrder);
  }

  searchExpired() {
    let searchTerm = (<HTMLInputElement>document.getElementById("expiredSearch")).value.toLowerCase();
    this.expiredMediaFiltered = this.expiredMedia.filter(m => this.filter(m, searchTerm));

    // restore order
    this.expiredOrder *= -1;
    this.sortExpiredBy(this.currentExpiredOrder);
  }

  searchRequested() {
    let searchTerm = (<HTMLInputElement>document.getElementById("requestedSearch")).value.toLowerCase();
    this.requestedMediaFiltered = this.requestedMedia.filter(m => this.filter(m, searchTerm));

    // restore order
    this.requestedOrder *= -1;
    this.sortRequestedBy(this.currentRequestedOrder);
  }

  searchReservations() {
    let searchTerm = (<HTMLInputElement>document.getElementById("reservationSearch")).value.toLowerCase();
    this.reservationsFiltered = this.reservations.filter(r => {
      let priorityName = environment.priority.find(p => p[0] == r.priority.toString()).substring(2);
      return (r.logicalBook.booktitle.toLowerCase().includes(searchTerm) ||
        r.logicalBook.authors.find(a => a.name.toLowerCase().includes(searchTerm)) != null ||
        priorityName.toLowerCase().includes(searchTerm) ||
        r.purpose.toLowerCase().includes(searchTerm.toLowerCase()));
    })
    // restore order
    this.reservationsOrder *= -1;
    this.sortReservationsBy(this.currentReservationsOrder);
  }

  private filter(m: ResponseBorrowingEnititiesExtended, searchTerm: string): boolean {
    // searchTerm is included in booktitle, ...
    return (m.medium.booktitle.toLowerCase().includes(searchTerm) ||
      // ... dueDate, ...
      m.borrowingEntity.dueDate.includes(searchTerm) ||
      // m.borrowingEntity.borrowDate.includes(searchTerm) ||
      // ... or an authors name
      m.medium.authors.find(a => a.name.toLowerCase().includes(searchTerm)) != null);
  }

  /**
   * displays the waiting list of a given physical
   * @param id the id of the physcial
   */
  showWaitingList(id: number) {
    this.availableRanks = 0;
    this.hideWaitingList();
    var book: Book = this.ownMedia.find(m => m.physical.id == id).media;
    this.reservationTitle = book.booktitle;
    // cutting the title if it has too many characters to display
    if (this.reservationTitle.length > 50) {
      this.reservationTitle = this.reservationTitle.slice(0, 50) + "...";
    }

    this.addReservationsToWaitingList(id);
    this.waitingTable.column(0).visible(false);
  }

  /**
   * Clears the data in the waiting list and hides it
   */
  hideWaitingList() {
    document.getElementById("waitingList").style.visibility = "hidden";
    this.displayedWaitingList = [];
    this.waitingTable.clear().draw();
  }

  /**
   * Adds all the reservation of a given physical to the reservation table.
   * @param id The id of the phyiscal
   */
  addReservationsToWaitingList(id: number) {
    this.borrowService.getReservationsByPhysicalID(id).subscribe((data: Reservation[]) => {
      this.displayedWaitingList = data;
      // set the waiting list to visible
      document.getElementById("waitingList").style.visibility = "visible";
      // sorting the data. Highest priority: rank; Second priority: Priority; Lowest priority: Date
      data.sort((a, b) => {
        if (a.readyToBorrow) {
          return -1;
        } else if (b.readyToBorrow) {
          return 1;
        } else if (a.rank != null && b.rank != null) {
          return (a.rank <= b.rank) ? -1 : 1;
        } else if (a.rank != null) {
          return -1;
        } else if (b.rank != null) {
          return 1;
        } else if (b.priority != a.priority) {
          return (a.priority <= b.priority) ? -1 : 1;
        } else {
          return (a.date <= b.date) ? -1 : 1;
        }
      })

      // add the data to the waiting list table
      data.forEach(d => {
        let rank: string = (d.rank == null || d.rank == "") ? "/" : d.rank;
        if (rank != "/") {
          this.availableRanks++;
        }
        if (d.readyToBorrow) rank = "Ready";
        this.waitingTable.row.add([
          d.id, rank, d.user.name, this.getPriorityNameByNumber(d.priority), d.date, d.purpose, d.requiredTime
        ]).draw();
      })

      this.waitingTable.columns.adjust().draw();
    },
      error => {
        document.getElementById("failReturn" + id).style.display = 'block';
        document.getElementById("request" + id).style.display = 'none';
      });
  }

  cancelReservation(id: number) {
    this.borrowService.deleteReservation(id).subscribe(_ => {
      this.removeReservation(id);
    });
  }

  formatPriority(prio: number): string {
    return environment.priority.find(p => p[0] == prio.toString()).substring(2);
  }

  redeemBorrowing(id: number) {
    this.borrowService.redeemReservation(id).subscribe(r => {
      this.removeReservation(id);
      alert("Book successfully borrowed!");
    });
    error => alert("An error occured.");
  }

  private removeReservation(id) {
    this.reservations.find((r, i) => {
      if (r.id == id) {
        this.reservations.splice(i, 1);
        if (this.reservations.length == 0) {
          this.reservations == null;
        }
        return true;
      }
    })
    this.reservationsFiltered.find((r, i) => {
      if (r.id == id) {
        this.reservations.splice(i, 1);
        return true;
      }
    })
  }

  getPriorityNameByNumber(number: number): string {
    return environment.priority.find(p => p[0] == number.toString()).substring(2);
  }

  /**
   * toggles the value for whether a physical can be borrowed or is unavailable
   * @param extendedPhysical the extended Physical
   * @param event the event including the value of the checkbox 
   */
  changeAllowToBorrow(extendedPhysical: ResponsePhysicalsExtended, event) {
    const physical: PhysicalBook = extendedPhysical.physical;
    const borrowed: boolean = extendedPhysical.borrowings.length > 0;
    const allow = event.target.checked;
    const oldStatus = physical.status;
    this.borrowService.getReservationsByPhysicalID(physical.id).subscribe(r => {
      let newStatus;

      if (allow) {
        if (borrowed) {
          newStatus = PhysicalStatus.BORROWED;
        } else if (r.length > 0) {
          newStatus = PhysicalStatus.RESERVED;
        } else {
          newStatus = PhysicalStatus.AVAILABLE;
        }
      } else {
        newStatus = PhysicalStatus.UNAVAILABLE;
      }
      physical.status = newStatus;
      this.borrowService.updatePhysical(physical).subscribe(res => {
        if (res.status == 500) physical.status = oldStatus;
      })
    });
  }
}
