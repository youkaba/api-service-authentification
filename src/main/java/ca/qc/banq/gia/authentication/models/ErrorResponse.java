package ca.qc.banq.gia.authentication.models;

public record ErrorResponse(boolean success, String message) {

    public ErrorResponse(String message) {
        this(false, message);
    }
}
