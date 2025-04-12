package by.onlinebanking.service;

import by.onlinebanking.dto.account.AccountDto;
import by.onlinebanking.dto.transaction.SingleAccountTransactionDto;
import by.onlinebanking.dto.response.TransactionResponseDto;
import by.onlinebanking.dto.transaction.TransferTransactionDto;
import by.onlinebanking.exception.BusinessException;
import by.onlinebanking.exception.NotFoundException;
import by.onlinebanking.model.Account;
import by.onlinebanking.model.User;
import by.onlinebanking.model.enums.AccountStatus;
import by.onlinebanking.model.enums.Currency;
import by.onlinebanking.model.enums.TransactionType;
import by.onlinebanking.repository.AccountRepository;
import by.onlinebanking.repository.UserRepository;
import by.onlinebanking.validation.TransactionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionValidator transactionValidator;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setIban("TESTIBAN123");
        testAccount.setBalance(BigDecimal.valueOf(1000));
        testAccount.setCurrency(Currency.USD);
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setUser(testUser);
    }

    @Test
    void getAccountsByUserId_ReturnsAccounts() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(testAccount));

        List<AccountDto> result = accountService.getAccountsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals("TESTIBAN123", result.get(0).getIban());
    }

    @Test
    void getAccountsByUserId_NoAccounts_ThrowsException() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> accountService.getAccountsByUserId(1L));

        assertEquals("No accounts found for user", exception.getMessage());
    }

    @Test
    void createAccount_CreatesSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountDto result = accountService.createAccount(1L, Currency.USD);

        assertNotNull(result);
        assertEquals("TESTIBAN123", result.getIban());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> accountService.createAccount(1L, Currency.USD));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void closeAccount_ClosesSuccessfully() {
        testAccount.setBalance(BigDecimal.ZERO);
        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        TransactionResponseDto response = accountService.closeAccount("TESTIBAN123");

        assertEquals(AccountStatus.CLOSED, testAccount.getStatus());
        assertEquals("Account is closed", response.getMessage());
    }

    @Test
    void closeAccount_AccountNotFound_ThrowsNotFoundException() {
        when(accountRepository.findByIban("NONEXISTENT_IBAN")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> accountService.closeAccount("NONEXISTENT_IBAN"));

        assertEquals("Account not found", exception.getMessage());
        assertEquals("NONEXISTENT_IBAN", exception.getDetails().get("iban"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void closeAccount_AlreadyClosed_ThrowsException() {
        testAccount.setStatus(AccountStatus.CLOSED);
        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> accountService.closeAccount("TESTIBAN123"));

        assertEquals("Account is already closed", exception.getMessage());
    }

    @Test
    void closeAccount_PositiveBalance_ThrowsException() {
        testAccount.setBalance(BigDecimal.valueOf(100));
        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> accountService.closeAccount("TESTIBAN123"));

        assertEquals("You cannot close an account with a positive balance.", exception.getMessage());
    }

    @Test
    void openAccount_OpensSuccessfully() {
        testAccount.setStatus(AccountStatus.CLOSED);
        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        TransactionResponseDto response = accountService.openAccount("TESTIBAN123");

        assertEquals(AccountStatus.ACTIVE, testAccount.getStatus());
        assertEquals("Account is opened", response.getMessage());
    }

    @Test
    void openAccount_AlreadyOpen_ThrowsException() {
        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> accountService.openAccount("TESTIBAN123"));

        assertEquals("Account is already open or in an invalid state", exception.getMessage());
    }

    @Test
    void deleteAccount_DeletesSuccessfully() {
        testAccount.setStatus(AccountStatus.CLOSED);
        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        TransactionResponseDto response = accountService.deleteAccount("TESTIBAN123");

        verify(accountRepository).delete(testAccount);
        assertEquals("Account TESTIBAN123 is deleted", response.getMessage());
    }

    @Test
    void deleteAccount_NotClosed_ThrowsException() {
        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> accountService.deleteAccount("TESTIBAN123"));

        assertEquals("Account is not closed", exception.getMessage());
    }

    @Test
    void processTransaction_Deposit_ProcessesSuccessfully() {
        SingleAccountTransactionDto deposit = new SingleAccountTransactionDto();
        deposit.setTransactionType(TransactionType.DEPOSIT);
        deposit.setIban("TESTIBAN123");
        deposit.setAmount(BigDecimal.valueOf(100));

        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        TransactionResponseDto response = accountService.processTransaction(deposit);

        assertEquals("Deposit success: +100 USD", response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    void deposit_IncreasesBalance() {
        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        TransactionResponseDto response = accountService.deposit("TESTIBAN123", BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(1100), testAccount.getBalance());
        assertEquals("Deposit success: +100 USD", response.getMessage());
    }

    @Test
    void withdraw_DecreasesBalance() {
        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        TransactionResponseDto response = accountService.withdraw("TESTIBAN123", BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(900), testAccount.getBalance());
        assertEquals("Withdrawal success: -100 USD", response.getMessage());
    }

    @Test
    void withdraw_InsufficientFunds_ThrowsException() {
        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));

        BigDecimal value = BigDecimal.valueOf(2000);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> accountService.withdraw("TESTIBAN123", value));

        assertEquals("Insufficient funds for withdraw", exception.getMessage());
    }

    @Test
    void transfer_TransfersSuccessfully() {
        Account toAccount = new Account();
        toAccount.setIban("TOIBAN123");
        toAccount.setBalance(BigDecimal.ZERO);
        toAccount.setCurrency(Currency.USD);

        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByIban("TOIBAN123")).thenReturn(Optional.of(toAccount));

        TransactionResponseDto response = accountService.transfer("TESTIBAN123", "TOIBAN123", BigDecimal.valueOf(500));

        assertEquals(BigDecimal.valueOf(500), testAccount.getBalance());
        assertEquals(BigDecimal.valueOf(500), toAccount.getBalance());
        assertEquals("Transfer 500 USD from TESTIBAN123 to TOIBAN123", response.getMessage());
    }
    @Test
    void processTransaction_Withdrawal_ProcessesSuccessfully() {
        SingleAccountTransactionDto withdrawal = new SingleAccountTransactionDto();
        withdrawal.setTransactionType(TransactionType.WITHDRAWAL);
        withdrawal.setIban("TESTIBAN123");
        withdrawal.setAmount(BigDecimal.valueOf(100));

        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));
        doNothing().when(transactionValidator).validateTransaction(withdrawal);

        TransactionResponseDto response = accountService.processTransaction(withdrawal);

        assertEquals("Withdrawal success: -100 USD", response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void processTransaction_Transfer_ProcessesSuccessfully() {
        Account toAccount = new Account();
        toAccount.setIban("TOIBAN123");
        toAccount.setBalance(BigDecimal.ZERO);
        toAccount.setCurrency(Currency.USD);

        TransferTransactionDto transfer = new TransferTransactionDto();
        transfer.setTransactionType(TransactionType.TRANSFER);
        transfer.setFromIban("TESTIBAN123");
        transfer.setToIban("TOIBAN123");
        transfer.setAmount(BigDecimal.valueOf(500));

        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByIban("TOIBAN123")).thenReturn(Optional.of(toAccount));
        doNothing().when(transactionValidator).validateTransaction(transfer);

        TransactionResponseDto response = accountService.processTransaction(transfer);

        assertEquals("Transfer 500 USD from TESTIBAN123 to TOIBAN123", response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transfer_InsufficientFunds_ThrowsException() {
        Account toAccount = new Account();
        toAccount.setIban("TOIBAN123");

        when(accountRepository.findByIban("TESTIBAN123")).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByIban("TOIBAN123")).thenReturn(Optional.of(toAccount));

        BigDecimal value = BigDecimal.valueOf(2000);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> accountService.transfer("TESTIBAN123", "TOIBAN123",value));

        assertEquals("Insufficient funds for transfer", exception.getMessage());
    }
}