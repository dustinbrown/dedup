package dedupe;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DedupeServiceTest {
    private final String jsonFileName = "fakeFileName";

    @RegisterExtension
    LogTrackerStub logTrackerStub = LogTrackerStub.create()
            .recordForLevel(LogTracker.LogLevel.INFO)
            .recordForType(DedupeService.class);

    @InjectMocks
    private DedupeService subject;

    @Mock
    private FileHelper fileHelper;

    @Test
    @DisplayName("3 source leads, 3 unique leads at INFO log level")
    public void threeUniqueLeads() {
        Account account1 = getAccount("apples", "1", 0);
        Account account2 = getAccount("bananas", "2", 0);
        Account account3 = getAccount("oranges", "3", 0);
        List<Account> sourceLeads = Arrays.asList(account1, account2, account3);
        when(fileHelper.getSourceLeadsFromFile(jsonFileName)).thenReturn(sourceLeads);
        subject.dedupeLeadsFromFile(jsonFileName);

        assertThat(logTrackerStub.size(), is(2));
        ILoggingEvent firstEvent = logTrackerStub.getLogEvents().get(0);
        ILoggingEvent secondEvent = logTrackerStub.getLogEvents().get(1);
        assertSourceLeadAccounts(firstEvent);
        assertThat(secondEvent.getFormattedMessage(), containsString("apples"));
        assertThat(secondEvent.getFormattedMessage(), containsString("bananas"));
        assertThat(secondEvent.getFormattedMessage(), containsString("oranges"));
    }

    @Test
    @DisplayName("3 source leads, 2 unique leads, INFO log level, duplicate id, same dates")
    public void duplicateIdSameDates() {
        Account account1 = getAccount("apples", "1", 0);
        Account account2 = getAccount("bananas", "2", 0);
        Account account3 = getAccount("oranges", "1", 0);
        List<Account> sourceLeads = Arrays.asList(account1, account2, account3);
        when(fileHelper.getSourceLeadsFromFile(jsonFileName)).thenReturn(sourceLeads);
        subject.dedupeLeadsFromFile(jsonFileName);

        assertThat(logTrackerStub.size(), is(4));
        ILoggingEvent firstEvent = logTrackerStub.getLogEvents().get(0);
        ILoggingEvent secondEvent = logTrackerStub.getLogEvents().get(1);
        ILoggingEvent thirdEvent = logTrackerStub.getLogEvents().get(2);
        ILoggingEvent fourthEvent = logTrackerStub.getLogEvents().get(3);
        assertSourceLeadAccounts(firstEvent);
        assertThat(secondEvent.getFormattedMessage(), is("duplicate detected: Account{id='1', emailAddress='oranges', firstName='firstoranges', lastName='lastoranges', streetAddress='adddressoranges', entryDate=Wed Dec 31 16:00:00 PST 1969}"));
        assertThat(thirdEvent.getFormattedMessage(), is("removing duplicate account: 'Account{id='1', emailAddress='apples', firstName='firstapples', lastName='lastapples', streetAddress='adddressapples', entryDate=Wed Dec 31 16:00:00 PST 1969}' Reason: entryDate is same and account last in source list"));
        assertThat(fourthEvent.getFormattedMessage(), containsString("bananas"));
        assertThat(fourthEvent.getFormattedMessage(), containsString("oranges"));
    }

    @Test
    @DisplayName("3 source leads, 2 unique leads, INFO log level, duplicate id, different dates natural order")
    public void duplicateIdDifferentDatesNaturalOrder() {
        Account account1 = getAccount("apples", "1", 0);
        Account account2 = getAccount("bananas", "2", 100);
        Account account3 = getAccount("oranges", "1", 1000);
        List<Account> sourceLeads = Arrays.asList(account1, account2, account3);
        when(fileHelper.getSourceLeadsFromFile(jsonFileName)).thenReturn(sourceLeads);
        subject.dedupeLeadsFromFile(jsonFileName);

        assertThat(logTrackerStub.size(), is(4));
        ILoggingEvent firstEvent = logTrackerStub.getLogEvents().get(0);
        ILoggingEvent secondEvent = logTrackerStub.getLogEvents().get(1);
        ILoggingEvent thirdEvent = logTrackerStub.getLogEvents().get(2);
        ILoggingEvent fourthEvent = logTrackerStub.getLogEvents().get(3);
        assertSourceLeadAccounts(firstEvent);
        assertThat(secondEvent.getFormattedMessage(), is("duplicate detected: Account{id='1', emailAddress='oranges', firstName='firstoranges', lastName='lastoranges', streetAddress='adddressoranges', entryDate=Wed Dec 31 16:16:40 PST 1969}"));
        assertThat(thirdEvent.getFormattedMessage(), is("removing duplicate account: 'Account{id='1', emailAddress='apples', firstName='firstapples', lastName='lastapples', streetAddress='adddressapples', entryDate=Wed Dec 31 16:00:00 PST 1969}' Reason: entryDate is older"));
        assertThat(fourthEvent.getFormattedMessage(), containsString("bananas"));
        assertThat(fourthEvent.getFormattedMessage(), containsString("oranges"));
    }

    @Test
    @DisplayName("3 source leads, 2 unique leads, INFO log level, duplicate id, different dates")
    public void duplicateIdDifferentDates() {
        Account account1 = getAccount("apples", "1", 1000);
        Account account2 = getAccount("bananas", "2", 100);
        Account account3 = getAccount("oranges", "1", 0);
        List<Account> sourceLeads = Arrays.asList(account1, account2, account3);
        when(fileHelper.getSourceLeadsFromFile(jsonFileName)).thenReturn(sourceLeads);
        subject.dedupeLeadsFromFile(jsonFileName);

        assertThat(logTrackerStub.size(), is(4));
        ILoggingEvent firstEvent = logTrackerStub.getLogEvents().get(0);
        ILoggingEvent secondEvent = logTrackerStub.getLogEvents().get(1);
        ILoggingEvent thirdEvent = logTrackerStub.getLogEvents().get(2);
        ILoggingEvent fourthEvent = logTrackerStub.getLogEvents().get(3);
        assertSourceLeadAccounts(firstEvent);
        assertThat(secondEvent.getFormattedMessage(), is("duplicate detected: Account{id='1', emailAddress='oranges', firstName='firstoranges', lastName='lastoranges', streetAddress='adddressoranges', entryDate=Wed Dec 31 16:00:00 PST 1969}"));
        assertThat(thirdEvent.getFormattedMessage(), is("removing duplicate account: 'Account{id='1', emailAddress='oranges', firstName='firstoranges', lastName='lastoranges', streetAddress='adddressoranges', entryDate=Wed Dec 31 16:00:00 PST 1969}' Reason: entryDate is older"));
        assertThat(fourthEvent.getFormattedMessage(), containsString("apples"));
        assertThat(fourthEvent.getFormattedMessage(), containsString("bananas"));
    }

    @Test
    @DisplayName("3 source leads, 2 unique leads, DEBUG log level, duplicate id, different dates natural order")
    public void duplicateIdDifferentDatesNaturalOrderDebug() {
        logTrackerStub.recordForLevel(LogTracker.LogLevel.DEBUG);
        Account account1 = getAccount("apples", "1", 0);
        Account account2 = getAccount("bananas", "2", 100);
        Account account3 = getAccount("oranges", "1", 1000);
        List<Account> sourceLeads = Arrays.asList(account1, account2, account3);
        when(fileHelper.getSourceLeadsFromFile(jsonFileName)).thenReturn(sourceLeads);
        subject.dedupeLeadsFromFile(jsonFileName);

        assertThat(logTrackerStub.size(), is(5));
        ILoggingEvent firstEvent = logTrackerStub.getLogEvents().get(0);
        ILoggingEvent secondEvent = logTrackerStub.getLogEvents().get(1);
        ILoggingEvent thirdEvent = logTrackerStub.getLogEvents().get(2);
        ILoggingEvent fourthEvent = logTrackerStub.getLogEvents().get(3);
        ILoggingEvent fifthEvent = logTrackerStub.getLogEvents().get(4);

        assertSourceLeadAccounts(firstEvent);
        assertThat(secondEvent.getFormattedMessage(), is("duplicate detected: Account{id='1', emailAddress='oranges', firstName='firstoranges', lastName='lastoranges', streetAddress='adddressoranges', entryDate=Wed Dec 31 16:16:40 PST 1969}"));
        assertThat(thirdEvent.getFormattedMessage(), is("removing duplicate account: 'Account{id='1', emailAddress='apples', firstName='firstapples', lastName='lastapples', streetAddress='adddressapples', entryDate=Wed Dec 31 16:00:00 PST 1969}' Reason: entryDate is older"));
        assertThat(fourthEvent.getFormattedMessage(), containsString("bananas"));
        assertThat(fourthEvent.getFormattedMessage(), containsString("oranges"));
        assertThat(fifthEvent.getFormattedMessage(), containsString("apples"));
    }

    @Test
    @DisplayName("4 source leads, 3 unique leads at INFO log level with null email")
    public void threeUniqueLeadsWithNullField() {
        Account account1 = getAccount("apples", "1", 0);
        Account account2 = getAccount("bananas", "2", 0);
        Account account3 = getAccount("oranges", "3", 0);
        Account account4 = getAccount(null, "1", 0);
        List<Account> sourceLeads = Arrays.asList(account1, account2, account3, account4);
        when(fileHelper.getSourceLeadsFromFile(jsonFileName)).thenReturn(sourceLeads);
        subject.dedupeLeadsFromFile(jsonFileName);

        assertThat(logTrackerStub.size(), is(3));
        ILoggingEvent firstEvent = logTrackerStub.getLogEvents().get(0);
        ILoggingEvent secondEvent = logTrackerStub.getLogEvents().get(1);
        ILoggingEvent thirdEvent = logTrackerStub.getLogEvents().get(2);
        assertSourceLeadAccounts(firstEvent);
        assertThat(secondEvent.getFormattedMessage(), containsString("skipping account with required fields set as null! Account{id='1', emailAddress='null', firstName='firstnull', lastName='lastnull', streetAddress='adddressnull', entryDate=Wed Dec 31 16:00:00 PST 1969}"));
        assertThat(thirdEvent.getFormattedMessage(), containsString("apples"));
        assertThat(thirdEvent.getFormattedMessage(), containsString("bananas"));
        assertThat(thirdEvent.getFormattedMessage(), containsString("oranges"));
    }

    private void assertSourceLeadAccounts(ILoggingEvent firstEvent) {
        assertThat(firstEvent.getFormattedMessage(), containsString("apples"));
        assertThat(firstEvent.getFormattedMessage(), containsString("bananas"));
        assertThat(firstEvent.getFormattedMessage(), containsString("oranges"));
    }

    private Account getAccount(String email, String id, int secondsAddedToEpoch) {
        return new Account(id,
                email,
                "first" + email,
                "last" + email,
                "adddress" + email,
                Date.from(Instant.EPOCH.plusSeconds(secondsAddedToEpoch))
        );
    }
}