import java.net.UnknownHostException;
import java.net.InetAddress;

// 
// Decompiled by Procyon v0.5.36
// 

public class User
{
    private InetAddress ipAddress;
    private String username;
    private int port;
    
    public User(final String ipAddress, final String username, final int port) {
        try {
            this.ipAddress = InetAddress.getByName(ipAddress);
            this.username = username;
            this.port = port;
        }
        catch (UnknownHostException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
    
    public boolean equals(final String ipAddress, final String username, final int port) {
        return this.ipAddress.getHostAddress().equals(ipAddress) && this.username.equals(username) && this.port == port;
    }
    
    public InetAddress getAddress() {
        return this.ipAddress;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public int getPort() {
        return this.port;
    }
}
