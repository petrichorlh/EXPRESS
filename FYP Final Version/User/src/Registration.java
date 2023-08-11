import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mnb
 */
public class Registration extends javax.swing.JFrame{
    Connection con = null;
    
    public void connect() {
        try {

            Class.forName("com.mysql.jdbc.Driver");     //general connection error msg; error msg get from this class
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/delivery", "root", "123456"); //connection to Sql
        } catch (ClassNotFoundException | SQLException | HeadlessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

    }

   int backup = 1;      //backup Privkey
   int uID;
   boolean regValid = false;
   JFrame f1 = new JFrame();
   BigInteger[] resSign;
   
    /**
     * Creates new form Registration
     */    
    public Registration() {
        initComponents();
        Security.addProvider(new BouncyCastleProvider());
    }
    
    public boolean isInteger(String id) {
        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
 
    public void emailTo() {
        final String fromEmail = "fypdooraccess@gmail.com"; //requires valid gmail id
        final String username = "fypdooraccess";
        final String password = "Door12345"; // correct password for gmail id
        String host = "smtp.gmail.com";    //gmail smtp server
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.host", host); // Setup mail server
        props.put("mail.smtp.port", "587"); //setup SMTP Port

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message msg = new MimeMessage(session);    // Create a default MimeMessage object.
            msg.setFrom(new InternetAddress(fromEmail));      //set the sender
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(regEmail.getText()));   //set the receiver
            msg.setSubject("Door Access Key Backup");     //set the email title
            BodyPart msgBody = new MimeBodyPart();    //create email body object
            msgBody.setText("Your details - ID: " + regID.getText() + " \n \n User Name: " + regName.getText());     //set the email body
            Multipart multi = new MimeMultipart();    //make the email multiple part so able to add body content and attachment
            multi.addBodyPart(msgBody);       //add into email
            String imgfile = "D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\UserFD\\User\\Img.png";
            msgBody = new MimeBodyPart();
            DataSource source = new FileDataSource(imgfile);      //get the source img
            msgBody.setDataHandler(new DataHandler(source));
            msgBody.setFileName(imgfile);
            multi.addBodyPart(msgBody);       //add into email
            msg.setContent(multi);
            Transport.send(msg);          //using transport layer to send
            JOptionPane.showMessageDialog(this, "Email sent! ", "Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
    
    public void generateQR(byte by[], int na) {
        String indicator = "111";
        String myCodeText = indicator + "," + na + "," + Base64.getEncoder().encodeToString(by);

        int size = 400;
        try {
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); //set the error correction level
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);  //change to byte from QR code
            int CrunchifyWidth = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);     //set into image file
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < CrunchifyWidth; i++) {
                for (int j = 0; j < CrunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            if (backup == 1) {
                try {
                    File outputfile = new File("Img.png");
                    ImageIO.write(image, "png", outputfile);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage());
                }
            }

            f1.getContentPane().setLayout(new FlowLayout());
            f1.getContentPane().add(new JLabel(new ImageIcon(image)));
            f1.setSize(700, 500);
            f1.setResizable(false);
            f1.setTitle("Generate QR");
            f1.setVisible(true);

        } catch (WriterException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

    }
    
    public void regDetails() {
        try {
            connect();
            String hash = md5(regPW.getPassword());
            String sql = "INSERT INTO user (User_ID, User_Name, User_Password) " + " VALUES (?,?,?)";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, uID);
            pstmt.setString(2, regName.getText());
            pstmt.setBytes(3, hash.getBytes());//.setString(3,hash);
            pstmt.execute();
            pstmt.close();

            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime192v1");

            KeyPairGenerator g = null;
            try {
                g = KeyPairGenerator.getInstance("ECDSA", "BC");   //create instance which use ECDSA and using Bouncy castle library
            } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
                Logger.getLogger(Registration.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                g.initialize(ecSpec, new SecureRandom());   //initialize the key pair generator
            } catch (InvalidAlgorithmParameterException ex) {
                Logger.getLogger(Registration.class.getName()).log(Level.SEVERE, null, ex);
            }

            KeyPair pair = g.generateKeyPair();     //generating key

            PrivateKey privk = pair.getPrivate();   //get the private key
            byte[] da = privk.getEncoded();

            PublicKey pubkey = pair.getPublic();    //get the public key
            byte[] qa = pubkey.getEncoded();

            try {

                sql = "INSERT INTO `ss` (Public_Key, User_ID) VALUES (?, ?)";

                PreparedStatement ps = con.prepareStatement(sql);

                ps.setBinaryStream(1, new ByteArrayInputStream(qa));
                ps.setInt(2, uID);
                ps.execute();
                ps.close();
                con.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
            dispose();

            generateQR(da, uID);

            JOptionPane.showMessageDialog(this, "Successful registration!", "Successful", JOptionPane.INFORMATION_MESSAGE);
            f1.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    new Main().setVisible(true);
                }
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());

        }

    }

    public boolean validUserName(String un) {
        boolean found = false;
        try {
            connect();
            Statement stat = con.createStatement();
            String sql = "SELECT * from user where User_Name = '" + un + "'";
            ResultSet rs = stat.executeQuery(sql);
            while (rs.next()) {
                found = true;
            }
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        if (found == true) {
            return false;
        } else {
            return true;
        }
    }
     
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        regID = new javax.swing.JTextField();
        regName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        regEmail = new javax.swing.JTextField();
        btnReset = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        regSub = new javax.swing.JButton();
        cbEmail = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        regPW = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Registration");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Registration Form");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("ID :");

        regID.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                regIDFocusLost(evt);
            }
        });

        regName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                regNameFocusLost(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Name :");

        jLabel8.setText("Email :");

        regEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                regEmailFocusLost(evt);
            }
        });

        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        regSub.setText("Submit");
        regSub.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regSubActionPerformed(evt);
            }
        });

        cbEmail.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        cbEmail.setSelected(true);
        cbEmail.setText("Send backup copy to email");
        cbEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbEmailActionPerformed(evt);
            }
        });

        jLabel4.setText("Password:");

        regPW.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                regPWFocusLost(evt);
            }
        });
        regPW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regPWActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("*Please complete your details");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(regID, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(regName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(regEmail, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(regPW, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(regSub)
                        .addGap(35, 35, 35)
                        .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbEmail))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(regID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20)
                        .addComponent(regName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(regEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(regPW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addComponent(cbEmail)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(regSub)
                    .addComponent(btnReset)
                    .addComponent(btnBack))
                .addGap(40, 40, 40))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cbEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbEmailActionPerformed
        cbEmail.setForeground(Color.black);
        if (!cbEmail.isSelected()){
            int choice = JOptionPane.showConfirmDialog(this, "No backup copy will stored in server!!! Confirm no backup to email?", "WARNING", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == 0){
                cbEmail.setSelected(false);
                cbEmail.setForeground(Color.red);
                backup = 0;
            }
            else {
                cbEmail.setSelected(true);
                backup = 1;
            }
        }
        
    }//GEN-LAST:event_cbEmailActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        dispose();

        new Main().setVisible(true);
        
    }//GEN-LAST:event_btnBackActionPerformed

    private void regIDFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_regIDFocusLost
        // TODO add your handling code here:
        regID.setBackground(null);
        if(!isInteger(regID.getText()) || regID.getText().length()<5){
            regID.setBackground(Color.red);
        }
        else uID = parseInt(regID.getText());
    }//GEN-LAST:event_regIDFocusLost

    private void regEmailFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_regEmailFocusLost

    }//GEN-LAST:event_regEmailFocusLost

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        regID.setText(null);
        regName.setText(null);
        regEmail.setText(null);
        regPW.setText(null);
    }//GEN-LAST:event_btnResetActionPerformed

    private void regSubActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regSubActionPerformed
        if(isInteger(regID.getText()) && regID.getText().length()>4){
            if(!regName.getText().isEmpty() && validUserName(regName.getText())){
                if(regPW.getText().length()>7){
                    if(cbEmail.isSelected() && !regEmail.getText().equals("")){
                        if (isValidEmailAddress(regEmail.getText())){
                            backup =1;
                            regDetails();
                            emailTo();
                        }else {
                            JOptionPane.showMessageDialog(this, "Please enter valid email address for backup!!","WARNING", JOptionPane.WARNING_MESSAGE);
                    }
                    
                    }else if(!cbEmail.isSelected()){
                        regDetails();
                    }
                else{
                        JOptionPane.showMessageDialog(this, "Please enter valid email address for backup!!","WARNING", JOptionPane.WARNING_MESSAGE);
                    }
            }else{
                    JOptionPane.showMessageDialog(this, "Please enter valid password! ","WARNING", JOptionPane.WARNING_MESSAGE);
            }  
            }else{
                JOptionPane.showMessageDialog(this, "You had registered BEFORE or user name had BEEN USED! ","WARNING", JOptionPane.WARNING_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(this, "INVALID ID!!!  \n \n ***Please enter ID MORE THAN 4 digit & integer ONLY!*** ","WARNING", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_regSubActionPerformed

    private void regNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_regNameFocusLost
            regName.setBackground(null);
        if (regName.getText().isEmpty()){
            regName.setBackground(Color.red);
        }
    }//GEN-LAST:event_regNameFocusLost

    private void regPWFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_regPWFocusLost
        // TODO add your handling code here:
            regPW.setBackground(null);
        if (regPW.getText().isEmpty() || regPW.getText().length()<8 ){
            regPW.setBackground(Color.red);
        }
    }//GEN-LAST:event_regPWFocusLost

    private void regPWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regPWActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_regPWActionPerformed

    public static String md5(char[] c) {
        try {
            MessageDigest digs = MessageDigest.getInstance("MD5");

            digs.update((new String(c)).getBytes("UTF8"));

            String str = new String(digs.digest());

            return str;
        } catch (Exception ex) {
            return "";
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Registration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Registration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Registration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Registration.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Registration().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnReset;
    private javax.swing.JCheckBox cbEmail;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JTextField regEmail;
    private javax.swing.JTextField regID;
    private javax.swing.JTextField regName;
    private javax.swing.JPasswordField regPW;
    private javax.swing.JButton regSub;
    // End of variables declaration//GEN-END:variables
}
