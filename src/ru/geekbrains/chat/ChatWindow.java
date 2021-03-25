package ru.geekbrains.chat;

import javax.naming.AuthenticationException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JButton sendButton;
    private JTextField inputField;

    private final String SERVER_ADDR = "localhost";
    private final int SERVER_PORT = 8081;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isAuthorized;

    public ChatWindow() {
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        prepareGUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatWindow();
            }
        });
    }

    public void setAuthorized(boolean authorized) {
        this.isAuthorized = authorized;
        if (!authorized) {
            setNick(null);
        }
    }

    public void setNick(String nick) {
        if (nick == null) {
            this.setTitle("Tovarisch polkovnik");
        } else {
            this.setTitle("Tovarisch polkovnik: " + nick);
        }
    }

    public void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        setAuthorized(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String strFromServer = in.readUTF();
                        if (strFromServer.startsWith("/authok")) {
                            setAuthorized(true);
                            setNick(strFromServer.split(" ")[1]);
                            chatArea.append(strFromServer + '\n');
                            break;
                        }
                        chatArea.append(strFromServer + '\n');
                    }
                    while (true) {
                        String strFromServer = in.readUTF();
                        if(strFromServer.equals("/q")) {
                            break;
                        }
                        chatArea.append(strFromServer);
                        chatArea.append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void closeConnection() {
        try {
            out.flush();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    public void prepareGUI() {
        setTitle("Tovarish Polkovnik");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(300, 300, 400, 600);
        setMinimumSize(new Dimension(300, 100));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel);

        chatArea = ChatWindow.createChatArea();
        mainPanel.add(ChatWindow.wrapChatArea(chatArea));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        inputField = new JTextField();
        sendButton = new JButton("Send");
        mainPanel.add(ChatWindow.createSendPanel(inputField, sendButton));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    out.writeUTF("/q");
                    closeConnection();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        setVisible(true);

        sendButton.addActionListener((ActionEvent e) -> { sendMessage(); });

        inputField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 10) {
                    sendMessage();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }

        });
        inputField.requestFocus();
    }

    private void sendMessage() {
        if (!inputField.getText().trim().isEmpty()) {
            try {
                String messageToServer = inputField.getText();
                out.writeUTF(messageToServer);
                inputField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error sending message");
            }
        }
    }

    // Helpers

    public static JTextArea createChatArea() {
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);

        return chatArea;
    }


    private static JComponent wrapChatArea(JTextArea chatArea) {
        JScrollPane scrollPane = new JScrollPane(chatArea);

        JPanel borderLayoutPanel = new JPanel(new BorderLayout());
        borderLayoutPanel.add(scrollPane, BorderLayout.CENTER);

        return borderLayoutPanel;
    }


    private static JComponent createSendPanel(JTextField inputField, JButton sendButton) {
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.X_AXIS));
        sendPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));

        sendPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        sendPanel.add(inputField);
        sendPanel.add(sendButton);

        sendPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        return sendPanel;
    }
}
