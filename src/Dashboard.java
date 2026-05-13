import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Dashboard principal com abas para cada funcionalidade.
 * Suporta tanto Desenvolvedor quanto Gestor.
 */
public class Dashboard extends JFrame {
    private Usuario usuarioLogado;
    private Sistema sistema = Sistema.getInstance();
    private JTabbedPane tabbedPane;

    // Construtores
    public Dashboard(UsuarioDev dev) {
        this((Usuario) dev);
    }

    public Dashboard(UsuarioGestor gestor) {
        this((Usuario) gestor);
    }

    private Dashboard(Usuario user) {
        this.usuarioLogado = user;
        setTitle("Sistema de Gestão de Rotina - " + user.getNome());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        if (user instanceof UsuarioDev) {
            UsuarioDev dev = (UsuarioDev) user;
            tabbedPane.addTab("Minhas Tarefas", new MinhasTarefasPanel(dev));
            tabbedPane.addTab("Progresso da Equipe", new ProgressoEquipePanel(dev));
            tabbedPane.addTab("Detalhes de Colega", new DetalhesColegaPanel(dev));
            tabbedPane.addTab("Enviar Relatório", new EnviarRelatorioPanel(dev));
            tabbedPane.addTab("Solicitar Reorganização", new SolicitarReorganizacaoPanel(dev));
        } else {
            UsuarioGestor gestor = (UsuarioGestor) user;
            tabbedPane.addTab("Visão Geral", new VisaoGeralPanel(gestor));
            tabbedPane.addTab("Criar Projeto/Tarefa", new CriarProjetoTarefaPanel(gestor));
            tabbedPane.addTab("Validar Finalizações", new ValidarFinalizacoesPanel(gestor));
            tabbedPane.addTab("Solicitações Pendentes", new SolicitacoesPanel(gestor));
            tabbedPane.addTab("Reatribuir Atrasadas", new ReatribuirAtrasadasPanel(gestor));
            tabbedPane.addTab("Relatório Diário", new RelatorioDiarioPanel(gestor));
        }

        add(tabbedPane, BorderLayout.CENTER);

        // Inicia verificação automática de prazos (a cada 1 minuto)
        sistema.iniciarVerificadorPrazos(60000);

        // Para o timer ao fechar a janela
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                sistema.pararVerificadorPrazos();
            }
        });
    }

    // ======================== CLASSES INTERNAS (PAINÉIS) ========================

    // ---------- Painéis para Desenvolvedor ----------

    private class MinhasTarefasPanel extends JPanel {
        private UsuarioDev dev;
        private JTable table;
        private DefaultTableModel model;

        public MinhasTarefasPanel(UsuarioDev dev) {
            this.dev = dev;
            setLayout(new BorderLayout());
            model = new DefaultTableModel(new String[]{"ID", "Descrição", "Prazo", "Status", "Progresso", "Horas (Trab/Esim)"}, 0);
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel botoes = new JPanel();
            JButton btnAddHoras = new JButton("Adicionar Horas");
            JButton btnConcluir = new JButton("Marcar como FEITO");
            JButton btnRefresh = new JButton("Atualizar");
            botoes.add(btnAddHoras);
            botoes.add(btnConcluir);
            botoes.add(btnRefresh);
            add(botoes, BorderLayout.SOUTH);

            carregarTarefas();

            btnAddHoras.addActionListener(e -> adicionarHoras());
            btnConcluir.addActionListener(e -> concluirTarefa());
            btnRefresh.addActionListener(e -> carregarTarefas());
        }

        private void carregarTarefas() {
            model.setRowCount(0);
            for (Tarefa t : dev.carregarTarefas()) {
                model.addRow(new Object[]{
                        t.getId(),
                        t.getDescricao(),
                        t.getPrazo(),
                        t.getStatus(),
                        String.format("%.1f%%", t.calcularProgresso()),
                        String.format("%.1f / %.1f", t.getHorasTrabalhadas(), t.getHorasEstimadas())
                });
            }
        }

        private void adicionarHoras() {
            int linha = table.getSelectedRow();
            if (linha == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma tarefa.");
                return;
            }
            int id = (int) model.getValueAt(linha, 0);
            Tarefa tarefa = dev.carregarTarefas().stream().filter(t -> t.getId() == id).findFirst().orElse(null);
            if (tarefa == null) return;
            String horasStr = JOptionPane.showInputDialog(this, "Horas trabalhadas:");
            try {
                double horas = Double.parseDouble(horasStr);
                tarefa.adicionarHorasTrabalhadas(horas);
                carregarTarefas();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Número inválido.");
            }
        }

        private void concluirTarefa() {
            int linha = table.getSelectedRow();
            if (linha == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma tarefa.");
                return;
            }
            int id = (int) model.getValueAt(linha, 0);
            Tarefa tarefa = dev.carregarTarefas().stream().filter(t -> t.getId() == id).findFirst().orElse(null);
            if (tarefa == null) return;
            dev.alterarStatusTarefa(tarefa, StatusTarefa.FEITO);
            carregarTarefas();
        }
    }

    private class ProgressoEquipePanel extends JPanel {
        private UsuarioDev dev;
        private JTextArea textArea;

        public ProgressoEquipePanel(UsuarioDev dev) {
            this.dev = dev;
            setLayout(new BorderLayout());
            textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            add(new JScrollPane(textArea), BorderLayout.CENTER);
            JButton btnRefresh = new JButton("Atualizar");
            add(btnRefresh, BorderLayout.SOUTH);
            btnRefresh.addActionListener(e -> carregarProgresso());
            carregarProgresso();
        }

        private void carregarProgresso() {
            textArea.setText(dev.visualizarProgressoEquipe());
        }
    }

    private class DetalhesColegaPanel extends JPanel {
        private UsuarioDev dev;
        private JComboBox<UsuarioDev> cbColegas;
        private JTextArea textArea;

        public DetalhesColegaPanel(UsuarioDev dev) {
            this.dev = dev;
            setLayout(new BorderLayout());
            JPanel top = new JPanel();
            top.add(new JLabel("Colega:"));
            cbColegas = new JComboBox<>();
            JButton btnCarregar = new JButton("Exibir Detalhes");
            top.add(cbColegas);
            top.add(btnCarregar);
            add(top, BorderLayout.NORTH);
            textArea = new JTextArea();
            textArea.setEditable(false);
            add(new JScrollPane(textArea), BorderLayout.CENTER);

            carregarColegas();
            btnCarregar.addActionListener(e -> exibirDetalhes());
        }

        private void carregarColegas() {
            cbColegas.removeAllItems();
            for (UsuarioDev d : Sistema.getInstance().getDevs()) {
                if (d.getId() != dev.getId()) {
                    cbColegas.addItem(d);
                }
            }
        }

        private void exibirDetalhes() {
            UsuarioDev colega = (UsuarioDev) cbColegas.getSelectedItem();
            if (colega != null) {
                textArea.setText(dev.visualizarDetalhesColega(colega));
            }
        }
    }

    private class EnviarRelatorioPanel extends JPanel {
        private UsuarioDev dev;
        private JComboBox<String> tipoItem;
        private JComboBox<Object> itemCombo;
        private JTextArea txtConteudo;

        public EnviarRelatorioPanel(UsuarioDev dev) {
            this.dev = dev;
            setLayout(new BorderLayout(10,10));
            JPanel top = new JPanel(new GridLayout(3,2,5,5));
            top.add(new JLabel("Tipo:"));
            tipoItem = new JComboBox<>(new String[]{"Tarefa", "Projeto"});
            top.add(tipoItem);
            top.add(new JLabel("Selecione:"));
            itemCombo = new JComboBox<>();
            top.add(itemCombo);
            top.add(new JLabel("Conteúdo:"));
            top.add(new JLabel());
            add(top, BorderLayout.NORTH);

            txtConteudo = new JTextArea(10,40);
            add(new JScrollPane(txtConteudo), BorderLayout.CENTER);

            JButton btnEnviar = new JButton("Enviar Relatório");
            add(btnEnviar, BorderLayout.SOUTH);

            tipoItem.addActionListener(e -> atualizarItens());
            atualizarItens();
            btnEnviar.addActionListener(e -> enviar());
        }

        private void atualizarItens() {
            itemCombo.removeAllItems();
            String tipo = (String) tipoItem.getSelectedItem();
            if ("Tarefa".equals(tipo)) {
                for (Tarefa t : dev.carregarTarefas()) {
                    itemCombo.addItem(t);
                }
            } else {
                // Projetos que o dev participa
                dev.carregarTarefas().stream()
                        .map(Tarefa::getProjetoPai)
                        .filter(p -> p != null)
                        .distinct()
                        .forEach(itemCombo::addItem);
            }
        }

        private void enviar() {
            Object item = itemCombo.getSelectedItem();
            String conteudo = txtConteudo.getText().trim();
            if (item == null || conteudo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione um item e digite o conteúdo.");
                return;
            }
            dev.enviarRelatorioFinal(item, conteudo);
            JOptionPane.showMessageDialog(this, "Relatório enviado com sucesso!");
            txtConteudo.setText("");
        }
    }

    private class SolicitarReorganizacaoPanel extends JPanel {
        private UsuarioDev dev;
        private JTextArea txtJustificativa;

        public SolicitarReorganizacaoPanel(UsuarioDev dev) {
            this.dev = dev;
            setLayout(new BorderLayout());
            txtJustificativa = new JTextArea(10,40);
            add(new JLabel("Justificativa:"), BorderLayout.NORTH);
            add(new JScrollPane(txtJustificativa), BorderLayout.CENTER);
            JButton btnSolicitar = new JButton("Enviar Solicitação");
            add(btnSolicitar, BorderLayout.SOUTH);
            btnSolicitar.addActionListener(e -> solicitar());
        }

        private void solicitar() {
            String justif = txtJustificativa.getText().trim();
            if (justif.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Digite uma justificativa.");
                return;
            }
            dev.solicitarReorganizacao(justif);
            JOptionPane.showMessageDialog(this, "Solicitação enviada ao gestor.");
            txtJustificativa.setText("");
        }
    }

    // ---------- Painéis para Gestor ----------

    private class VisaoGeralPanel extends JPanel {
        private UsuarioGestor gestor;
        private JTextArea textArea;

        public VisaoGeralPanel(UsuarioGestor gestor) {
            this.gestor = gestor;
            setLayout(new BorderLayout());
            textArea = new JTextArea();
            textArea.setEditable(false);
            add(new JScrollPane(textArea), BorderLayout.CENTER);
            JButton btnRefresh = new JButton("Atualizar");
            add(btnRefresh, BorderLayout.SOUTH);
            btnRefresh.addActionListener(e -> carregarVisao());
            carregarVisao();
        }

        private void carregarVisao() {
            textArea.setText(gestor.visualizarTodosProjetosTarefas());
        }
    }

    private class CriarProjetoTarefaPanel extends JPanel {
        private UsuarioGestor gestor;
        private JComboBox<String> tipoCriacao;
        private JPanel cardPanel;
        private CardLayout cardLayout;

        public CriarProjetoTarefaPanel(UsuarioGestor gestor) {
            this.gestor = gestor;
            setLayout(new BorderLayout());
            tipoCriacao = new JComboBox<>(new String[]{"Projeto", "Tarefa Avulsa", "Tarefa em Projeto"});
            add(tipoCriacao, BorderLayout.NORTH);
            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);
            cardPanel.add(new CriarProjetoPanel(), "Projeto");
            cardPanel.add(new CriarTarefaAvulsaPanel(), "Tarefa Avulsa");
            cardPanel.add(new CriarTarefaEmProjetoPanel(), "Tarefa em Projeto");
            add(cardPanel, BorderLayout.CENTER);
            tipoCriacao.addActionListener(e -> cardLayout.show(cardPanel, (String) tipoCriacao.getSelectedItem()));
        }

        private class CriarProjetoPanel extends JPanel {
            private JTextField txtNome;
            private JSpinner spPrazo;
            private JComboBox<NivelImportancia> cbImportancia;

            CriarProjetoPanel() {
                setLayout(new GridLayout(0,2,5,5));
                add(new JLabel("Nome:"));
                txtNome = new JTextField(20);
                add(txtNome);
                add(new JLabel("Prazo (dias a partir de hoje):"));
                spPrazo = new JSpinner(new SpinnerNumberModel(10, 1, 365, 1));
                add(spPrazo);
                add(new JLabel("Importância:"));
                cbImportancia = new JComboBox<>(NivelImportancia.values());
                add(cbImportancia);
                JButton btnCriar = new JButton("Criar Projeto");
                add(btnCriar);
                btnCriar.addActionListener(e -> criarProjeto());
            }

            private void criarProjeto() {
                String nome = txtNome.getText().trim();
                if (nome.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nome do projeto é obrigatório.");
                    return;
                }
                int dias = (Integer) spPrazo.getValue();
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_MONTH, dias);
                java.util.Date prazo = cal.getTime();
                NivelImportancia imp = (NivelImportancia) cbImportancia.getSelectedItem();
                gestor.criarProjeto(nome, prazo, imp);
                JOptionPane.showMessageDialog(this, "Projeto criado com sucesso!");
                txtNome.setText("");
                spPrazo.setValue(10);
            }
        }

        private class CriarTarefaAvulsaPanel extends JPanel {
            private JComboBox<UsuarioDev> cbDev;
            private JTextField txtDesc;
            private JSpinner spPrazo;
            private JComboBox<NivelImportancia> cbImportancia;
            private JSpinner spHoras;

            CriarTarefaAvulsaPanel() {
                setLayout(new GridLayout(0,2,5,5));
                add(new JLabel("Desenvolvedor:"));
                cbDev = new JComboBox<>();
                add(cbDev);
                add(new JLabel("Descrição:"));
                txtDesc = new JTextField(20);
                add(txtDesc);
                add(new JLabel("Prazo (dias):"));
                spPrazo = new JSpinner(new SpinnerNumberModel(5, 1, 180, 1));
                add(spPrazo);
                add(new JLabel("Importância:"));
                cbImportancia = new JComboBox<>(NivelImportancia.values());
                add(cbImportancia);
                add(new JLabel("Horas estimadas:"));
                spHoras = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 100.0, 0.5));
                add(spHoras);
                JButton btnCriar = new JButton("Criar Tarefa");
                add(btnCriar);
                carregarDevs();
                btnCriar.addActionListener(e -> criarTarefa());
            }

            private void carregarDevs() {
                cbDev.removeAllItems();
                for (UsuarioDev d : gestor.getEquipe()) {
                    cbDev.addItem(d);
                }
            }

            private void criarTarefa() {
                UsuarioDev dev = (UsuarioDev) cbDev.getSelectedItem();
                if (dev == null) {
                    JOptionPane.showMessageDialog(this, "Nenhum desenvolvedor na equipe.");
                    return;
                }
                String desc = txtDesc.getText().trim();
                if (desc.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Descrição obrigatória.");
                    return;
                }
                int dias = (Integer) spPrazo.getValue();
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_MONTH, dias);
                java.util.Date prazo = cal.getTime();
                NivelImportancia imp = (NivelImportancia) cbImportancia.getSelectedItem();
                double horas = (Double) spHoras.getValue();
                gestor.criarAtribuirTarefa(desc, prazo, imp, dev.getId(), horas);
                JOptionPane.showMessageDialog(this, "Tarefa criada e atribuída.");
                txtDesc.setText("");
            }
        }

        private class CriarTarefaEmProjetoPanel extends JPanel {
            private JComboBox<UsuarioDev> cbDev;
            private JComboBox<Projeto> cbProjeto;
            private JTextField txtDesc;
            private JSpinner spPrazo;
            private JComboBox<NivelImportancia> cbImportancia;
            private JSpinner spHoras;

            CriarTarefaEmProjetoPanel() {
                setLayout(new GridLayout(0,2,5,5));
                add(new JLabel("Desenvolvedor:"));
                cbDev = new JComboBox<>();
                add(cbDev);
                add(new JLabel("Projeto:"));
                cbProjeto = new JComboBox<>();
                add(cbProjeto);
                add(new JLabel("Descrição:"));
                txtDesc = new JTextField(20);
                add(txtDesc);
                add(new JLabel("Prazo (dias):"));
                spPrazo = new JSpinner(new SpinnerNumberModel(5, 1, 180, 1));
                add(spPrazo);
                add(new JLabel("Importância:"));
                cbImportancia = new JComboBox<>(NivelImportancia.values());
                add(cbImportancia);
                add(new JLabel("Horas estimadas:"));
                spHoras = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 100.0, 0.5));
                add(spHoras);
                JButton btnCriar = new JButton("Criar Tarefa");
                add(btnCriar);
                carregarDados();
                btnCriar.addActionListener(e -> criarTarefa());
            }

            private void carregarDados() {
                cbDev.removeAllItems();
                for (UsuarioDev d : gestor.getEquipe()) {
                    cbDev.addItem(d);
                }
                cbProjeto.removeAllItems();
                for (Projeto p : Sistema.getInstance().getProjetos()) {
                    cbProjeto.addItem(p);
                }
            }

            private void criarTarefa() {
                UsuarioDev dev = (UsuarioDev) cbDev.getSelectedItem();
                Projeto proj = (Projeto) cbProjeto.getSelectedItem();
                if (dev == null || proj == null) {
                    JOptionPane.showMessageDialog(this, "Selecione dev e projeto.");
                    return;
                }
                String desc = txtDesc.getText().trim();
                if (desc.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Descrição obrigatória.");
                    return;
                }
                int dias = (Integer) spPrazo.getValue();
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_MONTH, dias);
                java.util.Date prazo = cal.getTime();
                NivelImportancia imp = (NivelImportancia) cbImportancia.getSelectedItem();
                double horas = (Double) spHoras.getValue();
                gestor.criarAtribuirTarefaEmProjeto(desc, prazo, imp, dev.getId(), proj.getId(), horas);
                JOptionPane.showMessageDialog(this, "Tarefa adicionada ao projeto.");
                txtDesc.setText("");
            }
        }
    }

    private class ValidarFinalizacoesPanel extends JPanel {
        private UsuarioGestor gestor;
        private JTable table;
        private DefaultTableModel model;

        public ValidarFinalizacoesPanel(UsuarioGestor gestor) {
            this.gestor = gestor;
            setLayout(new BorderLayout());
            model = new DefaultTableModel(new String[]{"ID", "Descrição", "Responsável", "Status"}, 0);
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);
            JButton btnValidar = new JButton("Validar Selecionada");
            add(btnValidar, BorderLayout.SOUTH);
            carregarTarefasFeito();
            btnValidar.addActionListener(e -> validar());
        }

        private void carregarTarefasFeito() {
            model.setRowCount(0);
            try {
                for (Tarefa t : Sistema.getInstance().getTarefasDaEquipe(gestor.getId())) {
                    if (t.getStatus() == StatusTarefa.FEITO) {
                        model.addRow(new Object[]{t.getId(), t.getDescricao(), t.getDevResponsavel().getNome(), t.getStatus()});
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar: " + ex.getMessage());
            }
        }

        private void validar() {
            int linha = table.getSelectedRow();
            if (linha == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma tarefa.");
                return;
            }
            int id = (int) model.getValueAt(linha, 0);
            Tarefa tarefa = null;
            try {
                tarefa = Sistema.getInstance().getTarefas().stream().filter(t -> t.getId() == id).findFirst().orElse(null);
                if (tarefa != null) {
                    gestor.validarFinalizacao(tarefa);
                    carregarTarefasFeito();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao validar: " + ex.getMessage());
            }
        }
    }

    private class SolicitacoesPanel extends JPanel {
        private UsuarioGestor gestor;
        private JTable table;
        private DefaultTableModel model;

        public SolicitacoesPanel(UsuarioGestor gestor) {
            this.gestor = gestor;
            setLayout(new BorderLayout());
            model = new DefaultTableModel(new String[]{"ID", "Solicitante", "Justificativa", "Status"}, 0);
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);
            JPanel botoes = new JPanel();
            JButton btnAprovar = new JButton("Aprovar");
            JButton btnRejeitar = new JButton("Rejeitar");
            JButton btnRefresh = new JButton("Atualizar");
            botoes.add(btnAprovar);
            botoes.add(btnRejeitar);
            botoes.add(btnRefresh);
            add(botoes, BorderLayout.SOUTH);
            carregarSolicitacoes();
            btnAprovar.addActionListener(e -> processar(true));
            btnRejeitar.addActionListener(e -> processar(false));
            btnRefresh.addActionListener(e -> carregarSolicitacoes());
        }

        private void carregarSolicitacoes() {
            model.setRowCount(0);
            for (SolicitacaoMudanca s : gestor.listarSolicitacoesPendentes()) {
                model.addRow(new Object[]{s.getId(), s.getSolicitante().getNome(), s.getJustificativa(), s.getStatus()});
            }
        }

        private void processar(boolean aprovar) {
            int linha = table.getSelectedRow();
            if (linha == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma solicitação.");
                return;
            }
            int id = (int) model.getValueAt(linha, 0);
            SolicitacaoMudanca solicitacao = null;
            for (SolicitacaoMudanca s : Sistema.getInstance().getSolicitacoes()) {
                if (s.getId() == id) {
                    solicitacao = s;
                    break;
                }
            }
            if (solicitacao != null) {
                gestor.processarSolicitacaoMudanca(solicitacao, aprovar);
                carregarSolicitacoes();
            }
        }
    }

    private class ReatribuirAtrasadasPanel extends JPanel {
        private UsuarioGestor gestor;
        private JTable table;
        private DefaultTableModel model;
        private JComboBox<UsuarioDev> cbNovoDev;

        public ReatribuirAtrasadasPanel(UsuarioGestor gestor) {
            this.gestor = gestor;
            setLayout(new BorderLayout());
            model = new DefaultTableModel(new String[]{"ID", "Descrição", "Responsável Atual", "Status"}, 0);
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);
            JPanel bottom = new JPanel();
            bottom.add(new JLabel("Reatribuir para:"));
            cbNovoDev = new JComboBox<>();
            bottom.add(cbNovoDev);
            JButton btnReatribuir = new JButton("Reatribuir");
            bottom.add(btnReatribuir);
            JButton btnRefresh = new JButton("Atualizar");
            bottom.add(btnRefresh);
            add(bottom, BorderLayout.SOUTH);
            carregarTarefasAtrasadas();
            carregarDevs();
            btnReatribuir.addActionListener(e -> reatribuir());
            btnRefresh.addActionListener(e -> { carregarTarefasAtrasadas(); carregarDevs(); });
        }

        private void carregarTarefasAtrasadas() {
            model.setRowCount(0);
            try {
                for (Tarefa t : Sistema.getInstance().getTarefasDaEquipe(gestor.getId())) {
                    if (t.getStatus() == StatusTarefa.ATRASADO) {
                        model.addRow(new Object[]{t.getId(), t.getDescricao(), t.getDevResponsavel().getNome(), t.getStatus()});
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar: " + ex.getMessage());
            }
        }

        private void carregarDevs() {
            cbNovoDev.removeAllItems();
            for (UsuarioDev d : gestor.getEquipe()) {
                cbNovoDev.addItem(d);
            }
        }

        private void reatribuir() {
            int linha = table.getSelectedRow();
            if (linha == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma tarefa atrasada.");
                return;
            }
            UsuarioDev novoDev = (UsuarioDev) cbNovoDev.getSelectedItem();
            if (novoDev == null) return;
            int id = (int) model.getValueAt(linha, 0);
            Tarefa tarefa = null;
            try {
                tarefa = Sistema.getInstance().getTarefas().stream().filter(t -> t.getId() == id).findFirst().orElse(null);
                if (tarefa != null) {
                    gestor.reatribuirTarefaAtrasada(tarefa, novoDev);
                    carregarTarefasAtrasadas();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        }
    }

    private class RelatorioDiarioPanel extends JPanel {
        private UsuarioGestor gestor;
        private JTextArea textArea;

        public RelatorioDiarioPanel(UsuarioGestor gestor) {
            this.gestor = gestor;
            setLayout(new BorderLayout());
            textArea = new JTextArea();
            textArea.setEditable(false);
            add(new JScrollPane(textArea), BorderLayout.CENTER);
            JButton btnGerar = new JButton("Gerar Relatório Diário");
            add(btnGerar, BorderLayout.SOUTH);
            btnGerar.addActionListener(e -> gerar());
        }

        private void gerar() {
            // Gera e exibe o relatório diário (também persiste no banco)
            Sistema.getInstance().gerarRelatorioDiario();
            // Recupera o último relatório (simples: mostra uma mensagem)
            textArea.setText("Relatório diário gerado com sucesso. Verifique o console para detalhes.");
            // Opcional: carregar o relatório do banco
            try {
                java.util.List<Relatorio> rels = Sistema.getInstance().getRelatorios();
                if (!rels.isEmpty()) {
                    Relatorio ultimo = rels.get(rels.size()-1);
                    textArea.setText(ultimo.getConteudo());
                }
            } catch (Exception ex) {
                // ignora
            }
        }
    }
}