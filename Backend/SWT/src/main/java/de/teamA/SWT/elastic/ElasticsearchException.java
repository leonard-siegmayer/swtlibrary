package de.teamA.SWT.elastic;

public class ElasticsearchException extends Exception {

    public ElasticsearchException() {
    }

    public ElasticsearchException(String message) {
        super(message);
    }

    public ElasticsearchException(Throwable cause) {
        super(cause);
    }

    public ElasticsearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticsearchException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
