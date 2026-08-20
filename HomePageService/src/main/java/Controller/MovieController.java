package Controller;

import Repository.MovieRespositry;
import entity.Movie;
import jakarta.annotation.security.PermitAll;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import Error.MovieError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/home")
@RestController
@AllArgsConstructor
public class MovieController {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String MOVIES_KEY = "movies";
    private final MovieRespositry movieRepository;
    List<Movie> dbMovies = new ArrayList<>();
    List<Object> redisObjects = new ArrayList<>();

    @GetMapping("/movies")
    @PermitAll
    public List<Movie> getMovies() {

        dbMovies = movieRepository.findAll();
        if (dbMovies.isEmpty()) {
            redisTemplate.opsForList().remove(MOVIES_KEY, 0, null);
            throw new MovieError(HttpStatus.NOT_FOUND, "not founnd any move ");
        }

        redisObjects = redisTemplate
                .opsForList()
                .range(MOVIES_KEY, 0, -1);

        // Redis chưa có dữ liệu
        if (redisObjects == null || redisObjects.isEmpty()) {

            redisTemplate.opsForList()
                    .rightPushAll(MOVIES_KEY, dbMovies.toArray());

            return dbMovies;
        }

        // Đồng bộ Redis với MySQL
        syncMovies(dbMovies, redisObjects);

        return dbMovies;
    }

    public List<Movie> loadMoviesToRedis() {

        List<Movie> movies = movieRepository.findAll();

        // Xóa dữ liệu cũ trong Redis
        redisTemplate.delete(MOVIES_KEY);

        // Push toàn bộ movie vào Redis List
        redisTemplate.opsForList()
                .rightPushAll(MOVIES_KEY, movies.toArray());

        return movies;
    }

    private void syncMovies(
            List<Movie> dbMovies,
            List<Object> redisObjects
    ) {

        Map<Long, Movie> dbMovieMap = dbMovies.stream()
                .collect(Collectors.toMap(
                        Movie::getId,
                        movie -> movie
                ));

        Map<Long, Movie> redisMovieMap = redisObjects.stream()
                .map(obj -> (Movie) obj)
                .collect(Collectors.toMap(
                        Movie::getId,
                        movie -> movie
                ));

        // 1. MySQL có movie mới hoặc movie thay đổi
        for (Movie dbMovie : dbMovies) {

            Movie redisMovie = redisMovieMap.get(dbMovie.getId());

            // Movie chưa tồn tại trong Redis
            if (redisMovie == null) {

                redisTemplate.opsForList()
                        .rightPush(MOVIES_KEY, dbMovie);

            }
            // Movie đã tồn tại nhưng bị thay đổi
            else if (!dbMovie.equals(redisMovie)) {

                int index = findMovieIndex(
                        redisObjects,
                        dbMovie.getId()
                );

                redisTemplate.opsForList()
                        .set(MOVIES_KEY, index, dbMovie);
            }
        }

        // 2. Redis có movie nhưng MySQL không còn
        for (Movie redisMovie : redisMovieMap.values()) {

            if (!dbMovieMap.containsKey(redisMovie.getId())) {

                redisTemplate.opsForList()
                        .remove(MOVIES_KEY, 1, redisMovie);
            }
        }
    }

    private int findMovieIndex(
            List<Object> redisObjects,
            Long movieId
    ) {

        for (int i = 0; i < redisObjects.size(); i++) {

            Movie movie = (Movie) redisObjects.get(i);

            if (movie.getId().equals(movieId)) {
                return i;
            }
        }

        return -1;
    }


    @GetMapping("/getMovies/{name}")
    private List<Movie> getMovies(@PathVariable String name) {
        List<Movie> movies = movieRepository.findByTitle(name);

        if (movies.isEmpty()) {
            throw new MovieError(HttpStatus.NOT_FOUND, "not founnd any move ");
        }

        return movies;
    }
}
