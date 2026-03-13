package ca.etsmtl.taf.payload.response;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MessageResponse — message DTO")
class MessageResponseTest {

    @Test
    @DisplayName("Constructor and getter work")
    void constructor_and_getter() {
        MessageResponse msg = new MessageResponse("Inscription Réussie.!");
        assertEquals("Inscription Réussie.!", msg.getMessage());
    }

    @Test
    @DisplayName("Setter updates message")
    void setter_updatesMessage() {
        MessageResponse msg = new MessageResponse("old");
        msg.setMessage("new");
        assertEquals("new", msg.getMessage());
    }
}
