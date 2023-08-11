import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.bouncycastle.util.encoders.Base64;
/////
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.imageio.ImageIO;
import org.opencv.core.Core; 
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
/////
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author WEI
 */
public class Main extends javax.swing.JFrame{ //implements WebcamMotionListener, Runnable
    
    Registration reg = new Registration ();
    
     byte[] rbytes;
     String uID = null;
     JFrame f1 = new JFrame();
     Connection con;
     Thread th;
     /////
    private DaemonThread myThread = null;
    int count = 0;
    VideoCapture webSource = null;
    Mat frame = new Mat();
    MatOfByte mem = new MatOfByte();
    //CascadeClassifier faceDetector = new CascadeClassifier(Main.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
    CascadeClassifier faceDetector = new CascadeClassifier("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\Admin\\src\\haarcascade_frontalface_alt.xml");

    MatOfRect faceDetections = new MatOfRect();
    /////  
    class DaemonThread implements Runnable {

            protected volatile boolean runnable = false;

            @Override
            public void run() {
                // Main.class 原来是this
                synchronized (Main.class) {
                    while (runnable) {
                        if (webSource.grab()) {
                            try {
                                webSource.retrieve(frame);
                                Graphics g = jPanel1.getGraphics();
                                faceDetector.detectMultiScale(frame, faceDetections);
                                for (Rect rect : faceDetections.toArray()) {
                                   // System.out.println("ttt");
                                    Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                                            new Scalar(0, 255,0),1);
                                  //撤销当前窗口并释放所有使用的资源
                                    f1.dispose();
                                    genRanVal();
                                }
                                Imgcodecs.imencode(".bmp", frame, mem);
                                Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                                BufferedImage buff = (BufferedImage) im;
                                if (g.drawImage(buff, 0, 0, getWidth(), getHeight()-150 , 0, 0, buff.getWidth(), buff.getHeight(), null)) {
                                    if (runnable == false) {
                                        System.out.println("run able= false Paused ..... ");
//                                        this.wait();
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
    /////

    public Main() {
        
        initComponents();
        faceDetectorStart();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void faceDetectorStart(){
                webSource = new VideoCapture(0); // video capture from default cam
                myThread = new DaemonThread(); //create object of threat class
                Thread t = new Thread(myThread);
                //t.setDaemon(true);
                t.setDaemon(true);

                myThread.runnable = true;
                t.start();                 //start thrad

    }

    public void faceDetectorStop(){
        myThread.runnable = false;            // stop thread
        webSource.release();  // stop caturing fron cam
    }
     
    public byte[] generateRandomValue() {
        try {
            SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
            byte bytes[] = new byte[20];
            rand.nextBytes(bytes);
            rbytes = bytes;
            //generateQR(bytes);
        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        return rbytes;
    }
 
    public void generateQR(byte by[]){
        //JOptionPane.showMessageDialog(this, new String(by)+"\n\n This is the rand value");
        long s = System.currentTimeMillis()/1000;
        String strLong = Long.toString(s);
        String indicator = "222";
        String myCodeText = indicator+","+strLong+","+Base64.toBase64String(by);
        //JOptionPane.showMessageDialog(this, myCodeText+"\n\n This is the rand value");
        int size = 400;

        try {
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText,BarcodeFormat.QR_CODE, size, size, hintMap);
            int CrunchifyWidth = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth,
                    BufferedImage.TYPE_INT_RGB);
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
            f1 = new JFrame();
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
    
    public void genRanVal() {

        generateQR(generateRandomValue());
        f1.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                f1.dispose();
                setVisible(true);
            }
        });
        
        dispose(); //close main
        faceDetectorStop(); //stop faceDetector

        try {
            Thread.sleep(5000);
            System.out.println("we will stop 5s wait");
            f1.dispose();
            FaceRecognition FR = new FaceRecognition();
            FR.setVisible(true);
            //Cam c = new Cam();
            //c.setVisible(true);
        } catch (Exception e) {
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Welcome to Door Access System");
        setBackground(new java.awt.Color(51, 51, 255));

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));
        jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(102, 102, 102)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 485, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 277, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("ACCESS CONTROL SYSTEM");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(84, 84, 84)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(79, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void loadOpenCV_Lib() throws Exception {
    // get the model
    String model = System.getProperty("sun.arch.data.model");
    // the path the .dll lib location
    String libraryPath = "D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\javalib/";
    // check for if system is 64 or 32
    if(model.equals("64")) {
        libraryPath = "D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\javalib/";
    }
    // set the path
    System.setProperty("java.library.path", libraryPath);
    Field sysPath = ClassLoader.class.getDeclaredField("sys_paths");
    sysPath.setAccessible(true);
    sysPath.set(null, null);
    // load the lib
//    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    System.load("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\javalib\\opencv_java300.dll");
}
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
         loadOpenCV_Lib();
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
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
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
