import { Keyword } from './../keyword';
import { JsonResponse } from './../json-response';
import { UserService } from './../user.service';
import { TagsService } from './../tags.service';
import { PhysicalBook } from './../physicalBook';
import { Book } from "../book";
import { User } from "./../user";
import { Tag } from "../tag";
import { Person } from "../person";
import { Role } from '../role';

import { Component, OnInit } from "@angular/core";
import { FormGroup, FormControl, FormBuilder, FormArray } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { ModalUpdateTagComponent } from '../modal-update-tag/modal-update-tag.component';
import { BooksService } from "./../books.service";

import { ModalDeleteTagComponent } from '../modal-delete-tag/modal-delete-tag.component';
import { ModalDeletePhysicalComponent } from '../modal-delete-physical/modal-delete-physical.component';
import { LoginService } from '../login.service';

@Component({
  selector: "app-cud-book-details",
  templateUrl: "./cud-book-details.component.html",
  styleUrls: ["./cud-book-details.component.scss"]
})

export class CUDBookDetailsComponent implements OnInit {

  //global variables
  titel: String;
  bookCreationState: Boolean;
  book: Book = new Book();
  bookProfileForm: FormGroup;
  physicalBookForm: FormGroup;
  tagForm: FormGroup;
  allStaffmembers: User[] = [];
  allTags: Tag[] = [];


  constructor(
    private bookService: BooksService,
    private tagService: TagsService,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private auth: LoginService,
    //for dynamic forms
    private fb: FormBuilder,
    //for modal
    private modalService: NgbModal


  ) { }

  ngOnInit() {
    //Check user authorization
    this.auth.getUser(
      user => {
        if (user == undefined || user.role == Role.ROLE_STUDENT) {
          alert("You have no permession to enter this page.");
          this.router.navigate([""]);
        }
      }
    );



    //initialize physicalBookForm
    this.initializePhysicalBookForm();

    //initilize tagForm
    this.initializeTagForm();

    //initialize bookProfilForm
    this.initializeBookProfilForm();

    //get all Tags 
    this.tagService.getAllTags().subscribe(
      (data: Tag[]) => {
        this.allTags = data;
      },
      (err) => {
        this.allTags = [];
      },
      () => {
      });

    //get all Staff users
    this.userService.getAllUsers().subscribe(
      (data: User[]) => {
        this.allStaffmembers = data;
        this.allStaffmembers.forEach(singleStaff => {
          if (singleStaff.role == Role.ROLE_STUDENT) {
            this.allStaffmembers = this.allStaffmembers.filter(list => list !== singleStaff);
          }
        });
      },
      (err) => {
        this.allStaffmembers = [];
      },
      () => {
      });

    //get id the page was called with
    let id = parseInt(this.route.snapshot.paramMap.get("id"));
    if (id) {
      this.book.id = id;
      this.bookCreationState = false;
      this.titel = "Edit or Delete Book";
      this.getBook();
    } else {
      this.book = new Book();
      this.book.physicals = [];
      this.book.id = null;
      this.bookCreationState = true;
      this.titel = "Create new logical book";
    }
  }

  //open update tag modals
  openUpdateModal(tagName: string) {
    const resetDescription: string = this.allTags.find(x => x.name == tagName).description;
    let tagToUpdate = this.allTags.find(x => x.name == tagName);
    const modalRef = this.modalService.open(ModalUpdateTagComponent);
    modalRef.componentInstance.tag = tagToUpdate;
    modalRef.componentInstance.passEntry.subscribe((receivedEntry) => {
      //data to change
      if ((receivedEntry != undefined)) {
        tagToUpdate.name = receivedEntry.name;
        tagToUpdate.description = receivedEntry.description;
        this.tagService.updateTag(tagToUpdate).subscribe(
          (data: Tag) => {
            if (data.name == tagToUpdate.name) {
              alert("The tag was succesfully updated");
            } else {
              alert(" If The tag was not updated. Please try again");
            }
          },
          (err) => {
            alert("The tag was not updated. Please try again");
          },
          () => {
          });
        modalRef.close();
      } else {
        tagToUpdate.name = tagName;
        tagToUpdate.description = resetDescription;
        modalRef.close();
      }
    })
  }

  //open delete Tag modal
  openDeleteModal(tagName: string) {
    let tagToDelete = this.allTags.find(x => x.name == tagName);
    const modalRef = this.modalService.open(ModalDeleteTagComponent);
    modalRef.componentInstance.tag = tagToDelete;
    modalRef.componentInstance.passEntry.subscribe((receivedEntry) => {
      if (!(receivedEntry == null)) {
        this.allTags.forEach((item, index) => {
          if (item === tagToDelete) {
            this.tagService.deleteTag(tagToDelete.id).subscribe(
              (data: JsonResponse) => {
                if (data.status == 200) {
                  alert("The tag was succesfully deleted");
                  this.allTags.splice(index, 1);
                } else {
                  alert("IF The tag was not deleted. Please try again");
                }
              },
              (err) => {
                alert("The tag was not deleted. Please try again");
              },
              () => {
              });
          }
        });
        if (tagToDelete.id !== undefined) {
          this.tagService.deleteTag(tagToDelete.id);
        }
        modalRef.close();
      } else {
        modalRef.close();
      }

    })
  }

  initializeTagForm() {
    this.tagForm = new FormGroup({
      name: new FormControl(""),
      description: new FormControl("")
    });
  }

  initializePhysicalBookForm() {
    this.physicalBookForm = new FormGroup({
      location: new FormControl(""),
      department: new FormControl(""),
      room: new FormControl(""),
      isInHandapparat: new FormControl(""),
      rvkSignature: new FormControl(""),
      name: new FormControl("")
    });
  }

  initializeBookProfilForm() {
    this.bookProfileForm = this.fb.group({
      isbn: [''],
      //Basic Details
      bookTitle: [''],
      title: [''],
      authors: this.fb.array([
        this.fb.control('')
      ]),
      editors: this.fb.array([
        this.fb.control('')
      ]),
      abstract: [''],
      keywords: this.fb.array([
        this.fb.control('')
      ]),
      //publication
      publisher: [''],
      address: [''],
      year: [''],
      howPublished: [''],
      //further Details
      coverURL: [''],
      language: [''],
      chapter: [''],
      edition: [''],
      number: [''],
      pages: [''],
      institution: [''],
      organization: [''],
      series: [''],
      type: [''],
      //identification
      doi: [''],
      ean: ['']
    });
  }

  //open delete physical
  openDeletePhysicalModal(displayId: number) {
    let physicalToDelete = this.book.physicals.find(x => x.displayId == displayId);
    const modalRef = this.modalService.open(ModalDeletePhysicalComponent);
    modalRef.componentInstance.physical = physicalToDelete;
    modalRef.componentInstance.passEntry.subscribe((receivedEntry) => {
      if (!(receivedEntry == null)) {
        this.book.physicals.forEach((item, index) => {
          if (item === physicalToDelete) this.book.physicals.splice(index, 1);
        });
        modalRef.close();
      } else {
        modalRef.close();
      }

    })
  }

  //analyse, if the tag coresponding to the parameter tagId is part of the actual book
  checkIfTagInBook(tagId: number): boolean {
    if (this.book.tags == undefined) {
      return false;
    } else {
      for (var i = 0; i < this.book.tags.length; i++) {
        if (this.book.tags[i].id == tagId) {
          return true;
        }
      }
      return false;
    }
  }

  //request book details from open library via ISBN
  completeBookDetails() {
    var isbn = this.bookProfileForm.get("isbn").value;
    if ((isbn == "")) {
      alert("You have to insert an ISBN.");
    } else if (isbn.match(/^[0-9,-]+$/) == null) {
      alert("The ISBN can only consist of digits nad separating lines");
    } else {
      this.bookService.completeBook(this.bookProfileForm.get("isbn").value).subscribe(
        (data: Book) => {
          if (data == null) {
            alert("No book details for this ISBN found.");
            this.initializeBookProfilForm();
          } else {
            this.book = data;
            this.setBookInfoOnPage();
            alert("The book details have been added");
          }
        },
        (err) => {
          alert("No book details for this ISBN found.");
        },
        () => {
        });
    }
  }

  newPhysicalBook() {
    if (this.book.physicals == undefined) {
      this.book.physicals = [];
    }
    //Id of new physical book is one digit bigger than actual biggest physical Id
    let biggestId: number = 0;
    this.book.physicals.forEach(element => {
      if (element.displayId != null) {
        if (element.displayId > biggestId) {
          biggestId = element.displayId;
        }
      }
    });
    let owner: User = new User();
    //set owner name
    if (this.physicalBookForm.get("isInHandapparat").value) {
      owner.name = "university";
      owner.id = -1;
    } else {
      let helpOwner: string[];
      helpOwner = this.physicalBookForm.get("name").value.split("; ");
      owner.name = helpOwner[0];
      owner.id = this.allStaffmembers.find(x => x.email == helpOwner[1]).id;
    }
    if (owner.name == "") {
      alert("You have to choose an owner.");
    } else if (this.physicalBookForm.get("location").value == "") {
      alert("You have to insert a location");
    } else if (owner.name == "university" && this.physicalBookForm.get("rvkSignature").value == "") {
      alert("If the book is owned by the university, you have to insert a RVK Signature");
    } else {
      let singlePhysicalBook: PhysicalBook = new PhysicalBook();
      singlePhysicalBook.location = this.physicalBookForm.get("location").value;
      singlePhysicalBook.department = this.physicalBookForm.get("department").value;
      singlePhysicalBook.room = this.physicalBookForm.get("room").value;
      singlePhysicalBook.owner = owner;
      singlePhysicalBook.displayId = biggestId + 1;
      singlePhysicalBook.handapparat = this.physicalBookForm.get("isInHandapparat").value;
      singlePhysicalBook.rvkSignature = this.physicalBookForm.get("rvkSignature").value;
      this.book.physicals.push(singlePhysicalBook);
      alert("Your Physical Book has been added");
      this.physicalBookForm.patchValue({
        location: '',
        department: '',
        room: '',
        isInHandapparat: '',
        rvkSignature: '',
        name: '',
      });
      var rvkLabel = document.getElementById("rvkLabel");
      var ownerInput = document.getElementById("ownerInput");
      rvkLabel.style.display = "none";
      ownerInput.style.display = "block";
    }
  }


  newTag() {
    if (this.tagForm.get("name").value == "") {
      alert("You have to enter the name of the Tag");
    } else if (this.tagForm.get("description").value == "") {
      alert("You have to enter the description for the tag");
    } else {
      let singleTag: Tag = new Tag();
      singleTag.name = this.tagForm.get("name").value;
      if (this.allTags.find(x => x.name == singleTag.name)) {
        alert("This Tag name exists already. Please rename!.")
      } else {
        singleTag.description = this.tagForm.get("description").value;
        this.tagService.createTag(singleTag).subscribe(
          (data: Tag) => {
            if (data.name == singleTag.name) {
              alert("The tag was succesfully created");
              this.allTags.push(data);
              this.initializeTagForm();
            } else {
              alert("The tag was not created. Please try again");
            }
          },
          (err) => {
            alert("The tag was not created. Please try again");
          },
          () => {
          });
      }

    }
  }

  deleteAuthor(index: number) {
    this.authors.removeAt(index);
  }

  get authors() {
    return this.bookProfileForm.get('authors') as FormArray;
  }

  addAuthor(content: string) {
    this.authors.push(this.fb.control(content));
  }

  deleteEditor(index: number) {
    this.editors.removeAt(index);
  }

  get editors() {
    return this.bookProfileForm.get('editors') as FormArray;
  }

  addEditor(content: string) {
    this.editors.push(this.fb.control(content));
  }

  deleteKeyword(index: number) {
    this.keywords.removeAt(index);
  }

  get keywords() {
    return this.bookProfileForm.get('keywords') as FormArray;
  }

  addKeyword(content: string) {
    this.keywords.push(this.fb.control(content));
  }

  getDataFromForm() {
    let allTags: Tag[] = this.allTags;
    let tagList: Tag[] = [];
    $('input[type=checkbox]').each(function () {
      if (this.checked) {
        let helpTag = allTags.find(x => x.name == this.name);
        tagList.push(helpTag);
      }
    });

    let keywords: Keyword[] = [];
    this.keywords.value.forEach(element => {
      let helpKeyword: Keyword = new Keyword();
      helpKeyword.name = element;
      keywords.push(helpKeyword);
    });

    let authors: Person[] = [];
    this.authors.value.forEach(element => {
      let helpAuthros: Person = new Person();
      helpAuthros.name = element;
      authors.push(helpAuthros);
    });

    let editors: Person[] = [];
    this.editors.value.forEach(element => {
      let helpEditor: Person = new Person();
      helpEditor.name = element;
      authors.push(helpEditor);
    });

    this.book.editors = editors;
    this.book.keywords = keywords;
    this.book.authors = authors;
    this.book.coverURL = this.bookProfileForm.get("coverURL").value;
    this.book.language = this.bookProfileForm.get("language").value;
    this.book.editors = this.editors.value;
    this.book.booktitle = this.bookProfileForm.get("bookTitle").value;
    this.book.title = this.bookProfileForm.get("title").value;
    this.book.abstract = this.bookProfileForm.get("abstract").value;
    this.book.tags = tagList;
    this.book.address = this.bookProfileForm.get("address").value;
    this.book.chapter = this.bookProfileForm.get("chapter").value;
    this.book.edition = this.bookProfileForm.get("edition").value;
    this.book.howPublished = this.bookProfileForm.get("howPublished").value;
    this.book.institution = this.bookProfileForm.get("institution").value;
    this.book.number = this.bookProfileForm.get("number").value;
    this.book.organization = this.bookProfileForm.get("organization").value;
    this.book.pages = this.bookProfileForm.get("pages").value;
    this.book.publisher = this.bookProfileForm.get("publisher").value;
    this.book.series = this.bookProfileForm.get("series").value;
    this.book.type = this.bookProfileForm.get("type").value;
    this.book.year = this.bookProfileForm.get("year").value;
    this.book.doi = this.bookProfileForm.get("doi").value;
    this.book.ean = this.bookProfileForm.get("ean").value;
    this.book.isbn = this.bookProfileForm.get("isbn").value;
    this.book.physicals = this.book.physicals;
  }

  //get speciic book from server
  getBook() {
    this.bookService.getBook(this.book.id).subscribe(
      (data: Book) => {
        if (data == null) {
          this.book = new Book();
          alert("Book with this Id was not found.");
        } else {
          this.book = data;
          this.setBookInfoOnPage();
        }
      },
      (err) => {
        this.book = new Book();
        alert("Book with this Id was not found.");
      },
      () => {
      });
  }

  //is called when create book btn is clicked
  onBookSubmit() {
    if (this.bookProfileForm.get("bookTitle").value == "") {
      alert("You need to insert a book title");
    } else {
      this.getDataFromForm();
      if (this.bookCreationState) {
        this.createBook();
      } else {
        this.updateBook();
      }
    }
  }

  //fct to send book data to server
  createBook() {
    this.bookService.setBook(this.book).subscribe(
      (data: Book) => {
        alert("Book was succesfully added");
        this.book = data;
      },
      (err) => {
        alert("Book was not inserted. Please try again");
        this.initializeBookProfilForm();
      },
      () => {
      });
  }

  //update book on server
  updateBook() {
    this.bookService.updateBook(this.book).subscribe(
      (data: Book) => {
        alert("Book was succesfully updated");
        this.router.navigate(["book", data.id]);
      },
      (err) => {
        alert("Book was not updated. Please try again");
      },
      () => {
      });
  }

  //delete book on server
  deleteBook() {
    this.bookService.deleteBook(this.book.id).subscribe(
      (data: JsonResponse) => {
        if (data.status == 200) {
          alert("Book was succesfully deleted");
          this.router.navigate([""]);
        } else {
          alert("Book was not deleted. Please try again");
        }
      },
      (err) => {
        alert("Book was not deleted. Please try again");
      },
      () => {
      });
  }

  //displays/undisplays rvk info in the html on call
  displayRVK() {
    // Get the checkbox
    var checkBox = <HTMLInputElement>document.getElementById("isInHandapperat");
    // Get the output text
    var rvkLabel = document.getElementById("rvkLabel");
    var ownerInput = document.getElementById("ownerInput");

    // If the checkbox is checked, display the output text
    if (checkBox.checked) {
      rvkLabel.style.display = "block";
      ownerInput.style.display = "none";
    } else {
      rvkLabel.style.display = "none";
      ownerInput.style.display = "block";
    }
  }



  //sets all information from parameter book on html page
  setBookInfoOnPage() {

    for (var i = 0; i < this.keywords.length; i++) {
      this.deleteKeyword(i);
    }
    for (var i = 0; i < this.book.keywords.length; i++) {
      this.addKeyword(this.book.keywords[i].name);
    }

    for (var i = 0; i < this.editors.length; i++) {
      this.deleteEditor(i);
    }
    for (var i = 0; i < this.book.editors.length; i++) {
      this.addEditor(this.book.editors[i].name);
    }

    for (var i = 0; i < this.authors.length; i++) {
      this.deleteAuthor(i);
    }
    for (var i = 0; i < this.book.authors.length; i++) {
      this.addAuthor(this.book.authors[i].name);
    }
    for (var i = 0; i < this.book.physicals.length; i++) {
      this.book.physicals[i].displayId = i;
    }
    this.bookProfileForm.patchValue({
      bookTitle: this.book.booktitle,
      title: this.book.title,
      abstract: this.book.abstract,

      publisher: this.book.publisher,
      address: this.book.address,
      year: this.book.year,
      howPublished: this.book.howPublished,

      coverURL: this.book.coverURL,
      language: this.book.language,
      chapter: this.book.chapter,
      edition: this.book.edition,
      pages: this.book.pages,
      institution: this.book.institution,
      organization: this.book.organization,
      series: this.book.series,
      number: this.book.number,
      type: this.book.type,

      doi: this.book.doi,
      ean: this.book.ean,
      isbn: this.book.isbn,
    });

  }
}