import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Tag } from '../tag';

@Component({
  selector: 'app-modal-update-tag',
  templateUrl: './modal-update-tag.component.html',
  styleUrls: ['./modal-update-tag.component.scss']
})
export class ModalUpdateTagComponent implements OnInit {

  @Input() public tag: Tag;
  @Output() public passEntry: EventEmitter<any> = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  passBack() {
    this.passEntry.emit(this.tag);
  }

  cancel() {
    this.passEntry.emit(null);
  }

}
