package Error;

import lombok.*;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovieError extends RuntimeException {
    private HttpStatus status;

    private String message;



}
