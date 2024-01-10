import { Component, OnInit } from '@angular/core';
import { User } from '../user';
import { LoginService } from '../login.service';
import { UserSettings } from '../userSettings';
import { UserService } from '../user.service';
import { JsonResponse } from '../json-response';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {

  user: User;
  settings: UserSettings;
  changed: boolean = false;

  constructor(private auth: LoginService, private userService: UserService, private loginService: LoginService) { }

  ngOnInit() {
    // get operating user
    this.auth.getUser(
      user => {
        this.user = user;
        if (!user.settings) {
          user.settings = new UserSettings();
        }
        this.settings = user.settings;
      }
    );
  }

  preferenceChanged(checkBoxId: string) {
    switch (checkBoxId) {
      case "borrowInfoBorrower":
        this.settings.getBorrowInfoAsBorrower = !this.settings.getBorrowInfoAsBorrower;
        break;
      case "borrowInfoOwner":
        this.settings.getBorrowInfoAsOwner = !this.settings.getBorrowInfoAsOwner;
        break;
      case "returnInfoBorrower":
        this.settings.getReturnInfoAsBorrower = !this.settings.getReturnInfoAsBorrower;
        break;
      case "returnInfoOwner":
        this.settings.getReturnInfoAsOwner = !this.settings.getReturnInfoAsOwner;
        break;
      case "extensionInfoBorrower":
        this.settings.getExtensionInfoAsBorrower = !this.settings.getExtensionInfoAsBorrower;
        break;
      case "extensionInfoOwner":
        this.settings.getExtensionInfoAsOwner = !this.settings.getExtensionInfoAsOwner;
        break;
      case "overdueInfoBorrower":
        this.settings.getOverdueInfoAsBorrower = !this.settings.getOverdueInfoAsBorrower;
        break;
      case "overdueInfoOwner":
        this.settings.getOverdueInfoAsOwner = !this.settings.getOverdueInfoAsOwner;
        break;
      case "expireInfoBorrower":
        this.settings.getExpireInfoAsBorrower = !this.settings.getExpireInfoAsBorrower;
        break;
      case "infoReservation":
        this.settings.getReservationInfo = !this.settings.getReservationInfo;
        break;
      case "missedReservationInfo":
        this.settings.getMissedReservationInfo = !this.settings.getMissedReservationInfo;
        break;
      default:
        break;
    }
    this.changed = true;
  }

  updateEmailPreferences() {
    // updating the settings
    this.userService.updateUserSettings(this.user.id, this.settings).subscribe((resp: JsonResponse) => {
      if (resp.status == 200) {
        this.user.settings = this.settings;
        this.loginService.setUser(this.user);
      }
      alert(resp.message);
    });
  }
}
