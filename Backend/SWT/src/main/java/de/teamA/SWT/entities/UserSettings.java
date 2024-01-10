package de.teamA.SWT.entities;

import javax.persistence.*;

@Entity
@Table(name = "userSettings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "returnInfoOwner", columnDefinition = "Boolean default true")
    private Boolean getReturnInfoAsOwner;

    @Column(name = "returnInfoBorrower", columnDefinition = "Boolean default true")
    private Boolean getReturnInfoAsBorrower;

    @Column(name = "borrowInfoOwner", columnDefinition = "Boolean default true")
    private Boolean getBorrowInfoAsOwner;

    @Column(name = "borrowInfoBorrower", columnDefinition = "Boolean default true")
    private Boolean getBorrowInfoAsBorrower;

    @Column(name = "expireInfoBorrower", columnDefinition = "Boolean default true")
    private Boolean getExpireInfoAsBorrower;

    @Column(name = "overdueInfoBorrower", columnDefinition = "Boolean default true")
    private Boolean getOverdueInfoAsBorrower;

    @Column(name = "overdueInfoOwner", columnDefinition = "Boolean default true")
    private Boolean getOverdueInfoAsOwner;

    @Column(name = "extensionInfoBorrower", columnDefinition = "Boolean default true")
    private Boolean getExtensionInfoAsBorrower;

    @Column(name = "extensionInfoOwner", columnDefinition = "Boolean default true")
    private Boolean getExtensionInfoAsOwner;

    @Column(name = "reservationInfo", columnDefinition = "Boolean default true")
    private Boolean getReservationInfo;

    @Column(name = "missedReservationInfo", columnDefinition = "Boolean default true")
    private Boolean getMissedReservationInfo;

    /*
     * generates UserSettings Object with default values
     */
    public UserSettings() {
        this.getBorrowInfoAsBorrower = true;
        this.getBorrowInfoAsOwner = true;
        this.getReturnInfoAsBorrower = true;
        this.getReturnInfoAsOwner = true;
        this.getExtensionInfoAsBorrower = true;
        this.getExtensionInfoAsOwner = true;
        this.getOverdueInfoAsBorrower = true;
        this.getOverdueInfoAsOwner = true;
        this.getExpireInfoAsBorrower = true;
        this.getReservationInfo = true;
        this.getMissedReservationInfo = true;
    }

    public UserSettings(Boolean getReturnInfoAsOwner, Boolean getReturnInfoAsBorrower, Boolean getBorrowInfoAsOwner,
            Boolean getBorrowInfoAsBorrower, Boolean getExpireInfoAsBorrower, Boolean getOverdueInfoAsBorrower,
            Boolean getOverdueInfoAsOwner, Boolean getExtensionInfoAsBorrower, Boolean getExtensionInfoAsOwner,
            Boolean getReservationInfo, Boolean getMissedReservationInfo) {
        this.getReturnInfoAsOwner = getReturnInfoAsOwner;
        this.getReturnInfoAsBorrower = getReturnInfoAsBorrower;
        this.getBorrowInfoAsOwner = getBorrowInfoAsOwner;
        this.getBorrowInfoAsBorrower = getBorrowInfoAsBorrower;
        this.getExpireInfoAsBorrower = getExpireInfoAsBorrower;
        this.getOverdueInfoAsBorrower = getOverdueInfoAsBorrower;
        this.getOverdueInfoAsOwner = getOverdueInfoAsOwner;
        this.getExtensionInfoAsBorrower = getExtensionInfoAsBorrower;
        this.getExtensionInfoAsOwner = getExtensionInfoAsOwner;
        this.getReservationInfo = getReservationInfo;
        this.getMissedReservationInfo = getMissedReservationInfo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean getGetReturnInfoAsOwner() {
        return getReturnInfoAsOwner;
    }

    public void setGetReturnInfoAsOwner(Boolean getReturnInfoAsOwner) {
        this.getReturnInfoAsOwner = getReturnInfoAsOwner;
    }

    public Boolean getGetReturnInfoAsBorrower() {
        return getReturnInfoAsBorrower;
    }

    public void setGetReturnInfoAsBorrower(Boolean getReturnInfoAsBorrower) {
        this.getReturnInfoAsBorrower = getReturnInfoAsBorrower;
    }

    public Boolean getGetBorrowInfoAsOwner() {
        return getBorrowInfoAsOwner;
    }

    public void setGetBorrowInfoAsOwner(Boolean getBorrowInfoAsOwner) {
        this.getBorrowInfoAsOwner = getBorrowInfoAsOwner;
    }

    public Boolean getGetBorrowInfoAsBorrower() {
        return getBorrowInfoAsBorrower;
    }

    public void setGetBorrowInfoAsBorrower(Boolean getBorrowInfoAsBorrower) {
        this.getBorrowInfoAsBorrower = getBorrowInfoAsBorrower;
    }

    public Boolean getGetExpireInfoAsBorrower() {
        return getExpireInfoAsBorrower;
    }

    public void setGetExpireInfoAsBorrower(Boolean getExpireInfoAsBorrower) {
        this.getExpireInfoAsBorrower = getExpireInfoAsBorrower;
    }

    public Boolean getGetOverdueInfoAsBorrower() {
        return getOverdueInfoAsBorrower;
    }

    public void setGetOverdueInfoAsBorrower(Boolean getOverdueInfoAsBorrower) {
        this.getOverdueInfoAsBorrower = getOverdueInfoAsBorrower;
    }

    public Boolean getGetOverdueInfoAsOwner() {
        return getOverdueInfoAsOwner;
    }

    public void setGetOverdueInfoAsOwner(Boolean getOverdueInfoAsOwner) {
        this.getOverdueInfoAsOwner = getOverdueInfoAsOwner;
    }

    public Boolean getGetExtensionInfoAsBorrower() {
        return getExtensionInfoAsBorrower;
    }

    public void setGetExtensionInfoAsBorrower(Boolean getExtensionInfoAsBorrower) {
        this.getExtensionInfoAsBorrower = getExtensionInfoAsBorrower;
    }

    public Boolean getGetExtensionInfoAsOwner() {
        return getExtensionInfoAsOwner;
    }

    public void setGetExtensionInfoAsOwner(Boolean getExtensionInfoAsOwner) {
        this.getExtensionInfoAsOwner = getExtensionInfoAsOwner;
    }

    public Boolean isGetReservationInfo() {
        return getReservationInfo;
    }

    public void setGetReservationInfo(Boolean getReservationInfo) {
        this.getReservationInfo = getReservationInfo;
    }

    public Boolean getGetMissedReservationInfo() {
        return getMissedReservationInfo;
    }

    public void setGetMissedReservationInfo(Boolean getMissedReservationInfo) {
        this.getMissedReservationInfo = getMissedReservationInfo;
    }

    @Override
    public String toString() {
        return "UserSettings [getBorrowInfoAsBorrower=" + getBorrowInfoAsBorrower + ", getBorrowInfoAsOwner="
                + getBorrowInfoAsOwner + ", getExpireInfoAsBorrower=" + getExpireInfoAsBorrower
                + ", getExtensionInfoAsBorrower=" + getExtensionInfoAsBorrower + ", getExtensionInfoAsOwner="
                + getExtensionInfoAsOwner + ", getOverdueInfoAsBorrower=" + getOverdueInfoAsBorrower
                + ", getOverdueInfoAsOwner=" + getOverdueInfoAsOwner + ", getReservationInfo=" + getReservationInfo
                + ", getMissedReservationInfo=" + getMissedReservationInfo + ", getReturnInfoAsBorrower="
                + getReturnInfoAsBorrower + ", getReturnInfoAsOwner=" + getReturnInfoAsOwner + ", id=" + id + "]";
    }
}
