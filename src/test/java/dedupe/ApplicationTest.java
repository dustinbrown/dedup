package dedupe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ApplicationTest {
    @InjectMocks
    private Application application;

    @Mock
    private DedupeService dedupeService;

    @Test
    @DisplayName("test more than one argument throws exception")
    public void mainThrowsException() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> application.run("fakeFile", "fakeFile2"));
        assertThat(thrown.getMessage(), is("Invalid command line argument! Expected execution 'mvn --batch-mode -q spring-boot:run -Dspring-boot.run.arguments=<nameOfJsonFile>'"));
    }
}