import { NgModule } from "@angular/core";
import { Routes, RouterModule } from "@angular/router";
import { BookDetailsComponent } from "./book-details/book-details.component";
import { LandingPageComponent } from "./landing-page/landing-page.component";
import { AuthenticationComponent } from "./authentication/authentication.component";
import { CUDBookDetailsComponent } from "./cud-book-details/cud-book-details.component"
import { ProfileComponent } from './profile/profile.component';
import { StudentBorrowComponent } from './student-borrow/student-borrow.component';
import { StudentReturnComponent } from './student-return/student-return.component';
import { UserlistComponent } from './userlist/userlist.component';
import { StatisticsComponent } from './statistics/statistics.component';
import { WishlistComponent } from './wishlist/wishlist.component';
import { AboutComponent } from './about/about.component';
import { SettingsComponent } from './settings/settings.component';

const routes: Routes = [
  { path: "book/:id", component: BookDetailsComponent },
  { path: "search", component: LandingPageComponent },
  { path: "", redirectTo: "/search", pathMatch: "full" },
  { path: "auth", component: AuthenticationComponent },
  { path: "createBook", component: CUDBookDetailsComponent },
  { path: "editBook/:id", component: CUDBookDetailsComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'studentBorrow', component: StudentBorrowComponent },
  { path: 'studentReturn', component: StudentReturnComponent },
  { path: 'userlist', component: UserlistComponent },
  { path: 'statistics', component: StatisticsComponent },
  { path: 'wishlist', component: WishlistComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'about', component: AboutComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
export const routingComponents = [
  BookDetailsComponent,
  LandingPageComponent,
  AuthenticationComponent,
  ProfileComponent,
  StudentBorrowComponent,
  StudentReturnComponent,
  UserlistComponent
]
