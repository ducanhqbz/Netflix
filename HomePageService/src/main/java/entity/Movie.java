package entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String posterUrl;

    private String backdropUrl;

    private String trailerUrl;

    private String genre;

    private Integer releaseYear;

    private Double rating;

    private Integer duration;

    private String country;

    private Boolean featured;
}