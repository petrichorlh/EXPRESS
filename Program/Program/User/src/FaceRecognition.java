
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import static javax.xml.bind.DatatypeConverter.parseInt;
import org.bouncycastle.util.encoders.Base64;
import org.bytedeco.javacpp.opencv_core;
//import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.MatVector;
//import org.bytedeco.javacpp.opencv_core.RectVector;
//import static org.bytedeco.javacpp.opencv_core.cvReleaseImage;
//import org.bytedeco.javacpp.opencv_core.CvMat;

import org.bytedeco.javacpp.opencv_face.*;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import org.bytedeco.javacv.OpenCVFrameConverter;
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author boonfu
 */
public class FaceRecognition extends javax.swing.JFrame {

    Connection con;
    private DaemonThread myThread = null;
    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
    //CascadeClassifier faceDetector = new CascadeClassifier(FaceRecognition.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
    CascadeClassifier faceDetector = new CascadeClassifier("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\Admin\\src\\haarcascade_frontalface_alt.xml");
    //FaceRecognizer faceRecognizer = createLBPHFaceRecognizer(1,8,8,8,90);
    FaceRecognizer faceRecognizer;
    VideoCapture grabber = null;
    Mat frame = new Mat();
    Mat frame2 = new Mat();
    MatOfByte mem = new MatOfByte();
    MatOfRect faces = new MatOfRect();
    Frame javacvframe = new Frame();
    MatVector images = new MatVector();
    opencv_core.Mat labels = new opencv_core.Mat();
    Mat resizeimage = new Mat();
    opencv_core.Mat videoMatGray = new opencv_core.Mat();

    int i = 1;
    public static int prediction;
    byte[] rbytes;
    JFrame f1 = new JFrame();
    //Registration reg = new Registration ();
    Result result = null;
    public String r;
    MatOfRect faceDetections = new MatOfRect();
    BufferedImage buff;
    int counter = 0;
    int id;
    static int interval;
    static Timer timer;
    static  boolean QR_verify=false;

    class DaemonThread implements Runnable {

        protected volatile boolean runnable = false;

        public void run() {
            synchronized (DaemonThread.class) {
                while (runnable) {
                    if (grabber.grab()) {
                        try {

                            grabber.retrieve(frame);

                            Graphics g = jPanel1.getGraphics();
                            faceDetector.detectMultiScale(frame, faceDetections);
                            Imgcodecs.imencode(".bmp", frame, mem);
                            Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                            buff = (BufferedImage) im;

                            Imgcodecs.imencode(".bmp", frame, mem);
                            Image qr = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                            BufferedImage qrcode = (BufferedImage) qr;

                            //timer();
                            if (QR_verify==false){
                                QRscanner();
                            }


                            if (counter == 1) {
                                getFaceData();
                                System.out.println("已获取人脸数据");
                                FaceRecog();
                            }


                           
//                            Timer timer = new Timer(5000, new ActionListener() {
//                                @Override
//                                public void actionPerformed(ActionEvent arg0) {
//                                    faceDetectorStop();
//                                    dispose();
//
//                                    Main main = new Main();
//                                    main.setVisible(true);
//                                }
//                            }
//                            );
//                            timer.setRepeats(false);
//                            timer.start();
                            if (g.drawImage(buff, 0, 0, getWidth(), getHeight() - 150, 0, 0, buff.getWidth(), buff.getHeight(), null)) {
                                if (runnable == false) {
                                    System.out.println("facerecognition 运行超时 .... Paused ..... ");
                                    JOptionPane.showMessageDialog(FaceRecognition.this, "UNAUTHORIZED");
                                    //this.wait();
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("facerecognition --- Error");
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public boolean validTime(String t) {
        long curTime = System.currentTimeMillis() / 1000;
        long timeFrame = Long.parseLong(t) + 40;       //70sec of time frame
        if (curTime < timeFrame) {
            return true;
        } else {
            return true;
        }
    }

    public void verSign(String asd,BufferedImage qrcode) {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());     //get the provider which is bouncy castle
        String[] t;
        String delims = "[,]";      //set the delimiter
        t = asd.split(delims);      //split the string
        System.out.println(t[0]+t[1]+t[2]+t[3]);
        boolean found = false;
        try {
            /*
            if any QR code scan, will show unathorized
             */
            if (t.length == 1) {
                JOptionPane.showMessageDialog(this, "UNAUTHORIZED");
                setVisible(true);
                //prediction = -1;
            } else {
                id = parseInt(t[0]);
                if (validTime(t[3])) {
                    byte[] encSKey = Base64.decode(t[1]);
                    byte[] cMSG = Base64.decode(t[2]);
                    byte[] aa = null;
                    connect();
                    Statement stat = con.createStatement();
                    String sql = "SELECT * FROM ss WHERE User_ID = " + id;
                    ResultSet rs = stat.executeQuery(sql);
                    while (rs.next()) {
                        found = true;
                        aa = rs.getBytes("Public_Key");
                        System.out.println("public key get " + id);
                    }
                    if (found) {
                        /*
                    if valid user
                         */
                        Signature signature = Signature.getInstance("ECDSA", "BC");
                        PublicKey kf = KeyFactory.getInstance("ECDSA", "BC").generatePublic(new X509EncodedKeySpec(aa));    //get data from database and convert to public key
                        signature.initVerify(kf);   //initial the verify function
                        signature.update(cMSG);     //verified the signature
                        // System.out.println("是否签名？"+signature.verify(encSKey));
                        if (signature.verify(encSKey)) {      //if the signature is valid
                            System.out.println("验证通过");
                            QR_verify = true;
                            String sql_update_sign = "UPDATE user SET  Sign_Status = 1 WHERE User_ID = " + id;
                            String sql_update_order = "UPDATE order_record SET  signstatus = 1 WHERE user_id = " + id;

                            stat.executeUpdate(sql_update_sign);
                            stat.executeUpdate(sql_update_order);

                            String sql_update_order_qrimage = "UPDATE order_record SET qrimage = ? WHERE user_id = ? ";
                            PreparedStatement pstmt = con.prepareStatement(sql_update_order_qrimage);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(qrcode, "jpg", baos);
                            InputStream is = new ByteArrayInputStream(baos.toByteArray());
                            pstmt.setBinaryStream(1, is );
                            pstmt.setInt(2, id);
                            pstmt.executeUpdate();
                            File outputfile = new File("QR_saved.png");
                            ImageIO.write(qrcode, "png", outputfile);
                            JOptionPane.showMessageDialog(this, "Signature Authenticated");
                            counter = 1;
                            System.out.println("QR验证通过，请输入人脸数据");
                        } else {
                            JOptionPane.showMessageDialog(this, "Signature UNAUTHORIZED");
                            faceDetectorStop();
                            dispose();
                            /*Main main = new Main();
                            main.setVisible(true);*/
                        }
                    } else {

                        JOptionPane.showMessageDialog(this, "Signature UNAUTHORIZED");
                        faceDetectorStop();
                        dispose();
                        /*Main main = new Main();
                        main.setVisible(true);*/
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Signature UNAUTHORIZED");
                    faceDetectorStop();
                    dispose();
                    /*Main main = new Main();
                    main.setVisible(true);*/
                }
            }

        } catch (Exception e) {
        }
    }

    public void FaceRecog() throws IOException, InterruptedException, SQLException {
        System.out.println("正在进行人脸识别");
        String facedata = ("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\UserFD\\" + id + "_FaceData.xml");
        connect();
        Statement stat = con.createStatement();


        faceDetector.detectMultiScale(frame, faces);
        for (Rect rect : faces.toArray()) {
            Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 0, 255),2);

            Rect rectcrop = null;
            rectcrop = new Rect(rect.x, rect.y, rect.width, rect.height);
            Mat cropimg = new Mat(frame, rectcrop);
            Size sz = new Size(100, 100);
            Imgproc.resize(cropimg, resizeimage, sz);
            Imgcodecs.imencode(".bmp", resizeimage, mem);

            Image img = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
            BufferedImage buffImg = (BufferedImage) img;


            Frame javaframe = java2dConverter.convert(buffImg);
            ////////////////////////////////////////////////////////////////////////////////
            opencv_core.Mat videoMat = new opencv_core.Mat();
            videoMat = converterToMat.convert(javaframe);
            System.out.println("videoMat: "+videoMat);

            // Convert the current frame to grayscale:
            cvtColor(videoMat, videoMatGray, COLOR_BGRA2GRAY);
            equalizeHist(videoMatGray, videoMatGray);

            faceRecognizer.load(facedata);
            prediction = faceRecognizer.predict(videoMatGray);

            System.out.println("Predicted label: " + prediction);
            System.out.println(facedata);

            if (prediction != -1) {
                if (id != prediction) {
                    JOptionPane.showMessageDialog(this, "UNAUTHORIZED");
                } else {
                    // 存储认证的图片到数据库中
                    File outputfile = new File("saved.png");
                    ImageIO.write(buffImg, "png", outputfile);
                    String sql = "UPDATE user SET User_Authen = ? WHERE User_ID = ? ";
                    PreparedStatement pstmt = con.prepareStatement(sql);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(buffImg, "jpg", baos);
                    InputStream is = new ByteArrayInputStream(baos.toByteArray());



                    pstmt.setBinaryStream(1, is );
                    pstmt.setInt(2, id);

                    String sql_order_faceimage = "UPDATE order_record SET authimage = ? , signstatus=2 WHERE user_id = ? ";
                    PreparedStatement pstmt2 = con.prepareStatement(sql_order_faceimage);
                    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                    ImageIO.write(buffImg, "jpg", baos2);
                    InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
                    pstmt2.setBinaryStream(1, is2 );
                    pstmt2.setInt(2, id);
                    pstmt2.executeUpdate();
                    System.out.println(pstmt);
                    System.out.println(pstmt2);
                    System.out.println("Authentication OK, PICTURE HAS BEEN SAVED ");
                   // JOptionPane.showMessageDialog(this, "AUTHORIZED");
                    faceDetectorStop();
                    dispose();
                    String sql_update_sign = "UPDATE user SET  Face_Status = 1 WHERE User_ID = " + id;
                    stat.executeUpdate(sql_update_sign);
                    String sql_update_order = "UPDATE order_record SET status  = 3 WHERE user_id = " + id;
                    stat.executeUpdate(sql_update_order);
                   new Success().setVisible(true);
                   //wait(20000);
//                    Main main = new Main();
//                    main.setVisible(true);
                }
                //genRanVal();
            }



        }

        //}
    }

    public void QRscanner() throws IOException {
        Imgcodecs.imencode(".bmp", frame, mem);
        Image qr = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
        BufferedImage qrcode = (BufferedImage) qr;
        if (result == null) {


            LuminanceSource source = new BufferedImageLuminanceSource(qrcode);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                /*
                                    the bitmap will be decoded and the zxing class will decode QR code
                 */
                result = new MultiFormatReader().decode(bitmap);
            } catch (NotFoundException e) {
            }
        }
        if (result != null) {
            r = result.getText();

            verSign(r,qrcode);
            //taCam.setText(result.getText());
        }

    }

    static int getprediction() {
        return prediction;
    }

    /*public void faceDetectorStart(){
                //webSource = new VideoCapture(0); // video capture from default cam
                //myThread = new DaemonThread(); //create object of threat class
                //Thread t = new Thread(myThread);
                //t.setDaemon(true);
                //myThread.runnable = true;
                //t.start();                 //start thrad
                grabber = new VideoCapture(0);
                myThread = new DaemonThread();
                Thread t = new Thread(myThread);
                t.setDaemon(true);
                myThread.runnable = true;
                t.start();
                //train();
                
    }*/
    public void CamStart() {
        //webSource = new VideoCapture(0); // video capture from default cam
        //myThread = new DaemonThread(); //create object of threat class
        //Thread t = new Thread(myThread);
        //t.setDaemon(true);
        //myThread.runnable = true;
        //t.start();                 //start thrad
        grabber = new VideoCapture(0);
        myThread = new DaemonThread();
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();
        //train();

    }

    /////////////////////////////////////////////////////////////////////////
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
        //getFaceData();
    }

    public void getFaceData() {

        File filePath = new File("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\UserFD\\" + id + "_FaceData.xml");
        System.out.println("id = " + id);
        //i++;
        // filePath.deleteOnExit();
        if (filePath.exists()) {
            System.out.println("FaceData already exists");
        } else {
            String sql = "SELECT FaceData FROM user WHERE User_ID = ?";
            try {
                PreparedStatement statement = con.prepareStatement(sql);
                statement.setInt(1, id);
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    Blob blob = result.getBlob("FaceData");
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

                //con.close();
                //faceDetectorStart();
            } catch (SQLException ex) {
                Logger.getLogger(FaceRecognition.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FaceRecognition.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FaceRecognition.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void timer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                faceDetectorStop();

                dispose();

//                Main main = new Main();
//                main.setVisible(true);
            }
        }, (long) (0.30 * 60 * 1000));
    }

    /*
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
            f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
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
            Thread.sleep(10000);
            f1.dispose();
            Cam c = new Cam();
            c.setVisible(true);
        } catch (Exception e) {
        }
    }
     */
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
                    //getFaceData();
                } else if (rs.getString("FaceRecognizer").equals("Eigen Face Recognizer")) {
                    faceRecognizer = createEigenFaceRecognizer(1, 80);
                    System.out.println("Eigen Face Recognizer");
                    //getFaceData();
                } else if (rs.getString("FaceRecognizer").equals("Fisher Face Recognizer")) {
                    faceRecognizer = createFisherFaceRecognizer(0, 80);
                    System.out.println("Fisher Face Recognizer");
                    //getFaceData();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(FaceRecognition.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FaceRecognition() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        connect();
        getFaceRecog();
        //getFaceData2();
        //getFaceData3();
        initComponents();
        CamStart();
        //timer();

        //faceDetectorStart();

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
        setTitle("EXPRESS2.0 Authentication System");

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 513, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 334, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("EXPRESS2.0 Authentication System");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(122, 122, 122)
                        .addComponent(jLabel1)))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        /* Set the Nimbus look and feel */
        Main.loadOpenCV_Lib();
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
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
            java.util.logging.Logger.getLogger(FaceRecognizer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FaceRecognizer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FaceRecognizer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FaceRecognizer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FaceRecognition().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
