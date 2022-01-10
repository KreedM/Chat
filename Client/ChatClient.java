import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.net.DatagramPacket;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JFrame;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.awt.event.KeyListener;
import java.awt.event.WindowListener;

// 
// Decompiled by Procyon v0.5.36
// 

public class ChatClient extends Thread implements WindowListener, KeyListener
{
    private DatagramSocket socket;
    private InetAddress ipAddress;
    private JFrame chatFrame;
    private JTextField textField;
    private JScrollPane scrollPane;
    private ArrayList<Message> messages;
    private StringTokenizer st;
    private String username;
    private int port;
    
    public ChatClient(final String ipAddress, final int port) {
        try {
            this.socket = new DatagramSocket();
            this.ipAddress = InetAddress.getByName(ipAddress);
        }
        catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        this.port = port;
        this.messages = new ArrayList<Message>();
        (this.chatFrame = new JFrame()).setTitle("Chat Room");
        this.chatFrame.setLayout(new BorderLayout());
        this.chatFrame.setSize(480, 480);
        this.chatFrame.setDefaultCloseOperation(0);
        this.chatFrame.setLocationRelativeTo(null);
        this.chatFrame.addWindowListener(this);
        (this.textField = new JTextField()).addKeyListener(this);
        this.chatFrame.add(this.textField, "South");
        this.scrollPane = new JScrollPane();
        this.chatFrame.add(this.scrollPane, "Center");
        this.chatFrame.setVisible(true);
        this.username = JOptionPane.showInputDialog(this.chatFrame, new JLabel("Type in your username:", 0), "Login", -1);
        if (this.username == null || this.username.length() == 0) {
            this.username = "Guest";
        }
    }
    
    @Override
    public void run() {
        this.sendData(("00 " + this.getExternalIP() + " " + this.username).getBytes());
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
        if (this.st.countTokens() < 2) {
            return;
        }
        final String ID = this.st.nextToken();
        String user = this.st.nextToken();
        final String s;
        switch (s = ID) {
            case "00": {
                this.addMessage(user, "has entered the chat.");
                return;
            }
            case "01": {
                user = "<" + user + ">";
                String message = "";
                while (this.st.hasMoreTokens()) {
                    message = String.valueOf(message) + this.st.nextToken() + " ";
                }
                this.addMessage(user, message);
                return;
            }
            case "02": {
                this.addMessage(user, "has left the chat.");
                return;
            }
            case "03": {
                this.addMessage(user, "other user(s) are currently online.");
                return;
            }
            case "04": {
                JOptionPane.showMessageDialog(this.chatFrame, new JLabel("You have been kicked from the room.", 0), "GTFO", 2);
                System.exit(0);
                break;
            }
            case "05": {
                break;
            }
        }
        this.username = JOptionPane.showInputDialog(this.chatFrame, new JLabel("Type in another username:", 0), "Login", -1);
        if (this.username == null || this.username.length() == 0) {
            this.username = "Guest";
        }
        this.sendData(("00 " + this.getExternalIP() + " " + this.username).getBytes());
    }
    
    public void sendData(final byte[] data) {
        try {
            this.socket.send(new DatagramPacket(data, data.length, this.ipAddress, this.port));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void addMessage(final String username, final String message) {
        this.messages.add(new Message(username, message));
        final JPanel messagePanel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = 21;
        int rows = 0;
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
        if (e.getKeyCode() == 10 && this.textField.getText().length() > 0) {
            this.sendData(("01 " + this.getExternalIP() + " " + this.username + " " + this.textField.getText()).getBytes());
            this.textField.setText("");
        }
    }
    
    @Override
    public void windowClosing(final WindowEvent e) {
        this.sendData(("02 " + this.getExternalIP() + " " + this.username).getBytes());
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
