package olhodebairro;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;

public class Cliente extends JFrame {

    private Socket socket;       // Conexão com o servidor
    private PrintStream saida;   // Canal para enviar mensagens ao servidor
    private Scanner entrada;     // Canal para receber mensagens do servidor
    private String nome;         // Nome do morador exibido no chat

    // Componentes da interface gráfica
    private JTextArea areaChat;
    private JTextField campomensagem;
    private JButton botaoEnviar;
    private JButton botaoSeguranca;
    private JButton botaoInfraestrutura;
    private JButton botaoEventos;
    private JButton botaoAjuda;

    public Cliente(String nome) {
        this.nome = nome;
        initComponents(); // Monta a interface gráfica
        setTitle("Olho de Bairro - " + nome);
        conectar();         // Estabelece conexão com o servidor
        receberMensagens(); // Inicia a thread que escuta mensagens do servidor
    }

    // Conecta ao servidor via socket e envia o nome do morador como primeiro dado
    private void conectar() {
        try {
            socket  = new Socket("127.0.0.1", 3334);
            saida   = new PrintStream(socket.getOutputStream());
            entrada = new Scanner(socket.getInputStream());
            saida.println(nome); // Primeiro envio: identificação do morador
        } catch (IOException e) {
            exibirMensagem("[ERRO] Não foi possível conectar ao servidor.");
        }
    }

    // Executa em thread separada para não travar a interface gráfica
    // Fica ouvindo mensagens do servidor e as exibe na área de chat
    private void receberMensagens() {
        new Thread(() -> {
            while (entrada.hasNextLine()) {
                String msg = entrada.nextLine();
                // Atualiza a interface na thread do Swing de forma segura
                SwingUtilities.invokeLater(() -> exibirMensagem(msg));
            }
        }).start();
    }

    // Envia a mensagem digitada para o servidor e limpa o campo de texto
    private void enviarMensagem(String msg) {
        if (msg == null || msg.trim().isEmpty()) return;
        saida.println(msg);
        campomensagem.setText("");
    }

    // Monta o comando com o prefixo da categoria e o conteúdo digitado no campo
    // Exemplo: prefixo="/seguranca" + texto="Buraco na rua" → "/seguranca Buraco na rua"
    private void enviarComando(String prefixo) {
        String texto = campomensagem.getText().trim();
        if (texto.isEmpty()) {
            exibirMensagem("[DICA] Digite sua mensagem no campo de texto antes de clicar no botão.");
            return;
        }
        enviarMensagem(prefixo + " " + texto); // O servidor processa e categoriza
    }

    // Adiciona a mensagem na área de chat e rola para a mais recente
    private void exibirMensagem(String msg) {
        areaChat.append(msg + "\n");
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }

    // Monta todos os componentes visuais da janela
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setMinimumSize(new Dimension(500, 400));
        setLayout(new BorderLayout(10, 10));

        // Título no topo da janela
        JLabel titulo = new JLabel("🏘 Olho de Bairro - Plataforma Comunitária", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        add(titulo, BorderLayout.NORTH);

        // Área de chat: somente leitura, com scroll automático
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        areaChat.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(areaChat), BorderLayout.CENTER);

        // Campo de texto e botão Enviar (para chat geral ou comandos manuais)
        JPanel painelInferior = new JPanel(new BorderLayout(5, 5));
        painelInferior.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        campomensagem = new JTextField();
        campomensagem.setFont(new Font("Arial", Font.PLAIN, 13));
        campomensagem.addActionListener(e -> enviarMensagem(campomensagem.getText())); // Envia com Enter

        botaoEnviar = new JButton("Enviar");
        botaoEnviar.addActionListener(e -> enviarMensagem(campomensagem.getText()));

        painelInferior.add(campomensagem, BorderLayout.CENTER);
        painelInferior.add(botaoEnviar, BorderLayout.EAST);

        // Botões de categoria: cada um monta o comando correto e envia ao servidor
        JPanel painelComandos = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        painelComandos.setBorder(BorderFactory.createTitledBorder("Publicar como:"));

        botaoSeguranca      = new JButton("🚨 Segurança");
        botaoInfraestrutura = new JButton("🔧 Infraestrutura");
        botaoEventos        = new JButton("📅 Eventos");
        botaoAjuda          = new JButton("❓ Ajuda");

        // Cada botão chama enviarComando com o prefixo correspondente
        botaoSeguranca.addActionListener(e      -> enviarComando("/seguranca"));
        botaoInfraestrutura.addActionListener(e -> enviarComando("/infraestrutura"));
        botaoEventos.addActionListener(e        -> enviarComando("/eventos"));
        botaoAjuda.addActionListener(e          -> enviarMensagem("/ajuda")); // /ajuda não precisa de conteúdo

        painelComandos.add(botaoSeguranca);
        painelComandos.add(botaoInfraestrutura);
        painelComandos.add(botaoEventos);
        painelComandos.add(botaoAjuda);

        // Painel sul agrupa os botões de categoria e o campo de envio
        JPanel painelSul = new JPanel(new BorderLayout());
        painelSul.add(painelComandos, BorderLayout.NORTH);
        painelSul.add(painelInferior, BorderLayout.SOUTH);
        add(painelSul, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null); // Centraliza a janela na tela
    }

    // Ponto de entrada: solicita o nome do morador antes de abrir a janela
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            String nome = JOptionPane.showInputDialog(
                null,
                "Bem-vindo ao Olho de Bairro!\nDigite seu nome:",
                "Identificação",
                JOptionPane.PLAIN_MESSAGE
            );
            // Encerra se o nome não for informado
            if (nome == null || nome.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Nome não informado. Encerrando.");
                return;
            }
            new Cliente(nome.trim()).setVisible(true);
        });
    }
}