import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { WishlistItem } from '../wishlist-item';
import { FormGroup, Validators, FormBuilder, FormControl, FormArray, Form } from '@angular/forms';
import { WishlistService } from '../wishlist.service';

import { environment } from 'src/environments/environment';



export interface Prio {
  value: String;
}

@Component({
  selector: 'app-wishlist-item',
  templateUrl: './wishlist-item.component.html',
  styleUrls: ['./wishlist-item.component.scss']
})
export class WishlistItemComponent implements OnInit {



  @Input() index: number;
  @Input() item: WishlistItem;
  @Input() selected: boolean;
  @Output() addEvent: EventEmitter<any> = new EventEmitter();
  @Output() deleteEvent: EventEmitter<any> = new EventEmitter();
  @Output() updateEvent: EventEmitter<any> = new EventEmitter();
  @Output() selectEvent: EventEmitter<any> = new EventEmitter();
  @Output() newEvent: EventEmitter<any> = new EventEmitter();

  requestError: boolean = false;
  addError: boolean = false;
  updateError: boolean = false;
  deleteError: boolean = false;

  currentDate: string;
  saved: boolean;

  checkBoxCtrl: FormControl;
  itemFormGroup: FormGroup;

  // in enviroment you can Change and add new Prioritys 
  priorities = Object.assign([], environment.priority);

  //used to iteriate over all Wishlist 
  allWishes: WishlistItem[];

  constructor(

    private fb: FormBuilder,
    private wishlistService: WishlistService
  ) { }

  ngOnInit() {
    this.priorities.forEach((p, i) => this.priorities[i] = p.substring(2));
    // check if component is used to create new item or handle existing item

    if (this.item) {
      this.itemFormGroup = this.fb.group({
        title: [this.item.title, Validators.required],
        url: this.item.url,
        note: this.item.note,
        isbn: this.item.isbn,
        author: this.item.author,
        year: this.item.year,
        edition: this.item.edition,
        publisher: this.item.publisher,
        priority: this.item.priority,
        booktitle: this.item.booktitle,
        counter: this.item.counter
      });
      this.saved = true;
      this.currentDate = this.item.date;
      this.checkBoxCtrl = new FormControl(this.selected);
    } else {
      this.itemFormGroup = this.fb.group({
        title: ['', Validators.required],
        url: '',
        note: '',
        isbn: '',
        author: '',
        year: '',
        edition: '',
        publisher: '',
        priority: '',
        booktitle: '',
        counter: ''
      });
      this.saved = false;
    }
    this.wishlistService.getWishlist().subscribe(
      (wishData) => {
        this.allWishes = wishData;
      }
    )
  }


  addItem() {
    if (this.itemFormGroup.invalid) {
      return;
    }

    let wish: WishlistItem = this.itemFormGroup.value;
    wish.counter = 1;
    if (this.allWishes.find(w => w.isbn == wish.isbn) != null) {
      wish.counter = this.allWishes.find(w => w.isbn == wish.isbn).counter;
    }

    this.wishlistService.addWish(wish).subscribe(
      () => {
        this.requestError = false;
        this.addError = false;
        this.itemFormGroup.reset();
        if (this.saved) {
          this.addEvent.emit(null);
        } else {
          this.newEvent.emit();
        }
        this.allWishes.push(wish);
      },
      err => {
        this.requestError = true;
        this.addError = true;
        console.error(err);
      }
    );
  }

  deleteItem() {
    this.deleteEvent.emit(this.item.id);
  }

  updateItem() {
    if (this.itemFormGroup.invalid)
      return;

    this.updateEvent.emit();
  }

  setUpdateError() {
    this.updateError = true;
    this.requestError = true;
  }

  setDeleteError() {
    this.deleteError = true;
    this.requestError = true;
  }

  setItemValue() {
    this.item.title = this.itemFormGroup.value.title;
    this.item.url = this.itemFormGroup.value.url;
    this.item.note = this.itemFormGroup.value.note;
    this.item.isbn = this.itemFormGroup.value.isbn;
    this.item.publisher = this.itemFormGroup.value.publisher;
    this.item.year = this.itemFormGroup.value.year;
    this.item.edition = this.itemFormGroup.value.edition;
    this.item.priority = this.itemFormGroup.value.priority;
    this.item.author = this.itemFormGroup.value.author;
    this.item.booktitle = this.itemFormGroup.value.booktitle;
    this.item.counter = this.itemFormGroup.value.counter;
  }

  resetForm() {
    if (this.item) {
      this.itemFormGroup.reset(this.item);
    } else {
      this.itemFormGroup.reset();
    }
  }

  isFormPristine() {
    return this.itemFormGroup.pristine;
  }

  isFormValid() {
    return this.itemFormGroup.valid;
  }

  toggleCheckbox() {
    const eventData = {
      id: this.item.id,
      value: this.checkBoxCtrl.value
    };

    this.selectEvent.emit(eventData);
  }

  setCheckboxValue(val: boolean) {
    this.checkBoxCtrl.setValue(val);
    this.toggleCheckbox();
  }


  //request book details from open library via ISBN
  completeBookDetails() {
    var isbn = this.itemFormGroup.value.isbn;
    if ((isbn == "")) {
      alert("You have to insert an ISBN.");
    } else if (isbn.match(/^[0-9,-]+$/) == null) {
      alert("The ISBN can only consist of digits nad separating lines");
    } else {
      this.wishlistService.completeBook(this.itemFormGroup.value.isbn).subscribe(
        (data: WishlistItem) => {
          if (data == null) {
            alert("No book details for this ISBN found.");
            this.initializeBookProfilForm();
          } else {
            this.item = data;
            this.setBookInfoOnPage();
            alert("The book details have been added");
          }
        },
        (err) => {
          alert("No book details for this ISBN found. (error)");
        },
        () => {
        });
    }
  }

  initializeBookProfilForm() {
    this.itemFormGroup = this.fb.group({
      isbn: [''],
      //Basic Details
      title: [''],
      author: [''],
      //publication
      publisher: [''],
      year: [''],
      //further Details
      edition: [''],
      url: ['']
    });
  }

  //sets all information from parameter book on html page
  setBookInfoOnPage() {
    this.itemFormGroup.patchValue({
      author: this.item.author,
      title: this.item.title,
      publisher: this.item.publisher,
      year: this.item.year,
      edition: this.item.edition,
      isbn: this.item.isbn,
      booktitle: this.item.booktitle,
      url: this.item.url
    });

  }
  deleteAuthor(index: number) {
    this.authors.removeAt(index);
  }

  get authors() {
    return this.itemFormGroup.get('author') as FormArray;
  }

  addAuthor(content: string) {
    this.authors.push(this.fb.control(content));
  }

  //Checks if some wishes have the same ISBN
  allWishesIsbn(): boolean {
    let result = false;
    this.allWishes.find((wishData) => {
      if (wishData.isbn == this.itemFormGroup.value.isbn) {
        result = true;
        return true;
      } else {
        return false;
      }
    });
    return result;
  }

  // Counter is the Wishcounter(if someone wishes for a Wish that is already on the Wishlist)
  getCounter(): number {
    if (this.item) {
      return this.item.counter;
    } else {
      return null;
    }
  }
}


