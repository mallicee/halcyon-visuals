package dev.halcyon.loader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Окно загрузчика Halcyon в стиле сайта: тёмный фон, стеклянные карточки, изумрудный акцент. */
public final class HalcyonLoader {
    static final Color BG = new Color(0x0d0d11), BG2 = new Color(0x12121a), FG = new Color(0xe9ecf2),
            MUTED = new Color(0x8b90a5), ACCENT = new Color(0x3dffa0), ACCENT2 = new Color(0x20d6c8),
            LINE = new Color(255, 255, 255, 22), DANGER = new Color(0xff5c7a);

    private JFrame frame;
    private JTextField dirField;
    private JCheckBox separate;
    private JCheckBox optimize;
    private JTextArea config;
    private JLabel status;
    private Bar bar;
    private NeonButton installBtn;
    private GhostButton uninstallBtn;

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        UIManager.put("OptionPane.background", BG2);
        UIManager.put("Panel.background", BG2);
        UIManager.put("OptionPane.messageForeground", FG);
        UIManager.put("FileChooser.background", BG2);
        SwingUtilities.invokeLater(() -> new HalcyonLoader().show());
    }

    private static Font font(int style, float size) {
        for (String f : new String[]{"Manrope", "Segoe UI Variable Text", "Segoe UI", "SansSerif"}) {
            Font ft = new Font(f, style, Math.round(size));
            if (!ft.getFamily().equals("Dialog") || f.equals("SansSerif")) return ft.deriveFont(size);
        }
        return new Font("SansSerif", style, Math.round(size));
    }

    private static Font mono(float size) {
        for (String f : new String[]{"JetBrains Mono", "Cascadia Mono", "Consolas", "Monospaced"}) {
            Font ft = new Font(f, Font.PLAIN, Math.round(size));
            if (!ft.getFamily().equals("Dialog")) return ft.deriveFont(size);
        }
        return new Font("Monospaced", Font.PLAIN, Math.round(size));
    }

    private void show() {
        frame = new JFrame("Halcyon Loader");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setIconImage(icon(64));

        JPanel root = new Backdrop();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(28, 32, 24, 32));

        // Шапка
        JPanel head = row();
        JLabel logo = new JLabel(new ImageIcon(icon(36)));
        JLabel title = label("halcyon.", font(Font.BOLD, 22f), FG);
        JLabel tag = label("LOADER · " + Installer.MC_VERSION + " · FABRIC", mono(11f), MUTED);
        JPanel t = new JPanel(); t.setOpaque(false); t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(title); t.add(Box.createVerticalStrut(2)); t.add(tag);
        head.add(logo); head.add(Box.createHorizontalStrut(12)); head.add(t); head.add(Box.createHorizontalGlue());
        root.add(head);
        root.add(Box.createVerticalStrut(18));

        JLabel h1 = label("<html>Установи визуалы Halcyon<br>и играй в один клик</html>", font(Font.BOLD, 24f), FG);
        root.add(left(h1));
        root.add(Box.createVerticalStrut(8));
        root.add(left(label("<html><body style='width:430px'>HUD, прицелы, эффекты попаданий и стеклянный GUI. Загрузчик поставит Fabric, Fabric API и мод, затем откроет официальный Minecraft Launcher с профилем «Halcyon Visuals».</body></html>", font(Font.PLAIN, 13f), MUTED)));
        root.add(Box.createVerticalStrut(20));

        // Карточка настроек
        Card card = new Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        card.add(left(label("ПАПКА MINECRAFT", mono(11f), MUTED)));
        card.add(Box.createVerticalStrut(6));
        JPanel dirRow = row();
        dirField = field(Installer.defaultMinecraftDir().toString());
        GhostButton browse = new GhostButton("Изменить");
        browse.addActionListener(e -> chooseDir());
        dirRow.add(dirField); dirRow.add(Box.createHorizontalStrut(8)); dirRow.add(browse);
        card.add(dirRow);
        card.add(Box.createVerticalStrut(12));

        separate = new JCheckBox("Отдельная папка игры (рекомендуется: ваши моды не затронуты)", true);
        styleCheck(separate);
        card.add(left(separate));
        card.add(Box.createVerticalStrut(6));
        optimize = new JCheckBox("Оптимизация: Sodium, Lithium, FerriteCore (больше FPS)", true);
        styleCheck(optimize);
        card.add(left(optimize));
        card.add(Box.createVerticalStrut(14));

        card.add(left(label("КОНФИГ HUD С САЙТА (НЕОБЯЗАТЕЛЬНО)", mono(11f), MUTED)));
        card.add(Box.createVerticalStrut(6));
        config = new JTextArea(4, 40);
        config.setLineWrap(true);
        config.setFont(mono(11.5f));
        config.setForeground(FG); config.setCaretColor(ACCENT); config.setBackground(new Color(0x0b0b0f));
        config.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane sp = new JScrollPane(config);
        sp.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 30)));
        sp.getViewport().setBackground(new Color(0x0b0b0f));
        sp.setAlignmentX(0);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        card.add(sp);
        card.add(Box.createVerticalStrut(4));
        card.add(left(label("Конструктор HUD на сайте → кнопка JSON → «Скопировать JSON» → вставьте сюда.", font(Font.PLAIN, 11.5f), MUTED)));
        card.setAlignmentX(0);
        root.add(card);
        root.add(Box.createVerticalStrut(18));

        // Прогресс
        bar = new Bar();
        bar.setAlignmentX(0);
        root.add(bar);
        root.add(Box.createVerticalStrut(8));
        status = label("Готово к установке", mono(11.5f), MUTED);
        root.add(left(status));
        root.add(Box.createVerticalStrut(16));

        // Кнопки
        JPanel actions = row();
        installBtn = new NeonButton("Установить и играть");
        installBtn.addActionListener(e -> install());
        uninstallBtn = new GhostButton("Удалить");
        uninstallBtn.addActionListener(e -> uninstall());
        actions.add(installBtn); actions.add(Box.createHorizontalStrut(10)); actions.add(uninstallBtn); actions.add(Box.createHorizontalGlue());
        root.add(actions);
        root.add(Box.createVerticalStrut(16));
        root.add(left(label("<html><body style='width:430px'>Вход в аккаунт выполняет официальный Minecraft Launcher. Загрузчик не запрашивает и не хранит пароли. Только визуальные изменения, без игровых преимуществ. Не является продуктом Mojang AB или Microsoft.</body></html>", font(Font.PLAIN, 11f), new Color(0x6b7085))));

        frame.setContentPane(root);
        frame.setSize(560, 790);
        frame.setMinimumSize(new Dimension(520, 750));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void chooseDir() {
        JFileChooser fc = new JFileChooser(dirField.getText());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Папка .minecraft");
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) dirField.setText(fc.getSelectedFile().getAbsolutePath());
    }

    private void setBusy(boolean busy) {
        installBtn.setEnabled(!busy); uninstallBtn.setEnabled(!busy);
        dirField.setEnabled(!busy); separate.setEnabled(!busy); optimize.setEnabled(!busy); config.setEnabled(!busy);
    }

    private void install() {
        Path dir = Paths.get(dirField.getText().trim());
        Installer inst = new Installer(dir, separate.isSelected(), optimize.isSelected(), config.getText(), step -> SwingUtilities.invokeLater(() -> {
            bar.setValue((step.index() - 0.5f) / step.total());
            status.setForeground(MUTED);
            status.setText(step.index() + "/" + step.total() + " · " + step.text() + "…");
        }));
        String problem = inst.validate();
        if (problem != null) { error(problem); return; }
        if (!confirm("Перед установкой закройте Minecraft Launcher, если он открыт:\nиначе он может перезаписать список профилей.\n\nПродолжить?")) return;

        setBusy(true);
        new SwingWorker<Void, Void>() {
            Exception failure;
            @Override protected Void doInBackground() {
                try { inst.install(); } catch (Exception e) { failure = e; }
                return null;
            }
            @Override protected void done() {
                setBusy(false);
                if (failure != null) {
                    bar.setValue(0);
                    status.setForeground(DANGER);
                    status.setText("Установка не завершена");
                    error("Не удалось установить:\n" + (failure.getMessage() == null ? failure.toString() : failure.getMessage())
                            + "\n\nПроверьте интернет и попробуйте ещё раз.");
                    return;
                }
                bar.setValue(1f);
                status.setForeground(ACCENT);
                status.setText("Установлено · папка игры: " + inst.gameDir().getFileName());
                boolean opened = Installer.openLauncher();
                info(opened
                        ? "Готово! Открываем Minecraft Launcher.\n\nВыберите профиль «Halcyon Visuals» слева от кнопки «Играть» и запускайте.\nВ игре клавиша H перезагружает конфиг HUD."
                        : "Готово! Откройте Minecraft Launcher вручную,\nвыберите профиль «Halcyon Visuals» и нажмите «Играть».");
            }
        }.execute();
    }

    private void uninstall() {
        Path dir = Paths.get(dirField.getText().trim());
        if (!Files.isDirectory(dir)) { error("Папка Minecraft не найдена: " + dir); return; }
        int r = JOptionPane.showOptionDialog(frame,
                "Удалить профиль «Halcyon Visuals» и мод?\nОтдельную папку игры (сохранения миров внутри неё) можно оставить.",
                "Удаление", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                new Object[]{"Удалить всё", "Оставить папку игры", "Отмена"}, "Отмена");
        if (r != 0 && r != 1) return;
        try {
            String msg = Installer.uninstall(dir, r == 0);
            bar.setValue(0);
            status.setForeground(MUTED); status.setText("Удалено");
            info(msg);
        } catch (Exception e) {
            error("Не удалось удалить: " + e.getMessage());
        }
    }

    private void error(String m) { JOptionPane.showMessageDialog(frame, m, "Halcyon Loader", JOptionPane.ERROR_MESSAGE); }
    private void info(String m) { JOptionPane.showMessageDialog(frame, m, "Halcyon Loader", JOptionPane.INFORMATION_MESSAGE); }
    private boolean confirm(String m) { return JOptionPane.showConfirmDialog(frame, m, "Halcyon Loader", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION; }

    // ---------- Компоненты ----------

    private static JPanel row() {
        JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); p.setAlignmentX(0);
        return p;
    }

    private static JComponent left(JComponent c) { c.setAlignmentX(0); return c; }

    private static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text); l.setFont(f); l.setForeground(c); return l;
    }

    private static JTextField field(String text) {
        JTextField f = new JTextField(text);
        f.setFont(mono(12f)); f.setForeground(FG); f.setCaretColor(ACCENT); f.setBackground(new Color(0x0b0b0f));
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 30)), new EmptyBorder(8, 10, 8, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return f;
    }

    private static void styleCheck(JCheckBox c) {
        c.setOpaque(false); c.setForeground(FG); c.setFont(font(Font.PLAIN, 13f)); c.setFocusPainted(false);
    }

    static BufferedImageHolder ICON_CACHE = new BufferedImageHolder();
    static final class BufferedImageHolder { java.util.Map<Integer, Image> m = new java.util.HashMap<>(); }

    /** Знак Halcyon: две дуги и точка (как на сайте). */
    static Image icon(int size) {
        return ICON_CACHE.m.computeIfAbsent(size, s -> {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(s, s, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(61, 255, 160, 28));
            g.fill(new RoundRectangle2D.Float(0, 0, s, s, s * .3f, s * .3f));
            g.setColor(new Color(61, 255, 160, 110));
            g.draw(new RoundRectangle2D.Float(.5f, .5f, s - 1, s - 1, s * .3f, s * .3f));
            float k = s / 24f;
            g.setStroke(new BasicStroke(2f * k, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(ACCENT);
            g.drawArc(Math.round(4.5f * k), Math.round(6.5f * k), Math.round(15 * k), Math.round(16 * k), 0, 180);
            g.drawArc(Math.round(8 * k), Math.round(10.5f * k), Math.round(8 * k), Math.round(8 * k), 0, 180);
            g.fill(new java.awt.geom.Ellipse2D.Float(10.6f * k, 16.6f * k, 2.8f * k, 2.8f * k));
            g.dispose();
            return img;
        });
    }

    /** Фон: тёмный с мягкими аурами. */
    static final class Backdrop extends JPanel {
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(BG); g.fillRect(0, 0, getWidth(), getHeight());
            aura(g, -120, -160, 420, new Color(61, 255, 160, 46));
            aura(g, getWidth() - 220, 160, 360, new Color(32, 214, 200, 34));
            g.dispose();
        }
        private static void aura(Graphics2D g, int x, int y, int d, Color c) {
            RadialGradientPaint p = new RadialGradientPaint(new Point(x + d / 2, y + d / 2), d / 2f,
                    new float[]{0f, 1f}, new Color[]{c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)});
            g.setPaint(p); g.fillOval(x, y, d, d);
        }
    }

    /** Стеклянная карточка. */
    static final class Card extends JPanel {
        Card() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D r = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 16), 0, getHeight(), new Color(255, 255, 255, 5)));
            g.fill(r);
            g.setColor(LINE); g.draw(r);
            g.dispose();
        }
    }

    /** Тонкая полоса прогресса с градиентом. */
    static final class Bar extends JComponent {
        private float value, shown;
        private final Timer anim = new Timer(16, e -> { shown += (value - shown) * 0.18f; if (Math.abs(value - shown) < 0.002f) { shown = value; ((Timer) e.getSource()).stop(); } repaint(); });
        Bar() { setPreferredSize(new Dimension(10, 6)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 6)); }
        void setValue(float v) { value = Math.max(0, Math.min(1, v)); anim.start(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            g.setColor(new Color(255, 255, 255, 18));
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), h, h, h));
            int w = Math.round(getWidth() * shown);
            if (w > 0) {
                g.setPaint(new GradientPaint(0, 0, ACCENT2, getWidth(), 0, ACCENT));
                g.fill(new RoundRectangle2D.Float(0, 0, Math.max(w, h), h, h, h));
            }
            g.dispose();
        }
    }

    /** Основная кнопка: градиент и свечение при наведении. */
    static class NeonButton extends JButton {
        boolean hover;
        NeonButton(String text) {
            super(text);
            setFont(font(Font.BOLD, 14f)); setForeground(new Color(0x04120b));
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(14, 26, 14, 26));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (!isEnabled()) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            if (hover && isEnabled()) {
                for (int i = 6; i > 0; i--) {
                    g.setColor(new Color(61, 255, 160, 10));
                    g.fill(new RoundRectangle2D.Float(-i + 6, -i + 6, w + 2 * i - 12, h + 2 * i - 12, 18 + i, 18 + i));
                }
            }
            g.setPaint(new GradientPaint(0, 0, ACCENT, w, h, ACCENT2));
            g.fill(new RoundRectangle2D.Float(3, 3, w - 6, h - 6, 14, 14));
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Вторичная кнопка: прозрачная с тонкой рамкой. */
    static class GhostButton extends JButton {
        boolean hover;
        GhostButton(String text) {
            super(text);
            setFont(font(Font.BOLD, 13f)); setForeground(FG);
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 16, 10, 16));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!isEnabled()) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            RoundRectangle2D r = new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            g.setColor(hover ? new Color(61, 255, 160, 22) : new Color(255, 255, 255, 8));
            g.fill(r);
            g.setColor(hover ? new Color(61, 255, 160, 120) : new Color(255, 255, 255, 30));
            g.draw(r);
            g.dispose();
            super.paintComponent(g0);
        }
    }
}
