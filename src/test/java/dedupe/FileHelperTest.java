package dedupe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class FileHelperTest {
    @InjectMocks
    private FileHelper subject;

    @Test
    @DisplayName("File not found exception")
    public void fileNotFound() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> subject.getSourceLeadsFromFile("fakeFile"));
        assertThat(thrown.getMessage(), is("File not found: fakeFile"));
    }

    @Test
    @DisplayName("json parsing exception")
    public void JsonParseException() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> subject.getSourceLeadsFromFile("leadsParsing.json"));
        assertThat(thrown.getMessage(), is("JSON file parsing exception with file: leadsParsing.json"));
    }

    @Test
    @DisplayName("json mapping exception")
    public void JsonMappingException() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> subject.getSourceLeadsFromFile("leadsMapping.json"));
        assertThat(thrown.getMessage(), is("JSON file mapping exception with file: leadsMapping.json"));
    }

    @Test
    @DisplayName("load json file without errors")
    public void happyPath() {
        List<Account> sourceLeadsFromFile = subject.getSourceLeadsFromFile("leads.json");
        assertThat(sourceLeadsFromFile, hasSize(11));
    }
}