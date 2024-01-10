import { Component, OnInit, QueryList, ViewChildren, ViewChild, ElementRef } from '@angular/core';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { LoginService } from '../login.service';
import { User } from '../user';
import { Role } from '../role';
import { WishlistService } from '../wishlist.service';
import { WishlistItem } from '../wishlist-item';
import { WishlistItemComponent } from '../wishlist-item/wishlist-item.component';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-wishlist',
  templateUrl: './wishlist.component.html',
  styleUrls: ['./wishlist.component.scss']
})
export class WishlistComponent implements OnInit {

  @ViewChildren('wishItem') itemComponents: QueryList<WishlistItemComponent>

  user: User;
  role: typeof Role = Role;

  wishlist: WishlistItem[] = null;
  wishlistFiltered: WishlistItem[] = null;
  selectedIds: number[] = [];
  allItemsSelected: boolean = false;
  allItemsUnselected: boolean = true;

  dataReceived: boolean = true;
  exportSuccess: boolean = false;
  exportError: boolean = false;
  exportPdfSuccess: boolean = false;
  exportPdfError: boolean = false;
  itemSelectedForExport: boolean = false;

  sortOrder: number = -1;
  sortType: string = "";

  constructor(
    private auth: LoginService,
    private wishlistService: WishlistService
  ) { }

  ngOnInit() {
    this.auth.getUser(
      user => {
        this.user = user
      }
    );
    this.getWishlist();
  }

  // fetch wishlist data
  getWishlist(): void {
    this.wishlistService.getWishlist().subscribe(
      data => {
        this.wishlist = data;
        this.wishlistFiltered = this.wishlist;
        this.dataReceived = true;
      },
      err => {
        console.error(err);
        this.wishlist = null;
        this.dataReceived = false;
      }
    );
  }

  addWishToWishlist() {
    this.getWishlist();
  }

  // send wishlist to server to update content of wishlist item, add new item or change priorities
  updateWishlist(previousIdx?: number, currentIdx?: number): void {
    // check for unsubmitted changes + submit them
    let changedIds = [];
    this.itemComponents.forEach((child) => {
      if (!child.isFormPristine() && child.isFormValid()) {
        child.setItemValue();
        changedIds.push(child.item.id);
      }
    });

    this.wishlistService.updateWishlist(this.wishlist).subscribe(
      () => {
        this.getWishlist();
      },
      err => {
        console.error(err);

        //revert changes if update forced by drag and drop event
        if (previousIdx !== undefined && currentIdx !== undefined) {
          moveItemInArray(this.wishlist, currentIdx, previousIdx);
          this.itemComponents.forEach((child) => {
            // send error status to non drag and dropped components
            if (child.index === currentIdx || child.index === previousIdx) {
              child.setUpdateError();
            }
          });
        }
        // send error status to non updated child components
        this.itemComponents.forEach((child) => {
          if (changedIds.includes(child.item.id)) {
            child.setUpdateError();
          }
        });
      }
    );
  }

  // delete items from wishlist by given array of ids
  deleteItems(ids: number[]) {
    // bundle all delete requests + make multiple delete requests via forkjoin
    let deleteRequests = [];
    ids.forEach((id) => deleteRequests.push(this.wishlistService.deleteWish(id)));

    forkJoin(deleteRequests).subscribe(
      () => {
        // remove items from selectedIds array + update allItemsSelected/ allItemsUnselected
        ids.forEach((id) => this.toggleSelection({
          id: id,
          val: false
        }));
        // refetch wishlist data
        this.getWishlist();
      },
      err => {
        console.error(err);
        // send error status to non deleted child components
        this.itemComponents.forEach((child) => {
          if (ids.includes(child.item.id)) {
            child.setDeleteError();
          }
        });
      }
    );
  }

  // delete all selected items
  deleteSelectedItems() {
    this.deleteItems(this.selectedIds);
  }

  // delete a single item
  deleteItem(id: number) {
    this.deleteItems([id]); // convert id to array to fit deleteItems()
  }

  //export all Selected Item for the E-Mail export
  exportSelectedItems() {
    this.exportSuccess = false;
    this.exportError = false;

    const selectedItems = this.wishlist.filter((item) => this.isItemSelected(item.id));
    this.wishlistService.exportWishList(selectedItems).subscribe(
      res => {
        this.exportSuccess = true;
        if (res['status'] !== 200) {
          this.exportError = true;
        }
      },
      err => {
        this.exportError = true;
        console.error(err)
      }
    );
  }

  /**
   * For cereate export Pdf(uses wishlistService) and open a new Window with the new Created PDF
   */
  exportPdf() {
    const itemsExport = this.wishlist.filter((item) => this.isItemSelected(item.id));

    this.wishlistService.exportWishListPdf(itemsExport).subscribe(
      (data: ArrayBuffer) => {
        let blob = new Blob([data], { type: 'application/pdf' });
        let url = window.URL.createObjectURL(blob);
        window.open(url);

      }
    );

  }

  // sorting not neccessary as server data is already sorted
  sortByRanking(): void {
    this.wishlist.sort((itemA, itemB) => itemA.rank - itemB.rank);
  }

  // add or remove item id from selectedIds array 
  toggleSelection(eventData: object) {
    const val = eventData['value'];
    const id = eventData['id'];

    if (val) { // val === true -> set selected
      if (!this.isItemSelected(id)) {
        this.selectedIds.push(id);
      }
    } else { // val === false -> set unselected
      this.selectedIds = this.selectedIds.filter((elem) => elem !== id);
    }

    // check if all items (un-)selected
    this.allItemsSelected = this.wishlist.length === this.selectedIds.length;
    this.allItemsUnselected = this.selectedIds.length === 0;
  }

  selectAll() {
    this.itemComponents.forEach((child) => child.setCheckboxValue(true));
  }

  unselectAll() {
    this.itemComponents.forEach((child) => child.setCheckboxValue(false));
  }

  isItemSelected(id: number) {
    return this.selectedIds.includes(id);
  }

  drop(event: CdkDragDrop<string[]>): void {
    // stop if position remains identical
    if (event.previousIndex === event.currentIndex)
      return;

    // change order + update wishlist
    moveItemInArray(this.wishlist, event.previousIndex, event.currentIndex);
    this.updateWishlist(event.previousIndex, event.currentIndex);
  }

  sortWishes(type: string, order?: number) {
    if (order != null) {
      this.sortOrder = order;
    } else if (this.sortType == type) {
      this.sortOrder *= -1;
    } else {
      this.sortOrder = 1;
    }
    this.sortType = type;
    this.wishlistFiltered.sort((a, b) => {
      switch (type) {
        case "title":
          return (a.title < b.title ? -1 : 1) * this.sortOrder;
        case "booktitle":
          if (a.booktitle == null) {
            return -1 * this.sortOrder;
          } else if (b.booktitle != null) {
            return (a.booktitle < b.booktitle ? -1 : 1) * this.sortOrder;
          } else {
            return -1;
          }
        case "priority":
          return (a.priority < b.priority ? -1 : 1) * this.sortOrder;
        case "date":
          return (a.date < b.date ? -1 : 1) * this.sortOrder;
        case "counter":
          return (a.counter < b.counter ? -1 : 1) * this.sortOrder;
        default:
          break;
      }
    });
  }

  searchWishes() {
    let searchTerm = (<HTMLInputElement>document.getElementById("wishSearch")).value.toLowerCase();
    this.wishlistFiltered = this.wishlist.filter((w) => {
      let title;
      w.title != null ? title = w.title.toLowerCase() : title = "";
      let booktitle = w.booktitle.toLowerCase();
      let counter = w.counter.toString();
      let priority = w.priority.toLowerCase();
      let date = w.date.toLowerCase();

      return title.includes(searchTerm) || booktitle.includes(searchTerm) || counter.includes(searchTerm) || priority.includes(searchTerm) || date.includes(searchTerm);
    })

    // restore order
    if (this.sortType != "") {
      this.sortWishes(this.sortType, this.sortOrder);
    }
  }
}
