package de.teamA.SWT.service.email;

import java.util.List;

/**
 * The generic EmailTemplate for all mails defined in /templates/emails.
 * <p>
 * Attributes corresponding to template values. Note: MUST be of type String or
 * List&lt;String&gt;!
 * <p>
 * You can add attributes as much as you like, get an EmailTemplate Object
 * through TemplateManager#getTemplate(String templateName) and fill them e.g.
 * in a method of the EmailService.
 */
public class EmailTemplate extends AbstractEmailTemplate {
    protected String adminMail;
    protected String user;
    protected String userMail;
    protected String staffMember;
    protected String staffMemberMail;
    protected String overdueDays;
    protected String title;
    protected String physical;
    protected List<String> borrowedMedia;
    protected List<String> wishList;

    protected String signature;

    protected EmailTemplate(String subjectText, List<String> templateText) {
        super(subjectText, templateText);
    }

    public String getAdminMail() {
        return adminMail;
    }

    public void setAdminMail(String adminMail) {
        this.adminMail = adminMail;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public String getStaffMember() {
        return staffMember;
    }

    public void setStaffMember(String staffMember) {
        this.staffMember = staffMember;
    }

    public String getStaffMemberMail() {
        return staffMemberMail;
    }

    public void setStaffMemberMail(String staffMemberMail) {
        this.staffMemberMail = staffMemberMail;
    }

    public String getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(String overdueDays) {
        this.overdueDays = overdueDays;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhysical() {
        return physical;
    }

    public void setPhysical(String physical) {
        this.physical = physical;
    }

    public List<String> getBorrowedMedia() {
        return borrowedMedia;
    }

    public void setBorrowedMedia(List<String> borrowedMedia) {
        this.borrowedMedia = borrowedMedia;
    }

    public List<String> getWishList() {
        return wishList;
    }

    public void setWishList(List<String> wishList) {
        this.wishList = wishList;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

}
