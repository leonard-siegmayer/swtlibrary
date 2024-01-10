import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http'
import { Observable } from 'rxjs';
import { Book } from './book';
import { JsonResponse } from './json-response';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: "root"
})
export class BooksService {
  restApi = environment.API_HOST + ":" + environment.API_PORT + "/api/media";

  constructor(private http: HttpClient) { }

  //returns all books
  getAllBooks(): Observable<Book[]> {
    return this.http.get<Book[]>(this.restApi + '/getAll');
  }

  //returns a single book based on ID
  getBook(id: Number): Observable<Book> {
    return this.http.get<Book>(this.restApi + '/get/' + id.toString());
  }

  //requests elasticsearch with a given elasticsearch query
  getElasticResults(elasticQuery): Observable<any> {
    return this.http.post<any>(this.restApi + '/search', elasticQuery, { headers: { 'Authorization': 'Basic YWRtaW46YWRtaW4=' } });
  }

  setBook(book: Book): Observable<Book> {
    return this.http.post<Book>(this.restApi, book, { withCredentials: true });
  }

  updateBook(book: Book): Observable<Book> {
    return this.http.post<Book>(this.restApi + '/', book, { withCredentials: true });
  }

  deleteBook(id: Number): Observable<JsonResponse> {
    success: Boolean;
    const options = {
      params: { id: id.toString() },
      withCredentials: true
    };
    return this.http.delete<JsonResponse>(this.restApi, options);

  }

  completeBook(ISBN: number): Observable<Book> {
    const options = {
      params: { isbn: ISBN.toString() },
      withCredentials: true
    };
    return this.http.get<Book>(this.restApi + '/ISBN', options);


  }

  saveNote(mediumId: number, text: string, noteId: number): Observable<any> {
    const options = {
      params: {
        medium: mediumId.toString()
      },
      withCredentials: true
    }

    let body = {
      note: text
    }

    if (noteId) {
      body["id"] = noteId;
    }

    return this.http.post<any>(this.restApi + '/note', body, options);
  }

  deleteNote(id: number): Observable<any> {
    const options = {
      params: {
        "note": id.toString()
      },
      withCredentials: true
    }

    return this.http.delete<any>(this.restApi + '/note', options);
  }
}
