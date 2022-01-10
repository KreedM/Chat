// 
// Decompiled by Procyon v0.5.36
// 

public class ServerMain
{
    public static void main(final String[] args) {
        try {
            final ChatServer server = new ChatServer(Integer.parseInt(args[0]));
            server.start();
        }
        catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Invalid argument! Please input the server port.");
        }
    }
}
