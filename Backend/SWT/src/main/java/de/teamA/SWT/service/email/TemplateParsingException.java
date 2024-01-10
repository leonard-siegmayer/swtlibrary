package de.teamA.SWT.service.email;

public class TemplateParsingException extends RuntimeException {
    public TemplateParsingException() {
    }

    public TemplateParsingException(String s) {
        super(s);
    }

    public TemplateParsingException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public TemplateParsingException(Throwable throwable) {
        super(throwable);
    }

    public TemplateParsingException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
