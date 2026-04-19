import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class MockitoInlineCheckTest {

    @Test
    void checkInlineMockMaker() {
        try {
            mock(AutoCloseable.class);
            System.out.println("INLINE MOCK MAKER IS ACTIVE");
        } catch (Exception e) {
            System.out.println("INLINE MOCK MAKER IS NOT ACTIVE");
            throw e;
        }
    }
}