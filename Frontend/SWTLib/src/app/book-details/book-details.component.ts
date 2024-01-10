import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { BooksService } from "../books.service";
import { Book } from "../book";
import { User } from '../user';
import { LoginService } from '../login.service';
import { Role } from '../role';

@Component({
  selector: "app-book-details",
  templateUrl: "./book-details.component.html",
  styleUrls: ["./book-details.component.scss"]
})
export class BookDetailsComponent implements OnInit {

  book: Book;
  user: User;

  role: typeof Role = Role;

  constructor(
    private bookService: BooksService,
    private route: ActivatedRoute,
    private auth: LoginService
  ) { }

  ngOnInit() {
    let id = parseInt(this.route.snapshot.paramMap.get("id"));
    this.getBook(id);

    this.auth.getUser(
      user => {
        this.user = user
      }
    );

  }

  getBook(id: number) {
    this.bookService.getBook(id).subscribe(
      (data: Book) => {
        this.book = data;
      },
      (err) => {
        this.book = null;
      });
  }
}
