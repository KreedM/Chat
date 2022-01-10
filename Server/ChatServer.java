import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.IOException;
import java.net.DatagramPacket;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.net.DatagramSocket;
import java.awt.event.WindowListener;
import java.awt.event.KeyListener;

// 
// Decompiled by Procyon v0.5.36
// 

public class ChatServer extends Thread implements KeyListener, WindowListener
{
    private DatagramSocket socket;
    private JTextField textField;
    private JScrollPane scrollPane;
    private ArrayList<Message> messages;
    private ArrayList<User> users;
    private StringTokenizer st;
    
    public ChatServer(final int port) {
        this.users = new ArrayList<User>();
        try {
            this.socket = new DatagramSocket(port);
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        this.messages = new ArrayList<Message>();
        final JFrame chatFrame = new JFrame();
        chatFrame.setTitle("Chat Console");
        chatFrame.setLayout(new BorderLayout());
        chatFrame.setSize(480, 480);
        chatFrame.setDefaultCloseOperation(0);
        chatFrame.setLocationRelativeTo(null);
        chatFrame.addWindowListener(this);
        (this.textField = new JTextField()).addKeyListener(this);
        chatFrame.add(this.textField, "South");
        chatFrame.add(this.scrollPane = new JScrollPane(), "Center");
        chatFrame.setVisible(true);
    }
    
    @Override
    public void run() {
        while (true) {
            final byte[] data = new byte[1024];
            final DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                this.socket.receive(packet);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            this.parsePacket(packet);
        }
    }
    
    public void parsePacket(final DatagramPacket packet) {
        this.st = new StringTokenizer(new String(packet.getData()).trim());
        if (this.st.countTokens() < 3) {
            return;
        }
        final String ID = this.st.nextToken();
        String ipAddress = this.st.nextToken();
        final String username = this.st.nextToken();
        if (ipAddress.equals(this.getExternalIP())) {
            ipAddress = packet.getAddress().getHostAddress();
        }
        final String s;
        switch (s = ID) {
            case "00": {
                for (final User person : this.users) {
                    if (username.equals(person.getUsername())) {
                        try {
                            this.sendData("05 Server".getBytes(), InetAddress.getByName(ipAddress), packet.getPort());
                        }
                        catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
                this.users.add(new User(ipAddress, username, packet.getPort()));
                this.addMessage("[" + ipAddress + ":" + packet.getPort() + "] " + username, "has entered the chat.");
                this.sendToAllUsers(("03 " + Integer.toString(this.users.size() - 1)).getBytes());
                this.sendToAllUsers(("00 " + username).getBytes());
                break;
            }
            case "01": {
                boolean ValidUser = false;
                for (int i = 0; i < this.users.size(); ++i) {
                    if (this.users.get(i).equals(ipAddress, username, packet.getPort())) {
                        ValidUser = true;
                        break;
                    }
                }
                if (ValidUser) {
                    String message = "";
                    while (this.st.hasMoreTokens()) {
                        message = String.valueOf(message) + this.st.nextToken() + " ";
                    }
                    this.addMessage("[" + ipAddress + ":" + packet.getPort() + "] <" + username + ">", message);
                    this.sendToAllUsers(("01 " + username + " " + message).getBytes());
                    break;
                }
                try {
                    this.sendData("05 Server".getBytes(), InetAddress.getByName(ipAddress), packet.getPort());
                }
                catch (UnknownHostException e2) {
                    e2.printStackTrace();
                }
                break;
            }
            case "02": {
                for (int i = 0; i < this.users.size(); ++i) {
                    if (this.users.get(i).equals(ipAddress, username, packet.getPort())) {
                        this.users.remove(i);
                        this.addMessage("[" + ipAddress + ":" + packet.getPort() + "] " + username, "has left the chat.");
                        this.sendToAllUsers(("03 " + Integer.toString(this.users.size() - 1)).getBytes());
                        this.sendToAllUsers(("02 " + username).getBytes());
                        return;
                    }
                }
                break;
            }
            default:
                break;
        }
    }
    
    public void parseCommand(final String command) {
        if (command.length() != 0) {
            if (command.charAt(0) == '/') {
                this.sendToAllUsers(("01 Server " + command).getBytes());
                this.addMessage("<Server>", command);
            }
            else {
                this.st = new StringTokenizer(command);
                final String lowerCase;
                switch (lowerCase = this.st.nextToken().toLowerCase()) {
                    case "kick": {
                        if (this.st.countTokens() > 0) {
                            while (this.st.hasMoreTokens()) {
                                final String username = this.st.nextToken();
                                for (final User person : this.users) {
                                    if (username.equals(person.getUsername())) {
                                        this.users.remove(person);
                                        this.sendToAllUsers(("02 " + username).getBytes());
                                        this.sendData("04 Server".getBytes(), person.getAddress(), person.getPort());
                                        this.addMessage(username, "has been kicked.");
                                        break;
                                    }
                                }
                            }
                        }
                        return;
                    }
                    case "stop": {
                        this.sendToAllUsers("04 Server".getBytes());
                        System.exit(0);
                        return;
                    }
                    case "clear": {
                        this.messages = new ArrayList<Message>();
                        this.paint();
                        return;
                    }
                    default:
                        break;
                }
                this.sendToAllUsers(("01 Server " + command).getBytes());
                this.addMessage("<Server>", command);
            }
        }
    }
    
    public void sendToAllUsers(final byte[] data) {
        for (final User user : this.users) {
            this.sendData(data, user.getAddress(), user.getPort());
        }
    }
    
    public void sendData(final byte[] data, final InetAddress ipAddress, final int port) {
        try {
            this.socket.send(new DatagramPacket(data, data.length, ipAddress, port));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void addMessage(final String username, final String message) {
        this.messages.add(new Message(username, message));
        this.paint();
    }
    
    public void paint() {
        final JPanel messagePanel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        int rows = 0;
        c.anchor = 21;
        for (final Message text : this.messages) {
            c.gridx = 0;
            c.gridy = rows;
            c.weightx = 0.0;
            final JPanel messageHolder = new JPanel();
            messageHolder.add(text.getUsername());
            messageHolder.add(text.getMessage());
            messagePanel.add(messageHolder, c);
            c.gridx = 1;
            c.weightx = 1.0;
            messagePanel.add(new JLabel(""), c);
            ++rows;
        }
        if (this.scrollPane.getVerticalScrollBar().getWidth() == 0) {
            c.gridy = rows;
            c.weighty = 1.0;
            messagePanel.add(new JLabel(), c);
        }
        this.scrollPane.setViewportView(messagePanel);
        this.scrollPane.revalidate();
    }
    
    @Override
    public void keyPressed(final KeyEvent e) {
        if (e.getKeyCode() == 10) {
            if (this.textField.getText().length() > 0) {
                if (this.textField.getText().charAt(0) == '/') {
                    this.parseCommand(this.textField.getText().substring(1));
                }
                else {
                    this.sendToAllUsers(("01 Server " + this.textField.getText()).getBytes());
                    this.addMessage("<Server>", this.textField.getText());
                }
            }
            this.textField.setText("");
        }
    }
    
    @Override
    public void windowClosing(final WindowEvent e) {
        this.sendToAllUsers("04 Server".getBytes());
        System.exit(0);
    }
    
    public String getExternalIP() {
        try {
            final URL ipURL = new URL("http://checkip.amazonaws.com");
            final BufferedReader sc = new BufferedReader(new InputStreamReader(ipURL.openStream()));
            return sc.readLine().trim();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public void keyTyped(final KeyEvent e) {
    }
    
    @Override
    public void keyReleased(final KeyEvent e) {
    }
    
    @Override
    public void windowOpened(final WindowEvent e) {
    }
    
    @Override
    public void windowClosed(final WindowEvent e) {
    }
    
    @Override
    public void windowIconified(final WindowEvent e) {
    }
    
    @Override
    public void windowDeiconified(final WindowEvent e) {
    }
    
    @Override
    public void windowActivated(final WindowEvent e) {
    }
    
    @Override
    public void windowDeactivated(final WindowEvent e) {
    }
}
