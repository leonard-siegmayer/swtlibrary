package de.teamA.SWT.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "borrowing")
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User borrower;

    @ManyToOne
    @JoinColumn(name = "medium_id")
    private PhysicalMedium medium;

    @Column(name = "date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate borrowDate;

    @Column(name = "due_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate dueDate;

    @Column(name = "return_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate returnDate = null;

    @Enumerated(EnumType.STRING)
    private BorrowingStatus status;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User responsibleStaff;

    public Borrowing() {
    }

    public Borrowing(User borrower, PhysicalMedium medium, LocalDate dueDate) {
        this.borrowDate = LocalDate.now();
        this.borrower = borrower;
        this.medium = medium;
        this.dueDate = dueDate;
        status = BorrowingStatus.BORROWED;

    }

    public Borrowing(User borrower, PhysicalMedium medium, LocalDate dueDate, boolean isStudent) {
        this.borrowDate = LocalDate.now();
        this.borrower = borrower;
        this.medium = medium;
        this.dueDate = dueDate;

        if (isStudent) {
            status = BorrowingStatus.REQUESTED;
        } else {
            status = BorrowingStatus.BORROWED;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public User getBorrower() {
        return borrower;
    }

    public void setBorrower(User borrower) {
        this.borrower = borrower;
    }

    @JsonIgnore
    public PhysicalMedium getMedium() {
        return medium;
    }

    public void setMedium(PhysicalMedium medium) {
        this.medium = medium;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate date) {
        returnDate = date;
    }

    public BorrowingStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowingStatus status) {
        this.status = status;
    }

    public PhysicalMedium getPhysical() {
        return medium;
    }

    public void accept(User staff, LocalDate dueDate) {
        this.status = BorrowingStatus.BORROWED;
        this.dueDate = dueDate;
        this.responsibleStaff = staff;
    }

    public void decline(User staff) {
        LocalDate dueDate = LocalDate.MIN;

        this.status = BorrowingStatus.DECLINED;
        this.dueDate = dueDate;
        this.returnDate = dueDate;
        this.responsibleStaff = staff;

    }

    public User getResponsibleStaff() {
        return responsibleStaff;
    }

    public void setResponsibleStaff(User responsibleStaff) {
        this.responsibleStaff = responsibleStaff;
    }

}
