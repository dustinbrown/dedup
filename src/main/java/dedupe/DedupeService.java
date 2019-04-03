package dedupe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DedupeService {
    private static final Logger logger = LoggerFactory.getLogger(DedupeService.class);
    private static final String ENTRY_IS_OLDER_REASON = "entryDate is older";
    private static final String SAME_DATE_REASON = "entryDate is same and account last in source list";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private FileHelper fileHelper;

    public DedupeService() {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }

    public void dedupeLeadsFromFile(String jsonFileName) {
        List<Account> sourceLeads = fileHelper.getSourceLeadsFromFile(jsonFileName);
        logAccounts(sourceLeads, "Source Records:");
        List<Account> uniqueLeadAccounts = getUniqueLeadAccounts(sourceLeads);
        logAccounts(uniqueLeadAccounts, "Unique Records:");
        displayDuplicateLeadAccountsIfDebug(sourceLeads, uniqueLeadAccounts);
    }

    private void displayDuplicateLeadAccountsIfDebug(List<Account> sourceLeads, List<Account> uniqueLeadAccounts)  {
        if (logger.isDebugEnabled()) {
            List<Account> duplicateAccounts = sourceLeads.stream()
                    .filter(account -> !uniqueLeadAccounts.contains(account))
                    .collect(Collectors.toList());
                logger.debug("Duplicate accounts: {}", jsonPrettyPrint(duplicateAccounts));
        }
    }

    private List<Account> getUniqueLeadAccounts(List<Account> sourceLeads) {
        List<Account> uniqueLeadAccounts = new LinkedList<>(); // In favor of ArrayList due to expected list mutation
        for (Account account : sourceLeads) {
            if (account.getId() == null ||
                    account.getEmailAddress() == null ||
                    account.getEntryDate() == null) {
                logger.error("skipping account with required fields set as null! {}", account);
                continue;
            }
            addToOrUpdateUniqueLeadAccounts(uniqueLeadAccounts, account);
        }
        return uniqueLeadAccounts;
    }

    private void addToOrUpdateUniqueLeadAccounts(List<Account> uniqueLeadAccounts, Account account) {
        Optional<Account> duplicate = getDuplicateAccountIfPresent(uniqueLeadAccounts, account);
        if (duplicate.isPresent()) {
            Account duplicateAccount = duplicate.get();
            logger.info("duplicate detected: " + account);
            replaceDuplicateAccountForReason(uniqueLeadAccounts, account, duplicateAccount);
        } else {
            uniqueLeadAccounts.add(account);
        }
    }

    private void replaceDuplicateAccountForReason(List<Account> uniqueLeadAccounts, Account account, Account duplicateAccount) {
        if (duplicateAccountIsOlder(account, duplicateAccount)) {
            displayDuplicateReason(duplicateAccount, ENTRY_IS_OLDER_REASON);
            replaceDuplicateAccount(uniqueLeadAccounts, account, duplicateAccount);
        } else if (duplicateAccountHasSameDate(account, duplicateAccount)) {
            displayDuplicateReason(duplicateAccount, SAME_DATE_REASON);
            replaceDuplicateAccount(uniqueLeadAccounts, account, duplicateAccount);
        } else {
            displayDuplicateReason(account, ENTRY_IS_OLDER_REASON);
            // No need to replace as the 'account' is the older account and as such does not get added to uniqueLeadAccounts
        }
    }

    private boolean duplicateAccountHasSameDate(Account account, Account duplicateAccount) {
        return duplicateAccount.getEntryDate().equals(account.getEntryDate());
    }

    private boolean duplicateAccountIsOlder(Account account, Account duplicateAccount) {
        return duplicateAccount.getEntryDate().before(account.getEntryDate());
    }

    private Optional<Account> getDuplicateAccountIfPresent(List<Account> uniqueLeadAccounts, Account account) {
        return uniqueLeadAccounts.stream()
                .filter(uniqueAccount -> duplicateIdOrEmailAddressFound(account, uniqueAccount))
                .findFirst();
    }

    private boolean duplicateIdOrEmailAddressFound(Account account, Account uniqueAccount) {
        return Objects.equals(account.getId(), uniqueAccount.getId()) ||
                Objects.equals(account.getEmailAddress(), uniqueAccount.getEmailAddress());
    }

    private void displayDuplicateReason(Account duplicateAccount, String reason) {
        logger.info("removing duplicate account: '" + duplicateAccount + "' Reason: " + reason);
    }

    private void replaceDuplicateAccount(List<Account> uniqueLeadAccounts, Account account, Account duplicateAccount) {
        uniqueLeadAccounts.remove(duplicateAccount);
        uniqueLeadAccounts.add(account);
    }

    private void logAccounts(List<Account> accounts, String headerMessage) {
        logger.info(headerMessage + " {}", jsonPrettyPrint(accounts));
    }

    private String jsonPrettyPrint(List<Account> accounts) {
        try {

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(accounts);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Unknown json processing exception", exception.getCause());
        }
    }
}
