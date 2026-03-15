package olhodebairro;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {

    // Lista com os canais de saída de todos os clientes conectados simultaneamente
    private static List<PrintStream> clientes = new ArrayList<>();

    // Históricos por categoria — armazenados na memória do servidor enquanto estiver ativo
    private static List<String> historicoSeguranca      = new ArrayList<>();
    private static List<String> historicoInfraestrutura = new ArrayList<>();
    private static List<String> historicoEventos        = new ArrayList<>();

    public static void main(String[] args) {
        // Abre o servidor na porta 3334 e aguarda conexões indefinidamente
        try (ServerSocket servidor = new ServerSocket(3334)) {
            System.out.println("Servidor Olho de Bairro iniciado na porta 3334");

            while (true) {
                // Aguarda um novo cliente se conectar
                Socket socket = servidor.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress().getHostAddress());

                // Cada cliente é atendido em uma thread separada, permitindo múltiplas conexões simultâneas
                new Thread(new ClienteHandler(socket)).start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar servidor");
        }
    }

    // Classe responsável por gerenciar a comunicação com um cliente específico
    static class ClienteHandler implements Runnable {

        private Socket socket;
        private Scanner entrada;   // Lê mensagens enviadas pelo cliente
        private PrintStream saida; // Envia mensagens para este cliente
        private String nome;       // Nome do morador conectado

        public ClienteHandler(Socket socket) {
            try {
                this.socket  = socket;
                this.entrada = new Scanner(socket.getInputStream());
                this.saida   = new PrintStream(socket.getOutputStream());

                // O primeiro dado enviado pelo cliente é o nome do morador
                this.nome = entrada.nextLine();

                // Adiciona o canal de saída deste cliente na lista global
                clientes.add(saida);

                // Avisa todos os clientes conectados que um novo morador entrou
                enviarParaTodos("[SISTEMA] " + nome + " entrou no Olho de Bairro.");

                // Envia mensagem de boas-vindas somente para o novo morador
                saida.println("[SISTEMA] Bem-vindo(a), " + nome + "! Digite /ajuda para ver os comandos.");

            } catch (IOException e) {
                System.out.println("Erro ao conectar cliente");
            }
        }

        @Override
        public void run() {
            try {
                // Fica em loop lendo mensagens enquanto o cliente estiver conectado
                while (entrada.hasNextLine()) {
                    String msg = entrada.nextLine().trim();
                    processarMensagem(msg); // Encaminha cada mensagem para o processador de comandos
                }
            } finally {
                // Quando o cliente desconecta, remove da lista e avisa os demais
                clientes.remove(saida);
                enviarParaTodos("[SISTEMA] " + nome + " saiu do sistema.");
                socketClose();
            }
        }

        // Identifica se a mensagem é um comando categorizado ou chat geral
        private void processarMensagem(String msg) {
            if (msg.startsWith("/seguranca")) {
                processarSeguranca(msg);

            } else if (msg.startsWith("/infraestrutura")) {
                processarInfraestrutura(msg);

            } else if (msg.startsWith("/eventos")) {
                processarEventos(msg);

            } else if (msg.equals("/ajuda")) {
                // Exibe a lista de comandos apenas para quem solicitou
                saida.println("=== Comandos disponíveis ===");
                saida.println("/seguranca [msg]      - Reportar ocorrência de segurança");
                saida.println("/infraestrutura [msg] - Reportar problema de infraestrutura");
                saida.println("/eventos [msg]        - Divulgar evento no bairro");
                saida.println("/ajuda                - Exibir esta mensagem");
                saida.println("Sem comando           - Chat geral com todos");
                saida.println("============================");

            } else {
                // Mensagem sem comando: transmite como chat geral para todos
                enviarParaTodos(nome + ": " + msg);
            }
        }

        // Processa o comando /seguranca, salva no histórico e transmite para todos
        private void processarSeguranca(String msg) {
            String conteudo = extrairConteudo(msg, "/seguranca");
            if (conteudo.isEmpty()) { saida.println("[ERRO] Use: /seguranca [sua mensagem]"); return; }
            String registro = "[SEGURANÇA] " + nome + ": " + conteudo;
            historicoSeguranca.add(registroComData(registro));
            enviarParaTodos(registroComData(registro));
            System.out.println("[LOG SEGURANÇA] " + registroComData(registro));
        }

        // Processa o comando /infraestrutura, salva no histórico e transmite para todos
        private void processarInfraestrutura(String msg) {
            String conteudo = extrairConteudo(msg, "/infraestrutura");
            if (conteudo.isEmpty()) { saida.println("[ERRO] Use: /infraestrutura [sua mensagem]"); return; }
            String registro = "[INFRAESTRUTURA] " + nome + ": " + conteudo;
            historicoInfraestrutura.add(registroComData(registro));
            enviarParaTodos(registroComData(registro));
            System.out.println("[LOG INFRAESTRUTURA] " + registroComData(registro));
        }

        // Processa o comando /eventos, salva no histórico e transmite para todos
        private void processarEventos(String msg) {
            String conteudo = extrairConteudo(msg, "/eventos");
            if (conteudo.isEmpty()) { saida.println("[ERRO] Use: /eventos [sua mensagem]"); return; }
            String registro = "[EVENTO] " + nome + ": " + conteudo;
            historicoEventos.add(registroComData(registro));
            enviarParaTodos(registroComData(registro));
            System.out.println("[LOG EVENTO] " + registroComData(registro));
        }

        // Remove o prefixo do comando e retorna apenas o conteúdo da mensagem
        // Exemplo: "/seguranca Buraco na rua" → "Buraco na rua"
        private String extrairConteudo(String msg, String comando) {
            if (msg.length() > comando.length()) return msg.substring(comando.length()).trim();
            return "";
        }

        // Envia uma mensagem para todos os clientes conectados, com data e hora formatadas
        private void enviarParaTodos(String msg) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String dataHora = LocalDateTime.now().format(formatter);
            for (PrintStream cliente : clientes) {
                cliente.println("[" + dataHora + "] " + msg);
            }
        }

        // Formata uma string adicionando a data e hora atual
        private String registroComData(String msg) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return "[" + LocalDateTime.now().format(formatter) + "] " + msg;
        }

        // Fecha o socket do cliente com segurança ao desconectar
        private void socketClose() {
            try { socket.close(); } catch (IOException e) {}
        }
    }
}