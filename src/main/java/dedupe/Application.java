package dedupe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.lang.System.exit;

@SpringBootApplication
public class Application implements CommandLineRunner {
    @Autowired
    private DedupeService dedupeService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        assertExpectedCommandLineArguments(args);
        dedupeService.dedupeLeadsFromFile(args[0]);
        exit(0);
    }

    private void assertExpectedCommandLineArguments(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("Invalid command line argument! Expected execution 'mvn --batch-mode -q spring-boot:run -Dspring-boot.run.arguments=<nameOfJsonFile>'");
        }
    }
}
