package de.teamA.SWT.controller;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.teamA.SWT.entities.TopFlopStatistic;
import de.teamA.SWT.entities.reqres.Distribution;
import de.teamA.SWT.entities.reqres.PlotData;
import de.teamA.SWT.service.LibraryService;
import de.teamA.SWT.service.StatisticsService;

@RestController
@RequestMapping("api/stats")
@CrossOrigin
public class StatisticsController {

    @Autowired
    StatisticsService statisticsService;

    @Autowired
    LibraryService libraryService;

    @RequestMapping(value = "/library/tags", method = RequestMethod.GET)
    public Distribution getAllTags() {
        return statisticsService.getAvailableTags();
    }

    @RequestMapping(value = "/borrow/tags", method = RequestMethod.GET)
    public Distribution getBorrowTags() {
        return statisticsService.getBorrowingsTags();
    }

    @RequestMapping(value = "/library/keywords", method = RequestMethod.GET)
    public Distribution getAllKeywords() {
        return statisticsService.getAvailableKeywords();
    }

    @RequestMapping(value = "/borrow/keywords", method = RequestMethod.GET)
    public Distribution getBorrowKeywords() {
        return statisticsService.getBorrowingsKeywords();
    }

    @RequestMapping(value = "/library/owner", method = RequestMethod.GET)
    public Distribution getAllOwner() {
        return statisticsService.getAvailableOwner();
    }

    @RequestMapping(value = "/borrow/owner", method = RequestMethod.GET)
    public Distribution getBorrowOwner() {
        return statisticsService.getBorrowingsOwner();
    }

    @RequestMapping(value = "/borrow/borrowers", method = RequestMethod.GET)
    public Distribution getTotalBorrowings() {
        return statisticsService.getTotalBorrowings();
    }

    @RequestMapping(value = "/gen/by_keywords/current", method = RequestMethod.POST)
    public PlotData generateStatsByKeywordsForCurrentSemester(@RequestBody @Valid List<String> keywords) {
        return statisticsService.generateStatsByKeywordsForCurrentSemester(keywords);
    }

    @RequestMapping(value = "/gen/by_keywords/allTime", method = RequestMethod.POST)
    public PlotData generateStatsByKeywordsForAllTime(@RequestBody @Valid List<String> keywords) {
        return statisticsService.generateStatsByKeywordsForAllTime(keywords);
    }

    @RequestMapping(value = "/gen/by_tags/current", method = RequestMethod.POST)
    public PlotData generateStatsByTagsForCurrentSemester(@RequestBody @Valid List<String> tags) {

        return statisticsService.generateStatsByTagsForCurrentSemester(tags);

    }

    @RequestMapping(value = "/gen/by_tags/allTime", method = RequestMethod.POST)
    public PlotData generateStatsByTagsForAllTime(@RequestBody @Valid List<String> tags) {

        return statisticsService.generateStatsByTagsForAllTime(tags);

    }

    @RequestMapping(value = "/gen/by_owners/current", method = RequestMethod.POST)
    public PlotData generateStatsByOwnersForCurrentSemester(@RequestBody @Valid List<String> ownerIds) {
        return statisticsService.generateStatsByOwnerForCurrentSemester(ownerIds);
    }

    @RequestMapping(value = "/gen/by_owners/allTime", method = RequestMethod.POST)
    public PlotData generateStatsByOwnersForAllTime(@RequestBody @Valid List<String> ownerIds) {
        return statisticsService.generateStatsByOwnerForAllTime(ownerIds);
    }

    @RequestMapping(value = "/gen/by_role/current", method = RequestMethod.POST)
    public PlotData generateStatsByRoleForCurrentSemester() {
        return statisticsService.generateStatsByRoleForCurrentSemester();
    }

    @RequestMapping(value = "/gen/by_role/allTime", method = RequestMethod.POST)
    public PlotData generateStatsByRoleForAllTime() {
        return statisticsService.generateStatsByRoleForAllTime();
    }

    /**
     * Returns the results of a "Top/Flop Query" wrapped inside a TopFlopStatistics
     * Object.
     * 
     * @param number      the required number of results
     * @param fromDate    the start date of the requested time interval
     * @param untilDate   the end date of the requested time interval
     * @param searchTerms search terms restricting the list. Separated with spaces
     * @param top         true if the top list is required. False if the flop list
     *                    is required
     * @return the list of TopFlopStatistic Objects matching the query
     */
    @RequestMapping(value = "/topFlops", method = RequestMethod.GET)
    public List<TopFlopStatistic> getTopFlops(@RequestParam(value = "number") int number,
            @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(value = "untilDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate untilDate,
            @RequestParam(value = "searchTerms") String searchTerms, @RequestParam(value = "top") boolean top) {
        return statisticsService.topFlopQuery(number, fromDate, untilDate, searchTerms, top);
    }
}
