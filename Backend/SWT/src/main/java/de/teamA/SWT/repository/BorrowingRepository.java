package de.teamA.SWT.repository;

import de.teamA.SWT.entities.Borrowing;
import de.teamA.SWT.entities.BorrowingStatus;
import de.teamA.SWT.entities.PhysicalMedium;
import de.teamA.SWT.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    @Modifying
    @Query("update Borrowing b set b.returnDate = ?1 where b.id = ?2")
    void setReturnDateById(Date returnDate, Long userId);

    default List<Borrowing> findByBorrower(User user) {
        return findByStatus(BorrowingStatus.BORROWED);
    }

    default List<Borrowing> findByBorrowerREQUESTED(User user) {
        return findByStatus(BorrowingStatus.REQUESTED);
    }

    default List<Borrowing> findAllByOrderByDueDateDesc() {
        return findByStatus(BorrowingStatus.BORROWED);
    }

    List<Borrowing> findByMediumAndReturnDateIsNull(PhysicalMedium medium);

    List<Borrowing> findByMedium(PhysicalMedium medium);

    @Transactional
    @Modifying
    @Query("select b from Borrowing b where b.borrower = ?1 and ( (DATEDIFF(b.borrowDate, NOW()) >= 30 and b.status = 'DECLINED') or b.status = 'REQUESTED' or b.status = 'BORROWED' ) ")
    List<Borrowing> getBorrowingsFromUser(User user);

    @Transactional
    @Modifying
    @Query("select b from Borrowing b where b.status = 'BORROWED' and b.responsibleStaff IS NOT NULL")
    List<Borrowing> getBorrowingsFromStudents();

    List<Borrowing> findAllByStatusAndDueDateBetween(BorrowingStatus status, LocalDate dueDateStart,
            LocalDate dueDateEnd);

    List<Borrowing> findAllByStatusAndDueDateBefore(BorrowingStatus status, LocalDate dueDate);

    List<Borrowing> findAllByStatusAndReturnDateBetween(BorrowingStatus status, LocalDate returnDateStart,
            LocalDate returnDateEnd);

    // Helper Methods
    List<Borrowing> findByStatus(BorrowingStatus state);
}
