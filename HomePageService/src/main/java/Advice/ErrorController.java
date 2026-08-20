package Advice;

import Error.MovieError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorController {
    @ExceptionHandler(MovieError.class)
    public MovieError handle(MovieError e) {
        return MovieError.builder()
                .status(e.getStatus())
                .message(e.getMessage())
                .build();
    }
}
