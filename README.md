# Sistema de Gestão de Rotina

Sistema desenvolvido em **Java** com interface gráfica **Swing** para gerenciar projetos, tarefas, equipes de desenvolvimento e prazos.  
Implementa os papéis de **Desenvolvedor (DEV)** e **Gestor (GESTOR)** com persistência em banco de dados **MySQL**, controle de horas trabalhadas, notificações automáticas e geração de relatórios.

---

## Requisitos Funcionais (RF)

### Acesso e Segurança
- **RF01:** Cadastro e autenticação de usuários (e-mail + senha).
- **RF02:** Painel exclusivo para desenvolvedor (com abas específicas).
- **RF03:** Painel exclusivo para gestor (com abas específicas).

### Perfil Desenvolvedor
- **RF04:** Visualizar suas tarefas, progresso da equipe e detalhes de colegas.
- **RF05:** Alterar status de suas tarefas de `PENDENTE`/`ATRASADO` para `FEITO`.
- **RF06:** Enviar relatórios (tarefa ou projeto).
- **RF07:** Solicitar reorganização de tarefas (com justificativa).

### Perfil Gestor
- **RF08:** Visualizar todos os projetos, tarefas e prazos da equipe.
- **RF09:** Criar projetos, tarefas avulsas e tarefas dentro de projetos (com horas estimadas).
- **RF10:** Aprovar ou rejeitar solicitações de reorganização (reatribuindo tarefas se aprovado).
- **RF11:** Validar tarefas e projetos (mudar status de `FEITO` para `PRONTO`).
- **RF12:** Reatribuir tarefas com status `ATRASADO` para outro desenvolvedor.

### Automações e Notificações
- **RF13:** Notificar o gestor automaticamente (pop‑up) quando um DEV altera o status de uma tarefa.
- **RF14:** Gerar relatório detalhado da equipe (tarefas cumpridas, atrasadas, relatórios enviados).
- **RF15:** Alterar automaticamente tarefas `PENDENTES` para `ATRASADAS` ao expirar o prazo.
- **RF16:** Notificar o gestor se existirem tarefas com status `FEITO` (via pop‑up).
- **RF17:** Alertar o gestor se existirem tarefas com status `ATRASADO` (via pop‑up).

---

## Tecnologias Utilizadas

- IDE: IntelliJ IDEA
- Java SE Development Kit 25.0.2
- MySQL Workbench 8.0
- Driver MySQL Connector/J (9.7.0)

---

### Pré-requisitos do sistema

- Servidor MySQL instalado.
- Driver JDBC do MySQL.

---

## Estrutura do Projeto

```
/  
├── src/ # Código-fonte Java  
│ ├── Sistema.java  
│ ├── Usuario.java / UsuarioDev.java / UsuarioGestor.java  
│ ├── Projeto.java / Tarefa.java  
│ ├── Relatorio.java / Solicitacao.java  
│ ├── *DAOs.java  
│ ├── Dashboard.java  
│ ├── LoginScreen.java  
│ ├── Main.java  
│ └── enums (StatusTarefa, StatusSolicitacao, NivelImportancia, TipoUsuario)  
│  
├── Script do MySQL/ # Scripts de banco de dados  
│ ├── script_mysql.sql # Criação do esquema  
│ └── dados_teste.sql # Dados de exemplo  
│  
├── lib/ # JDBC  
│ └── mysql-connector-j-9.7.0.jar  
│  
└── README.md  
```

---

## Configuração do Banco de Dados

1. **Instale e inicie o MySQL Workbench** (padrão: `localhost:3306`).
2. **Execute o script de criação do esquema** (`script_mysql.sql`).
    - O script cria o banco `gestao_rotina` e todas as tabelas necessárias.
3. **(Opcional)** Para popular o sistema com dados de teste, execute também `dados_teste.sql`.

4. **Credenciais de acesso:**
    - O arquivo `DatabaseConnection.java` usa `root` como usuário e `root` como senha.
    - Se o seu MySQL tiver credenciais diferentes, altere as constantes `USER` e `PASSWORD` nesse arquivo.

---

## Como Executar o Projeto

1. Abra o projeto na sua IDE (IntelliJ/Eclipse/NetBeans).
2. Adicione a biblioteca `mysql-connector-j-9.7.0.jar` ao classpath do projeto.
3. Certifique-se de que o MySQL está rodando e que o banco `gestao_rotina` foi criado.
4. Execute a classe `Main.java`.

---

## Estrutura do Código e Principais Métodos

O projeto é organizado em camadas implícitas: **model** (entidades), **dao** (acesso a dados), **sistema** (lógica de negócio centralizada) e **view** (interface gráfica Swing).

### Entidades (model)

| Classe               | Descrição / Métodos Principais                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Usuario` (abstrata) | Base para `UsuarioDev` e `UsuarioGestor` – contém `id`, `nome`, `cpf`, `email`, `senha`, `tipoUsuario`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `UsuarioDev`         | Representa o desenvolvedor. Métodos principais: <br> • `carregarTarefas()` – retorna lista de tarefas do banco. <br> • `visualizarProgressoEquipe()` – exibe progresso dos colegas da mesma equipe. <br> • `visualizarDetalhesColega(UsuarioDev colega)` – mostra tarefas e projetos de outro dev. <br> • `alterarStatusTarefa(Tarefa, StatusTarefa)` – altera status da própria tarefa (apenas PENDENTE/ATRASADO → FEITO). <br> • `enviarRelatorioFinal(Object item, String conteudo)` – envia relatório (tarefa ou projeto). <br> • `solicitarReorganizacao(Tarefa, String justificativa)` – cria solicitação para o gestor. <br> • `calcularProgressoTotal()` – média do progresso de todas as suas tarefas.                                                                                                                                                                    |
| `UsuarioGestor`      | Representa o gestor. Métodos principais: <br> • `getEquipe()` – lista desenvolvedores sob sua gestão. <br> • `visualizarTodosProjetosTarefas()` – retorna string com todos os projetos e tarefas da equipe. <br> • `criarProjeto(String nome, Date prazo, NivelImportancia importancia)` – persiste novo projeto. <br> • `criarAtribuirTarefa(String descricao, Date prazo, NivelImportancia importancia, int devId, double horasEstimadas)` – cria tarefa avulsa. <br> • `criarAtribuirTarefaEmProjeto(...)` – cria tarefa vinculada a um projeto existente. <br> • `processarSolicitacao(Solicitacao, boolean aprovado)` – aprova/rejeita solicitação (atualiza status no banco). <br> • `validarFinalizacao(Object item)` – valida tarefa ou projeto (muda status FEITO → PRONTO). <br> • `listarSolicitacoesPendentes()` – retorna solicitações com status PENDENTE da equipe. |
| `Projeto`            | Agrupa tarefas. Métodos principais: <br> • `calcularProgresso(List<Tarefa> tarefasDoProjeto)` – média do progresso das tarefas. <br> • `getTotalHorasTrabalhadas()` e `getTotalHorasEstimadas()` – somatórios. <br> • `verificarConclusao(List<Tarefa>, ProjetoDAO)` – se todas as tarefas estão PRONTO, muda status do projeto para FEITO e persiste. <br> • `getInformacoesDetalhadas()` – versão simples e completa (com lista de tarefas).                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `Tarefa`             | Unidade de trabalho. Métodos principais: <br> • `calcularProgresso()` – baseado em `min(100, (horasTrabalhadas/horasEstimadas)*100)` ou 100% se status PRONTO/FEITO. <br> • `adicionarHorasTrabalhadas(double horas)` – incrementa e persiste via TarefaDAO. <br> • `getInformacoesDetalhadas()` – exibe ID, descrição, prazo, status, progresso, horas.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `Relatorio`          | Armazena `conteudo`, `dataEnvio`, referência opcional a `tarefaRelacionada` ou `projetoRelacionado`, e o `devRemetente`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `Solicitacao`        | Armazena `justificativa`, `status` (PENDENTE/APROVADA/REJEITADA), `dataCriacao`, `solicitante` (UsuarioDev) e `tarefaRelacionada`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |

### Camada de Persistência (DAO)

| Classe               | Responsabilidade / Métodos Principais                                                                                                                                                                                                              |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DatabaseConnection` | Gerencia a conexão com MySQL (`getConnection()`). Credenciais fixas (usuário `root`, senha `root`).                                                                                                                                                |
| `UsuarioDAO`         | `inserir(Usuario)`, `autenticar(email, senha)`, `buscarPorId(int)`, `buscarPorEmail(String)`, `buscarPorCpf(String)`, `listarDevsPorGestor(int)`, `listarTodos()`.                                                                                 |
| `ProjetoDAO`         | `inserir(Projeto)`, `buscarPorId(int)`, `listarTodos()`, `listarPorGestor(int)`, `atualizarStatus(int, StatusTarefa)`.                                                                                                                             |
| `TarefaDAO`          | `inserir(Tarefa)`, `buscarPorId(int, UsuarioDAO, ProjetoDAO)`, `listarPorDev(int, ...)`, `listarPorProjeto(int, ...)`, `listarTodas()`, `atualizarStatus(int, StatusTarefa)`, `adicionarHorasTrabalhadas(int, double)`, `reatribuirDev(int, int)`. |
| `RelatorioDAO`       | `inserir(Relatorio)`, `listarTodos()` (carrega também as referências a tarefa/projeto/dev).                                                                                                                                                        |
| `SolicitacaoDAO`     | `inserir(Solicitacao)`, `listarTodos()`, `listarPorGestor(int)`, `atualizarStatus(int, StatusSolicitacao)`.                                                                                                                                        |

### Classe `Sistema`

Centraliza as operações do sistema, integrando os DAOs e controlando verificações automáticas.

**Principais métodos:**
- `realizarCadastro(Usuario)` – verifica duplicidade de e-mail/CPF antes de inserir.
- `autenticar(String email, String senha)` – retorna o usuário logado ou null.
- `adicionarProjeto(Projeto)`, `adicionarTarefa(Tarefa)`, `adicionarRelatorio(Relatorio)`, `adicionarSolicitacao(Solicitacao)` – delegam ao respectivo DAO.
- `buscarDevPorId(int)`, `buscarProjetoPorId(int)`, `buscarGestorPorDev(UsuarioDev)` – consultas auxiliares.
- `getDevs()`, `getGestores()`, `getProjetos()`, `getTarefas()`, `getRelatorios()`, `getSolicitacoes()` – listagens completas.
- `getSolicitacoesPorGestor(int gestorId)` – filtra solicitações dos devs da equipe.
- `getTarefasDaEquipe(int gestorId)`, `getProjetosDaEquipe(int gestorId)` – tarefas/projetos vinculados aos devs do gestor.
- `notificarConsoleGestorMudancaStatus(Tarefa, UsuarioDev)` – faz notificação via console para o gestor.
- `verificarPrazosExpirados()` – percorre tarefas e projetos; os com status PENDENTE e prazo vencido tornam-se ATRASADO (persistido via DAO).
- `gerarRelatorioEquipe(int gestorId)` – retorna string detalhada com estatísticas e lista de relatórios enviados.
- `iniciarVerificadorPrazos(long intervalo)` – agenda execução periódica de `verificarPrazosExpirados()`.
- `pararVerificadorPrazos()` – cancela o timer.

### Enums

- `StatusTarefa`: `PENDENTE`, `FEITO`, `PRONTO`, `ATRASADO`
- `StatusSolicitacao`: `PENDENTE`, `APROVADA`, `REJEITADA`
- `NivelImportancia`: `BAIXA`, `MEDIA`, `ALTA`, `URGENTE`
- `TipoUsuario`: `DEV`, `GESTOR`

### Interface Gráfica (Swing)

- `LoginScreen` – tela de autenticação e cadastro de novos usuários (com máscara para CPF e campos condicionais para gestor/desenvolvedor).
- `Dashboard` – janela principal com abas (JTabbedPane). O conteúdo das abas é diferente para `UsuarioDev` e `UsuarioGestor`.
    - Painéis implementam a interface `Refreshable` para atualizar dados após ações.
    - Inclui funcionalidades como: adicionar horas, alterar status, validar tarefas, reatribuir tarefas atrasadas, gerenciar solicitações, gerar relatório, verificar notificações, etc.
    - Utiliza-se de um mecanismo de pooling em `verificarNotificacoes` para notificações pop-up.
- `Main` – ponto de entrada; abre ` LoginScreen`.

---

# Utilização do Sistema

## Tela de Login

- Informe e-mail e senha cadastrados.
- Botão **“Cadastrar”** permite criar novo usuário (escolha entre Gestor ou Desenvolvedor).
- Para desenvolvedor, é obrigatório vincular a um gestor já existente.

## Painel do Desenvolvedor

### Minhas Tarefas
- Lista com: ID, descrição, projeto, prazo, status, progresso (%) e horas trabalhadas/estimadas.

**Botões:**
- **Adicionar Horas** – registra horas trabalhadas.
- **Marcar como FEITO** – altera status para FEITO.

### Progresso da Equipe
- Exibe o percentual geral de cada colega da mesma equipe.

### Detalhes de Colega
- Seleciona um colega e visualiza suas tarefas e projetos detalhadamente.

### Enviar Relatório
- Escolhe uma tarefa ou projeto, escreve o conteúdo e envia ao gestor.

### Solicitar Reorganização
- Seleciona uma tarefa **PENDENTE**, escreve justificativa e envia solicitação ao gestor.

## Painel do Gestor

### Visão Geral
- Mostra todos os projetos da equipe com suas tarefas e também tarefas avulsas.

### Criar Projeto/Tarefa
Permite criar:

- **Projeto** (nome, prazo em dias, importância).
- **Tarefa avulsa** (desenvolvedor, descrição, prazo, importância, horas estimadas).
- **Tarefa dentro de um projeto** (mesmos dados + projeto).

### Validar Finalizações
Lista tarefas com status **FEITO**. O gestor pode:
- **Validar** (torna PRONTO)
- **Rejeitar** (volta para PENDENTE)

### Solicitações Pendentes
- Exibe pedidos de reorganização. **Aprovar** permite reatribuir a tarefa a outro desenvolvedor da equipe.

### Reatribuir Atrasadas
- Lista tarefas **ATRASADO** e permite transferi‑las para outro desenvolvedor (com opção de alterar o prazo).

### Relatório
- Gera um relatório completo da equipe (tarefas prontas, atrasadas, todos os relatórios enviados).

## Automatismos em segundo plano

- A cada 60 segundos o sistema verifica prazos expirados e altera tarefas/projetos **PENDENTE** para **ATRASADO**.
- Quando um desenvolvedor marca uma tarefa como **FEITO**, o gestor recebe um pop‑up de notificação.
- As notificações visuais (pop‑ups) para tarefas que se tornam FEITO ou ATRASADO são exibidas através de um mecanismo de polling (consulta ao banco a cada 5 segundos).
- Se uma tarefa vence o prazo, o gestor é alertado.
- Quando todas as tarefas de um projeto estão **PRONTO**, o projeto automaticamente se torna **FEITO** (e depois o gestor pode validá‑lo para **PRONTO**).

---

# Observações Finais

- **O sistema impede** que um desenvolvedor/gestor alterem coisas que não lhe pertencem.
- **O gestor não pode marcar tarefas como `FEITO`** – apenas validar de `FEITO` para `PRONTO`.
- **O gestor pode reatribuir tarefas atrasadas** a qualquer membro da equipe.
- **O cadastro é bloqueado** se o e‑mail já existir.
- **As horas trabalhadas** só podem ser adicionadas se a tarefa ainda não estiver concluída.