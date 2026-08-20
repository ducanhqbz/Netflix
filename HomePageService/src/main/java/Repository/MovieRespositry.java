package Repository;

import entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRespositry extends JpaRepository<Movie, Long> {
    @Query("SELECT e FROM Movie e WHERE e.title LIKE %:title%")
    List<Movie> findByTitle(@Param("title") String title);

 public    List<Movie> findAll();

}
