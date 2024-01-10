import { Component, OnInit, Input, EventEmitter, Output } from "@angular/core";
import { BooksService } from "../books.service";
import { Book } from "../book";

@Component({
  selector: "app-filter-click",
  templateUrl: "./filter-click.component.html",
  styleUrls: ["./filter-click.component.scss"]
})
export class FilterClickComponent implements OnInit {
  @Input() query;
  @Output() searchResults = new EventEmitter<Book[]>();
  results: Book[] = [];

  gteYear: number;
  ltYear: number;

  constructor(private bookService: BooksService) { }

  ngOnInit() { }

  searchYear() {
    if (!(this.gteYear && this.ltYear)) {
      return;
    }

    this.results = [];
    //skeleton for elasticsearch query
    let rangeQuery = {
      range: {
        year: {
          gte: this.gteYear,
          lt: this.ltYear
        }
      }
    }

    //get previous query and add newly specified range query
    // JSON.parse -> JSON.stringify for deep copy
    let tmpQuery = JSON.parse(JSON.stringify(this.query))
    tmpQuery.query.bool.must.push(rangeQuery);


    this.bookService.getElasticResults(tmpQuery).subscribe(
      (results: any) => {

        let hits = results.hits.hits;
        hits.forEach(wrapped => {
          let data = wrapped._source;
          this.results.push(data);
        });
      });
    this.searchResults.emit(this.results);

  }
}
