import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';
import { TopFlopStatistic } from './top-flop-statistic';

@Injectable({
  providedIn: 'root'
})
export class StatisticService {

  restApi = environment.API_HOST + ":" + environment.API_PORT + "/api";

  constructor(private http: HttpClient) { }

  getAllBorrowers() {
    return this.http.get(this.restApi + '/stats/borrow/borrowers', { withCredentials: true });
  }

  getAllTags() {
    return this.http.get(this.restApi + '/stats/library/tags', { withCredentials: true });
  }

  getAllKeywords() {
    return this.http.get(this.restApi + '/stats/library/keywords', { withCredentials: true });
  }

  getAllOwner() {
    return this.http.get(this.restApi + '/stats/library/owner', { withCredentials: true });
  }

  getTagTimelineBorrowed(tags: string[], byYear?: boolean) {
    if (byYear) {
      return this.http.post(this.restApi + '/stats/gen/by_tags/allTime', tags, { withCredentials: true });
    }
    return this.http.post(this.restApi + '/stats/gen/by_tags/current', tags, { withCredentials: true });
  }

  getKeywordTimelineBorrowed(tags: string[], byYear?: boolean) {
    if (byYear) {
      return this.http.post(this.restApi + '/stats/gen/by_keywords/allTime', tags, { withCredentials: true });
    }
    return this.http.post(this.restApi + '/stats/gen/by_keywords/current', tags, { withCredentials: true });
  }

  getOwnerTimelineBorrowed(tags: string[], byYear?: boolean) {
    if (byYear) {
      return this.http.post(this.restApi + '/stats/gen/by_owners/allTime', tags, { withCredentials: true });
    }
    return this.http.post(this.restApi + '/stats/gen/by_owners/current', tags, { withCredentials: true });
  }

  /**
     * Returns the results of a "Top/Flop Query" wrapped inside a TopFlopStatistics Object.
     * @param number the required number of results
     * @param fromDate the start date of the requested time interval (yyyy-mm-dd)
     * @param untilDate the end date of the requested time interval (yyyy-mm-dd)
     * @param searchTerms search terms restricting the list. Separated with spaces
     * @param top true if the top list is required. False if the flop list is required
     */
  getTopFlops(number: number, fromDate: string, untilDate: string, searchTerms: string, top: boolean): Observable<TopFlopStatistic[]> {
    const options = {
      params: {
        number: number.toString(),
        fromDate: fromDate,
        untilDate: untilDate,
        searchTerms: searchTerms,
        top: top.toString()
      },
      withCredentials: true
    }
    let result: Observable<any> = this.http.get(this.restApi + '/stats/topFlops', options);
    return result;
  }

}
