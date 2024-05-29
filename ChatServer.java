//ANGEL GAEL ANGUIANO GONZALEZ
//PRACTICA 6 LABORATORIO
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 8087;
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<String, PrintWriter> clients = new HashMap<>();

    public static void main(String[] args) {
        // IMPRIME UN MENSAJE INDICANDO QUE EL SERVIDOR DE CHAT HA INICIADO
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // CREA UN BUCLE INFINITO PARA ESPERAR CONEXIONES DE CLIENTES
            while (true) {
                // ACEPTA UNA CONEXION DE CLIENTE Y CREA UN NUEVO HILO PARA MANEJARLO
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            // EN CASO DE ERROR AL CREAR EL SERVERSOCKET O AL ACEPTAR UNA CONEXION
            // IMPRIME EL STACK TRACE DEL ERROR
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            // SE ESTABLECEN LOS FLUJOS DE ENTRADA Y SALIDA PARA EL CLIENTE
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                // SE SINCRONIZA EL ACCESO A LA LISTA DE ESCRITORES DE CLIENTES PARA AÑADIR EL NUEVO ESCRITOR
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("Hola")) {
                        out.println("Hola");
                    } else if (inputLine.startsWith("Usuario:")) {
                        // SI SE RECIBE UN MENSAJE DE TIPO "Usuario:", SE EXTRAE EL NOMBRE DE USUARIO Y SE REGISTRA EN EL MAPA DE CLIENTES
                        username = inputLine.substring(8);
                        clients.put(username, out);
                        broadcast(username + " se ha unido al chat.");
                    } else if (inputLine.startsWith("Enviar")) {
                        // SI SE RECIBE UN MENSAJE DE TIPO "Enviar", SE EXTRAE EL MENSAJE Y SE ENVÍA A TODOS LOS CLIENTES
                        String message = inputLine.substring(6); //EXTRAER MENSAJE
                        broadcast(message);
                        // SI SE RECIBE "Adios", SE SALE DEL BUCLE Y TERMINA EL MANEJO DEL CLIENTE
                    } else if (inputLine.equals("Adios")) {
                        
                        break;
                    }
                }
            } catch (IOException e) {
                // EN CASO DE ERROR EN LA LECTURA O ESCRITURA CON EL CLIENTE, SE IMPRIME EL STACK TRACE DEL ERROR
                e.printStackTrace();
            } finally {
                // EN EL BLOQUE FINALLY SE REALIZAN LAS TAREAS DE LIMPIEZA Y CIERRE DE RECURSOS
                if (username != null) {
                    clients.remove(username);
                    broadcast(username + " se ha desconectado.");
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
