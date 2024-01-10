import { Component, OnInit } from '@angular/core';
import { StatisticService } from '../statistic.service';
import { LoginService } from '../login.service';
import { Role } from '../role';
import { FormControl, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { BooksService } from "./../books.service";
import { Book } from '../book';
import { TopFlopStatistic } from '../top-flop-statistic';
import { Tag } from '../tag';
import { TagsService } from '../tags.service';

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss']
})


export class StatisticsComponent implements OnInit {

  topFlopList: TopFlopStatistic[] = [];
  selectedTags: String[] = [];
  allTags: Tag[] = [];

  //Formgroup for TopFlop to get the Number of Books to show in the statistics 
  form: FormGroup = new FormGroup({
    displayNum: new FormControl('25'),
    fromDate: new FormControl,
    untilDate: new FormControl,
    mostPopular: new FormControl('true'),
    currentTag: new FormControl(""),
  })
  datePickerForm: FormControl = new FormControl('', [
    Validators.required,
    // check if type is date as safari does not suport input type date
    Validators.pattern(/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/)
  ]);

  //Boolean to show Top and Flop statistics
  topAndFlop: boolean = false;


  //fields offered and selected for timeline
  plotOptions = [];
  selectedPlotOptions = [];

  //boolean to decide if pie-chart or timeline is shown
  showTimeLinePlot = false;

  // construct to decide which rest-endpoint to call based on choosen metric
  private timelineServiceMethodTypes = { "tag": 1, "keyword": 2, "owner": 3 };
  private timelineServiceMethodType;


  // piechart skeleton
  public graph = {
    data: [{
      values: [],
      labels: [],
      type: "none"
    }],
    layout: { autosize: true, title: '' },
  };

  // timeline skeleton
  public timeline = {
    data: [],
    layout: {
      autosize: true, title: 'Borrow History over Time', yaxis: {
        rangemode: 'tozero',
        autorange: true
      }
    }
  };

  isAuthorized: boolean;

  constructor(private statisticService: StatisticService, private loginService: LoginService, private bookService: BooksService, private tagService: TagsService) { }
  results: Book[] = [];

  ngOnInit() {
    this.loginService.getUser(
      user => {
        this.isAuthorized = (user != null && (user.role === Role.ROLE_ADMIN || user.role === Role.ROLE_STAFF))
      }
    );
    this.tagService.getAllTags().subscribe(res => this.allTags = res);
  }

  getAllTags() {
    this.topAndFlop = false;
    this.timelineServiceMethodType = this.timelineServiceMethodTypes.tag;
    this.showTimeLinePlot = false;
    this.graph.layout.title = "Books in Library by Tags"
    this.statisticService.getAllTags().subscribe(data => this.setNewPieChartData(data));
  }
  getAllKeywords() {
    this.topAndFlop = false;
    this.timelineServiceMethodType = this.timelineServiceMethodTypes.keyword;
    this.showTimeLinePlot = false;
    this.graph.layout.title = "Books in Library by Keywords"
    this.statisticService.getAllKeywords().subscribe(data => this.setNewPieChartData(data));
  }
  getAllOwner() {
    this.topAndFlop = false;
    this.timelineServiceMethodType = this.timelineServiceMethodTypes.owner;
    this.showTimeLinePlot = false;
    this.graph.layout.title = "Books in Library by Owner"
    this.statisticService.getAllOwner().subscribe(data => this.setNewPieChartData(data));
  }

  toggleChartType() {
    this.showTimeLinePlot = !this.showTimeLinePlot;
  }

  setNewPieChartData(data: any) {
    this.clearPieChartPlot();
    this.configurePieChart(data.distribution);
    this.prepareTimeLine(data.distribution);
  }

  private clearPieChartPlot() {
    this.plotOptions = [];
    this.graph.data[0].labels = [];
    this.graph.data[0].values = [];
  }

  private configurePieChart(distribution: any) {
    for (let label in distribution) {
      this.graph.data[0].labels.push(label)
      this.graph.data[0].values.push(distribution[label]);
      this.graph.data[0].type = "pie";
      this.graph.data[0]["hoverinfo"] = 'label+percent',
        this.graph.data[0]["textinfo"] = 'value+label'
    }
  }

  private prepareTimeLine(distribution: any) {
    for (let option in distribution) {
      this.plotOptions.push(option);
    }
  }

  setNewTimeLineData(data: any) {
    this.clearTimelineplot();
    this.configureTimelineChart(data);
  }

  private clearTimelineplot() {
    this.timeline.data = [];
    this.timeline.data = [];
  }

  configureTimelineChart(data: any) {

    for (let key in this.selectedPlotOptions) {
      let category = this.selectedPlotOptions[key]
      let line = {
        x: [],
        y: [],
        type: "line",
        opacity: 0.7,
        name: category
      };
      // iterate over response and create zero y values for every date
      for (let date in data.timeline) {
        line.x.push(data.timeline[date])
        line.y.push(0)
      }
      // iterate over actual data and replace zeros by count
      for (let point in data.data) {
        if (data.data[point].distribution[category]) {
          line.y[data.timeline.indexOf(point)] = data.data[point].distribution[category]
        }
      }
      this.timeline.data.push(line);
    }
  }


  // the timeline data is requested depending on the choosen metric (owner, tag, keyword)
  // therefore a request to the statisticsService sets the flag timelineServiceMethodType (see e.g. getAllKeywords())
  // when the actual request is performed, the switch statement decides on the method to use in the statisticsService

  requestTimeline(byYear?: boolean) {
    this.selectedPlotOptions = [];
    for (let key in this.plotOptions) {
      if (document.forms["plotOptionsForm"][key].checked) {
        this.selectedPlotOptions.push(document.forms["plotOptionsForm"][key].name)
      }
    }
    switch (this.timelineServiceMethodType) {
      case this.timelineServiceMethodTypes.keyword:
        this.statisticService.getKeywordTimelineBorrowed(this.selectedPlotOptions, byYear).subscribe(data => this.setNewTimeLineData(data))
        break;
      case this.timelineServiceMethodTypes.tag:
        this.statisticService.getTagTimelineBorrowed(this.selectedPlotOptions, byYear).subscribe(data => this.setNewTimeLineData(data))
        break;
      case this.timelineServiceMethodTypes.owner:
        this.statisticService.getOwnerTimelineBorrowed(this.selectedPlotOptions, byYear).subscribe(data => this.setNewTimeLineData(data))
        break;
      default:
        break;
    }

  }


  getTopAndFlop() {
    const fromDate = this.form.get('fromDate').value;
    const untilDate = this.form.get('untilDate').value;
    const top = this.form.get('mostPopular').value;
    const number = this.form.get('displayNum').value;
    let searchTerms = "";

    this.selectedTags.forEach(t => searchTerms += t + " ");

    if (fromDate == null || untilDate == null) {
      alert("Please enter a date");
      return;
    }
    this.statisticService.getTopFlops(number, fromDate, untilDate, searchTerms, top).subscribe(res => this.topFlopList = res);
  }

  addTag() {
    if (this.selectedTags.find(t => t == this.form.value.currentTag) || !this.form.value.currentTag) { return; }
    this.selectedTags.push(this.form.get('currentTag').value);
    this.form.setValue({
      displayNum: this.form.value.displayNum,
      fromDate: this.form.value.fromDate,
      untilDate: this.form.value.untilDate,
      mostPopular: this.form.value.mostPopular,
      currentTag: "",
    });
  }

  removeTag(name: string) {
    this.selectedTags = this.selectedTags.filter(t => t != name);
  }
}
