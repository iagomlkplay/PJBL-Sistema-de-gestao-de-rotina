-- ============================
-- INSERÇÃO DE DADOS DE TESTE
-- ============================

-- Usar o banco de dados
USE gestao_rotina;

-- ------------------------------------------------------------
-- 1. Inserir Gestores (5 gestores com departamentos variados)
-- ------------------------------------------------------------
INSERT INTO usuarios (nome, cpf, email, senha, tipo, departamento, gestor_id) VALUES
('Ana Silva', '111.222.333-44', 'ana.silva@email.com', 'senha', 'GESTOR', 'Tecnologia', NULL),
('Carlos Souza', '222.333.444-55', 'carlos.souza@email.com', 'senha', 'GESTOR', 'Marketing', NULL),
('Fernanda Lima', '333.444.555-66', 'fernanda.lima@email.com', 'senha', 'GESTOR', 'Vendas', NULL),
('Ricardo Mendes', '444.555.666-77', 'ricardo.mendes@email.com', 'senha', 'GESTOR', 'Recursos Humanos', NULL),
('Juliana Costa', '555.666.777-88', 'juliana.costa@email.com', 'senha', 'GESTOR', 'Financeiro', NULL);

-- ------------------------------------------------------------
-- 2. Inserir Desenvolvedores (20 desenvolvedores, distribuídos entre os gestores)
-- ------------------------------------------------------------
-- Gestor 1 (Ana Silva) – 5 devs
INSERT INTO usuarios (nome, cpf, email, senha, tipo, departamento, gestor_id) VALUES
('Bruno Alves', '111.111.111-11', 'bruno.alves@email.com', 'senha', 'DEV', NULL, 1),
('Carla Dias', '222.222.222-22', 'carla.dias@email.com', 'senha', 'DEV', NULL, 1),
('Diego Nunes', '333.333.333-33', 'diego.nunes@email.com', 'senha', 'DEV', NULL, 1),
('Elaine Rocha', '444.444.444-44', 'elaine.rocha@email.com', 'senha', 'DEV', NULL, 1),
('Fabio Torres', '555.555.555-55', 'fabio.torres@email.com', 'senha', 'DEV', NULL, 1);

-- Gestor 2 (Carlos Souza) – 4 devs
INSERT INTO usuarios (nome, cpf, email, senha, tipo, departamento, gestor_id) VALUES
('Gabriela Freitas', '666.666.666-66', 'gabriela.freitas@email.com', 'senha', 'DEV', NULL, 2),
('Henrique Brito', '777.777.777-77', 'henrique.brito@email.com', 'senha', 'DEV', NULL, 2),
('Igor Campos', '888.888.888-88', 'igor.campos@email.com', 'senha', 'DEV', NULL, 2),
('Jessica Martins', '999.999.999-99', 'jessica.martins@email.com', 'senha', 'DEV', NULL, 2);

-- Gestor 3 (Fernanda Lima) – 4 devs
INSERT INTO usuarios (nome, cpf, email, senha, tipo, departamento, gestor_id) VALUES
('Leonardo Monteiro', '121.212.121-21', 'leonardo.monteiro@email.com', 'senha', 'DEV', NULL, 3),
('Mariana Ribeiro', '131.313.131-31', 'mariana.ribeiro@email.com', 'senha', 'DEV', NULL, 3),
('Natalia Castro', '141.414.141-41', 'natalia.castro@email.com', 'senha', 'DEV', NULL, 3),
('Otavio Lopes', '151.515.151-51', 'otavio.lopes@email.com', 'senha', 'DEV', NULL, 3);

-- Gestor 4 (Ricardo Mendes) – 4 devs
INSERT INTO usuarios (nome, cpf, email, senha, tipo, departamento, gestor_id) VALUES
('Patricia Ferreira', '161.616.161-61', 'patricia.ferreira@email.com', 'senha', 'DEV', NULL, 4),
('Rafael Oliveira', '171.717.171-71', 'rafael.oliveira@email.com', 'senha', 'DEV', NULL, 4),
('Sabrina Cardoso', '181.818.181-81', 'sabrina.cardoso@email.com', 'senha', 'DEV', NULL, 4),
('Thiago Azevedo', '191.919.191-91', 'thiago.azevedo@email.com', 'senha', 'DEV', NULL, 4);

-- Gestor 5 (Juliana Costa) – 3 devs
INSERT INTO usuarios (nome, cpf, email, senha, tipo, departamento, gestor_id) VALUES
('Vanessa Souza', '202.020.202-02', 'vanessa.souza@email.com', 'senha', 'DEV', NULL, 5),
('Wagner Lima', '212.121.212-12', 'wagner.lima@email.com', 'senha', 'DEV', NULL, 5),
('Yara Nunes', '222.222.222-23', 'yara.nunes@email.com', 'senha', 'DEV', NULL, 5);

-- ---------------------
-- 3. Inserir Projetos
-- ---------------------

INSERT INTO projetos (nome, prazo, importancia, status, gestor_id) VALUES
('Sistema de BI', '2027-12-31', 'ALTA', 'PENDENTE', 1),
('Portal de Transparência', '2027-10-15', 'URGENTE', 'PENDENTE', 1),
('Aplicativo Mobile', '2024-09-30', 'MEDIA', 'PRONTO', 2),
('Migração de Dados', '2026-08-20', 'ALTA', 'FEITO', 3),
('Atualização de Segurança', '2027-11-10', 'URGENTE', 'PENDENTE', 4),
('E-commerce B2B', '2027-01-20', 'ALTA', 'PENDENTE', 5);

-- --------------------
-- 4. Inserir Tarefas
-- --------------------
-- Combinação de devs e projetos, com horas estimadas, horas trabalhadas e status variados.

INSERT INTO tarefas (descricao, prazo, nivel_importancia, status, horas_estimadas, horas_trabalhadas, dev_responsavel_id, projeto_id) VALUES
-- Projeto 1 (Sistema de BI) – gestor 1
('Levantamento de requisitos', '2024-11-15', 'ALTA', 'PRONTO', 20.0, 20.0, (SELECT id FROM usuarios WHERE email='bruno.alves@email.com'), 1),
('Modelagem do DW', '2026-06-30', 'URGENTE', 'FEITO', 40.0, 38.5, (SELECT id FROM usuarios WHERE email='carla.dias@email.com'), 1),
('Desenvolvimento ETL', '2026-12-10', 'ALTA', 'PENDENTE', 50.0, 25.0, (SELECT id FROM usuarios WHERE email='diego.nunes@email.com'), 1),
('Criação de dashboards', '2026-12-20', 'MEDIA', 'PENDENTE', 30.0, 10.0, (SELECT id FROM usuarios WHERE email='elaine.rocha@email.com'), 1),
-- Projeto 2 (Portal de Transparência) – gestor 1
('Design de interface', '2024-09-30', 'URGENTE', 'PRONTO', 25.0, 25.0, (SELECT id FROM usuarios WHERE email='bruno.alves@email.com'), 2),
('Backend APIs', '2026-05-25', 'ALTA', 'ATRASADO', 35.0, 30.0, (SELECT id FROM usuarios WHERE email='fabio.torres@email.com'), 2),
('Testes de segurança', '2026-10-10', 'URGENTE', 'PENDENTE', 15.0, 0.0, (SELECT id FROM usuarios WHERE email='carla.dias@email.com'), 2),
-- Projeto 3 (Aplicativo Mobile) – gestor 2
('Prototipação', '2024-08-20', 'MEDIA', 'PRONTO', 10.0, 10.0, (SELECT id FROM usuarios WHERE email='gabriela.freitas@email.com'), 3),
('Desenvolvimento iOS', '2026-06-10', 'ALTA', 'FEITO', 60.0, 60.0, (SELECT id FROM usuarios WHERE email='henrique.brito@email.com'), 3),
('Testes beta', '2024-09-25', 'MEDIA', 'PRONTO', 20.0, 20.0, (SELECT id FROM usuarios WHERE email='igor.campos@email.com'), 3),
-- Projeto 4 (Migração de Dados) – gestor 3
('Planejamento', '2024-07-15', 'ALTA', 'PRONTO', 15.0, 15.0, (SELECT id FROM usuarios WHERE email='leonardo.monteiro@email.com'), 4),
('Execução migração', '2026-06-10', 'URGENTE', 'FEITO', 80.0, 80.0, (SELECT id FROM usuarios WHERE email='mariana.ribeiro@email.com'), 4),
('Validação pós-migração', '2024-08-15', 'ALTA', 'PRONTO', 25.0, 25.0, (SELECT id FROM usuarios WHERE email='natalia.castro@email.com'), 4),
-- Projeto 5 (Atualização de Segurança) – gestor 4
('Análise de vulnerabilidades', '2026-10-15', 'URGENTE', 'PENDENTE', 30.0, 15.0, (SELECT id FROM usuarios WHERE email='patricia.ferreira@email.com'), 5),
('Aplicação de patches', '2026-11-01', 'ALTA', 'PENDENTE', 20.0, 5.0, (SELECT id FROM usuarios WHERE email='rafael.oliveira@email.com'), 5),
('Teste de penetração', '2026-11-05', 'URGENTE', 'PENDENTE', 40.0, 0.0, (SELECT id FROM usuarios WHERE email='sabrina.cardoso@email.com'), 5),
-- Projeto 6 (E-commerce B2B) – gestor 5
('Configuração de catálogo', '2026-12-01', 'MEDIA', 'PENDENTE', 35.0, 20.0, (SELECT id FROM usuarios WHERE email='vanessa.souza@email.com'), 6),
('Integração de pagamentos', '2026-05-26', 'ALTA', 'ATRASADO', 45.0, 40.0, (SELECT id FROM usuarios WHERE email='wagner.lima@email.com'), 6),
('Testes de carga', '2027-01-10', 'MEDIA', 'PENDENTE', 25.0, 0.0, (SELECT id FROM usuarios WHERE email='yara.nunes@email.com'), 6),
-- Tarefas avulsas (sem projeto) para vários devs
('Correção de bug crítico', '2024-07-20', 'URGENTE', 'PRONTO', 8.0, 8.0, (SELECT id FROM usuarios WHERE email='bruno.alves@email.com'), NULL),
('Documentação técnica', '2026-05-22', 'BAIXA', 'FEITO', 15.0, 15.0, (SELECT id FROM usuarios WHERE email='carla.dias@email.com'), NULL),
('Treinamento da equipe', '2026-09-10', 'MEDIA', 'PENDENTE', 10.0, 2.0, (SELECT id FROM usuarios WHERE email='gabriela.freitas@email.com'), NULL),
('Refatoração de código', '2026-05-25', 'ALTA', 'ATRASADO', 25.0, 12.0, (SELECT id FROM usuarios WHERE email='henrique.brito@email.com'), NULL),
('Otimização de consultas', '2026-10-15', 'ALTA', 'PENDENTE', 20.0, 10.0, (SELECT id FROM usuarios WHERE email='leonardo.monteiro@email.com'), NULL),
('Configuração de servidor', '2024-10-20', 'MEDIA', 'PRONTO', 12.0, 12.0, (SELECT id FROM usuarios WHERE email='patricia.ferreira@email.com'), NULL),
('Revisão de código', '2026-06-05', 'BAIXA', 'FEITO', 6.0, 6.0, (SELECT id FROM usuarios WHERE email='vanessa.souza@email.com'), NULL),
('Planejamento de sprint', '2026-11-12', 'MEDIA', 'PENDENTE', 8.0, 4.0, (SELECT id FROM usuarios WHERE email='diego.nunes@email.com'), NULL),
('Atualização de bibliotecas', '2026-11-18', 'ALTA', 'PENDENTE', 10.0, 0.0, (SELECT id FROM usuarios WHERE email='mariana.ribeiro@email.com'), NULL),
('Homologação com cliente', '2026-11-25', 'URGENTE', 'PENDENTE', 16.0, 2.0, (SELECT id FROM usuarios WHERE email='rafael.oliveira@email.com'), NULL),
('Monitoramento de logs', '2024-12-01', 'BAIXA', 'PRONTO', 5.0, 5.0, (SELECT id FROM usuarios WHERE email='natalia.castro@email.com'), NULL),
('Elaboração de relatórios', '2024-12-05', 'MEDIA', 'PENDENTE', 12.0, 3.0, (SELECT id FROM usuarios WHERE email='igor.campos@email.com'), NULL);

-- ------------------------------------------------------------
-- 5. Inserir Relatórios (enviados por desenvolvedores)
-- ------------------------------------------------------------

INSERT INTO relatorios (data_envio, conteudo, tarefa_id, projeto_id, dev_id) VALUES

-- Relatórios do gestor 1 (Ana Silva) – devs Bruno, Carla, Diego, Elaine, Fabio
(NOW() - INTERVAL 5 DAY, 'Relatório final do levantamento de requisitos. Documentação aprovada pelo cliente.', 
 (SELECT id FROM tarefas WHERE descricao='Levantamento de requisitos' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='bruno.alves@email.com')),

(NOW() - INTERVAL 3 DAY, 'Modelagem do DW concluída com 96% de aproveitamento. Pendente ajustes no particionamento.', 
 (SELECT id FROM tarefas WHERE descricao='Modelagem do DW' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='carla.dias@email.com')),

(NOW() - INTERVAL 1 DAY, 'Desenvolvimento ETL: 50% concluído. Dificuldade com fonte de dados legada.', 
 (SELECT id FROM tarefas WHERE descricao='Desenvolvimento ETL' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='diego.nunes@email.com')),

(NOW() - INTERVAL 2 DAY, 'Criação de dashboards: protótipo inicial pronto. Aguardando feedback dos usuários.', 
 (SELECT id FROM tarefas WHERE descricao='Criação de dashboards' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='elaine.rocha@email.com')),

(NOW() - INTERVAL 7 DAY, 'Backend APIs: 85% dos endpoints implementados. Documentação Swagger anexada.', 
 (SELECT id FROM tarefas WHERE descricao='Backend APIs' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='fabio.torres@email.com')),

(NOW() - INTERVAL 4 DAY, 'Testes de segurança: plano de testes elaborado. Ferramenta configurada.', 
 (SELECT id FROM tarefas WHERE descricao='Testes de segurança' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='carla.dias@email.com')),

-- Relatórios do gestor 2 (Carlos Souza) – devs Gabriela, Henrique, Igor, Jessica
(NOW() - INTERVAL 6 DAY, 'Prototipação do aplicativo finalizada e validada com stakeholders.', 
 (SELECT id FROM tarefas WHERE descricao='Prototipação' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='gabriela.freitas@email.com')),

(NOW() - INTERVAL 10 DAY, 'Desenvolvimento iOS: código entregue para revisão. Próximo passo: testes beta.', 
 (SELECT id FROM tarefas WHERE descricao='Desenvolvimento iOS' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='henrique.brito@email.com')),

(NOW() - INTERVAL 2 DAY, 'Testes beta: 90% dos casos aprovados. Bugs críticos corrigidos.', 
 (SELECT id FROM tarefas WHERE descricao='Testes beta' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='igor.campos@email.com')),

(NOW() - INTERVAL 1 DAY, 'Relatório de progresso do projeto Aplicativo Mobile – fase de testes concluída.', 
 NULL, (SELECT id FROM projetos WHERE nome='Aplicativo Mobile' LIMIT 1), 
 (SELECT id FROM usuarios WHERE email='gabriela.freitas@email.com')),

-- Relatórios do gestor 3 (Fernanda Lima) – devs Leonardo, Mariana, Natalia, Otavio
(NOW() - INTERVAL 8 DAY, 'Planejamento da migração de dados finalizado. Cronograma aprovado.', 
 (SELECT id FROM tarefas WHERE descricao='Planejamento' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='leonardo.monteiro@email.com')),

(NOW() - INTERVAL 4 DAY, 'Execução da migração: 100% dos dados migrados. Validação em andamento.', 
 (SELECT id FROM tarefas WHERE descricao='Execução migração' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='mariana.ribeiro@email.com')),

(NOW() - INTERVAL 3 DAY, 'Validação pós-migração: consistência de dados verificada. Relatório final anexado.', 
 (SELECT id FROM tarefas WHERE descricao='Validação pós-migração' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='natalia.castro@email.com')),

(NOW() - INTERVAL 12 DAY, 'Otimização de consultas: análise de índices concluída. Melhorias de 30% na performance.', 
 (SELECT id FROM tarefas WHERE descricao='Otimização de consultas' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='leonardo.monteiro@email.com')),

-- Relatórios do gestor 4 (Ricardo Mendes) – devs Patricia, Rafael, Sabrina, Thiago
(NOW() - INTERVAL 5 DAY, 'Análise de vulnerabilidades: relatório de riscos entregue. Classificação crítica/média/baixa.', 
 (SELECT id FROM tarefas WHERE descricao='Análise de vulnerabilidades' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='patricia.ferreira@email.com')),

(NOW() - INTERVAL 2 DAY, 'Aplicação de patches: 40% dos servidores atualizados. Agendado reboot noturno.', 
 (SELECT id FROM tarefas WHERE descricao='Aplicação de patches' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='rafael.oliveira@email.com')),

(NOW() - INTERVAL 1 DAY, 'Teste de penetração: em andamento. Primeiros achados reportados.', 
 (SELECT id FROM tarefas WHERE descricao='Teste de penetração' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='sabrina.cardoso@email.com')),

(NOW() - INTERVAL 14 DAY, 'Configuração de servidor: ambiente de homologação pronto. Documentação atualizada.', 
 (SELECT id FROM tarefas WHERE descricao='Configuração de servidor' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='patricia.ferreira@email.com')),

-- Relatórios do gestor 5 (Juliana Costa) – devs Vanessa, Wagner, Yara
(NOW() - INTERVAL 6 DAY, 'Configuração de catálogo: produtos importados. Preços revisados.', 
 (SELECT id FROM tarefas WHERE descricao='Configuração de catálogo' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='vanessa.souza@email.com')),

(NOW() - INTERVAL 4 DAY, 'Integração de pagamentos: gateway configurado. Testes sandbox aprovados.', 
 (SELECT id FROM tarefas WHERE descricao='Integração de pagamentos' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='wagner.lima@email.com')),

(NOW() - INTERVAL 1 DAY, 'Testes de carga: cenários de pico simulados. Servidor respondeu adequadamente.', 
 (SELECT id FROM tarefas WHERE descricao='Testes de carga' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='yara.nunes@email.com')),

(NOW() - INTERVAL 9 DAY, 'Revisão de código: análise estática concluída. 10 pontos de melhoria identificados.', 
 (SELECT id FROM tarefas WHERE descricao='Revisão de código' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='vanessa.souza@email.com')),

(NOW() - INTERVAL 11 DAY, 'Elaboração de relatórios: modelos criados no Power BI. Aguardando dados de produção.', 
 (SELECT id FROM tarefas WHERE descricao='Elaboração de relatórios' LIMIT 1), NULL, 
 (SELECT id FROM usuarios WHERE email='igor.campos@email.com')),

-- Relatórios sobre projetos (sem tarefa específica)
(NOW() - INTERVAL 3 DAY, 'Relatório de progresso do projeto Sistema de BI – primeiros dashboards entregues.', 
 NULL, (SELECT id FROM projetos WHERE nome='Sistema de BI' LIMIT 1), 
 (SELECT id FROM usuarios WHERE email='bruno.alves@email.com')),

(NOW() - INTERVAL 7 DAY, 'Relatório final do projeto Portal de Transparência – entregue com atraso devido a requisitos.', 
 NULL, (SELECT id FROM projetos WHERE nome='Portal de Transparência' LIMIT 1), 
 (SELECT id FROM usuarios WHERE email='fabio.torres@email.com')),

(NOW() - INTERVAL 15 DAY, 'Atualização de segurança: relatório de vulnerabilidades corrigidas.', 
 NULL, (SELECT id FROM projetos WHERE nome='Atualização de Segurança' LIMIT 1), 
 (SELECT id FROM usuarios WHERE email='patricia.ferreira@email.com')),

(NOW() - INTERVAL 2 DAY, 'E-commerce B2B: integração com ERP concluída. Testes funcionais em andamento.', 
 NULL, (SELECT id FROM projetos WHERE nome='E-commerce B2B' LIMIT 1), 
 (SELECT id FROM usuarios WHERE email='vanessa.souza@email.com'));

-- ------------------------------------------------------------
-- 6. Inserir Solicitações (vinculadas a tarefas específicas)
-- ------------------------------------------------------------
INSERT INTO solicitacoes (justificativa, status, data_criacao, dev_solicitante_id, tarefa_id) VALUES
('Prazo muito curto para conclusão da ETL. Solicito extensão de 10 dias.', 'PENDENTE', NOW() - INTERVAL 2 DAY, (SELECT id FROM usuarios WHERE email='diego.nunes@email.com'), (SELECT id FROM tarefas WHERE descricao='Desenvolvimento ETL' LIMIT 1)),
('Necessito de mais um desenvolvedor para auxiliar na integração de pagamentos.', 'APROVADA', NOW() - INTERVAL 5 DAY, (SELECT id FROM usuarios WHERE email='wagner.lima@email.com'), (SELECT id FROM tarefas WHERE descricao='Integração de pagamentos' LIMIT 1)),
('Backend APIs: documentação incompleta, preciso de mais informações para prosseguir.', 'REJEITADA', NOW() - INTERVAL 1 DAY, (SELECT id FROM usuarios WHERE email='fabio.torres@email.com'), (SELECT id FROM tarefas WHERE descricao='Backend APIs' LIMIT 1)),
('Teste de penetração requer acesso a ambientes de homologação. Solicito liberação.', 'PENDENTE', NOW() - INTERVAL 3 DAY, (SELECT id FROM usuarios WHERE email='sabrina.cardoso@email.com'), (SELECT id FROM tarefas WHERE descricao='Teste de penetração' LIMIT 1)),
('Refatoração de código está mais complexa que o previsto. Preciso de mais 5 dias.', 'PENDENTE', NOW() - INTERVAL 4 DAY, (SELECT id FROM usuarios WHERE email='henrique.brito@email.com'), (SELECT id FROM tarefas WHERE descricao='Refatoração de código' LIMIT 1)),
('Homologação com cliente demandará viagem. Solicito reembolso de despesas.', 'APROVADA', NOW() - INTERVAL 6 DAY, (SELECT id FROM usuarios WHERE email='rafael.oliveira@email.com'), (SELECT id FROM tarefas WHERE descricao='Homologação com cliente' LIMIT 1)),
('Otimização de consultas depende de nova versão do banco. Aguardando liberação de recurso.', 'PENDENTE', NOW() - INTERVAL 2 DAY, (SELECT id FROM usuarios WHERE email='leonardo.monteiro@email.com'), (SELECT id FROM tarefas WHERE descricao='Otimização de consultas' LIMIT 1));