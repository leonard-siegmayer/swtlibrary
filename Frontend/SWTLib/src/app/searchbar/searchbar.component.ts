import { Component, OnInit, EventEmitter, Output } from "@angular/core";
import { BooksService } from "../books.service";
import { Book } from "../book";
import { Router, ActivatedRoute } from "@angular/router";

@Component({
  selector: "app-searchbar",
  templateUrl: "./searchbar.component.html",
  styleUrls: ["./searchbar.component.scss"]
})
export class SearchbarComponent implements OnInit {

  constructor(private bookService: BooksService, private router: Router, private route: ActivatedRoute) { }

  results: Book[] = [];
  searchValue: string = "";
  @Output() searchResults = new EventEmitter<Book[]>();
  @Output() queryObject = new EventEmitter<Object>();

  ngOnInit() {
    this.results = [];

    if (this.route.snapshot.paramMap.has('search')) {
      this.searchValue = this.route.snapshot.paramMap.get('search');
      this.search(this.route.snapshot.paramMap.get('search'))
    }
  }

  search(value) {
    this.router.navigate([".", { search: value }]);

    let elasticQueryTemplate = {
      size: 1000,
      query: {
        bool: {
          must: [
            {
              match: {
                "full_info": value
              }
            }
          ]
        }
      }
    };
    this.results = [];

    this.bookService
      .getElasticResults(elasticQueryTemplate)
      .subscribe((results: any) => {

        let hits = results.hits.hits;
        hits.forEach(wrapped => {
          let data = wrapped._source;
          this.results.push(data);
        });
      });

    this.queryObject.emit(elasticQueryTemplate);
    this.searchResults.emit(this.results);
  }
}
