import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs';

import { JsonResponse } from './json-response';
import { Tag } from './tag';

import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root'
})
export class TagsService {
  restApi = environment.API_HOST + ":" + environment.API_PORT + "/api/tag";

  constructor(private http: HttpClient) { }


  getAllTags(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.restApi, { withCredentials: true });
  }

  createTag(tag: Tag): Observable<Tag> {
    return this.http.post<Tag>(this.restApi, tag, { withCredentials: true });
  }

  updateTag(tag: Tag): Observable<Tag> {
    return this.http.post<Tag>(this.restApi, tag, { withCredentials: true });
  }

  deleteTag(tagId: number): Observable<JsonResponse> {
    const options = {
      params: { id: tagId.toString() },
      withCredentials: true
    };
    return this.http.delete<JsonResponse>(this.restApi, options);
  }
}

