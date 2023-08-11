-- phpMyAdmin SQL Dump
-- version 4.1.6
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Feb 03, 2016 at 03:47 PM
-- Server version: 5.6.16
-- PHP Version: 5.5.9

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `door`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE IF NOT EXISTS `admin` (
  `Admin_ID` int(10) NOT NULL,
  `Admin_Name` varchar(50) NOT NULL,
  `Admin_Password` blob NOT NULL,
  `FaceData_Admin` LONGBLOB NULL,
  `Admin_Image_1` blob NULL,
  `Admin_Image_2` blob NULL,
  `Admin_Image_3` blob NULL,
  `Admin_Image_4` blob NULL,
  `Admin_Image_5` blob NULL,
  `Admin_Image_6` blob NULL,
  `Admin_Image_7` blob NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`Admin_ID`, `Admin_Name`, `Admin_Password`) VALUES
(1010101010, 'Admin01', 0x3fefbfbdc2a0efbfbd1cefbfbdefbfbd01efbfbd69efbfbdefbfbd57efbfbd2b);



-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `User_ID` int(10) NOT NULL,
  `User_Name` varchar(50) NOT NULL,
  `User_Password` varchar(1000) DEFAULT NULL,
  `User_Image_1` BLOB NULL,
  `User_Image_2` BLOB NULL,
  `User_Image_3` BLOB NULL,
  `User_Image_4` BLOB NULL,
  `User_Image_5` BLOB NULL,
  `User_Image_6` BLOB NULL,
  `User_Image_7` BLOB NULL,
  `FaceData` LONGBLOB NULL,
  PRIMARY KEY (`User_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `ss`
--

CREATE TABLE IF NOT EXISTS `ss` (
  `Public_Key` blob NOT NULL,
  `User_ID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `setting`
--

CREATE TABLE IF NOT EXISTS `setting` (
  `Admin_ID` int(10) NOT NULL,
  `Email` varchar(50) NULL,
  `EmailPW` varchar(50) NULL,
  `FaceRecognizer` varchar(30) NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `setting`
--

INSERT INTO `setting` (`Admin_ID`, `Email`, `EmailPW`, `FaceRecognizer`) VALUES
(1010101010 , 'fypdooraccess@gmail.com', 'Door12345' , 'LBPH Face Recognizer');



/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
