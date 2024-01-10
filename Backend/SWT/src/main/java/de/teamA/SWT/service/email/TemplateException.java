package de.teamA.SWT.service.email;

/**
 * Exception throw when interacting with AbstractEmailTemplate or EmailTemplate
 */
public class TemplateException extends Exception {
    public TemplateException() {
    }

    public TemplateException(String s) {
        super(s);
    }

    public TemplateException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public TemplateException(Throwable throwable) {
        super(throwable);
    }

    public TemplateException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
