import { Component, OnInit, Input, ViewChild, Output, EventEmitter } from '@angular/core';
import { Note } from '../note';
import { User } from '../user';
import { Role } from '../role';
import { BooksService } from '../books.service';
import { NoteFormComponent } from '../note-form/note-form.component';

@Component({
  selector: 'app-note',
  templateUrl: './note.component.html',
  styleUrls: ['./note.component.scss']
})
export class NoteComponent implements OnInit {

  @Input() bookId: number;
  @Input() note: Note;
  @Input() user: User;
  @Output() updateBookEvent: EventEmitter<any> = new EventEmitter();
  @ViewChild(NoteFormComponent) noteForm: NoteFormComponent;
  role: typeof Role = Role;

  inEdit: boolean | null;
  errorResponse: boolean = false;

  constructor(
    private bookService: BooksService
  ) { }

  ngOnInit() {
  }

  toggleForm() {
    this.inEdit = !this.inEdit;

    if (this.inEdit) {
      this.noteForm.openForm();
    }
  }

  deleteNote(): void {
    this.bookService.deleteNote(this.note.id).subscribe(
      () => {
        this.errorResponse = false;
        this.updateBook();
      },
      err => {
        this.errorResponse = true;
        console.error(err);
      },
      () => { }
    );
  }

  updateBook() {
    this.updateBookEvent.emit(null);
  }
}
