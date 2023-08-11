
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;
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
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
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
import org.bytedeco.javacv.Java2DFrameConverter;
import org.opencv.core.Size;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author boonfu
 */
public class AdminFaceRecog extends javax.swing.JFrame {
    Connection con;
    private DaemonThread myThread = null;
    int count = 0;
    VideoCapture webSource = null;
    Mat frame = new Mat();
    Mat image = new Mat();
    MatOfByte mem = new MatOfByte();
   // CascadeClassifier faceDetector = new CascadeClassifier(AdminFaceRecog.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
    //CascadeClassifier faceDetector2 = new CascadeClassifier(AdminFaceRecog.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
    CascadeClassifier faceDetector = new CascadeClassifier("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\Admin\\src\\haarcascade_frontalface_alt.xml");
    CascadeClassifier faceDetector2 = new CascadeClassifier("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\Admin\\src\\haarcascade_frontalface_alt.xml");
    MatOfRect faceDetections = new MatOfRect();
    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
    Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
    JFrame f1 = new JFrame();
    int i = 0;
    opencv_face.FaceRecognizer faceRecognizer;
    MatVector images = new MatVector();
    opencv_core.Mat labels = new opencv_core.Mat();
    Mat MatImage = new Mat();
    Mat mat1 = new Mat();
    File outputfile;
    Mat resizeimage = new Mat();


  class DaemonThread implements Runnable
    {
    protected volatile boolean runnable = false;

            @Override
            public void run() {
                synchronized (this) {
                    while (runnable) {
                        if (webSource.grab()) {
                            try {
                                webSource.retrieve(frame);
                                Graphics g = jPanel1.getGraphics();
                                faceDetector2.detectMultiScale(frame, faceDetections);
                                for (Rect rect : faceDetections.toArray()) {
                                   // System.out.println("ttt");
                                    Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                                            new Scalar(0, 255,0));

                                }
                                Imgcodecs.imencode(".bmp", frame, mem);
                                Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                                BufferedImage buff = (BufferedImage) im;
                                if (g.drawImage(buff, 0, 0, getWidth(), getHeight()-150 , 0, 0, buff.getWidth(), buff.getHeight(), null)) {
                                    if (runnable == false) {
                                        System.out.println("Paused ..... ");
//                                        this.wait();

                                    }
                                }
                            } catch (Exception ex) {
                                System.out.println("Wesource Error");
                            }
                        }
                    }
                }
            }
        }
    public void faceDetectorStart(){
                webSource = new VideoCapture(0); // video capture from default cam
                myThread = new DaemonThread(); //create object of threat class
                Thread t = new Thread(myThread);
                t.setDaemon(true);
                myThread.runnable = true;
                t.start();                 //start thrad
    }
    
        public void faceDetectorStop(){
        myThread.runnable = false;          // stop thread
        webSource.release();  // stop caturing fron cam
        getimage();
        }
        
    public void connect() {
        try {

            Class.forName("com.mysql.jdbc.Driver");     //general connection error msg; error msg get from this class
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/delivery", "root", "123456"); //connection to Sql
        } catch (ClassNotFoundException | SQLException | HeadlessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

    }


        
    public void snapshot() throws SQLException, IOException{
        
        
        //Imgcodecs.imencode(".bmp", frame, mem);
	//Image image = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
        //BufferedImage buffered = (BufferedImage) image;
       //IplImage iplImage = iplConverter.convert(java2dConverter.convert(buffered));
          
       if(i<7) {
           Mat frame2 = new Mat();
           webSource.retrieve(frame2);
       // Mat frame2 = frame;
            //Imgcodecs.imencode(".bmp", frame, mem);
            //System.out.println("Success");
            //Imgcodecs.imwrite("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\img.jpg",frame);
        faceDetector.detectMultiScale(frame2, faceDetections);
                for (Rect rect : faceDetections.toArray()){
                        i++;
                        
                        jLabel1.setText(String.valueOf(i));
                        Rect rectcrop = null;
                        Imgproc.rectangle(frame2, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                            new Scalar(0, 255,0));
                               
                        rectcrop = new Rect(rect.x, rect.y, rect.width, rect.height);
                        MatImage = new Mat(frame2,rectcrop);
                        
                        ;
                        Size sz = new Size(100,100);
                        Imgproc.resize(MatImage, resizeimage, sz );
                        
                        //Imgcodecs.imwrite("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\img.jpg",MatImage);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        
                        String sql = "UPDATE admin SET Admin_Image_"+i+"=? WHERE Admin_ID = ? ";
                        PreparedStatement pstmt = con.prepareStatement(sql);

                        
                        Imgcodecs.imencode(".bmp", resizeimage, mem);
                        Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                        BufferedImage buffer = (BufferedImage) im;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(buffer, "jpg", baos);
                        InputStream is = new ByteArrayInputStream(baos.toByteArray());
                        
                        pstmt.setBinaryStream(1, is );
                        pstmt.setInt(2, 1010101010);
                        pstmt.executeUpdate();
                        System.out.println(pstmt);
                        System.out.println("OK "+i);
                        pstmt.close();
                       // SavedImage();
                   }
                }else{
                    i = 0;
                    int dialogButton = JOptionPane.YES_NO_OPTION;
                    int dialogResult = JOptionPane.showConfirmDialog(null, "Retake photo", "Warning", dialogButton);
                    if(dialogResult == 1){
                        faceDetectorStop();
                        trainimage();
                        dispose();
                        AdminMainMenu AMM = new AdminMainMenu();
                        AMM.setVisible(true);
                    }
                    }
    
    }
    

    

  //  public void SavedImage() throws IOException{

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                       /* Imgcodecs.imencode(".bmp", resizeimage, mem);
                        Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                        BufferedImage buffer = (BufferedImage) im;
                        
                        
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageIO.write(buffer, "jpg", os);
                        
                        InputStream input = new ByteArrayInputStream(os.toByteArray());
                        System.out.println("input stream create");
                        System.out.println(uID+"-"+regName+"_"+i);
                        
                        outputfile = new File("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\New folder\\"+uID+"-"+regName+"_"+i+".jpg");
                        ImageIO.write(buffer, "jpg", outputfile);
                        System.out.println("image saved"); 
                        outputfile.deleteOnExit();*/
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //File temp = File.createTempFile(String.valueOf(uID)+"-"+String.valueOf(i)+"-", ".jpg"); 
                //OutputStream outStream = new FileOutputStream(temp);

                //temp.deleteOnExit();
            /*copyFile(input, new FileOutputStream(temp));
            temp.deleteOnExit();
    	    System.out.println("Temp file : " + temp.getAbsolutePath());
        */

     //}
    
    
    public void getimage(){
                    try {
                
                String sql = "SELECT Admin_ID, Admin_Name FROM admin ";
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int Admin_ID = rs.getInt("Admin_ID");
                    String Admin_Name = rs.getString("Admin_Name");
                    System.out.println(Admin_ID+", "+Admin_Name);
                    
                    
                    for (int z = 1; z<=7 ; z++){
                    File filePath = new File("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\image\\"+Admin_ID+"-"+Admin_Name+"_"+z+".jpg");
                    filePath.deleteOnExit();
                    String sql2 = "SELECT Admin_Image_"+z+" From admin WHERE Admin_ID ="+Admin_ID;
                    PreparedStatement stmt2 = con.prepareStatement(sql2);
                    ResultSet rs2 = stmt2.executeQuery();
                    
                    try {
                        
                        if(rs2.next()){
                            Blob blob = rs2.getBlob("Admin_Image_"+z);
                            InputStream inputStream = blob.getBinaryStream();
                            OutputStream outputStream = new FileOutputStream(filePath);
                            byte[] buffer = new byte[1024];
                            while (inputStream.read(buffer) > 0) {
                                outputStream.write(buffer);
                            }   
                            
                            
                            inputStream.close();
                            outputStream.close();
                            
                            
                            
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
            //faceRecognizer = createEigenFaceRecognizer(1,80);
           //faceRecognizer = createLBPHFaceRecognizer(1,8,8,8,100);
            MatVector images = new MatVector();
            opencv_core.Mat labels = new opencv_core.Mat();


            
            String trainingDir = ("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\image\\");
//            String mPath = ("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\image\\7.jpg");
//            opencv_core.Mat imread = imread(mPath, CV_LOAD_IMAGE_GRAYSCALE);

            File root = new File(trainingDir);
            FilenameFilter imgFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    name = name.toLowerCase();
                    return name.endsWith(".jpg");
                }
            };
            
            File[] imageFiles = root.listFiles(imgFilter);
            
            images = new MatVector(imageFiles.length);
            
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
            faceRecognizer.save("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\FaceData_Admin.xml");
            System.out.println("train success");
           
        
            String sql = "UPDATE admin SET FaceData_Admin=? WHERE Admin_ID = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            File file = new File("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\save\\FaceData_Admin.xml");
            file.deleteOnExit();
            FileInputStream is = new FileInputStream(file);
            pstmt.setBinaryStream(1, is );
            pstmt.setInt(2, 1010101010);
            pstmt.executeUpdate();
            System.out.println(pstmt);
            System.out.println("Face Data Success");
            pstmt.close();
            
            
        
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(FaceRecog.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(AdminFaceRecog.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    /**
     * Creates new form test
     */
    public AdminFaceRecog() {
       
        initComponents();
        connect();
        getFaceRecog();
        faceDetectorStart();
        System.out.println(FaceRecog.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
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
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("EXPRESS2.0 Face Image Acquisition\n");

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 462, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 454, Short.MAX_VALUE)
        );

        jButton1.setText("Take Image");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Number of Image Taken");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(122, 122, 122)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(55, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            snapshot();
        } catch (SQLException | IOException ex) {
            Logger.getLogger(FaceRecog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.load("D:\\ExpressV2\\AccessControl\\FYP Final Version\\Program\\javalib\\opencv_java300.dll");
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
            java.util.logging.Logger.getLogger(FaceRecog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FaceRecog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FaceRecog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FaceRecog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminFaceRecog().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
