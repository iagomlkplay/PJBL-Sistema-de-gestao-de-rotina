# Sistema de Gestão de Rotina

Sistema desenvolvido em **Java** para gerenciar projetos, tarefas, prazos e o desempenho de equipes de desenvolvimento. O sistema implementa os papéis de **Desenvolvedor (DEV)** e **Gestor (GESTOR)**, com validação de tarefas, métricas de tempo (horas), notificações automáticas e relatórios.

> **Status atual:** Lógica de negócios e persistência em banco de dados (MySQL). **Interface gráfica (Swing) ainda será implementada**

---

## Requisitos Funcionais (RF)

### Acesso e Segurança
- **RF01:** Cadastro e autenticação de usuários (por e‑mail e senha).
- **RF02:** Painel (visualização) exclusivo para DEV.
- **RF03:** Painel (visualização) exclusivo para GESTOR.

### Perfil Desenvolvedor
- **RF04:** Visualizar seus próprios projetos/tarefas e o progresso dos colegas.
- **RF05:** Alterar status de suas tarefas de `PENDENTE` (ou `ATRASADO`) para `FEITO`.
- **RF06:** Enviar relatórios detalhados ao finalizar uma tarefa ou projeto.
- **RF07:** Solicitar reorganização de suas tarefas (com justificativa).

### Perfil Gestor
- **RF08:** Visualizar todos os projetos, tarefas e prazos da equipe.
- **RF09:** Criar e atribuir projetos/tarefas com prazo, importância e horas estimadas.
- **RF10:** Aprovar ou reprovar solicitações de reorganização feitas pelos DEVs.
- **RF11:** Validar tarefas/projetos (mudar status de `FEITO` para `PRONTO`).
- **RF12:** Reatribuir tarefas com status `ATRASADO` para outro desenvolvedor.

### Automações e Notificações
- **RF13:** Notificar o gestor automaticamente quando um DEV modificar o status de uma tarefa.
- **RF14:** Gerar relatório automático (tarefas cumpridas, atrasadas, relatórios enviados).
- **RF15:** Alterar automaticamente tarefas `PENDENTES` para `ATRASADAS` ao expirar o prazo.
- **RF16:** Notificar o gestor se existirem tarefas com status `FEITO`.
- **RF17:** Alertar o gestor se existirem tarefas com status `ATRASADO`.

---

## Estrutura do Código e Principais Métodos

O projeto é organizado em camadas: **model** (entidades), **dao** (acesso a dados) e **sistema**. A seguir, as classes principais e suas responsabilidades na versão atual.

### Entidades (model)

| Classe               | Descrição                                                                                                                                                                                                                                                                             |
|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Usuario` (abstrata) | Base para `UsuarioDev` e `UsuarioGestor` – contém id, nome, e‑mail, senha.                                                                                                                                                                                                            |
| `UsuarioDev`         | Desenvolvedor – possui lista de tarefas (carregada do banco). Métodos: `alterarStatusTarefa()`, `visualizarPropriosProjetosTarefas()`, `visualizarProgressoEquipe()`, `visualizarDetalhesColega()`, `enviarRelatorioFinal()`, `solicitarReorganizacao()`, `calcularProgressoTotal()`. |
| `UsuarioGestor`      | Gestor – gerencia equipe, projetos e tarefas. Métodos: `criarProjeto()`, `criarAtribuirTarefa()`, `criarAtribuirTarefaEmProjeto()`, `validarFinalizacao()`, `processarSolicitacaoMudanca()`, `reatribuirTarefaAtrasada()`, `listarSolicitacoesPendentes()`.                           |
| `Projeto`            | Agrupa tarefas. Possui métodos para calcular progresso (recebendo lista de tarefas) e verificar conclusão (`verificarConclusao()` – atualiza status do projeto para `FEITO` automaticamente).                                                                                         |
| `Tarefa`             | Unidade de trabalho – contém `descricao`, `prazo`, `nivelImportancia`, `status`, `horasEstimadas`, `horasTrabalhadas`, referência ao `projetoPai`. Métodos: `calcularProgresso()` (baseado em horas), `adicionarHorasTrabalhadas()`.                                                  |
| `Relatorio`          | Armazena conteúdo, data de envio e referência à tarefa/projeto relacionado.                                                                                                                                                                                                           |
| `SolicitacaoMudanca` | Armazena justificativa, status (`PENDENTE`/`APROVADA`/`REJEITADA`) e o desenvolvedor solicitante.                                                                                                                                                                                     |

### Camada de Persistência (DAO)

| Classe               | Responsabilidade                                                                                                                                                                           |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DatabaseConnection` | Gerencia a conexão com MySQL (URL, usuário, senha).                                                                                                                                        |
| `UsuarioDAO`         | CRUD de usuários, autenticação, busca por e-mail, listagem de devs por gestor.                                                                                                             |
| `ProjetoDAO`         | Inserir, buscar por ID, listar todos, atualizar status, deletar.                                                                                                                           |
| `TarefaDAO`          | Inserir, buscar por ID, listar por desenvolvedor, listar por projeto, listar todas (com relacionamentos carregados), atualizar status, adicionar horas, reatribuir desenvolvedor, deletar. |
| `RelatorioDAO`       | Inserir, listar todos, deletar.                                                                                                                                                            |
| `SolicitacaoDAO`     | Inserir, listar todos, deletar, atualizar status (para processamento de aprovação/rejeição).                                                                                               |

### Classe `Sistema`

Centraliza as operações do sistema, como cadastro, autenticação, obtenção de listas (que delegam aos DAOs), notificações, verificação de prazos expirados, geração de relatório e controle de timer para verificação automática de prazos.

Principais métodos:
- `realizarCadastro(Usuario)`, `autenticar(email, senha)`
- `adicionarProjeto()`, `adicionarTarefa()`, `adicionarRelatorio()`, `adicionarSolicitacao()`
- `buscarDevPorId()`, `buscarProjetoPorId()`, `buscarGestorPorDev()`
- `getUsuarios()`, `getDevs()`, `getGestores()`, `getProjetos()`, `getTarefas()`, `getRelatorios()`, `getSolicitacoes()`
- `notificarGestorMudancaStatus()`, `verificarPrazosExpirados()`, `gerarRelatorio()`
- `iniciarVerificadorPrazos(long intervalo)`, `pararVerificadorPrazos()`

### Enums

- `StatusTarefa`: `PENDENTE`, `FEITO`, `PRONTO`, `ATRASADO`
- `StatusSolicitacao`: `PENDENTE`, `APROVADA`, `REJEITADA`
- `NivelImportancia`: `BAIXA`, `MEDIA`, `ALTA`, `URGENTE`
- `TipoUsuario`: `DEV`, `GESTOR`

---

## Como o Sistema Funciona (Fluxo de Trabalho)

1. **Cadastro** – Usuários são criados via `Sistema.realizarCadastro()`. O sistema verifica e‑mail duplicado e associa o gestor ao desenvolvedor (coluna `gestor_id`).
2. **Login** – Autenticação por e‑mail + senha (`Sistema.autenticar()`).
3. **Criação de projetos e tarefas** – O gestor usa `criarProjeto()` para criar um projeto. Em seguida, `criarAtribuirTarefaEmProjeto()` cria tarefas dentro do projeto, associando cada tarefa a um desenvolvedor da equipe. Também pode criar tarefas avulsas (`criarAtribuirTarefa`).
4. **Registro de horas** – O desenvolvedor chama `tarefa.adicionarHorasTrabalhadas(horas)`. O método impede adição se a tarefa já estiver `FEITO` ou `PRONTO`, e persiste a alteração no banco.
5. **Alteração de status** – O desenvolvedor marca a tarefa como `FEITO` via `alterarStatusTarefa()`. Apenas transições de `PENDENTE` ou `ATRASADO` → `FEITO` são permitidas. Após a mudança, o gestor é notificado.
6. **Validação do gestor** – O gestor visualiza tarefas `FEITO` e as valida para `PRONTO` usando `validarFinalizacao()`. A tarefa é então considerada concluída. Quando todas as tarefas de um projeto estão `PRONTO`, o projeto automaticamente se torna `FEITO` (via `Projeto.verificarConclusao()`). O gestor pode então validar o projeto `FEITO → PRONTO`.
7. **Expiração de prazos** – O método `Sistema.verificarPrazosExpirados()` é executado periodicamente por um `Timer` (ou pode ser chamado manualmente). Tarefas e projetos com status `PENDENTE` e prazo anterior à data atual são alterados para `ATRASADO`.
8. **Reatribuição de tarefas atrasadas** – O gestor pode usar `reatribuirTarefaAtrasada()` para transferir uma tarefa `ATRASADO` para outro membro da equipe.
9. **Solicitações de reorganização** – Desenvolvedor chama `solicitarReorganizacao()`. O gestor lista as solicitações pendentes e decide aprovar ou rejeitar (`processarSolicitacaoMudanca()`). Em caso de aprovação, o gestor pode reatribuir tarefas manualmente.
10. **Relatório** – `Sistema.gerarRelatorio()` produz um resumo com quantidade de tarefas cumpridas (`PRONTO`), atrasadas, e lista os relatórios enviados pelos desenvolvedores. O relatório é armazenado no banco.
11. **Métrica de progresso** – Progresso da tarefa = `min(100, (horasTrabalhadas / horasEstimadas) * 100)` (ou 100% se status `FEITO`/`PRONTO`). Progresso do projeto = média do progresso de suas tarefas. Progresso do desenvolvedor = média do progresso de todas as suas tarefas.

---

- **Apenas o desenvolvedor responsável** pode alterar o status de uma tarefa (`alterarStatusTarefa` verifica se a tarefa está em sua lista pessoal).
- **O gestor não pode marcar tarefas como `FEITO`** – apenas validar de `FEITO` para `PRONTO`.
- **O gestor pode reatribuir tarefas atrasadas** a qualquer membro da equipe.
- **O cadastro é bloqueado** se o e‑mail já existir.


### Pré-requisitos

- **MySQL** instalado (padrão: `localhost:3306`).
- Usuário `root` com senha `root` (ou altere as credenciais em `DatabaseConnection.java`).
- Driver JDBC do MySQL.