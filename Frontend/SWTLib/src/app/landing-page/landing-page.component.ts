import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Book } from '../book';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.scss']
})
export class LandingPageComponent implements OnInit {

  constructor(private changeDetector: ChangeDetectorRef, private route: ActivatedRoute) { }

  // boolean to toggle shrinkage on initial search
  inCatalogue = false;
  // boolean to decide which search to show
  isAdvancedSearch = false;
  data: Book[];

  queryObject: Object;

  ngOnInit() {
    //check url to see whether advanced search is used
    if (this.route.snapshot.paramMap.keys.includes("search") || this.route.snapshot.paramMap.keys.length == 0) {
      this.isAdvancedSearch = false;
    } else {
      this.isAdvancedSearch = true;
    }
  }

  onSearch(books: Book[]) {
    this.inCatalogue = true;
    this.changeDetector.detectChanges()
    this.data = books;
  }

  toggleAdvancedSearch() {
    this.isAdvancedSearch = !this.isAdvancedSearch;
  }

  toggleCatalogue() {
    this.inCatalogue = true;
  }

  setQuery(queryObject) {
    this.queryObject = queryObject;
  }


}
