import { Component, OnInit } from '@angular/core';
import { User } from '../user';
import { LoginService } from '../login.service';
import { Role } from '../role';
import { UserService } from '../user.service';

@Component({
  selector: 'app-userlist',
  templateUrl: './userlist.component.html',
  styleUrls: ['./userlist.component.scss']
})
export class UserlistComponent implements OnInit {

  user: User;
  role: typeof Role = Role;
  allRoles: Role[] = [];
  userList: User[] = [];
  filteredList: User[] = [];
  alphabet: string[] = Array.from(Array(26).keys(), i => String.fromCharCode(i + 65));
  filterCriteria: string = null; // 'all', 'other' or 'A', 'B', 'C', ...
  dataReceived: boolean = true;

  constructor(
    private auth: LoginService,
    private userService: UserService
  ) { }

  ngOnInit() {
    // get operating user
    this.auth.getUser(
      user => {
        this.user = user
      }
    );

    // create array including all roles
    for (let r in this.role) {
      this.allRoles.push((<any>Role)[r]);
    }

    // set criteria to show all users on init
    this.filterCriteria = 'all';

    // fetch all users from rest api
    this.getAllUsers();
  }

  // fetch all users from rest api
  getAllUsers() {
    this.userService.getAllUsers().subscribe(
      data => {
        this.dataReceived = true;
        this.userList = data;
        // remove default library + university from userlist by their negative id
        this.userList = this.userList.filter(user => user.id > 0);
        this.sortUserListByName();
        // filter user list to match current filter criteria
        this.filterList(this.filterCriteria);
      },
      err => {
        this.dataReceived = false;
        console.error(err);
      }
    );
  }

  sortUserListByName() {
    this.userList.sort((userA, userB) => userA.name.localeCompare(userB.name));
  }

  // filter user list by given criteria
  filterList(criteria: string) {
    this.filterCriteria = criteria;

    switch (criteria) {
      case 'all':
        this.filterByAll();
        break;
      case 'other':
        this.filterByOther();
        break;
      default:
        this.filterByChar(criteria);
    }
  }

  filterByAll() {
    this.filteredList = this.userList;
  }

  filterByChar(char: string) {
    this.filteredList = this.userList.filter(user => user.name[0] === char);
  }

  // e.g. get users with umlaut as first letter
  filterByOther() {
    this.filteredList = [];
    this.userList.forEach(user => {
      if (!this.alphabet.includes(user.name[0])) {
        this.filteredList.push(user);
      }
    });
  }
}
