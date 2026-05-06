# Sistema de Gestão de Rotina

Sistema desenvolvido em **Java** para gerenciar projetos, tarefas, prazos e o desempenho de equipes de desenvolvimento. O sistema implementa os papéis de **Desenvolvedor (DEV)** e **Gestor (GESTOR)**, com validação de tarefas, métricas de tempo, notificações automáticas e relatórios diários.

> **Status atual:** Versão de lógica de negócios (backend) sem interface gráfica e sem persistência em banco de dados. Todos os dados são armazenados em memória durante a execução.

---

## Requisitos Funcionais (RF) Atendidos

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

## Estrutura do Código

### Classes principais

| Classe | Descrição |
|--------|-----------|
| `Usuario` (abstrata) | Base para `UsuarioDev` e `UsuarioGestor` |
| `UsuarioDev` | Desenvolvedor – possui lista de tarefas atribuídas |
| `UsuarioGestor` | Gestor – gerencia equipe, projetos e tarefas |
| `Projeto` | Agrupa tarefas (não possui dono) |
| `Tarefa` | Unidade de trabalho – contém horas estimadas, horas trabalhadas, status, projeto pai |
| `Sistema` (Singleton) | Centraliza listas de usuários, projetos, tarefas, relatórios, solicitações. Gerencia cadastro, autenticação e notificações. |
| `Relatorio` | Relatório enviado por DEV ou gerado automaticamente (diário) |
| `SolicitacaoMudanca` | Pedido de reorganização feito por DEV |

### Enums
- `StatusTarefa`: PENDENTE, FEITO, PRONTO, ATRASADO
- `StatusSolicitacao`: PENDENTE, APROVADA, REJEITADA
- `NivelImportancia`: BAIXA, MEDIA, ALTA, URGENTE
- `TipoUsuario`: DESENVOLVEDOR, GESTOR
