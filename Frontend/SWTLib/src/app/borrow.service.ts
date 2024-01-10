import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ResponseBorrowingEnititiesExtended } from './response-borrowing-entities-extended';
import { ResponsePhysicalsExtended } from './response-physicals-extended';
import { Config } from './config';
import { environment } from 'src/environments/environment';
import { BorrowingEntity } from './borrowing-entity';
import { Reservation } from './reservation';
import { JsonResponse } from './json-response';
import { PhysicalStatus } from './physical-status';
import { PhysicalBook } from './physicalBook';

@Injectable({
  providedIn: 'root'
})
export class BorrowService {

  restApi = environment.API_HOST + ":" + environment.API_PORT + "/api";

  constructor(private http: HttpClient) { }

  getBorrowingsFromPhysical(id: number): Observable<BorrowingEntity[]> {
    return this.http.get<BorrowingEntity[]>(this.restApi + '/borrow/physical/borrowings/' + id, { withCredentials: true });
  }

  // Requests for Profile Pages

  getBorrowedMediaByUserID(id: number): Observable<ResponseBorrowingEnititiesExtended[]> {
    return this.http.get<ResponseBorrowingEnititiesExtended[]>(this.restApi + '/borrow/borrower', { withCredentials: true });
  }

  getOwnMediaByUserID(id: number): Observable<ResponsePhysicalsExtended[]> {
    return this.http.get<ResponsePhysicalsExtended[]>(this.restApi + '/borrow/owner/' + id, { withCredentials: true });
  }

  // Requests for Student Borrowing Pages

  getStudentRequests(): Observable<ResponseBorrowingEnititiesExtended[]> {
    return this.http.get<ResponseBorrowingEnititiesExtended[]>(this.restApi + '/borrow/student/requests', { withCredentials: true });
  }

  getStudentBorrowings(): Observable<ResponseBorrowingEnititiesExtended[]> {
    return this.http.get<ResponseBorrowingEnititiesExtended[]>(this.restApi + '/borrow/student/borrowings', { withCredentials: true });
  }

  //Request for BookDetails

  get(id: number): Observable<ResponsePhysicalsExtended> {
    return this.http.get<ResponsePhysicalsExtended>(this.restApi + '/borrow/book/' + id);
  }

  getPDF(physicalId: number) {
    return this.http.get(this.restApi + '/borrow/pdf',
      { params: { "borrowing_id": physicalId.toString() }, withCredentials: true, responseType: "arraybuffer" });
  }
  getReturnPDF(physicalId: number) {
    return this.http.get(this.restApi + '/borrow/returnPdf',
      { params: { "borrowing_id": physicalId.toString() }, withCredentials: true, responseType: "arraybuffer" });
  }
  acceptStudentRequest(physicalId: number, date: string) {
    return this.http.get(this.restApi + '/borrow/accept',
      { params: { "borrowing": physicalId.toString(), "dueDate": date }, withCredentials: true });
  }
  declineStudentRequest(physicalId: number) {
    return this.http.get(this.restApi + '/borrow/decline',
      { params: { "borrowing": physicalId.toString() }, withCredentials: true });
  }
  extendStudentBorrow(physicalId: number, date: string) {
    return this.http.get(this.restApi + '/borrow/extend',
      { params: { "borrowing": physicalId.toString(), "date": date }, withCredentials: true, observe: "body" });
  }
  returnStudentBorrow(physicalId: number) {
    return this.http.get(this.restApi + '/borrow/return',
      { params: { "borrowing": physicalId.toString() }, withCredentials: true });
  }
  returnBorrowStaff(id: number) {
    return this.http.get(this.restApi + "/borrow/return", { params: { "borrowing": id.toString() }, withCredentials: true });
  }
  extendBorrowStaff(id: number, date: string) {
    return this.http.get(this.restApi + "/borrow/extend", { params: { "borrowing": id.toString(), "date": date }, withCredentials: true });
  }
  borrow(date: any, mediumId: number) {
    return this.http.get(this.restApi + '/borrow/',
      { params: { "date": date.toString(), "medium": mediumId.toString() }, withCredentials: true });
  }

  getConfig(callback?) {
    this.http.get(this.restApi + "/configs/", { withCredentials: true }).subscribe(
      (config: Config) => {
        sessionStorage.setItem("borrowingConfig", JSON.stringify(config));
        callback(config);
      }
    );
  }

  updatePhysical(physical: PhysicalBook): Observable<JsonResponse> {
    return this.http.post(this.restApi + '/borrow/physical', physical, { withCredentials: true }) as Observable<JsonResponse>;
  }


  // reservation functions

  getReservationsByPhysicalID(mediumId: number): Observable<Reservation[]> {
    return this.http.get(this.restApi + "/reservation/medium/" + mediumId, { withCredentials: true }) as Observable<Reservation[]>;
  }

  getReservationsByUser(userId: number): Observable<Reservation[]> {
    return this.http.get(this.restApi + "/reservation/user/" + userId, { withCredentials: true }) as Observable<Reservation[]>;
  }

  deleteReservation(id: number) {
    return this.http.delete(this.restApi + "/reservation/" + id, { withCredentials: true });
  }

  saveReservation(reservation: Reservation) {
    return this.http.post(this.restApi + "/reservation", reservation, { withCredentials: true });
  }

  updateReservationRank(id: number, rank: string) {
    return this.http.post(this.restApi + `/reservation/update/${id}/${rank}`, null, { withCredentials: true });
  }

  // after a user has been selected as the next borrower, he can redeem his reservation in exchange of a borrowing
  redeemReservation(id: number) {
    return this.http.get(this.restApi + "/reservation/redeem/" + id, { withCredentials: true });
  }
}
