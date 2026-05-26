-- Deleção e criação do database para testes

-- Deleta o banco de dados (Para facilitar os testes)
drop database gestao_rotina;

-- Cria o banco de dados (se não existir)
CREATE DATABASE IF NOT EXISTS gestao_rotina;
USE gestao_rotina;

-- Tabela de usuários (desenvolvedores e gestores)
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(100) NOT NULL,
    tipo ENUM('DEV', 'GESTOR') NOT NULL,
    departamento VARCHAR(100) NULL,      -- apenas para gestores
    gestor_id INT NULL,                  -- para desenvolvedores (referência ao gestor)
    FOREIGN KEY (gestor_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Tabela de projetos
CREATE TABLE IF NOT EXISTS projetos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    prazo DATE NOT NULL,
    importancia ENUM('BAIXA', 'MEDIA', 'ALTA', 'URGENTE') NOT NULL,
    status ENUM('PENDENTE', 'FEITO', 'PRONTO', 'ATRASADO') NOT NULL DEFAULT 'PENDENTE',
    gestor_id INT NULL,
    FOREIGN KEY (gestor_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- Tabela de tarefas
CREATE TABLE IF NOT EXISTS tarefas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    prazo DATE NOT NULL,
    nivel_importancia ENUM('BAIXA', 'MEDIA', 'ALTA', 'URGENTE') NOT NULL,
    status ENUM('PENDENTE', 'FEITO', 'PRONTO', 'ATRASADO') NOT NULL DEFAULT 'PENDENTE',
    horas_estimadas DECIMAL(6,2) NOT NULL,
    horas_trabalhadas DECIMAL(6,2) NOT NULL DEFAULT 0,
    dev_responsavel_id INT NOT NULL,
    projeto_id INT NULL,
    FOREIGN KEY (dev_responsavel_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (projeto_id) REFERENCES projetos(id) ON DELETE SET NULL
);

-- Tabela de relatórios (enviados por devs)
CREATE TABLE IF NOT EXISTS relatorios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    data_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    conteudo TEXT NOT NULL,
    dev_id INT NULL,
    tarefa_id INT NULL,
    projeto_id INT NULL,
    FOREIGN KEY (dev_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (tarefa_id) REFERENCES tarefas(id) ON DELETE SET NULL,
    FOREIGN KEY (projeto_id) REFERENCES projetos(id) ON DELETE SET NULL
);

-- Tabela de solicitações
CREATE TABLE IF NOT EXISTS solicitacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    justificativa TEXT NOT NULL,
    status ENUM('PENDENTE', 'APROVADA', 'REJEITADA') NOT NULL DEFAULT 'PENDENTE',
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dev_solicitante_id INT NOT NULL,
    tarefa_id INT NULL,
    FOREIGN KEY (dev_solicitante_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (tarefa_id) REFERENCES tarefas(id) ON DELETE SET NULL
);