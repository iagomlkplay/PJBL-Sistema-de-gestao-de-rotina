import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;

public class LoginScreen extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtSenha;
    private JButton btnEntrar;
    private JButton btnCadastrar;
    private JLabel lblMensagem;

    public LoginScreen() {
        setTitle("Sistema de Gestão de Rotina - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // E-mail
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        panel.add(txtEmail, gbc);

        // Senha
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        txtSenha = new JPasswordField(20);
        panel.add(txtSenha, gbc);

        // Botões
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnEntrar = new JButton("Entrar");
        btnCadastrar = new JButton("Cadastrar");
        botoesPanel.add(btnEntrar);
        botoesPanel.add(btnCadastrar);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(botoesPanel, gbc);

        // Mensagem
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        lblMensagem = new JLabel(" ");
        lblMensagem.setForeground(Color.RED);
        panel.add(lblMensagem, gbc);

        add(panel, BorderLayout.CENTER);

        btnEntrar.addActionListener(e -> realizarLogin());
        btnCadastrar.addActionListener(e -> abrirCadastro());
        txtSenha.addActionListener(e -> realizarLogin());
    }

    private void realizarLogin() {
        String email = txtEmail.getText().trim();
        String senha = new String(txtSenha.getPassword());

        if (email.isEmpty() || senha.isEmpty()) {
            lblMensagem.setText("Preencha e-mail e senha.");
            return;
        }

        Usuario usuario = Sistema.getInstance().autenticar(email, senha);

        if (usuario == null) {
            lblMensagem.setText("E-mail ou senha inválidos.");
            return;
        }

        if (usuario instanceof UsuarioDev) {
            new Dashboard((UsuarioDev) usuario).setVisible(true);
        } else if (usuario instanceof UsuarioGestor) {
            new Dashboard((UsuarioGestor) usuario).setVisible(true);
        } else {
            lblMensagem.setText("Tipo de usuário desconhecido.");
            return;
        }
        dispose();
    }

    private JFormattedTextField criarCampoCpf() {
        try {
            MaskFormatter maskCpf = new MaskFormatter("###.###.###-##");
            maskCpf.setPlaceholderCharacter('_');
            JFormattedTextField campo = new JFormattedTextField(maskCpf);
            campo.setColumns(15);
            return campo;
        } catch (ParseException ex) {
            JFormattedTextField campo = new JFormattedTextField();
            campo.setColumns(15);
            return campo;
        }
    }

    private void abrirCadastro() {
        JDialog dialog = new JDialog(this, "Novo Cadastro", true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Campos comuns
        JTextField txtNome = new JTextField(15);
        JFormattedTextField txtCpf = criarCampoCpf();
        JTextField txtEmail = new JTextField(15);
        JPasswordField txtSenha = new JPasswordField(15);
        JPasswordField txtConfirma = new JPasswordField(15);
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"DESENVOLVEDOR", "GESTOR"});

        // Campos específicos
        JTextField txtDepartamento = new JTextField(15);
        JComboBox<UsuarioGestor> cbGestor = new JComboBox<>();

        // Carregar gestores existentes (para o combo de desenvolvedor)
        for (UsuarioGestor g : Sistema.getInstance().getGestores()) {
            cbGestor.addItem(g);
        }

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Nome:*"), gbc);
        gbc.gridx = 1;
        form.add(txtNome, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("CPF (000.000.000-00):*"), gbc);
        gbc.gridx = 1;
        form.add(txtCpf, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("E-mail:*"), gbc);
        gbc.gridx = 1;
        form.add(txtEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Senha:*"), gbc);
        gbc.gridx = 1;
        form.add(txtSenha, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Confirmar senha:*"), gbc);
        gbc.gridx = 1;
        form.add(txtConfirma, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Tipo:*"), gbc);
        gbc.gridx = 1;
        form.add(cbTipo, gbc);

        // ========== Painel condicional ==========
        JPanel panelGestor = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelGestor.add(new JLabel("Departamento:"));
        panelGestor.add(txtDepartamento);
        txtDepartamento.setColumns(15);

        JPanel panelDev = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelDev.add(new JLabel("Gestor responsável:"));
        panelDev.add(cbGestor);
        cbGestor.setPreferredSize(new Dimension(150, 25));

        JPanel condPanel = new JPanel(new CardLayout());
        condPanel.add(panelGestor, "GESTOR");
        condPanel.add(panelDev, "DESENVOLVEDOR");
        condPanel.setVisible(true);

        // Define o card inicial baseado no tipo padrão (DESENVOLVEDOR)
        String tipoInicial = (String) cbTipo.getSelectedItem();
        CardLayout cl = (CardLayout) condPanel.getLayout();
        cl.show(condPanel, tipoInicial);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(condPanel, gbc);

        // Listener para trocar o card quando o tipo mudar
        cbTipo.addActionListener(e -> {
            String tipo = (String) cbTipo.getSelectedItem();
            CardLayout cl2 = (CardLayout) condPanel.getLayout();
            cl2.show(condPanel, tipo);
            dialog.pack();
        });

        JPanel botoes = new JPanel();
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        botoes.add(btnSalvar);
        botoes.add(btnCancelar);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(botoes, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            String cpf = txtCpf.getText().trim().replaceAll("\\D", "");
            String email = txtEmail.getText().trim();
            String senha = new String(txtSenha.getPassword());
            String confirma = new String(txtConfirma.getPassword());
            String tipo = (String) cbTipo.getSelectedItem();

            if (nome.isEmpty() || cpf.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Preencha todos os campos obrigatórios (*).");
                return;
            }
            if (cpf.length() != 11) {
                JOptionPane.showMessageDialog(dialog, "CPF inválido. Use o formato 000.000.000-00.");
                return;
            }
            if (!senha.equals(confirma)) {
                JOptionPane.showMessageDialog(dialog, "As senhas não coincidem.");
                return;
            }

            boolean ok = false;
            if ("GESTOR".equals(tipo)) {
                String departamento = txtDepartamento.getText().trim();
                if (departamento.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Departamento obrigatório para gestor.");
                    return;
                }
                UsuarioGestor novo = new UsuarioGestor(nome, cpf, email, senha, departamento);
                ok = Sistema.getInstance().realizarCadastro(novo);
                if (ok) JOptionPane.showMessageDialog(dialog, "Gestor cadastrado com sucesso!");
            } else {
                // Desenvolvedor
                UsuarioGestor gestorSelecionado = (UsuarioGestor) cbGestor.getSelectedItem();
                if (gestorSelecionado == null) {
                    JOptionPane.showMessageDialog(dialog, "Selecione um gestor responsável.");
                    return;
                }
                UsuarioDev novo = new UsuarioDev(nome, cpf, email, senha);
                novo.setGestorId(gestorSelecionado.getId());
                ok = Sistema.getInstance().realizarCadastro(novo);
                if (ok) JOptionPane.showMessageDialog(dialog, "Desenvolvedor cadastrado com sucesso!");
            }

            if (ok) {
                dialog.dispose();
                txtEmail.setText(email);
                txtSenha.setText("");
                lblMensagem.setText("Cadastro realizado! Faça login.");
            } else {
                JOptionPane.showMessageDialog(dialog, "Erro no cadastro. E-mail ou CPF já existentes.");
            }
        });

        dialog.setVisible(true);
    }
}