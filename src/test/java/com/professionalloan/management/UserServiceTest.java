package com.professionalloan.management;

import com.professionalloan.management.exception.DuplicateLoanApplicationException;
import com.professionalloan.management.exception.UserNotFoundException;
import com.professionalloan.management.model.User;
import com.professionalloan.management.repository.UserRepository;
import com.professionalloan.management.service.EmailService;
import com.professionalloan.management.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private static final String TEST_EMAIL = "user@test.com";
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String TEST_PASSWORD = "mypassword";
    private static final String TEST_NAME = "Test User";

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Registration Tests ---

    @Test
    void registerUserWithDuplicateEmailThrowsException() {
        User user = new User("John", "test@test.com", "password123", "USER");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        assertThrows(DuplicateLoanApplicationException.class, () -> service.registerUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserSuccessWithDefaultRole() {
        User user = new User("Jane Doe", "unique@test.com", "secure123", null);
        when(userRepository.findByEmail("unique@test.com")).thenReturn(Optional.empty());
        boolean result = service.registerUser(user);
        assertTrue(result);
        assertEquals("USER", user.getRole());
        verify(userRepository).save(user);
        verify(emailService).sendRegistrationSuccessEmail("unique@test.com", "Jane Doe");
    }

    @Test
    void registerUserWithShortNameThrowsException() {
        User user = new User("Al", "valid@email.com", "password123", null);
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
    }

    @Test
    void registerUserWithInvalidCharactersInNameThrowsException() {
        User user = new User("John@123", "john@example.com", "password123", null);
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
    }

    @Test
    void registerUserWithNullNameThrowsException() {
        User user = new User(null, "john@example.com", "password123", null);
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
    }

    @Test
    void registerUserWithInvalidEmailFormatThrowsException() {
        User user = new User("Valid Name", "invalid-email", "password123", null);
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
    }

    @Test
    void registerUserWithNullEmailThrowsException() {
        User user = new User("Valid Name", null, "password123", null);
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
    }

    @Test
    void registerUserWithShortPasswordThrowsException() {
        User user = new User("Valid Name", "valid@email.com", "123", null);
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
    }

    @Test
    void registerUserWithNullPasswordThrowsException() {
        User user = new User("Valid Name", "valid@email.com", null, null);
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
    }

    @Test
    void registerUserWithCustomRoleRetainsIt() {
        User user = new User("Rahul", "rahul@sample.com", "rahul123", "CUSTOM");
        when(userRepository.findByEmail("rahul@sample.com")).thenReturn(Optional.empty());
        boolean result = service.registerUser(user);
        assertTrue(result);
        assertEquals("CUSTOM", user.getRole());
        verify(userRepository).save(user);
        verify(emailService).sendRegistrationSuccessEmail("rahul@sample.com", "Rahul");
    }

    // --- Login Tests ---

    @Test
    void loginAdminWithCorrectCredentialsSuccess() {
        User result = service.findByEmailAndPassword(ADMIN_EMAIL, "admin");
        assertNotNull(result);
        assertEquals("ADMIN", result.getRole());
        assertEquals(ADMIN_EMAIL, result.getEmail());
    }

    @Test
    void loginAdminWithWrongPasswordThrowsException() {
        assertThrows(UserNotFoundException.class,
                () -> service.findByEmailAndPassword(ADMIN_EMAIL, "wrong"));
    }

    @Test
    void loginUserWithEmptyEmailThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.findByEmailAndPassword("", "pass"));
    }

    @Test
    void loginUserWithEmptyPasswordThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.findByEmailAndPassword("email@test.com", ""));
    }

    @Test
    void loginUserWithValidCredentialsReturnsUser() {
        User user = new User(TEST_NAME, TEST_EMAIL, TEST_PASSWORD, "USER");
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        User result = service.findByEmailAndPassword(TEST_EMAIL, TEST_PASSWORD);
        assertEquals("USER", result.getRole());
        assertEquals(TEST_EMAIL, result.getEmail());
    }

    @Test
    void loginUserWithWrongPasswordThrowsException() {
        User user = new User("Test", "abc@xyz.com", "correct", "USER");
        when(userRepository.findByEmail("abc@xyz.com")).thenReturn(Optional.of(user));
        assertThrows(UserNotFoundException.class,
                () -> service.findByEmailAndPassword("abc@xyz.com", "wrong"));
    }

    @Test
    void loginUserNotFoundThrowsException() {
        when(userRepository.findByEmail("missing@nowhere.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> service.findByEmailAndPassword("missing@nowhere.com", "whatever"));
    }

    @Test
    void loginUserWithNullRoleDefaultsToUSER() {
        User user = new User("No Role", "nobody@test.com", "pass123", null);
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.of(user));
        User result = service.findByEmailAndPassword("nobody@test.com", "pass123");
        assertEquals("USER", result.getRole());
    }

    // --- findByEmail Tests ---

    @Test
    void findUserByEmailSuccess() {
        User user = new User("Jane", "jane@x.com", "pass", "USER");
        when(userRepository.findByEmail("jane@x.com")).thenReturn(Optional.of(user));
        User result = service.findByEmail("jane@x.com");
        assertEquals("jane@x.com", result.getEmail());
    }

    @Test
    void findUserByEmailNotFoundThrowsException() {
        when(userRepository.findByEmail("noone@nowhere.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> service.findByEmail("noone@nowhere.com"));
    }

    // --- findById Tests ---

    @Test
    void findUserByIdWhenPresent() {
        User user = new User("X", "x@y.com", "pass", "USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = service.findById(1L);

        assertTrue(result.isPresent());

        result.ifPresent(foundUser -> {
            assertEquals("x@y.com", foundUser.getEmail());
            assertEquals("X", foundUser.getName());
        });
    }

    @Test
    void findUserByIdWhenNotFoundReturnsEmpty() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<User> result = service.findById(2L);
        assertFalse(result.isPresent());
    }

    // --- Save User ---

    @Test
    void saveUserSuccess() {
        User user = new User("Saved", "save@me.com", "pass123", "USER");
        service.save(user);
        verify(userRepository, times(1)).save(user);
    }
}
