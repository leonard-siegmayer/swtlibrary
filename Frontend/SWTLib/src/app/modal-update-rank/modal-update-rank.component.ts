import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-modal-update-rank',
  templateUrl: './modal-update-rank.component.html',
  styleUrls: ['./modal-update-rank.component.scss']
})
export class ModalUpdateRankComponent implements OnInit {

  @Input() public ranks: number;
  @Output() public selectedRank: EventEmitter<any> = new EventEmitter();
  rankList: number[] = [];

  constructor() { }

  ngOnInit() {
    this.ranks++;
    for (let i = 1; i <= this.ranks; i++) {
      this.rankList.push(i);
    }
  }

  cancel() {
    this.selectedRank.emit(undefined);
  }

  submit() {
    this.selectedRank.emit(this.ranks);
  }

}
