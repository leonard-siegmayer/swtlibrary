import { NgbModule, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import * as $ from 'jquery';
import { AppRoutingModule, routingComponents } from './app-routing.module';
import { AppComponent } from './app.component';
import { NavComponent } from './nav/nav.component';
import { BookDetailsComponent } from './book-details/book-details.component';
import { SearchbarComponent } from './searchbar/searchbar.component';
import { LandingPageComponent } from './landing-page/landing-page.component';
import { FilterClickComponent } from './filter-click/filter-click.component';
import { FilterTextComponent } from './filter-text/filter-text.component';
import { BookSearchResultComponent } from './book-search-result/book-search-result.component';
import { NoteSectionComponent } from './note-section/note-section.component';
import { LoginComponent } from './login/login.component';
import { AuthenticationComponent } from './authentication/authentication.component';
import { CUDBookDetailsComponent } from './cud-book-details/cud-book-details.component';
import { ModalUpdateTagComponent } from './modal-update-tag/modal-update-tag.component';
import { ModalDeleteTagComponent } from './modal-delete-tag/modal-delete-tag.component';
import { ProfileComponent } from './profile/profile.component';
import { StudentBorrowComponent } from './student-borrow/student-borrow.component';
import { BorrowInterfaceComponent } from './borrow-interface/borrow-interface.component';
import { StudentReturnComponent } from './student-return/student-return.component';
import { NoteComponent } from './note/note.component';
import { NoteFormComponent } from './note-form/note-form.component';
import { UserlistComponent } from './userlist/userlist.component';
import { UserlistEntryComponent } from './userlist-entry/userlist-entry.component';
import { StatisticsComponent } from './statistics/statistics.component';
import { PlotlyViaCDNModule } from 'angular-plotly.js';
import { WishlistComponent } from './wishlist/wishlist.component';
import { WishlistItemComponent } from './wishlist-item/wishlist-item.component';
import { DataTablesModule } from 'angular-datatables';

PlotlyViaCDNModule.plotlyVersion = 'latest';
PlotlyViaCDNModule.plotlyBundle = 'basic';
import { ModalDeletePhysicalComponent } from './modal-delete-physical/modal-delete-physical.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { AboutComponent } from './about/about.component';
import { SettingsComponent } from './settings/settings.component';
import { ModalUpdateRankComponent } from './modal-update-rank/modal-update-rank.component';

@NgModule({
  declarations: [
    AppComponent,
    NavComponent,
    routingComponents,
    BookDetailsComponent,
    SearchbarComponent,
    LandingPageComponent,
    FilterClickComponent,
    FilterTextComponent,
    BookSearchResultComponent,
    NoteSectionComponent,
    LoginComponent,
    AuthenticationComponent,
    CUDBookDetailsComponent,
    ModalUpdateTagComponent,
    ModalDeleteTagComponent,
    ProfileComponent,
    StudentBorrowComponent,
    BorrowInterfaceComponent,
    StudentReturnComponent,
    NoteComponent,
    NoteFormComponent,
    UserlistComponent,
    UserlistEntryComponent,
    StatisticsComponent,
    WishlistComponent,
    WishlistItemComponent,
    ModalDeletePhysicalComponent,
    AboutComponent,
    SettingsComponent,
    ModalUpdateRankComponent
  ],
  imports: [
    DataTablesModule,
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    PlotlyViaCDNModule,
    DragDropModule,
    NgbModule
  ],
  providers: [
    NgbActiveModal,
    LoginComponent
  ],
  bootstrap: [AppComponent],
  entryComponents: [ModalUpdateTagComponent, ModalDeleteTagComponent, ModalDeletePhysicalComponent, ModalUpdateRankComponent],
})
export class AppModule { }
