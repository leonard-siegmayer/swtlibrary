import { PhysicalBook } from './../physicalBook';
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-modal-delete-physical',
  templateUrl: './modal-delete-physical.component.html',
  styleUrls: ['./modal-delete-physical.component.scss']
})
export class ModalDeletePhysicalComponent implements OnInit {

  constructor() { }

  @Input() public physical: PhysicalBook;
  @Output() public passEntry: EventEmitter<any> = new EventEmitter();

  ngOnInit() {
  }

  passBack() {
    this.passEntry.emit(this.physical);
  }

  cancel() {
    this.passEntry.emit(null);
  }

}
