import { Component, OnInit, EventEmitter, Input, Output } from '@angular/core';
import { Tag } from '../tag';

@Component({
  selector: 'app-modal-delete-tag',
  templateUrl: './modal-delete-tag.component.html',
  styleUrls: ['./modal-delete-tag.component.scss']
})
export class ModalDeleteTagComponent implements OnInit {

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
