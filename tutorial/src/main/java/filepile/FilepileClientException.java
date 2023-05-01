package filepile;

import lombok.Getter;

public class FilepileClientException extends Exception {

    @Getter private final int code;

    public FilepileClientException(final int code, final String message) {
        super(message);
        this.code = code;
    }
}
