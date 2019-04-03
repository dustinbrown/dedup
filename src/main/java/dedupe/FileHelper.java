package dedupe;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class FileHelper {
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Account> getSourceLeadsFromFile(String jsonFileName) {
        try {
            File jsonFile = new File(jsonFileName);
            Leads leads = mapper.readValue(jsonFile, Leads.class);
            return leads.getLeads();
        } catch (JsonParseException exception) {
            throw new RuntimeException("JSON file parsing exception with file: " + jsonFileName, exception);
        } catch (JsonMappingException exception) {
            throw new RuntimeException("JSON file mapping exception with file: " + jsonFileName, exception);
        } catch (IOException exception) {
            throw new RuntimeException("File not found: " + jsonFileName);
        }
    }
}
