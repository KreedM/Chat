import java.awt.Font;
import javax.swing.JLabel;

// 
// Decompiled by Procyon v0.5.36
// 

public class Message
{
    private String username;
    private String message;
    
    public Message(final String username, final String message) {
        this.username = username;
        this.message = message;
    }
    
    public JLabel getUsername() {
        final JLabel label = new JLabel(this.username);
        label.setFont(new Font("Dialog", 1, 12));
        return label;
    }
    
    public JLabel getMessage() {
        final JLabel label = new JLabel(this.message);
        label.setFont(new Font("Dialog", 0, 12));
        return label;
    }
}
