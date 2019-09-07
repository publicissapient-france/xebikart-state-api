package fr.xebia.xebicon.xebikart.api.infra.http.endpoint.sse;

public class SSEClientException extends Exception {

    public SSEClientException() {
        super();
    }

    public SSEClientException(String message) {
        super(message);
    }

    public SSEClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SSEClientException(Throwable cause) {
        super(cause);
    }

    protected SSEClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
