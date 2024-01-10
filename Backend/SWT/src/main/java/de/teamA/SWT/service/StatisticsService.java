package de.teamA.SWT.service;

import static java.util.stream.Collectors.groupingBy;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.teamA.SWT.entities.Borrowing;
import de.teamA.SWT.entities.BorrowingStatus;
import de.teamA.SWT.entities.Keyword;
import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.PhysicalMedium;
import de.teamA.SWT.entities.Reservation;
import de.teamA.SWT.entities.Role;
import de.teamA.SWT.entities.Tag;
import de.teamA.SWT.entities.TopFlopStatistic;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.reqres.Distribution;
import de.teamA.SWT.entities.reqres.PlotData;
import de.teamA.SWT.repository.BorrowingRepository;
import de.teamA.SWT.repository.KeywordRepository;
import de.teamA.SWT.repository.MediumRepository;
import de.teamA.SWT.repository.MediumRepositoryWrapper;
import de.teamA.SWT.repository.PhysicalRepository;
import de.teamA.SWT.repository.TagRepository;
import de.teamA.SWT.repository.UserRepository;
import de.teamA.SWT.util.DateUtil;

// TODO: this class is too large. Better split into two classes: one pure
// service class and another class doing the underlying computations. However, better do this once we have test cases ;)

/**
 * General Note: A LinkedHashMap keeps the Order of Entries in which they were
 * inserted
 */
@Service
public class StatisticsService {

    private final TemporalField dayOfWeek = WeekFields.of(Locale.GERMANY).dayOfWeek();

    private final Function<Borrowing, Date> groupByWeekBeginOfWeek = borrowing -> {
        LocalDate beginOfWeek = borrowing.getReturnDate().with(dayOfWeek, 1L);
        return DateUtil.toDate(beginOfWeek);
    };

    private final Function<Borrowing, Date> groupBySemester = borrowing -> {
        LocalDate beginOfSemester = getBeginOfSemester(borrowing.getReturnDate());
        return DateUtil.toDate(beginOfSemester);
    };

    private final Function<Map.Entry<Borrowing, ?>, Date> groupAugmentedBorrowingsByBeginOfWeek = borrowingWithString -> {
        return groupByWeekBeginOfWeek.apply(borrowingWithString.getKey());
    };

    private final Function<Map.Entry<Borrowing, ?>, Date> groupAugmentedBorrowingsByBeginOfSemester = borrowingWithString -> {
        return groupBySemester.apply(borrowingWithString.getKey());
    };

    Function<Map.Entry<Date, List<Map.Entry<Borrowing, String>>>, Map.Entry<Date, Distribution>> convertAugmentedBorrowingsToDistributions = element -> {
        Date date = element.getKey();
        List<Map.Entry<Borrowing, String>> borrowingsOfOneWeekWithTag = element.getValue();
        Long total = Long.valueOf(borrowingsOfOneWeekWithTag.size());
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Map.Entry<Borrowing, String> borrowingWithTag : borrowingsOfOneWeekWithTag) {
            String tag = borrowingWithTag.getValue();
            // TODO: declare one bifunction at the beginning of this class for the three
            // later uses
            distribution.compute(tag, (key, value) -> (value == null) ? 1L : (value + 1L));
        }
        Distribution dataPoint = new Distribution(total, distribution);
        return new AbstractMap.SimpleEntry<>(date, dataPoint);
    };

    Function<Map.Entry<Date, List<Map.Entry<Borrowing, List<String>>>>, Map.Entry<Date, Distribution>> convertAugmentedListBorrowingsToDistributions = element -> {
        Date date = element.getKey();
        List<Map.Entry<Borrowing, List<String>>> borrowingsOfOneWeekWithTags = element.getValue();
        Long total = Long.valueOf(borrowingsOfOneWeekWithTags.size());
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Map.Entry<Borrowing, List<String>> borrowingWithTags : borrowingsOfOneWeekWithTags) {
            for (String tag : borrowingWithTags.getValue()) {
                distribution.compute(tag, (key, value) -> (value == null) ? 1L : (value + 1L));
            }
        }
        Distribution dataPoint = new Distribution(total, distribution);
        return new AbstractMap.SimpleEntry<>(date, dataPoint);
    };

    Function<Map.Entry<Borrowing, List<String>>, Distribution> convertAugmentedListBorrowingsToDistribution = element -> {
        Map.Entry<Borrowing, List<String>> borrowingsWithTags = element;

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (String tag : borrowingsWithTags.getValue()) {
            distribution.compute(tag, (key, value) -> (value == null) ? 1L : (value + 1L));
        }
        return new Distribution(Long.valueOf(distribution.size()), distribution);
    };

    BinaryOperator<Distribution> aggregateDistribution = ((d1, d2) -> {
        Map<String, Long> tMap = d1.getDistribution();
        Map<String, Long> map2 = d2.getDistribution();

        map2.forEach((key, value) -> tMap.merge(key, value, (l1, l2) -> l1 + l2));
        return new Distribution(Long.valueOf(tMap.size()), tMap);
    });

    private BorrowingRepository borrowingRepository;
    private TagRepository tagRepository;
    private KeywordRepository keywordRepository;
    private MediumRepository mediumRepository;
    private UserRepository userRepository;
    private PhysicalRepository physicalRepository;
    private MediumRepositoryWrapper mediumRepositoryWrapper;

    @Autowired
    public StatisticsService(BorrowingRepository borrowingRepository, TagRepository tagRepository,
            KeywordRepository keywordRepository, MediumRepository mediumRepository, UserRepository userRepository,
            PhysicalRepository physicalRepository, MediumRepositoryWrapper mediumRepositoryWrapper) {
        this.mediumRepository = mediumRepository;
        this.userRepository = userRepository;
        this.borrowingRepository = borrowingRepository;
        this.tagRepository = tagRepository;
        this.keywordRepository = keywordRepository;
        this.physicalRepository = physicalRepository;
        this.mediumRepositoryWrapper = mediumRepositoryWrapper;
    }

    private boolean isInSummerSemester(LocalDate date) {
        int month = date.getMonthValue();
        return month > 3 && month < 10;
    }

    private LocalDate getBeginOfSemester(LocalDate date) {
        int currentYear = date.getYear();
        LocalDate beginOfSemester;

        if (isInSummerSemester(date)) {
            beginOfSemester = LocalDate.of(currentYear, Month.APRIL, 1);
        } else {
            beginOfSemester = LocalDate.of(currentYear, Month.OCTOBER, 1);
        }
        return beginOfSemester;
    }

    private List<Date> getWeekTimeLine(LocalDate begin, LocalDate end) {
        List<Date> timeLine = new ArrayList<>();
        LocalDate beginOfWeek = begin.with(dayOfWeek, 1L);

        while (!beginOfWeek.isAfter(end)) {
            timeLine.add(DateUtil.toDate(beginOfWeek));
            beginOfWeek = beginOfWeek.plusDays(7L);
        }

        Collections.reverse(timeLine);

        return timeLine;
    }

    private List<Date> getSemesterTimeLine(LocalDate begin, LocalDate end) {
        List<Date> timeLine = new ArrayList<>();
        LocalDate beginOfSemester = getBeginOfSemester(begin);

        while (!beginOfSemester.isAfter(end)) {
            timeLine.add(DateUtil.toDate(beginOfSemester));
            beginOfSemester = beginOfSemester.plusMonths(6L);
        }

        Collections.reverse(timeLine);

        return timeLine;
    }

    private List<Borrowing> getBorrowingsOfCurrentSemester() {
        LocalDate today = LocalDate.now();
        return borrowingRepository.findAllByStatusAndReturnDateBetween(BorrowingStatus.RETURNED,
                getBeginOfSemester(LocalDate.now()), today);

    }

    private List<Borrowing> getBorrowingsOfAllTime() {
        return borrowingRepository.findByStatus(BorrowingStatus.RETURNED);
    }

    public Distribution getAvailableTags() {
        List<Tag> allTags = tagRepository.findAll();
        Collections.sort(allTags, Comparator.<Tag>comparingInt(tag -> tag.getMedia().size()).reversed());
        Map<String, Long> groupedTags = new LinkedHashMap<>();
        for (Tag tag : allTags) {
            groupedTags.put(tag.getName(), Long.valueOf(tag.getMedia().size()));
        }
        return new Distribution(Long.valueOf(allTags.size()), groupedTags);
    }

    public Distribution getBorrowingsTags() {
        List<Borrowing> allCurrentBorrowings = borrowingRepository.findByStatus(BorrowingStatus.BORROWED);

        Function<Borrowing, Map.Entry<Borrowing, List<String>>> augmentBorrowingsWithMediumTags = borrowing -> {
            Optional<Medium> mediumOptional = mediumRepository.findById(borrowing.getPhysical().getMediumId());
            if (!mediumOptional.isPresent()) {
                return null;
            }
            Medium medium = mediumOptional.get();
            List<String> tagsFromMedium = medium.getTags().stream().map(Tag::getName).collect(Collectors.toList());
            return new AbstractMap.SimpleEntry<>(borrowing, tagsFromMedium);
        };

        Optional<Distribution> optionalDistribution = allCurrentBorrowings.stream().map(augmentBorrowingsWithMediumTags)
                .map(convertAugmentedListBorrowingsToDistribution).reduce(aggregateDistribution);

        Distribution distribution = optionalDistribution.get();

        return distribution;
    }

    public Distribution getAvailableKeywords() {
        List<Keyword> allKeywords = keywordRepository.findAll();
        Collections.sort(allKeywords,
                Comparator.<Keyword>comparingInt(keyword -> keyword.getMedia().size()).reversed());
        Map<String, Long> groupedTags = new LinkedHashMap<>();
        for (Keyword keyword : allKeywords) {
            groupedTags.put(keyword.getName(), Long.valueOf(keyword.getMedia().size()));
        }
        return new Distribution(Long.valueOf(allKeywords.size()), groupedTags);
    }

    public Distribution getBorrowingsKeywords() {
        List<Borrowing> allCurrentBorrowings = borrowingRepository.findByStatus(BorrowingStatus.BORROWED);

        Function<Borrowing, Map.Entry<Borrowing, List<String>>> augmentBorrowingsWithMediumKeywords = borrowing -> {
            Optional<Medium> mediumOptional = mediumRepository.findById(borrowing.getPhysical().getMediumId());
            if (!mediumOptional.isPresent()) {
                return null;
            }
            Medium medium = mediumOptional.get();
            List<String> keywordsFromMedium = medium.getKeywords().stream().map(Keyword::getName)
                    .collect(Collectors.toList());
            return new AbstractMap.SimpleEntry<>(borrowing, keywordsFromMedium);
        };

        Optional<Distribution> optionalDistribution = allCurrentBorrowings.stream()
                .map(augmentBorrowingsWithMediumKeywords).map(convertAugmentedListBorrowingsToDistribution)
                .reduce(aggregateDistribution);

        Distribution distribution = optionalDistribution.get();

        return distribution;
    }

    public Distribution getAvailableOwner() {
        List<PhysicalMedium> allPhysicals = physicalRepository.findAll();

        Function<PhysicalMedium, String> groupByOwner = physical -> {
            User owner = physical.getOwner();
            return owner.getName();
        };

        Map<String, Long> groupedOwner = allPhysicals.stream().collect(groupingBy(groupByOwner, Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors
                        .toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        return new Distribution(Long.valueOf(groupedOwner.size()), groupedOwner);
    }

    public Distribution getBorrowingsOwner() {
        List<Borrowing> allCurrentBorrowings = borrowingRepository.findByStatus(BorrowingStatus.BORROWED);

        Function<PhysicalMedium, String> groupByOwner = physical -> physical.getOwner().getName();

        Map<String, Long> groupedOwner = allCurrentBorrowings.stream().map(borrowing -> borrowing.getPhysical())
                .collect(groupingBy(groupByOwner, Collectors.counting())).entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors
                        .toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        return new Distribution(Long.valueOf(groupedOwner.size()), groupedOwner);
    }

    public Distribution getTotalBorrowings() {

        Function<Borrowing, String> groupByRoles = borrowing -> {
            Role role = borrowing.getBorrower().getRole();
            // Just to keep the admin out of the way in statistics:
            if (role == Role.ROLE_ADMIN) {
                role = Role.ROLE_STAFF;
            }
            return role.getSimpleName().toLowerCase();
        };

        List<Borrowing> totalBorrowings = borrowingRepository.findByStatus(BorrowingStatus.BORROWED);
        Map<String, Long> groupedBorrowings = totalBorrowings.stream()
                .collect(groupingBy(groupByRoles, Collectors.counting())).entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors
                        .toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        return new Distribution(Long.valueOf(totalBorrowings.size()), groupedBorrowings);
    }

    public PlotData generateStatsByOwnerForCurrentSemester(final List<String> ownerIds) {

        Iterable<User> users = userRepository.findAllById(ownerIds);

        List<PhysicalMedium> physicals = new ArrayList<>();

        for (User user : users) {
            physicals.addAll(physicalRepository.findByOwner(user));
        }

        List<Borrowing> returnedBorrowingsOfThisSemester = getBorrowingsOfCurrentSemester();

        Function<Borrowing, Map.Entry<Borrowing, String>> augmentBorrowingWithOwnerId = borrowing -> {
            User user = borrowing.getPhysical().getOwner();
            return new AbstractMap.SimpleEntry<>(borrowing, user.getId());
        };

        Predicate<Map.Entry<Borrowing, String>> filterByOwnerIds = borrowingWithOwnerId -> ownerIds
                .contains(borrowingWithOwnerId.getValue());

        Map<Date, Distribution> data = returnedBorrowingsOfThisSemester.stream().map(augmentBorrowingWithOwnerId)
                .filter(filterByOwnerIds).collect(groupingBy(groupAugmentedBorrowingsByBeginOfWeek)).entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(convertAugmentedBorrowingsToDistributions).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        List<Date> timeLine = getWeekTimeLine(getBeginOfSemester(LocalDate.now()), LocalDate.now());
        return new PlotData(timeLine, data);
    }

    public PlotData generateStatsByOwnerForAllTime(List<String> ownerNames) {
        List<Borrowing> returnedBorrowingsOfAllTime = getBorrowingsOfAllTime();

        Collections.sort(returnedBorrowingsOfAllTime, Comparator.comparing(x -> x.getReturnDate()));
        LocalDate oldestDueDate = returnedBorrowingsOfAllTime.get(0).getReturnDate();

        Function<Borrowing, Map.Entry<Borrowing, String>> augmentBorrowingsWithOwnerNames = borrowing -> {
            PhysicalMedium physicalMedium = borrowing.getPhysical();
            User owner = physicalMedium.getOwner();
            return new AbstractMap.SimpleEntry<>(borrowing, owner.getName());
        };

        Predicate<Map.Entry<Borrowing, String>> filterByOwnerNames = borrowingWithOwnerName -> ownerNames
                .contains(borrowingWithOwnerName.getValue());

        Map<Date, Distribution> data = returnedBorrowingsOfAllTime.stream().map(augmentBorrowingsWithOwnerNames)
                .filter(filterByOwnerNames).collect(groupingBy(groupAugmentedBorrowingsByBeginOfSemester)).entrySet()
                .stream().sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(convertAugmentedBorrowingsToDistributions).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        List<Date> timeLine = getSemesterTimeLine(getBeginOfSemester(oldestDueDate), LocalDate.now());
        return new PlotData(timeLine, data);
    }

    public PlotData generateStatsByTagsForCurrentSemester(final List<String> tagStrings) {

        List<Borrowing> returnedBorrowingsOfThisSemester = getBorrowingsOfCurrentSemester();

        Function<Borrowing, Map.Entry<Borrowing, List<String>>> augmentBorrowingsWithMediumTags = borrowing -> {
            Optional<Medium> mediumOptional = mediumRepository.findById(borrowing.getPhysical().getMediumId());
            if (!mediumOptional.isPresent()) {
                return null;
            }
            Medium medium = mediumOptional.get();
            List<String> tagsFromMedium = medium.getTags().stream().map(Tag::getName).collect(Collectors.toList());
            return new AbstractMap.SimpleEntry<>(borrowing, tagsFromMedium);
        };

        Predicate<Map.Entry<Borrowing, List<String>>> filterByTagStrings = borrowingWithTags -> {
            return !Collections.disjoint(tagStrings, borrowingWithTags.getValue());
        };

        Map<Date, Distribution> data = returnedBorrowingsOfThisSemester.stream().map(augmentBorrowingsWithMediumTags)
                .filter(filterByTagStrings).collect(groupingBy(groupAugmentedBorrowingsByBeginOfWeek)).entrySet()
                .stream().sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(convertAugmentedListBorrowingsToDistributions).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        List<Date> timeLine = getWeekTimeLine(getBeginOfSemester(LocalDate.now()), LocalDate.now());

        return new PlotData(timeLine, data);
    }

    public PlotData generateStatsByTagsForAllTime(final List<String> tagStrings) {
        List<Borrowing> returnedBorrowingsOfAllTime = getBorrowingsOfAllTime();

        Collections.sort(returnedBorrowingsOfAllTime, Comparator.comparing(x -> x.getReturnDate()));
        LocalDate oldestDueDate = returnedBorrowingsOfAllTime.get(0).getReturnDate();

        Function<Borrowing, Map.Entry<Borrowing, List<String>>> augmentBorrowingsWithMediumTags = borrowing -> {
            Optional<Medium> mediumOptional = mediumRepository.findById(borrowing.getPhysical().getMediumId());
            if (!mediumOptional.isPresent()) {
                return null;
            }
            Medium medium = mediumOptional.get();
            List<String> tagsFromMedium = medium.getTags().stream().map(Tag::getName).collect(Collectors.toList());
            return new AbstractMap.SimpleEntry<>(borrowing, tagsFromMedium);
        };

        Predicate<Map.Entry<Borrowing, List<String>>> filterByTagStrings = borrowingWithTags -> {
            return !Collections.disjoint(tagStrings, borrowingWithTags.getValue());
        };

        Map<Date, Distribution> data = returnedBorrowingsOfAllTime.stream().map(augmentBorrowingsWithMediumTags)
                .filter(filterByTagStrings).collect(groupingBy(groupAugmentedBorrowingsByBeginOfSemester)).entrySet()
                .stream().sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(convertAugmentedListBorrowingsToDistributions).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        List<Date> timeLine = getSemesterTimeLine(getBeginOfSemester(oldestDueDate), LocalDate.now());

        return new PlotData(timeLine, data);
    }

    public PlotData generateStatsByKeywordsForCurrentSemester(List<String> keywordStrings) {
        List<Borrowing> returnedBorrowingsOfThisSemester = getBorrowingsOfCurrentSemester();

        Function<Borrowing, Map.Entry<Borrowing, List<String>>> augmentBorrowingsWithMediumKeywords = borrowing -> {
            Optional<Medium> mediumOptional = mediumRepository.findById(borrowing.getPhysical().getMediumId());
            if (mediumOptional.isPresent()) {
                Medium medium = mediumOptional.get();
                List<String> keywordsFromMedium = medium.getKeywords().stream().map(Keyword::getName)
                        .collect(Collectors.toList());
                return new AbstractMap.SimpleEntry<>(borrowing, keywordsFromMedium);
            }
            return null;
        };

        Predicate<Map.Entry<Borrowing, List<String>>> filterByKeywordStrings = borrowingWithKeywords -> {
            return !Collections.disjoint(keywordStrings, borrowingWithKeywords.getValue());
        };

        Map<Date, Distribution> data = returnedBorrowingsOfThisSemester.stream()
                .map(augmentBorrowingsWithMediumKeywords).filter(filterByKeywordStrings)
                .collect(groupingBy(groupAugmentedBorrowingsByBeginOfWeek)).entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(convertAugmentedListBorrowingsToDistributions).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        List<Date> timeLine = getWeekTimeLine(getBeginOfSemester(LocalDate.now()), LocalDate.now());

        return new PlotData(timeLine, data);
    }

    public PlotData generateStatsByKeywordsForAllTime(final List<String> keywordStrings) {
        List<Borrowing> returnedBorrowingsOfAllTime = getBorrowingsOfAllTime();

        Collections.sort(returnedBorrowingsOfAllTime, Comparator.comparing(x -> x.getReturnDate()));
        LocalDate oldestDueDate = returnedBorrowingsOfAllTime.get(0).getReturnDate();

        Function<Borrowing, Map.Entry<Borrowing, List<String>>> augmentBorrowingsWithMediumKeywords = borrowing -> {
            Optional<Medium> mediumOptional = mediumRepository.findById(borrowing.getPhysical().getMediumId());
            if (mediumOptional.isPresent()) {
                Medium medium = mediumOptional.get();
                List<String> keywordsFromMedium = medium.getKeywords().stream().map(Keyword::getName)
                        .collect(Collectors.toList());
                return new AbstractMap.SimpleEntry<>(borrowing, keywordsFromMedium);
            }
            return null;
        };

        Predicate<Map.Entry<Borrowing, List<String>>> filterByKeywordStrings = borrowingWithKeywords -> {
            return !Collections.disjoint(keywordStrings, borrowingWithKeywords.getValue());
        };

        Map<Date, Distribution> data = returnedBorrowingsOfAllTime.stream().map(augmentBorrowingsWithMediumKeywords)
                .filter(filterByKeywordStrings).collect(groupingBy(groupAugmentedBorrowingsByBeginOfSemester))
                .entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(convertAugmentedListBorrowingsToDistributions).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        List<Date> timeLine = getSemesterTimeLine(getBeginOfSemester(oldestDueDate), LocalDate.now());

        return new PlotData(timeLine, data);
    }

    public PlotData generateStatsByRoleForCurrentSemester() {
        List<Borrowing> returnedBorrowingsOfThisSemester = getBorrowingsOfCurrentSemester();

        Function<Borrowing, Map.Entry<Borrowing, String>> augmentBorrowingsWithRoles = borrowing -> {
            User borrower = borrowing.getBorrower();
            Role role = borrower.getRole();
            // Just to keep the admin out of the way in statistics:
            if (role == Role.ROLE_ADMIN) {
                role = Role.ROLE_STAFF;
            }
            return new AbstractMap.SimpleEntry<>(borrowing, role.name().substring(5).toLowerCase());
        };

        Map<Date, Distribution> data = returnedBorrowingsOfThisSemester.stream().map(augmentBorrowingsWithRoles)
                .collect(groupingBy(groupAugmentedBorrowingsByBeginOfWeek)).entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(convertAugmentedBorrowingsToDistributions).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        List<Date> timeLine = getWeekTimeLine(getBeginOfSemester(LocalDate.now()), LocalDate.now());

        return new PlotData(timeLine, data);
    }

    public PlotData generateStatsByRoleForAllTime() {
        List<Borrowing> returnedBorrowingsOfAllTime = getBorrowingsOfAllTime();

        Collections.sort(returnedBorrowingsOfAllTime, Comparator.comparing(x -> x.getReturnDate()));
        LocalDate oldestDueDate = returnedBorrowingsOfAllTime.get(0).getReturnDate();

        Function<Borrowing, Map.Entry<Borrowing, String>> augmentBorrowingsWithRoles = borrowing -> {
            User borrower = borrowing.getBorrower();
            Role role = borrower.getRole();
            // Just to keep the admin out of the way in statistics:
            if (role == Role.ROLE_ADMIN) {
                role = Role.ROLE_STAFF;
            }
            return new AbstractMap.SimpleEntry<>(borrowing, role.name().substring(5).toLowerCase());
        };

        Map<Date, Distribution> data = returnedBorrowingsOfAllTime.stream().map(augmentBorrowingsWithRoles)
                .collect(groupingBy(groupAugmentedBorrowingsByBeginOfSemester)).entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .map(convertAugmentedBorrowingsToDistributions).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));

        List<Date> timeLine = getSemesterTimeLine(getBeginOfSemester(oldestDueDate), LocalDate.now());

        return new PlotData(timeLine, data);
    }

    /**
     * Generates the List for the TopFlopStatistik
     * 
     * @param number      How many Results shall be displayed, (at the moment
     *                    25,50,100),
     * @param fromDate    specifys the start date of the timeframe
     * @param untilDate   specifys the end date of the timeframe
     * @param searchTerms is the Tag search Term
     * @param top         true= shows most popular books / false shows the least
     *                    popular Books
     * @return the list
     */
    public List<TopFlopStatistic> topFlopQuery(int number, LocalDate fromDate, LocalDate untilDate, String searchTerms,
            boolean top) {
        List<TopFlopStatistic> result = new ArrayList<>();

        List<Medium> media = this.mediumRepositoryWrapper.getAll();
        if (searchTerms != null && !searchTerms.isEmpty()) {
            String[] search = searchTerms.split(" ");

            media.removeIf(m -> {
                for (Tag t : m.getTags()) {
                    for (String s : search) {
                        if (t.getName().contains(s)) {
                            return false;
                        }
                    }
                }
                return true;
            });
        }

        for (Medium m : media) {
            int physicals = m.getPhysicals().size();
            List<LocalDate> resDates = m.getResDate();
            resDates.removeIf(d -> d.isBefore(fromDate) || d.isAfter(untilDate));
            int resNumb = resDates.size();

            List<LocalDate> borrowDates = m.getBorrowDate();
            borrowDates.removeIf(d -> d.isBefore(fromDate) || d.isAfter(untilDate));
            int borrowNumb = borrowDates.size();

            result.add(new TopFlopStatistic(m, physicals, resNumb, borrowNumb));
        }

        result.sort((a, b) -> {
            boolean aLessThanB = (a.getBorrowNumb() + a.getResNumb()) < (b.getBorrowNumb() + b.getResNumb());
            if (aLessThanB && top) {
                return 1;
            } else if (!aLessThanB && !top) {
                return 1;
            } else {
                return -1;
            }
        });

        if (result.size() > number) {
            result = result.subList(0, number);
        }

        for (TopFlopStatistic tfs : result) {

            // total waiting time for all reservation of a particular medium
            double total = 0;

            // TODO: maybe better use a stream computation here as well ...
            for (Reservation r : tfs.getMedium().getReservation()) {
                if (r.getBorrowed()) {
                    total += Duration.between(r.getDate(), r.getBorrowDate()).getSeconds();
                }
            }

            // use "days" as unit for the value of total
            total = ((total / 60) / 60) / 24;

            // number of reservations
            double counter = tfs.getMedium().getReservation().stream().filter(r -> r.getBorrowed()).count();

            // round up to two decimal places
            tfs.setAverageWaitingTime(Math.round((total / counter) * 100) / 100);
        }

        return result;
    }
}
