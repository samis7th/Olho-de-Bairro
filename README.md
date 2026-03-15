# 🏘 Olho de Bairro

Sistema de comunicação comunitária desenvolvido em Java com Sockets TCP, como protótipo inicial de um projeto extensionista da disciplina de Sistemas Distribuídos.

A proposta é conectar moradores de um bairro entre si e com a prefeitura, permitindo que cidadãos publiquem ocorrências, sugestões e eventos de forma categorizada em tempo real.

---

## 💡 Funcionalidades

- Conexão de múltiplos clientes simultaneamente via threads
- Publicação de mensagens categorizadas por tema
- Chat geral entre todos os moradores conectados
- Toda a lógica de processamento centralizada no servidor
- Histórico de publicações armazenado em memória durante a execução

---

## 🖥️ Comandos disponíveis

| Comando | Descrição |
|---|---|
| `/seguranca [mensagem]` | Reportar ocorrência de segurança |
| `/infraestrutura [mensagem]` | Reportar problema de infraestrutura |
| `/eventos [mensagem]` | Divulgar um evento no bairro |
| `/ajuda` | Exibir a lista de comandos |
| *(sem comando)* | Enviar mensagem no chat geral |

---

## 🚀 Como executar

### Pré-requisitos
- Java JDK 11 ou superior
- IDE como NetBeans ou IntelliJ (ou compilação via terminal)

### Passos

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/OlhoDeBairro.git
```

2. Abra o projeto na sua IDE e localize o pacote `olhodebairro`

3. Execute o **Server.java** primeiro

4. Execute o **Cliente.java** e/ou **Cliente2.java** (cada um em uma janela separada)

5. Digite seu nome quando solicitado e comece a usar

---

## 📁 Estrutura do projeto

```
OlhoDeBairro/
└── src/
    └── olhodebairro/
        ├── Server.java    # Servidor — processa comandos e gerencia conexões
        ├── Cliente.java   # Cliente 1 — interface gráfica com botões de categoria
        └── Cliente2.java  # Cliente 2 — segunda instância de cliente
```

---

## 🔮 Melhorias futuras

- **Banco de dados:** persistência das publicações com MySQL ou PostgreSQL, permitindo consultar o histórico mesmo após reiniciar o servidor
- **Interface web:** substituição da interface Swing por uma aplicação web responsiva, acessível por navegador e dispositivos móveis

---

## 📚 Tecnologias utilizadas

- Java
- Sockets TCP
- Swing (interface gráfica)
- Threads (concorrência)

---

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos.
