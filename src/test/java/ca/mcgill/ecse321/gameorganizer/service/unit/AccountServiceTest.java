package ca.mcgill.ecse321.gameorganizer.service.unit;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    private Account testAccount;

    // -- SETUP -- //

    // -- TEAR DOWN -- //

    // -- createAccount -- //

    @Test
    public void testCreateAccountSuccess() {}

    @Test
    public void testCreateGameOwnerSuccess() {}

    @Test
    public void testCreateAccountSuccessMultipleMixedAccounts() {}

    @Test
    public void testCreateAccountFailOnDuplicateAccounts() {}

    @Test
    public void testCreateAccountFailOnMissingFields() {}

    // -- updateAccount -- //

    @Test
    public void testUpdateAccountSuccess() {}

    @Test
    public void testUpdateGameOwnerSuccess() {}

    @Test
    public void testUpdateAccountSuccessNoNewPassword() {}

    @Test
    public void testUpdateGameOwnerSuccessNoNewPassword() {}

    @Test
    public void testUpdateAccountFailOnDuplicateEmail() {}

    @Test
    public void testUpdateAccountFailOnNonExistentAccount() {}

    @Test
    public void testUpdateAccountFailMissingFields() {}

    @Test
    public void testUpdateAccountFailWrongPassword() {}

    // -- deleteAccountByEmail -- //

    @Test
    public void testDeleteAccountSuccess() {}

    @Test
    public void testDeleteGameOwnerSuccess() {}

    @Test
    public void testDeleteAccountFail() {}

    @Test
    public void testDeleteGameOwnerFail() {}

    // -- upgradeUserToGameOwner -- //

    @Test
    public void testUpgradeSuccess() {}

    @Test
    public void testUpgradeSuccess2() {}

    @Test
    public void testUpgradeSuccess3() {}

    @Test
    public void testUpgradeFailUserDNE() {}

    @Test
    public void testUpgradeFailUserAlreadyGameOwner () {}

    // -- getAccountInfoByEmail -- //

    @Test
    public void testGetAccountSuccess() {}

    @Test
    public void testGetGameOwnerSuccess() {}

    @Test
    public void testGetAccountFailUserDNE() {}

}
