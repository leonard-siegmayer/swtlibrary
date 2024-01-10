import { Component, OnInit, Output, EventEmitter } from "@angular/core";
import { Book } from "../book";
import { BooksService } from "../books.service";
import { Router, ActivatedRoute } from "@angular/router";

@Component({
  selector: "app-filter-text",
  templateUrl: "./filter-text.component.html",
  styleUrls: ["./filter-text.component.scss"]
})
export class FilterTextComponent implements OnInit {
  filters = ["Autor", "Titel", "ISBN", "DOI", "Keywords"];

  //input field model attributes
  category1: string;
  value1: string;
  category2: string;
  value2: string;
  category3: string;
  value3: string;
  category4: string;
  value4: string;

  results: Book[];
  //event emitter propagate query and result to landing page for synchronization
  @Output() searchResults = new EventEmitter<Book[]>();
  @Output() queryObject = new EventEmitter<Object>();

  constructor(
    private bookService: BooksService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.results = [];
    // see if url contains search queries to react upon
    this.route.snapshot.paramMap.keys.forEach(key => {
      this.filters.forEach(filter => {
        if (key.match(filter)) {
          if (!this.category1) {
            this.category1 = key;
            this.value1 = this.route.snapshot.paramMap.get(key);
          } else if (!this.category2) {
            this.category2 = key;
            this.value2 = this.route.snapshot.paramMap.get(key);
          } else if (!this.category3) {
            this.category3 = key;
            this.value3 = this.route.snapshot.paramMap.get(key);
          } else if (!this.category4) {
            this.category4 = key;
            this.value4 = this.route.snapshot.paramMap.get(key);
          }
        }
      });
    });

    this.onSubmit();
  }

  onSubmit() {
    if (
      !(
        (this.value1 && this.category1) ||
        (this.value2 && this.category2) ||
        (this.value3 && this.category3) ||
        (this.value4 && this.category4)
      )
    ) {
      return;
    }


    let elasticQueryTemplate = this.getQuery();
    this.updateURL();

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
    this.searchResults.emit(this.results);
    this.queryObject.emit(elasticQueryTemplate);
  }

  updateURL() {
    let params = {};
    if (this.category1 && this.value1) {
      params[this.category1] = this.value1;
    }
    if (this.category2 && this.value2) {
      params[this.category2] = this.value2;
    }
    if (this.category3 && this.value3) {
      params[this.category3] = this.value3;
    }
    if (this.category4 && this.value4) {
      params[this.category4] = this.value4;
    }

    this.router.navigate([".", params]);
  }

  getQuery() {
    let searchRequest = {
      size: 1000,
      query: {
        bool: {
          must: []
        }
      }
    };

    let filters = {
      Autor: "authors_and_editors",
      Titel: "full_title",
      Jahr: "year",
      ISBN: "isbn",
      DOI: "doi",
      Keywords: "keywords.name"
    };

    if (this.category1 && this.value1) {
      let matchQuery = {
        match: {}
      };
      matchQuery.match[filters[this.category1]] = this.value1;
      searchRequest.query.bool.must.push(matchQuery);
    }
    if (this.category2 && this.value2) {
      let matchQuery = {
        match: {}
      };
      matchQuery.match[filters[this.category2]] = this.value2;
      searchRequest.query.bool.must.push(matchQuery);
    }
    if (this.category3 && this.value3) {
      let matchQuery = {
        match: {}
      };
      matchQuery.match[filters[this.category3]] = this.value3;
      searchRequest.query.bool.must.push(matchQuery);
    }
    if (this.category4 && this.value4) {
      let matchQuery = {
        match: {}
      };
      matchQuery.match[filters[this.category4]] = this.value4;
      searchRequest.query.bool.must.push(matchQuery);
    }

    return searchRequest;
  }

}
