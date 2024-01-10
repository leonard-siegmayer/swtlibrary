package de.teamA.SWT.service;

import de.teamA.SWT.entities.Borrowing;
import de.teamA.SWT.entities.BorrowingStatus;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.repository.BorrowingRepository;
import de.teamA.SWT.service.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Service
public class SchedulingService {

    @Value("${expiration.days}")
    private Integer expirationDays;

    @Value("${overdue.days}")
    private Integer overdueDays;

    private BorrowingRepository borrowingRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    public SchedulingService(BorrowingRepository borrowingRepository) {
        this.borrowingRepository = borrowingRepository;

    }

    @Scheduled(cron = "${cron.mail.overdueWarning}")
    public void checkForOverdueBorrowings() {

        LocalDateTime today = LocalDate.now().atStartOfDay();

        List<Borrowing> overdueBorrowings = borrowingRepository
                .findAllByStatusAndDueDateBefore(BorrowingStatus.BORROWED, today.toLocalDate());
        Map<User, Set<Borrowing>> groupedBorrowings = overdueBorrowings.stream()
                .collect(groupingBy(Borrowing::getBorrower, toSet()));

        emailService.sendOverdueWarningMails(groupedBorrowings);

    }

    @Scheduled(cron = "${cron.mail.expirationWarning}")
    public void checkForExpirationBorrowings() {

        LocalDateTime expirationRangeBegin = LocalDate.now().atStartOfDay();
        LocalDateTime expirationRangeEnd = expirationRangeBegin.plusDays(expirationDays).toLocalDate()
                .atTime(LocalTime.MAX);

        List<Borrowing> borrowingsInExpiration = borrowingRepository.findAllByStatusAndDueDateBetween(
                BorrowingStatus.BORROWED, expirationRangeBegin.toLocalDate(), expirationRangeEnd.toLocalDate());

        Map<User, Set<Borrowing>> groupedBorrowings = borrowingsInExpiration.stream()
                .collect(groupingBy(Borrowing::getBorrower, toSet()));

        emailService.sendExpirationWarningMails(groupedBorrowings);

    }

    @Scheduled(cron = "${cron.mail.overdueInfo}")
    public void checkForCriticalOverdueBorrowings() {
        LocalDateTime overdueDate = LocalDate.now().atStartOfDay().minusDays(overdueDays);

        List<Borrowing> criticalOverdueBorrowings = borrowingRepository
                .findAllByStatusAndDueDateBefore(BorrowingStatus.BORROWED, overdueDate.toLocalDate());

        Map<User, Set<Borrowing>> groupedBorrowings = criticalOverdueBorrowings.stream()
                .collect(groupingBy(Borrowing::getBorrower, toSet()));

        emailService.sendOverdueInfoMails(groupedBorrowings);

    }

    @Scheduled(cron = "${cron.mail.janitor}")
    public void cleanUpMailDatabase() {
        emailService.cleanupDatabase();
    }

}
