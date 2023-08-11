import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import net.proteanit.sql.DbUtils;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import org.bytedeco.javacpp.opencv_face;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import org.opencv.core.Core;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */
public class UserDatabase2 extends javax.swing.JFrame {

    Connection con = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    opencv_face.FaceRecognizer faceRecognizer;
    
    /**
     * Creates new form UserDatabase2
     */
    public UserDatabase2() {
        initComponents();
        //conn = MySQLConnect.ConnectDb();
        connect();
        Update_table();
    }

    private void Update_table() {
        try {
            String sql = "select User_ID, User_Name from user";
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
            Table_User.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    
        public void getFaceRecog(){
    //String sql = "SELECT FaceRecognizer FROM setting WHERE Admin_ID = ?";
        try{
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT FaceRecognizer FROM setting");
            //String LBPH = "LBPH Face Recognizer";
            while (rs.next()) {
                //String FaceRecognizer = rs.getString("FaceRecognizer");
            //String FaceRecog = rs.getString(4);
            if(rs.getString("FaceRecognizer").equals("LBPH Face Recognizer")){
                faceRecognizer = createLBPHFaceRecognizer(1,8,8,8,90);
                System.out.println("LBPH Face Recognizer");
                //getFaceData();
            }else if(rs.getString("FaceRecognizer").equals("Eigen Face Recognizer")){
                faceRecognizer = createEigenFaceRecognizer(1,80);
                System.out.println("Eigen Face Recognizer");
                //getFaceData();
            }else if(rs.getString("FaceRecognizer").equals("Fisher Face Recognizer")){
                faceRecognizer = createFisherFaceRecognizer(0,80);
                System.out.println("Fisher Face Recognizer");
                //getFaceData();
            }    
        }
    }catch (SQLException ex) {
                Logger.getLogger(UserDatabase2.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
        
    
        public void getimage(){
                    try {
                String sql = "SELECT User_ID, User_Name FROM user ";
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int User_ID = rs.getInt("User_ID");
                    String User_Name = rs.getString("User_Name");
                    System.out.println(User_ID+", "+User_Name);
                    
                    
                    for (int z = 1; z<=7 ; z++){   
                    File filePath = new File("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\New folder\\"+User_ID+"-"+User_Name+"_"+z+".jpg");
                    String sql2 = "SELECT User_Image_"+z+" From user WHERE User_ID ="+User_ID;
                    PreparedStatement stmt2 = con.prepareStatement(sql2);
                    ResultSet rs2 = stmt2.executeQuery();
                    
                    try {
                        
                        if(rs2.next()){
                            Blob blob = rs2.getBlob("User_Image_"+z);
                            InputStream inputStream = blob.getBinaryStream();
                            OutputStream outputStream = new FileOutputStream(filePath);
                            byte[] buffer = new byte[1024];
                            while (inputStream.read(buffer) > 0) {
                                outputStream.write(buffer);
                            }   
                            
                            
                            inputStream.close();
                            outputStream.close();
                            
                            filePath.deleteOnExit();
                            
                            }
                        }catch (SQLException | FileNotFoundException ex) {
                        Logger.getLogger(FaceRecog.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(FaceRecog.class.getName()).log(Level.SEVERE, null, ex);
                    } 
            }System.out.println("File saved");
                }

    }       catch (SQLException ex) {
                Logger.getLogger(FaceRecog.class.getName()).log(Level.SEVERE, null, ex);
            }
    
    
    }
    
    public void trainimage(){
      
        try {
            //faceRecognizer = createFisherFaceRecognizer(1,80);
            //faceRecognizer = createEigenFaceRecognizer(1,100);
            //faceRecognizer = createLBPHFaceRecognizer(1,8,8,8,80);
            opencv_core.MatVector images = new opencv_core.MatVector();
            opencv_core.Mat labels = new opencv_core.Mat();
            
            
            
            String trainingDir = ("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\New folder\\");
            //String mPath = ("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\7.jpg");
            //Mat testImage = imread(mPath, CV_LOAD_IMAGE_GRAYSCALE);
            
            File root = new File(trainingDir);
            FilenameFilter imgFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    name = name.toLowerCase();
                    return name.endsWith(".jpg");
                }
            };
            
            File[] imageFiles = root.listFiles(imgFilter);
            
            images = new opencv_core.MatVector(imageFiles.length);
            
            labels = new opencv_core.Mat(imageFiles.length, 1, CV_32SC1);
            IntBuffer labelsBuf = labels.getIntBuffer();
            int counter = 0;
            
            for (File image : imageFiles) {
                opencv_core.Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                
                int label = Integer.parseInt(image.getName().split("\\-")[0]);
                
                images.put(counter, img);
                
                labelsBuf.put(counter, label);
                
                counter++;
            }
            
            faceRecognizer.train(images, labels);
            faceRecognizer.save("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\FaceData.xml");
            System.out.println("train success");
            
            
            String sql = "UPDATE facedata SET FaceData=?  WHERE No_FaceData = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            File file = new File("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\FaceData.xml");
            file.deleteOnExit();
            FileInputStream is = new FileInputStream(file);
            pstmt.setBinaryStream(1, is );
            pstmt.setInt(2, 0);
            pstmt.executeUpdate();
            System.out.println(pstmt);
            System.out.println("Success");
            pstmt.close();
            
        } catch (SQLException | IOException ex) {
            Logger.getLogger(FaceRecog.class.getName()).log(Level.SEVERE, null, ex);
        }
     }

    
    
    public void connect() {
        try {

            Class.forName("com.mysql.jdbc.Driver");     //general connection error msg; error msg get from this class
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/delivery", "root", "123456"); //connection to Sql
        } catch (ClassNotFoundException | SQLException | HeadlessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
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

        btn_delete = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        Table_User = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("User Key Management (Admin)");

        btn_delete.setText("Delete");
        btn_delete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_deleteActionPerformed(evt);
            }
        });

        jButton2.setText("Back");
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        Table_User.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(Table_User);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Key Management");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(178, 178, 178))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btn_delete)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_delete)
                    .addComponent(jButton2))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_deleteActionPerformed
        // TODO add your handling code here:
        int row = Table_User.getSelectedRow();
        String Table_click = (Table_User.getModel().getValueAt(row, 0).toString());
        try{
            //String sql = "SELECT User_ID = ? FROM user WHERE User_ID='"+Table_click+"'";
                    //int User_ID = rs.getInt("User_ID");
                    //String User_Name = rs.getString("User_Name");
                    System.out.println(Table_click);
                    File file = new File("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\UserFD\\"+Table_click+"_FaceData.xml");
                     if (file.exists()){
                     file.delete();
                    }     
        }catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        try {
            String sql = "delete from user where User_ID='" + Table_click + "'";
            pst = con.prepareStatement(sql);

            pst.execute();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        try {
            String sql = "delete from ss where User_ID='" + Table_click + "'";
            pst = con.prepareStatement(sql);

            pst.execute();
            JOptionPane.showMessageDialog(null, "Deleted");
            //getFaceRecog();
            //getimage();
            //trainimage();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        
        Update_table();

    }//GEN-LAST:event_btn_deleteActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        this.dispose();
        new AdminMainMenu().setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//  System.load("E:\\project source file\\AccessControl\\FYP Final Version\\Program\\javalib\\opencv_java300.dll");
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
            java.util.logging.Logger.getLogger(UserDatabase2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UserDatabase2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UserDatabase2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UserDatabase2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UserDatabase2().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Table_User;
    private javax.swing.JButton btn_delete;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
