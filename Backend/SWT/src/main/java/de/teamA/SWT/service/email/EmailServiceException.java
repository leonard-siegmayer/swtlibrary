package de.teamA.SWT.service.email;

public class EmailServiceException extends Exception {
    public EmailServiceException() {
    }

    public EmailServiceException(String s) {
        super(s);
    }

    public EmailServiceException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public EmailServiceException(Throwable throwable) {
        super(throwable);
    }

    public EmailServiceException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
