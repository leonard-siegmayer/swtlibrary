import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WishlistItem } from './wishlist-item';

import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WishlistService {

  restApi = environment.API_HOST + ":" + environment.API_PORT + "/api/wish";

  constructor(private http: HttpClient) { }

  getWishlist(): Observable<WishlistItem[]> {
    return this.http.get<WishlistItem[]>(this.restApi, { withCredentials: true });
  }

  addWish(wish: WishlistItem) {
    return this.http.post(this.restApi, wish, { withCredentials: true });
  }

  deleteWish(id: number) {
    const options = {
      params: {
        "id": id.toString()
      },
      withCredentials: true
    };

    return this.http.delete(this.restApi, options);
  }

  updateWishlist(wishlist: WishlistItem[]) {
    return this.http.post(this.restApi + "/update", wishlist, { withCredentials: true });
  }

  exportWishList(wishlist: WishlistItem[]) {
    return this.http.post(this.restApi + "/send", wishlist, { withCredentials: true });
  }

  exportWishListPdf(wishlist: WishlistItem[]) {
    return this.http.post(this.restApi + "/pdfExport", wishlist, { withCredentials: true, responseType: "arraybuffer" });

  }

  completeBook(ISBN: number): Observable<WishlistItem> {
    const options = {
      params: { isbn: ISBN.toString() },
      withCredentials: true
    };
    return this.http.get<WishlistItem>(this.restApi + '/ISBN', options);


  }
}
