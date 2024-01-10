import { Component, OnInit, Input, ViewChild, ElementRef, EventEmitter, Output, AfterContentChecked } from '@angular/core';
import { User } from '../user';
import { Role } from '../role';
import { Note } from '../note';

@Component({
  selector: 'app-note-section',
  templateUrl: './note-section.component.html',
  styleUrls: ['./note-section.component.scss']
})
export class NoteSectionComponent implements OnInit, AfterContentChecked {

  @Input() bookId: number;
  @Input() notes: Note[];
  @Input() user: User;
  @ViewChild("noteSection") noteSection: ElementRef;
  @Output() updateBookEvent: EventEmitter<any> = new EventEmitter();
  role: typeof Role = Role;
  notesExpanded: boolean = false;

  constructor() { }

  ngOnInit() {
    this.notes.forEach((note) => {
      note.date = new Date(note.date);
    });
    this.sortNotes();
  }

  ngAfterContentChecked() {
    this.notes.forEach((note) => {
      note.date = new Date(note.date);
    });
    this.sortNotes();
  }


  sortNotes(): void {
    // display own notes at the top in reversed chronological order, then notes of other users in reversed chronological order
    this.notes.sort((noteA, noteB) => {
      if (noteA.user.id === this.user.id || noteB.user.id === this.user.id) {
        if (noteA.user.id === this.user.id && noteB.user.id === this.user.id) {
          const dateDiff = noteB.date.getTime() - noteA.date.getTime();
          if (dateDiff === 0) { // date is the same -> sort by id
            return noteB.id - noteA.id;
          } else {
            return dateDiff;
          }
        } else {
          if (noteA.user.id === this.user.id) {
            return -1;
          } else { // noteB.user.id === this.user.id
            return 1;
          }
        }
      } else {
        const dateDiff = noteB.date.getTime() - noteA.date.getTime();
        if (dateDiff === 0) { // date is the same -> sort by id
          return noteB.id - noteA.id;
        } else {
          return dateDiff;
        }
      }
    });
  }

  updateBook() {
    this.updateBookEvent.emit(null);
  }

  // "call" note-section.component
  expandNotes() {
    this.notesExpanded = !this.notesExpanded;

    // scroll note-section.component smoothly to top of page
    const scrollOptions = {
      block: "start",
      behavior: "smooth"
    };
    // setTimeout without an interval as a workaround
    setTimeout(() => {
      this.noteSection.nativeElement.scrollIntoView(scrollOptions);
    });
  }
}
