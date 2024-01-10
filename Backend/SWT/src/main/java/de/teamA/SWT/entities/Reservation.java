package de.teamA.SWT.entities;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "purpose")
    private String purpose;

    // a low value equals a high priority; 1 is the highest priority
    @Column(name = "priority")
    public int priority;

    @ManyToOne
    @JoinColumn(name = "user")
    private User user;

    @Column(name = "physicalID")
    private Long physicalID;

    @Column(name = "logicalID")
    private Long logicalID;

    @Column(name = "date")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    @Column(name = "borrowDate")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime borrowDate;

    @Column(name = "rank")
    private String rank;

    @Column(name = "ready")
    private Boolean readyToBorrow = false;

    // is true if it's not reserved anymore (for topFlopStatistics)
    @Column(name = "borrowed")
    private Boolean borrowed = false;

    @Column(name = "requiredTime")
    private int requiredTime;

    public Reservation() {
        this.date = LocalDateTime.now();
        this.readyToBorrow = false;
    }

    public Reservation(String purpose, User user, int priority, Long physicalID, Long logicalID) {
        this.purpose = purpose;
        this.user = user;
        this.priority = priority;
        this.date = LocalDateTime.now();
        this.physicalID = physicalID;
        this.readyToBorrow = false;
        this.logicalID = logicalID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public long getPhysicalID() {
        return physicalID;
    }

    public void setPhysicalID(Long physicalID) {
        this.physicalID = physicalID;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Boolean isReadyToBorrow() {
        return readyToBorrow;
    }

    public void setReadyToBorrow(Boolean readyToBorrow) {
        this.readyToBorrow = readyToBorrow;
    }

    public int getRequiredTime() {
        return requiredTime;
    }

    public void setRequiredTime(int requiredTime) {
        this.requiredTime = requiredTime;
    }

    public Long getLogicalID() {
        return logicalID;
    }

    public void setLogicalID(Long logicalID) {
        this.logicalID = logicalID;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public Boolean getBorrowed() {
        return borrowed;
    }

    public void setBorrowed(Boolean borrowed) {
        this.borrowed = borrowed;
    }

    public boolean hasMaxRank() {
        return this.rank != null ? this.rank.equals("1") : false;
    }

    /**
     * Updates the rank of this reservation, if another reservation of the same
     * physical changed.
     * 
     * @param oldRank the original rank of the reservation which rank changed
     * @param newRank the new rank of the reservation. Set it to 0 if the
     *                reservation is deleted.
     * @param delete  true if the rank has not been changed, but the reservation was
     *                completely deleted
     */
    public void updateRank(int oldRank, int newRank, boolean delete) {
        int rank = Integer.parseInt(this.getRank());
        if (rank == oldRank) {
            if (newRank == 0) {
                this.setRank(null);
            } else {
                this.setRank(Integer.toString(newRank));
            }
        } else if (delete || newRank == 0) {
            if (rank > oldRank) {
                this.setRank(Integer.toString(rank - 1));
            }
        } else if (oldRank < newRank) {
            if (rank > oldRank && rank <= newRank) {
                this.setRank(Integer.toString(rank - 1));
            }
        } else if (newRank < oldRank) {
            if (rank < oldRank && rank >= newRank) {
                this.setRank(Integer.toString(rank + 1));
            }
        }
    }

    public boolean isRankSet() {
        return this.getRank() == null;
    }

    @Override
    public String toString() {
        return "Reservation [date=" + date + ", id=" + id + ", logicalID=" + logicalID + ", physicalID=" + physicalID
                + ", priority=" + priority + ", purpose=" + purpose + ", rank=" + rank + ", readyToBorrow="
                + readyToBorrow + ", requiredTime=" + requiredTime + ", user=" + user + "]";
    }

}
