
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author boonfu
 */
public class AdminFaceRecognition extends javax.swing.JFrame {

    Connection con;
    private DaemonThread myThread = null;
    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
    CascadeClassifier faceDetector = new CascadeClassifier("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\Admin\\src\\haarcascade_frontalface_alt.xml");

    opencv_face.FaceRecognizer faceRecognizer = createLBPHFaceRecognizer(1, 8, 8, 8, 90);
    //opencv_face.FaceRecognizer faceRecognizer;
    VideoCapture grabber = null;
    Mat frame = new Mat();
    MatOfByte mem = new MatOfByte();
    MatOfRect faces = new MatOfRect();
    Frame javacvframe = new Frame();
    opencv_core.MatVector images = new opencv_core.MatVector();
    opencv_core.Mat labels = new opencv_core.Mat();
    Mat resizeimage = new Mat();
    opencv_core.Mat videoMatGray = new opencv_core.Mat();
    String facedata = ("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\FaceData_Admin.xml");
    int i = 1;
    public static int prediction;
    JFrame f1 = new JFrame();

    /**
     * Creates new form AdminFaceRecog
     */
    class DaemonThread implements Runnable {

        protected volatile boolean runnable = false;

        public void run() {
            synchronized (this) {
                while (runnable) {
                    if (grabber.grab()) {
                        try {
                            grabber.retrieve(frame);

                            grabber.retrieve(frame);
                            Graphics g = jPanel1.getGraphics();
                            faceDetector.detectMultiScale(frame, faces);
                            for (Rect rect : faces.toArray()) {
                                Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                                        new Scalar(0, 255, 0));

                                Rect rectcrop = null;
                                rectcrop = new Rect(rect.x, rect.y, rect.width, rect.height);
                                Mat cropimg = new Mat(frame, rectcrop);
                                Size sz = new Size(100, 100);
                                Imgproc.resize(cropimg, resizeimage, sz);
                                Imgcodecs.imencode(".bmp", resizeimage, mem);

                                Image img = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                                BufferedImage buffImg = (BufferedImage) img;

                                Frame javaframe = java2dConverter.convert(buffImg);
                                opencv_core.Mat videoMat = new opencv_core.Mat();
                                videoMat = converterToMat.convert(javaframe);
// Convert the current frame to grayscale:
                                cvtColor(videoMat, videoMatGray, COLOR_BGRA2GRAY);
                                equalizeHist(videoMatGray, videoMatGray);

                                faceRecognizer.load(facedata);
                                prediction = faceRecognizer.predict(videoMatGray);

                                System.out.println("Predicted label: " + prediction);
                                System.out.println(facedata);
                                if (prediction != -1) {
                                    faceDetectorStop();
                                    dispose();
                                    new AdminMainMenu().setVisible(true);
                                }
                            }

                            Imgcodecs.imencode(".bmp", frame, mem);
                            Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                            BufferedImage buff = (BufferedImage) im;

                            if (g.drawImage(buff, 0, 0, getWidth(), getHeight() - 150, 0, 0, buff.getWidth(), buff.getHeight(), null)) {
                                if (runnable == false) {
                                    System.out.println("Paused ..... ");
                                    this.wait();
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Error");
                        }
                    }
                }
            }
        }
    }

    public void faceDetectorStart() {
        grabber = new VideoCapture(0);
        myThread = new DaemonThread();
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();
    }

    public void faceDetectorStop() {
        myThread.runnable = false;          // stop thread
        grabber.release();  // stop caturing fron cam
    }

    public void connect() {
        try {

            Class.forName("com.mysql.jdbc.Driver");     //general connection error msg; error msg get from this class
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/delivery", "root", "123456"); //connection to Sql
        } catch (ClassNotFoundException | SQLException | HeadlessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    public void getFaceRecog() {
        //String sql = "SELECT FaceRecognizer FROM setting WHERE Admin_ID = ?";
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT FaceRecognizer FROM setting");
            //String LBPH = "LBPH Face Recognizer";
            while (rs.next()) {
                //String FaceRecognizer = rs.getString("FaceRecognizer");
                //String FaceRecog = rs.getString(4);
                if (rs.getString("FaceRecognizer").equals("LBPH Face Recognizer")) {
                    faceRecognizer = createLBPHFaceRecognizer(1, 8, 8, 8, 90);
                    System.out.println("LBPH Face Recognizer");
                    getFaceData();
                } else if (rs.getString("FaceRecognizer").equals("Eigen Face Recognizer")) {
                    faceRecognizer = createEigenFaceRecognizer(1, 80);
                    System.out.println("Eigen Face Recognizer");
                    getFaceData();
                } else if (rs.getString("FaceRecognizer").equals("Fisher Face Recognizer")) {
                    faceRecognizer = createFisherFaceRecognizer(0, 80);
                    System.out.println("Fisher Face Recognizer");
                    getFaceData();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AdminFaceRecognition.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getFaceData() {

        File filePath = new File("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\FaceData_Admin.xml");
        filePath.deleteOnExit();
        //i++;
        // if(filePath.exists()){
        //     System.out.println("FaceData_Admin already exists");
        // }
        // else{
        String sql = "SELECT FaceData_Admin FROM admin WHERE Admin_ID = ?";
        try {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, 1010101010);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                Blob blob = result.getBlob("FaceData_Admin");
                InputStream inputStream = blob.getBinaryStream();
                OutputStream outputStream = new FileOutputStream(filePath);
                byte[] buffer = new byte[1];
                while (inputStream.read(buffer) > 0) {
                    outputStream.write(buffer);
                }

                inputStream.close();
                outputStream.close();
                System.out.println("File saved");
            }
            filePath.deleteOnExit();
            //con.close();
            //faceDetectorStart();
        } catch (SQLException ex) {
            Logger.getLogger(AdminFaceRecognition.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AdminFaceRecognition.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AdminFaceRecognition.class.getName()).log(Level.SEVERE, null, ex);
        }
        // }
    }

    public AdminFaceRecognition() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        connect();
        //getFaceData();
        getFaceRecog();
        initComponents();
        faceDetectorStart();
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
        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Face Recognition");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("ACCESS CONTROL SYSTEM");

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 513, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 342, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addComponent(jLabel1)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        /* Set the Nimbus look and feel */
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        AdminMainMenu.loadOpenCV_Lib();
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
            java.util.logging.Logger.getLogger(AdminFaceRecognition.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminFaceRecognition.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminFaceRecognition.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminFaceRecognition.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminFaceRecognition().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
