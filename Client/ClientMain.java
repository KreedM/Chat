// 
// Decompiled by Procyon v0.5.36
// 

public class ClientMain
{
    public static void main(final String[] args) {
        try {
            final ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
            client.run();
        }
        catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Invalid arguments! Please input the server IP and port.");
        }
    }
}
