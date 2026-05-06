# Sistema de Gestão de Rotina

Sistema desenvolvido em **Java** para gerenciar projetos, tarefas, prazos e o desempenho de equipes de desenvolvimento. O sistema implementa os papéis de **Desenvolvedor (DEV)** e **Gestor (GESTOR)**, com validação de tarefas, métricas de tempo (horas), notificações automáticas e relatórios diários.

> **Status atual:** Versão de lógica de negócios (backend) sem interface gráfica e sem persistência em banco de dados. Todos os dados são armazenados em memória durante a execução.

---

## Requisitos Funcionais (RF)

### Acesso e Segurança
- **RF01:** Cadastro e autenticação de usuários (por e‑mail e senha).
- **RF02:** Painel (visualização) exclusivo para DEV.
- **RF03:** Painel (visualização) exclusivo para GESTOR.

### Perfil Desenvolvedor
- **RF04:** Visualizar seus próprios projetos/tarefas e o progresso dos colegas.
- **RF05:** Alterar status de suas tarefas de `PENDENTE` para `FEITO`.
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
- **RF14:** Gerar relatório diário automático (tarefas cumpridas, atrasadas, relatórios enviados).
- **RF15:** Alterar automaticamente tarefas `PENDENTES` para `ATRASADAS` ao expirar o prazo.
- **RF16:** Notificar o gestor se existirem tarefas com status `FEITO`.
- **RF17:** Alertar o gestor se existirem tarefas com status `ATRASADO`.

---

## Estrutura do Código e Principais Métodos

### Classes principais e suas responsabilidades

| Classe | Descrição |
|--------|-----------|
| `Usuario` (abstrata) | Base para `UsuarioDev` e `UsuarioGestor` – contém id, nome, e‑mail, senha. |
| `UsuarioDev` | Desenvolvedor – possui lista de tarefas atribuídas. Métodos principais: `alterarStatusTarefa()`, `visualizarPropriosProjetosTarefas()`, `visualizarProgressoEquipe()`, `visualizarDetalhesColega()`, `enviarRelatorioFinal()`, `solicitarReorganizacao()`, `calcularProgressoTotal()`. |
| `UsuarioGestor` | Gestor – gerencia equipe, projetos e tarefas. Métodos principais: `criarProjeto()`, `criarAtribuirTarefa()`, `criarAtribuirTarefaEmProjeto()`, `validarFinalizacao()`, `processarSolicitacaoMudanca()`, `reatribuirTarefaAtrasada()`, `listarSolicitacoesPendentes()`. |
| `Projeto` | Agrupa tarefas. Métodos principais: `calcularProgresso()`, `verificarConclusao()` (automaticamente marca projeto como `FEITO` quando todas as tarefas estão `PRONTO`). |
| `Tarefa` | Unidade de trabalho – contém `descricao`, `prazo`, `nivelImportancia`, `status`, `horasEstimadas`, `horasTrabalhadas`, referência ao `projetoPai`. Métodos principais: `calcularProgresso()` (baseado em horas), `adicionarHorasTrabalhadas()`. |
| `Sistema` | Centraliza listas de usuários, projetos, tarefas, relatórios e solicitações. Gerencia cadastro, autenticação e notificações. Métodos principais: `realizarCadastro()`, `autenticar()`, `gerarRelatorioDiario()`, `verificarPrazosExpirados()`, `notificarGestorMudancaStatus()`. |
| `Relatorio` | Armazena conteúdo, data de envio e referência à tarefa/projeto relacionado. |
| `SolicitacaoMudanca` | Armazena justificativa, status (PENDENTE/APROVADA/REJEITADA) e o desenvolvedor solicitante. |

### Enums
- `StatusTarefa`: PENDENTE, FEITO, PRONTO, ATRASADO
- `StatusSolicitacao`: PENDENTE, APROVADA, REJEITADA
- `NivelImportancia`: BAIXA, MEDIA, ALTA, URGENTE
- `TipoUsuario`: DESENVOLVEDOR, GESTOR

---

## Como o Sistema Funciona

### Fluxo de Trabalho Típico

1. **Cadastro** – Usuários criados via `Sistema.realizarCadastro()`. O sistema verifica se o e‑mail já está cadastrado e atribui um ID incremental.
2. **Login** – Autenticação por e‑mail + senha (`Sistema.autenticar()`).
3. **Criação de projetos e tarefas** – O gestor usa `criarProjeto()` para criar um projeto (sem responsável). Em seguida, `criarAtribuirTarefaEmProjeto()` cria tarefas dentro do projeto, associando cada tarefa a um desenvolvedor específico (pode ser qualquer dev da equipe). Também é possível criar tarefas avulsas (`criarAtribuirTarefa`).
4. **Registro de horas** – O desenvolvedor chama `tarefa.adicionarHorasTrabalhadas(horas)` para incrementar as horas trabalhadas. O método impede a adição se a tarefa já estiver `FEITO` ou `PRONTO`.
5. **Alteração de status** – Quando a tarefa está pronta, o dev a marca como `FEITO` via `alterarStatusTarefa()`. O sistema verifica se a tarefa pertence a ele e se não está `PRONTO`. Após a mudança, o gestor é notificado imediatamente (RF13) e o projeto pai (se houver) tem seu método `verificarConclusao()` chamado.
6. **Validação do gestor** – O gestor visualiza tarefas `FEITO` e as valida para `PRONTO` usando `validarFinalizacao()`. A tarefa é então considerada concluída. Quando todas as tarefas de um projeto estão `PRONTO`, o projeto automaticamente se torna `FEITO` (via `verificarConclusao()`). O gestor pode então validar o projeto `FEITO → PRONTO`.
7. **Expiração de prazos** – Chamando `Sistema.verificarPrazosExpirados()`, o sistema percorre tarefas e projetos com status `PENDENTE` e prazo anterior à data atual, alterando-os para `ATRASADO` e notificando o gestor.
8. **Reatribuição de tarefas atrasadas** – O gestor pode usar `reatribuirTarefaAtrasada()` para transferir uma tarefa `ATRASADO` para outro desenvolvedor da equipe.
9. **Solicitações de reorganização** – Um desenvolvedor pode solicitar ajuda (`solicitarReorganizacao()`). O gestor lista as solicitações pendentes (`listarSolicitacoesPendentes()`) e decide aprovar ou rejeitar (`processarSolicitacaoMudanca()`). Em caso de aprovação, o gestor pode reatribuir tarefas manualmente.
10. **Relatório diário** – `Sistema.gerarRelatorioDiario()` produz um resumo com quantidade de tarefas cumpridas (status `PRONTO`), atrasadas, e lista os relatórios enviados pelos desenvolvedores. O relatório é armazenado em uma lista separada (`relatoriosDiarios`) para manter histórico.
11. **Métrica de progresso** – O progresso de uma tarefa é `min(100, (horasTrabalhadas / horasEstimadas) * 100)` (ou 100% se status `FEITO`/`PRONTO`). O progresso de um projeto é a média do progresso de suas tarefas. O progresso de um desenvolvedor é a média do progresso de todas as suas tarefas.

### Permissões e Segurança

- **Apenas o desenvolvedor responsável** pode alterar o status de uma tarefa (`alterarStatusTarefa` verifica se a tarefa está em sua lista pessoal).
- **O gestor não pode marcar tarefas como `FEITO`** – apenas validar de `FEITO` para `PRONTO`.
- **O gestor pode reatribuir tarefas atrasadas** a qualquer membro da equipe.
- **O cadastro é bloqueado** se o e‑mail já existir.
