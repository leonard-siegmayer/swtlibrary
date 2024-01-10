import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { User } from '../user';
import { Role } from '../role';
import { FormControl, Validators } from '@angular/forms';
import { UserService } from '../user.service';

@Component({
  // modified selector to avoid <app-user-entry> in DOM, which messes up table layout
  selector: '[userlist-entry]',
  templateUrl: './userlist-entry.component.html',
  styleUrls: ['./userlist-entry.component.scss']
})
export class UserlistEntryComponent implements OnInit {

  @Input() user: User;
  @Input() userEntry: User;
  @Input() allRoles: Role[];
  @Output() updateEvent: EventEmitter<any> = new EventEmitter();

  formCtrl: FormControl;
  requestFailed: boolean = false;

  constructor(
    private userService: UserService
  ) { }

  ngOnInit() {
    this.formCtrl = new FormControl({
      value: this.userEntry.role,
      disabled: this.user.id === this.userEntry.id // user(admin) can not change his own role
    },
      Validators.required
    );
  }

  // send request setting given role for user with given id
  setRole() {
    if (this.formCtrl.invalid)
      return;

    const role = this.formCtrl.value.substring(5).toLowerCase();
    this.userService.setRole(this.userEntry.id, role).subscribe(
      () => {
        this.requestFailed = false;
        // send event to refetch all users (could be omitted if necessary)
        this.updateEvent.emit(null);
      },
      err => {
        this.formCtrl.reset(this.userEntry.role); // reset select menu to initial value
        this.requestFailed = true;
        console.error(err);
      }
    );
  }
}