//ANGEL GAEL ANGUIANO GONZALEZ
//PRACTICA 6 LABORATORIO
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ChatClient {
    private JFrame frame = new JFrame("Chat Client");// VENTANA PRINCIPAL DEL CLIENTE DE CHAT
    private JTextField serverAddressField = new JTextField("localhost");// CAMPO PARA LA DIRECCIÓN DEL SERVIDOR (VALOR POR DEFECTO: localhost)
    private JTextField serverPortField = new JTextField("8087");// CAMPO PARA EL PUERTO DEL SERVIDOR (VALOR POR DEFECTO: 8087)
    private JTextField usernameField = new JTextField(10);// CAMPO PARA INGRESAR EL NOMBRE DE USUARIO (10 CARACTERES DE ANCHO)
    private JTextArea messageArea = new JTextArea(20, 40);// ÁREA DE TEXTO PARA MOSTRAR LOS MENSAJES DEL CHAT (20 FILAS, 40 COLUMNAS)
    private JTextField messageField = new JTextField(40);// CAMPO PARA ESCRIBIR UN NUEVO MENSAJE (40 CARACTERES DE ANCHO)
    private JButton sendButton = new JButton("Enviar");// BOTÓN PARA ENVIAR UN MENSAJE

    private Socket socket; // SOCKET PARA LA CONEXIÓN CON EL SERVIDOR
    private PrintWriter out; // FLUJO DE SALIDA PARA ENVIAR MENSAJES AL SERVIDOR
    private BufferedReader in; // FLUJO DE ENTRADA PARA RECIBIR MENSAJES DEL SERVIDOR
    private String username; // NOMBRE DE USUARIO ASOCIADO AL CLIENTE

    public ChatClient() {
        frame.setLayout(new BorderLayout());// ESTABLECE EL LAYOUT DE LA VENTANA PRINCIPAL COMO BORDERLAYOUT
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// CONFIGURA EL COMPORTAMIENTO AL CERRAR LA VENTANA
        // PANEL SUPERIOR CON LOS CAMPOS DE DIRECCIÓN DEL SERVIDOR, PUERTO Y NOMBRE DE USUARIO
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Server Address:"));
        topPanel.add(serverAddressField);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(serverPortField);
        topPanel.add(new JLabel("Username:"));
        topPanel.add(usernameField);
        JButton connectButton = new JButton("Connect");
        topPanel.add(connectButton);
        frame.add(topPanel, BorderLayout.NORTH);// AGREGA EL PANEL SUPERIOR A LA PARTE NORTE DE LA VENTANA

        messageArea.setEditable(false);// HACE QUE EL ÁREA DE MENSAJES NO SEA EDITABLE
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);// AGREGA EL ÁREA DE MENSAJES CON SCROLL AL CENTRO
         // PANEL INFERIOR CON EL CAMPO PARA ESCRIBIR MENSAJES Y EL BOTÓN ENVIAR
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(messageField);
        bottomPanel.add(sendButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);// AGREGA EL PANEL INFERIOR AL SUR DE LA VENTANA

        sendButton.setEnabled(false);// INICIALIZA EL BOTÓN DE ENVIAR COMO DESHABILITADO
        // CONFIGURACIÓN DE ACCIONES PARA LOS BOTONES Y CAMPO DE MENSAJE
        connectButton.addActionListener(e -> new Thread(this::connectToServer).start());// ACCIÓN PARA CONECTARSE AL SERVIDOR
        sendButton.addActionListener(e -> sendMessage());// ACCIÓN PARA ENVIAR UN MENSAJE
        messageField.addActionListener(e -> sendMessage());// ACCIÓN PARA ENVIAR UN MENSAJE AL PRESIONAR ENTER EN EL CAMPO

        frame.pack();// AJUSTA EL TAMAÑO DE LA VENTANA SEGÚN SU CONTENIDO
        frame.setVisible(true);// HACE VISIBLE LA VENTANA PRINCIPAL
    }

    private void connectToServer() {
        // OBTIENE LA DIRECCIÓN DEL SERVIDOR, EL PUERTO Y EL NOMBRE DE USUARIO INGRESADOS POR EL USUARIO
        String serverAddress = serverAddressField.getText();
        int port = Integer.parseInt(serverPortField.getText());
        username = usernameField.getText();
        // VERIFICA QUE EL NOMBRE DE USUARIO NO ESTÉ VACÍO
        if (username.isEmpty()) {
            // MUESTRA UN MENSAJE DE ERROR SI EL NOMBRE DE USUARIO ESTÁ VACÍO
            JOptionPane.showMessageDialog(frame, "Username cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
             // INTENTA ESTABLECER LA CONEXIÓN CON EL SERVIDOR
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // INICIA UN HILO PARA LEER MENSAJES ENTRANTES DEL SERVIDOR
            new Thread(new IncomingReader()).start();
            // ENVÍA UN MENSAJE DE SALUDO AL SERVIDOR Y ESPERA UNA RESPUESTA
            out.println("Hola");
            if ("Hola".equals(in.readLine())) {
                // SI SE RECIBE UNA RESPUESTA DE SALUDO, ENVÍA EL NOMBRE DE USUARIO AL SERVIDOR
                out.println("Usuario:" + username);
                sendButton.setEnabled(true);// HABILITA EL BOTÓN DE ENVIAR
                messageArea.append("Connected to the chat server as " + username + "\n");
            } else {
                // SI NO SE RECIBE UNA RESPUESTA DE SALUDO, MUESTRA UN MENSAJE DE ERROR
                messageArea.append("Failed to connect to the chat server\n");
            }
        } catch (IOException e) {
            // EN CASO DE ERROR AL ESTABLECER LA CONEXIÓN, IMPRIME EL STACK TRACE DEL ERROR
            e.printStackTrace();
            messageArea.append("Failed to connect to the chat server\n");
        }
    }

    private void sendMessage() {
         // OBTIENE EL MENSAJE INGRESADO POR EL USUARIO
        String message = messageField.getText();
        if (message.isEmpty()) return;
         // ENVÍA EL MENSAJE AL SERVIDOR, AÑADIENDO EL NOMBRE DE USUARIO AL MENSAJE
        if (message.equals("Adios")) {
            // SI EL MENSAJE ES "Adios", ENVÍA EL MENSAJE DE DESPEDIDA Y DESCONECTA AL CLIENTE DEL SERVIDOR
            out.println("Adios");
            disconnect();
        } else {
            // SI EL MENSAJE NO ES "Adios", LO ENVÍA AL SERVIDOR PARA QUE SEA DIFUNDIDO A LOS DEMÁS CLIENTES
            out.println("Enviar" + username + ": " + message);
        }
        messageField.setText("");// LIMPIA EL CAMPO DE MENSAJE DESPUÉS DE ENVIAR EL MENSAJE
    }

    private void disconnect() {
        try {
            // CIERRA EL SOCKET DEL CLIENTE Y DESHABILITA EL BOTÓN DE ENVIAR
            if (socket != null) {
                socket.close();
            }
            sendButton.setEnabled(false);
            messageArea.append("Disconnected from the chat server\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // CLASE INTERNA PARA LEER MENSAJES ENTRANTES DEL SERVIDOR EN UN HILO SEPARADO
    private class IncomingReader implements Runnable {
        public void run() {
            try {
                String message;
                // LEE MENSAJES ENTRANTES DEL SERVIDOR Y LOS MUESTRA EN EL ÁREA DE MENSAJES
                while ((message = in.readLine()) != null) {
                    messageArea.append(message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                 // CUANDO TERMINA DE LEER MENSAJES, DESHABILITA EL BOTÓN DE ENVIAR Y MUESTRA UN MENSAJE DE DESCONEXIÓN
                sendButton.setEnabled(false);
                messageArea.append("Disconnected from the chat server\n");
                try {
                    socket.close();// CIERRA EL SOCKET DEL CLIENTE
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // MÉTODO MAIN PARA INICIAR LA APLICACIÓN
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}