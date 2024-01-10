import { Component, OnInit, ViewChild, ElementRef, Input, Output, EventEmitter } from '@angular/core';
import { Validators, FormControl } from '@angular/forms';
import { Note } from '../note';
import { BooksService } from '../books.service';

@Component({
  selector: 'app-note-form',
  templateUrl: './note-form.component.html',
  styleUrls: ['./note-form.component.scss']
})
export class NoteFormComponent implements OnInit {

  @Input() bookId: number;
  @Input() note: Note;

  @Output() closeFormEvent: EventEmitter<any> = new EventEmitter();
  @Output() updateBookEvent: EventEmitter<any> = new EventEmitter();

  @ViewChild("textarea") textarea: ElementRef;

  maxLength: number = 4096;

  formValidators: Validators = [
    Validators.required,
    Validators.maxLength(this.maxLength),
    Validators.pattern(/[^\s]/) // avoid text containing only line breaks, whitespaces etc.
  ];

  formCtrl: FormControl;

  errorResponse: boolean = false;

  constructor(
    private bookService: BooksService
  ) { }

  ngOnInit() {
    // check if note was passed and if so, set its text as initial value
    if (this.note) {
      this.formCtrl = new FormControl(this.note.note, this.formValidators);
    } else {
      this.formCtrl = new FormControl("", this.formValidators);
    }
  }

  // stretches the height of the textarea dynamically to fit its content
  autoSizeTextarea(): void {
    this.textarea.nativeElement.style.height = "auto";
    this.textarea.nativeElement.style.height = (this.textarea.nativeElement.scrollHeight + 2) + "px";
  }

  openForm() {
    this.formCtrl.setValue(this.note.note);
    setTimeout(() => {
      this.autoSizeTextarea();
      this.textarea.nativeElement.focus();
    });

  }

  closeForm() {
    this.errorResponse = false;
    this.closeFormEvent.emit(null);
    this.formCtrl.reset();
  }

  createNote() {
    this.saveNote(null);
  }

  editNote() {
    this.saveNote(this.note.id);
  }

  saveNote(noteId: number) {
    if (this.formCtrl.invalid)
      return;

    const noteText = this.replaceNewLines(this.formCtrl.value);
    this.bookService.saveNote(this.bookId, noteText, noteId).subscribe(
      () => {
        this.errorResponse = false;
        this.formCtrl.reset();
        this.autoSizeTextarea();
        this.updateBookEvent.emit(null);
      },
      err => {
        console.error(err);
        this.errorResponse = true;
      },
      () => { }
    );
  }

  // remove empty lines at start + end of text block
  replaceNewLines(s: string): string {
    return s.replace(/[\n\r|\r\n]+$/, "").replace(/^[\n\r|\r\n]+/, "");
  }
}