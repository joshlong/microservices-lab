package io.pivotal.microservices.recommendations;

import io.pivotal.microservices.recommendations.model.Likes;
import io.pivotal.microservices.recommendations.model.Movie;
import io.pivotal.microservices.recommendations.model.Person;
import io.pivotal.microservices.recommendations.repositories.LikesRepository;
import io.pivotal.microservices.recommendations.repositories.MovieRepository;
import io.pivotal.microservices.recommendations.repositories.PersonRepository;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableNeo4jRepositories(basePackages = "io.pivotal.microservices.recommendations.repositories")
@RestController
public class Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    GraphDatabaseService graphDatabaseService;
    @Autowired
    MovieRepository movieRepository;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    LikesRepository likesRepository;

    @Override
    public void run(String... strings) throws Exception {
        movieRepository.deleteAll();
        personRepository.deleteAll();
        likesRepository.deleteAll();
    }

    @RequestMapping(value = "/movies", method = RequestMethod.GET)
    public Iterable<Movie> movies() {
        return movieRepository.findAll();
    }

    @RequestMapping(value = "/movies", method = RequestMethod.POST)
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        movieRepository.save(movie);
        return new ResponseEntity<>(movie, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/people", method = RequestMethod.GET)
    public Iterable<Person> people() {
        return personRepository.findAll();
    }

    @RequestMapping(value = "/people", method = RequestMethod.POST)
    public ResponseEntity<Person> createPerson(@RequestBody Person person) {
        personRepository.save(person);
        return new ResponseEntity<>(person, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/likes", method = RequestMethod.GET)
    public Iterable<Likes> likes() {
        return likesRepository.findAll();
    }

    @RequestMapping(value = "/{userName}/likes/{mlId}", method = RequestMethod.POST)
    public ResponseEntity<Likes> createPersonMovieLink(@PathVariable String userName,
                                                       @PathVariable String mlId) {
        Person person = personRepository.findByUserName(userName);
        Movie movie = movieRepository.findByMlId(mlId);

        Likes likes = new Likes();
        likes.setPerson(person);
        likes.setMovie(movie);
        likesRepository.save(likes);

        return new ResponseEntity<>(likes, HttpStatus.CREATED);
    }

    @Autowired
    TransactionTemplate tpl;

    @RequestMapping(value = "/{userName}/recommendations", method = RequestMethod.GET)
    public Iterable<Movie> recommendedMoviesFor(@PathVariable String userName) {
        return tpl.execute(x -> movieRepository.recommendedMoviesFor(userName));
    }
}