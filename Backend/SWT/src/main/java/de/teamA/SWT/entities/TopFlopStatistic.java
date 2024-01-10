package de.teamA.SWT.entities;

public class TopFlopStatistic {

    private Medium medium;
    private int physicals;
    private int resNumb;
    private int borrowNumb;
    private double averageWaitingTime;

    public TopFlopStatistic(Medium medium, int physicals, int resNumb, int borrowNumb) {
        this.medium = medium;
        this.physicals = physicals;
        this.resNumb = resNumb;
        this.borrowNumb = borrowNumb;
    }

    public Medium getMedium() {
        return medium;
    }

    public void setMedium(Medium medium) {
        this.medium = medium;
    }

    public int getPhysicals() {
        return physicals;
    }

    public void setPhysicals(int physicals) {
        this.physicals = physicals;
    }

    public int getResNumb() {
        return resNumb;
    }

    public void setResNumb(int resNumb) {
        this.resNumb = resNumb;
    }

    public int getBorrowNumb() {
        return borrowNumb;
    }

    public void setBorrowNumb(int borrowNumb) {
        this.borrowNumb = borrowNumb;
    }

    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public void setAverageWaitingTime(double averageWaitingTime) {
        this.averageWaitingTime = averageWaitingTime;
    }

}
