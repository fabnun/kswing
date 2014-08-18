package com.kreadi.swing;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class KConfig extends javax.swing.JFrame {

    private Object[] values = new Object[0];
    private final File file;
    private final int keySize;
    private boolean exitOnClose;
    private boolean defaultValues = false;

    private JComponent build(Class type, final int idx) {
        if (type.equals(String.class)) {
            final JTextField field = new JTextField("" + values[idx]);
            field.addCaretListener(new CaretListener() {

                @Override
                public void caretUpdate(CaretEvent e) {
                    values[idx] = field.getText();
                }
            });
            return field;
        } else if (type.equals(Integer.class)) {
            final JTextField field = new JTextField();
            field.setDocument(new KPlainDocument(12, "\\d+"));
            field.setText("" + values[idx]);
            field.setText(null);
            field.addCaretListener(new CaretListener() {

                @Override
                public void caretUpdate(CaretEvent e) {
                    values[idx] = Integer.parseInt(field.getText());
                }
            });
            return field;
        } else if (type.equals(Boolean.class)) {
            final JCheckBox field = new JCheckBox();
            field.setSelected((boolean) values[idx]);
            field.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    values[idx] = field.isSelected();
                }
            });
            return field;
        } else if (type.equals(char[].class)) {
            final JPasswordField field = new JPasswordField();
            field.setText(new String((char[]) values[idx]));
            field.addCaretListener(new CaretListener() {

                @Override
                public void caretUpdate(CaretEvent e) {
                    values[idx] = field.getPassword();
                }
            });
            return field;
        }
        return null;
    }

    /**
     *
     * @param installPng imagen de instalacion
     * @param pngWidth ancho de la imagen de instalacion
     * @param width ancho de la ventana
     * @param height alto de la ventana
     * @param exit sale cuando guarda
     * @param title titulo de la ventana
     * @param icon icono de la ventana
     * @param save texto para guardar y salir
     * @param cfgFile archivo donde se guardara
     * @param passIdx indice del campo que se usara como password para acceder a la configuracion
     * @param ksize tamaño de la clave para encriptar los datos
     * @param texts array de textos de los campos
     * @param tooltips array de ayudas de los campos
     * @param types array de clases de los campos
     * @param defaults array de valores por defecto
     */
    public KConfig(String installPng, int pngWidth, int width, int height, boolean exit,
            String title, String icon, String save, String cfgFile, int passIdx, int ksize,
            String[] texts, String[] tooltips, Class[] types, Object[] defaults) {
        this(cfgFile, ksize);
        this.exitOnClose = exit;
        if (values == null) {
            defaultValues = true;
            values = defaults;
        }
        int size = texts.length;
        initComponents();
        setSize(width, height);
        setTitle(title);
        jButton1.setText(save);
        try {
            setIconImage(ImageIO.read(new File(icon)));
        } catch (Exception e) {
        }
        try {
            jLabel1.setIcon(new ImageIcon(installPng));
        } catch (Exception e) {
        }
        jLabel1.setPreferredSize(new Dimension(pngWidth, 0));

        if (tooltips.length != size || types.length != size || defaults.length != size) {
            System.err.println("Arrays de distinto tamaño");
            System.exit(-1);
        }
        jPanel2.setLayout(new GridLayout(size, 2, 8, 8));
        for (int i = 0; i < size; i++) {
            jPanel2.add(new JLabel(texts[i]));
            jPanel2.add(build(types[i], i));
        }
        if (!defaultValues && passIdx > 0 && passIdx < values.length) {
            JPanel panel = new JPanel();
            JLabel label = new JLabel("Clave de acceso");
            JPasswordField pass = new JPasswordField(10);
            panel.add(label);
            panel.add(pass);
            String[] options = new String[]{"OK", "Cancelar"};
            int option = JOptionPane.showOptionDialog(null, panel, "Acceso a configuración", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
            char[] password = pass.getPassword();
            if (option != 0 || (password != null && !Arrays.equals(password, (char[]) values[passIdx]))) {
                System.exit(-1);
            }
        }
        setLocationRelativeTo(null);
    }

    public void waitForValues() {
        while (values == null || defaultValues) {
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
        }
    }

    public KConfig(String cfgFile, int ksize) {
        file = new File(cfgFile);
        keySize = ksize;
        exitOnClose = true;
        if (file.exists()) {
            byte[] buff = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(buff);
                buff = decrypt(IDMachine.mix(keySize), buff);
                try (ByteArrayInputStream bais = new ByteArrayInputStream(buff); ObjectInputStream ois = new ObjectInputStream(bais)) {
                    values = (Object[]) ois.readObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            values = null;
        }
    }

    public Object[] getValues() {
        return values;
    }

    public static byte[] encrypt(byte[] password, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        Key aesKey = new SecretKeySpec(password, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] password, byte[] encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        Key aesKey = new SecretKeySpec(password, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return cipher.doFinal(encryptedData);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        jLabel1.setFocusable(false);
        jLabel1.setMaximumSize(new java.awt.Dimension(32767, 32767));
        jLabel1.setPreferredSize(new java.awt.Dimension(10, 0));
        getContentPane().add(jLabel1);

        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setFocusable(false);
        jPanel2.setLayout(null);
        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(" ");
        jLabel2.setFocusable(false);
        jLabel2.setMinimumSize(new java.awt.Dimension(3, 22));
        jLabel2.setPreferredSize(new java.awt.Dimension(3, 22));
        jLabel2.setRequestFocusEnabled(false);
        jPanel1.add(jLabel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setFocusable(false);

        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.setFocusable(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream ois = new ObjectOutputStream(baos)) {
            ois.writeObject(values);
            byte[] buff = encrypt(IDMachine.mix(keySize), baos.toByteArray());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(buff);
            }
            defaultValues = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (exitOnClose) {
            System.exit(0);
        } else {
            setVisible(false);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables

}
