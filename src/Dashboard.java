import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;

interface Refreshable {
    void refresh();
}

public class Dashboard extends JFrame {
    private Usuario usuarioLogado;
    private Sistema sistema = Sistema.getInstance();
    private JTabbedPane tabbedPane;
    private List<Refreshable> refreshablePanels = new ArrayList<>();

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
        setExtendedState(JFrame.MAXIMIZED_BOTH); // tela cheia

        tabbedPane = new JTabbedPane();

        if (user instanceof UsuarioDev) {
            UsuarioDev dev = (UsuarioDev) user;
            MinhasTarefasPanel p1 = new MinhasTarefasPanel(dev);
            ProgressoEquipePanel p2 = new ProgressoEquipePanel(dev);
            DetalhesColegaPanel p3 = new DetalhesColegaPanel(dev);
            EnviarRelatorioPanel p4 = new EnviarRelatorioPanel(dev);
            SolicitarReorganizacaoPanel p5 = new SolicitarReorganizacaoPanel(dev);
            refreshablePanels.add(p1);
            refreshablePanels.add(p2);
            refreshablePanels.add(p3);
            refreshablePanels.add(p4);
            refreshablePanels.add(p5);
            tabbedPane.addTab("Minhas Tarefas", p1);
            tabbedPane.addTab("Progresso da Equipe", p2);
            tabbedPane.addTab("Detalhes de Colega", p3);
            tabbedPane.addTab("Enviar Relatório", p4);
            tabbedPane.addTab("Solicitar Reorganização", p5);
        } else {
            UsuarioGestor gestor = (UsuarioGestor) user;
            VisaoGeralPanel p1 = new VisaoGeralPanel(gestor);
            CriarProjetoTarefaPanel p2 = new CriarProjetoTarefaPanel(gestor);
            ValidarFinalizacoesPanel p3 = new ValidarFinalizacoesPanel(gestor);
            SolicitacoesPanel p4 = new SolicitacoesPanel(gestor);
            ReatribuirAtrasadasPanel p5 = new ReatribuirAtrasadasPanel(gestor);
            RelatorioPanel p6 = new RelatorioPanel(gestor);
            refreshablePanels.add(p1);
            refreshablePanels.add(p2);
            refreshablePanels.add(p3);
            refreshablePanels.add(p4);
            refreshablePanels.add(p5);
            refreshablePanels.add(p6);
            tabbedPane.addTab("Visão Geral", p1);
            tabbedPane.addTab("Criar Projeto/Tarefa", p2);
            tabbedPane.addTab("Validar Finalizações", p3);
            tabbedPane.addTab("Solicitações Pendentes", p4);
            tabbedPane.addTab("Reatribuir Atrasadas", p5);
            tabbedPane.addTab("Relatório", p6);
        }

        add(tabbedPane, BorderLayout.CENTER);

        sistema.iniciarVerificadorPrazos(60000);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                sistema.pararVerificadorPrazos();
            }
        });
    }

    public void refreshAll() {
        for (Refreshable rp : refreshablePanels) {
            rp.refresh();
        }
    }

    // ======================== RENDERIZADOR DE BARRA DE PROGRESSO ========================
    private class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {
        public ProgressBarRenderer() {
            setStringPainted(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Integer) {
                int progress = (Integer) value;
                setValue(progress);
                setString(progress + "%");
            } else {
                setValue(0);
                setString("");
            }
            return this;
        }
    }

    // ======================== PAINÉIS PARA DESENVOLVEDOR ========================

    private class MinhasTarefasPanel extends JPanel implements Refreshable {
        private UsuarioDev dev;
        private JTable table;
        private DefaultTableModel model;

        public MinhasTarefasPanel(UsuarioDev dev) {
            this.dev = dev;
            setLayout(new BorderLayout());
            model = new DefaultTableModel(new String[]{"ID", "Descrição", "Prazo", "Status", "Progresso", "Horas (Trab/Esim)"}, 0);
            table = new JTable(model);
            table.setDefaultRenderer(Integer.class, new ProgressBarRenderer());
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel botoes = new JPanel();
            JButton btnAddHoras = new JButton("Adicionar Horas");
            JButton btnConcluir = new JButton("Marcar como FEITO");
            JButton btnRefresh = new JButton("Atualizar");
            botoes.add(btnAddHoras);
            botoes.add(btnConcluir);
            botoes.add(btnRefresh);
            add(botoes, BorderLayout.SOUTH);

            refresh();
            btnAddHoras.addActionListener(e -> adicionarHoras());
            btnConcluir.addActionListener(e -> concluirTarefa());
            btnRefresh.addActionListener(e -> refresh());
        }

        @Override
        public void refresh() {
            model.setRowCount(0);
            for (Tarefa t : dev.carregarTarefas()) {
                int progresso = (int) Math.round(t.calcularProgresso());
                model.addRow(new Object[]{
                        t.getId(),
                        t.getDescricao(),
                        t.getPrazo(),
                        t.getStatus(),
                        progresso,
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
                ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
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
            ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
        }
    }

    private class ProgressoEquipePanel extends JPanel implements Refreshable {
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
            btnRefresh.addActionListener(e -> refresh());
            refresh();
        }
        @Override
        public void refresh() {
            textArea.setText(dev.visualizarProgressoEquipe());
        }
    }

    private class DetalhesColegaPanel extends JPanel implements Refreshable {
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
            refresh();
            btnCarregar.addActionListener(e -> exibirDetalhes());
        }
        @Override
        public void refresh() {
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

    private class EnviarRelatorioPanel extends JPanel implements Refreshable {
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
        @Override
        public void refresh() {
            atualizarItens();
        }
        private void atualizarItens() {
            itemCombo.removeAllItems();
            String tipo = (String) tipoItem.getSelectedItem();
            if ("Tarefa".equals(tipo)) {
                for (Tarefa t : dev.carregarTarefas()) {
                    itemCombo.addItem(t);
                }
            } else {
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
            ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
        }
    }

    private class SolicitarReorganizacaoPanel extends JPanel implements Refreshable {
        private UsuarioDev dev;
        private JComboBox<Tarefa> cbTarefa;
        private JTextArea txtJustificativa;

        public SolicitarReorganizacaoPanel(UsuarioDev dev) {
            this.dev = dev;
            setLayout(new BorderLayout(10,10));
            JPanel top = new JPanel(new GridLayout(2,2,5,5));
            top.add(new JLabel("Tarefa:"));
            cbTarefa = new JComboBox<>();
            top.add(cbTarefa);
            top.add(new JLabel("Justificativa:"));
            top.add(new JLabel()); // placeholder
            add(top, BorderLayout.NORTH);
            txtJustificativa = new JTextArea(10,40);
            add(new JScrollPane(txtJustificativa), BorderLayout.CENTER);
            JButton btnSolicitar = new JButton("Enviar Solicitação");
            add(btnSolicitar, BorderLayout.SOUTH);
            refresh();
            btnSolicitar.addActionListener(e -> solicitar());
        }

        @Override
        public void refresh() {
            cbTarefa.removeAllItems();
            for (Tarefa t : dev.carregarTarefas()) {
                cbTarefa.addItem(t);
            }
        }

        private void solicitar() {
            Tarefa tarefa = (Tarefa) cbTarefa.getSelectedItem();
            String justif = txtJustificativa.getText().trim();
            if (tarefa == null || justif.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione uma tarefa e digite a justificativa.");
                return;
            }
            dev.solicitarReorganizacao(tarefa, justif); // precisamos criar este método
            JOptionPane.showMessageDialog(this, "Solicitação enviada ao gestor.");
            txtJustificativa.setText("");
            ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
        }
    }

    // ======================== PAINÉIS PARA GESTOR ========================

    private class VisaoGeralPanel extends JPanel implements Refreshable {
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
            btnRefresh.addActionListener(e -> refresh());
            refresh();
        }
        @Override
        public void refresh() {
            textArea.setText(gestor.visualizarTodosProjetosTarefas());
        }
    }

    private class CriarProjetoTarefaPanel extends JPanel implements Refreshable {
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
        @Override
        public void refresh() {
            // Atualiza os combos nos painéis filhos
            Component[] comps = cardPanel.getComponents();
            for (Component c : comps) {
                if (c instanceof Refreshable) {
                    ((Refreshable) c).refresh();
                }
            }
        }

        private class CriarProjetoPanel extends JPanel implements Refreshable {
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
            @Override public void refresh() {}
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
                ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
            }
        }

        private class CriarTarefaAvulsaPanel extends JPanel implements Refreshable {
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
                refresh();
                btnCriar.addActionListener(e -> criarTarefa());
            }
            @Override
            public void refresh() {
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
                ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
            }
        }

        private class CriarTarefaEmProjetoPanel extends JPanel implements Refreshable {
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
                refresh();
                btnCriar.addActionListener(e -> criarTarefa());
            }
            @Override
            public void refresh() {
                cbDev.removeAllItems();
                for (UsuarioDev d : gestor.getEquipe()) {
                    cbDev.addItem(d);
                }
                cbProjeto.removeAllItems();
                // FILTRO: apenas projetos da equipe
                for (Projeto p : Sistema.getInstance().getProjetosDaEquipe(gestor.getId())) {
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
                ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
            }
        }
    }

    private class ValidarFinalizacoesPanel extends JPanel implements Refreshable {
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
            JButton btnRejeitar = new JButton("Rejeitar");
            add(btnRejeitar, BorderLayout.SOUTH);
            btnRejeitar.addActionListener(e -> rejeitar());
            refresh();
            btnValidar.addActionListener(e -> validar());
        }
        @Override
        public void refresh() {
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
                    ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao validar: " + ex.getMessage());
            }
        }
        private void rejeitar() {
            int linha = table.getSelectedRow();
            if (linha == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma tarefa.");
                return;
            }
            int id = (int) model.getValueAt(linha, 0);
            Tarefa tarefa = Sistema.getInstance().getTarefas().stream()
                    .filter(t -> t.getId() == id).findFirst().orElse(null);
            if (tarefa != null && tarefa.getStatus() == StatusTarefa.FEITO) {
                tarefa.setStatus(StatusTarefa.PENDENTE);
                try {
                    TarefaDAO dao = new TarefaDAO();
                    dao.atualizarStatus(tarefa.getId(), StatusTarefa.PENDENTE);
                    JOptionPane.showMessageDialog(this, "Tarefa rejeitada e retornada para PENDENTE.");
                    ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao rejeitar: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Apenas tarefas com status FEITO podem ser rejeitadas.");
            }
        }
    }

    private class SolicitacoesPanel extends JPanel implements Refreshable {
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
            refresh();
            btnAprovar.addActionListener(e -> processar(true));
            btnRejeitar.addActionListener(e -> processar(false));
            btnRefresh.addActionListener(e -> refresh());
        }
        @Override
        public void refresh() {
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
            if (solicitacao == null) return;

            if (aprovar) {
                Tarefa tarefa = solicitacao.getTarefaRelacionada();
                if (tarefa == null) {
                    JOptionPane.showMessageDialog(this, "Solicitação não está associada a uma tarefa.");
                    return;
                }
                // Diálogo para reatribuir a tarefa
                JComboBox<UsuarioDev> cbDev = new JComboBox<>();
                for (UsuarioDev d : gestor.getEquipe()) {
                    if (d.getId() != solicitacao.getSolicitante().getId()) {
                        cbDev.addItem(d);
                    }
                }
                if (cbDev.getItemCount() == 0) {
                    JOptionPane.showMessageDialog(this, "Não há outro desenvolvedor na equipe para reatribuir.");
                    return;
                }
                int result = JOptionPane.showConfirmDialog(this, cbDev,
                        "Reatribuir tarefa para:", JOptionPane.OK_CANCEL_OPTION);
                if (result != JOptionPane.OK_OPTION) {
                    return;
                }
                UsuarioDev novoDev = (UsuarioDev) cbDev.getSelectedItem();
                if (novoDev != null) {
                    try {
                        TarefaDAO tarefaDAO = new TarefaDAO();
                        tarefaDAO.reatribuirDev(tarefa.getId(), novoDev.getId());
                        tarefa.setDevResponsavel(novoDev);
                        JOptionPane.showMessageDialog(this, "Tarefa reatribuída com sucesso.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Erro ao reatribuir: " + ex.getMessage());
                        return;
                    }
                } else {
                    return;
                }
            }
            gestor.processarSolicitacaoMudanca(solicitacao, aprovar);
            ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
        }
    }

    private class ReatribuirAtrasadasPanel extends JPanel implements Refreshable {
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
            refresh();
            btnReatribuir.addActionListener(e -> reatribuir());
            btnRefresh.addActionListener(e -> refresh());
        }
        @Override
        public void refresh() {
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
            int id = (int) model.getValueAt(linha, 0);
            Tarefa tarefa = null;
            try {
                tarefa = Sistema.getInstance().getTarefas().stream()
                        .filter(t -> t.getId() == id).findFirst().orElse(null);
                if (tarefa == null) return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
                return;
            }

            // Selecionar novo desenvolvedor
            UsuarioDev novoDev = (UsuarioDev) JOptionPane.showInputDialog(this,
                    "Selecione o novo desenvolvedor responsável:",
                    "Reatribuir Tarefa",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    gestor.getEquipe().toArray(),
                    tarefa.getDevResponsavel());
            if (novoDev == null) return;

            // Novo prazo (opcional)
            String prazoStr = JOptionPane.showInputDialog(this,
                    "Novo prazo (dias a partir de hoje) ou deixe em branco para manter o atual:");
            java.util.Date novoPrazo = null;
            if (prazoStr != null && !prazoStr.trim().isEmpty()) {
                try {
                    int dias = Integer.parseInt(prazoStr.trim());
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.add(java.util.Calendar.DAY_OF_MONTH, dias);
                    novoPrazo = cal.getTime();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Número inválido. O prazo não será alterado.");
                }
            }

            try {
                TarefaDAO tarefaDAO = new TarefaDAO();
                tarefaDAO.reatribuirDev(tarefa.getId(), novoDev.getId());
                if (novoPrazo != null) {
                    tarefa.setPrazo(novoPrazo);
                    String sql = "UPDATE tarefas SET prazo = ? WHERE id = ?";
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setDate(1, new java.sql.Date(novoPrazo.getTime()));
                        stmt.setInt(2, tarefa.getId());
                        stmt.executeUpdate();
                    }
                }
                // Mudar status para PENDENTE
                tarefa.setStatus(StatusTarefa.PENDENTE);
                tarefaDAO.atualizarStatus(tarefa.getId(), StatusTarefa.PENDENTE);
                tarefa.setDevResponsavel(novoDev);
                JOptionPane.showMessageDialog(this, "Tarefa reatribuída, prazo atualizado e status alterado para PENDENTE.");
                ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        }
    }

    private class RelatorioPanel extends JPanel implements Refreshable {
        private UsuarioGestor gestor;
        private JTextArea textArea;
        public RelatorioPanel(UsuarioGestor gestor) {
            this.gestor = gestor;
            setLayout(new BorderLayout());
            textArea = new JTextArea();
            textArea.setEditable(false);
            add(new JScrollPane(textArea), BorderLayout.CENTER);
            JButton btnGerar = new JButton("Gerar Relatório");
            add(btnGerar, BorderLayout.SOUTH);
            btnGerar.addActionListener(e -> gerar());
        }
        @Override
        public void refresh() {
            try {
                java.util.List<Relatorio> rels = Sistema.getInstance().getRelatorios();
                if (!rels.isEmpty()) {
                    Relatorio ultimo = rels.get(rels.size()-1);
                    textArea.setText(ultimo.getConteudo());
                } else {
                    textArea.setText("Nenhum relatório gerado ainda.");
                }
            } catch (Exception ex) {
                textArea.setText("Erro ao carregar relatório.");
            }
        }
        private void gerar() {
            Sistema.getInstance().gerarRelatorio();
            ((Dashboard) SwingUtilities.getWindowAncestor(this)).refreshAll();
        }
    }
}