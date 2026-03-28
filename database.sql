-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: dias_rx
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'System Administrator','admin@diasrx.com','$2b$10$1M2jA0wKKV/nXPHpw6uSgepzDDZ8Q//bES29Ek3yK5C87rGNK44UK','2026-02-25 09:12:55');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctors`
--

DROP TABLE IF EXISTS `doctors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctors` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `specialization` varchar(100) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctors`
--

LOCK TABLES `doctors` WRITE;
/*!40000 ALTER TABLE `doctors` DISABLE KEYS */;
INSERT INTO `doctors` VALUES (1,'Dr. Arjun Raman','arjun.raman@diasrx.com','Oral Surgery','$2b$10$lTVJsW./T6ewT/y8fiAiNeO3mE00KLAc6wuPLywJ7a03aRX40gJYq','2026-02-25 09:13:22'),(2,'Dr. Meena Krishnan','meena.krishnan@diasrx.com','Orthodontics','$2b$10$lTVJsW./T6ewT/y8fiAiNeO3mE00KLAc6wuPLywJ7a03aRX40gJYq','2026-02-25 09:13:22'),(3,'Dr. Vivek Sharma','vivek.sharma@diasrx.com','Endodontics','$2b$10$lTVJsW./T6ewT/y8fiAiNeO3mE00KLAc6wuPLywJ7a03aRX40gJYq','2026-02-25 09:13:22'),(4,'Dr. Priya Nair','priya.nair@diasrx.com','Periodontics','$2b$10$lTVJsW./T6ewT/y8fiAiNeO3mE00KLAc6wuPLywJ7a03aRX40gJYq','2026-02-25 09:13:22');
/*!40000 ALTER TABLE `doctors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drug_master`
--

DROP TABLE IF EXISTS `drug_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drug_master` (
  `id` int NOT NULL AUTO_INCREMENT,
  `drug_name` varchar(100) DEFAULT NULL,
  `strength` varchar(50) DEFAULT NULL,
  `stock` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drug_master`
--

LOCK TABLES `drug_master` WRITE;
/*!40000 ALTER TABLE `drug_master` DISABLE KEYS */;
INSERT INTO `drug_master` VALUES (1,'Amoxicillin','500mg',100,'2026-02-25 10:52:08'),(2,'Ibuprofen','400mg',200,'2026-02-25 10:52:08'),(3,'Paracetamol','500mg',300,'2026-02-25 10:52:08'),(4,'Metronidazole','400mg',120,'2026-02-25 10:52:08'),(5,'Clindamycin','300mg',90,'2026-02-25 10:52:08'),(6,'Azithromycin','500mg',75,'2026-02-25 10:52:08'),(7,'Diclofenac','50mg',150,'2026-02-25 10:52:08'),(8,'Doxycycline','100mg',80,'2026-02-25 10:52:08'),(9,'Cephalexin','500mg',70,'2026-02-25 10:52:08'),(10,'Chlorhexidine Mouthwash','0.2%',60,'2026-02-25 10:52:08');
/*!40000 ALTER TABLE `drug_master` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patients`
--

DROP TABLE IF EXISTS `patients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patients` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `gender` varchar(20) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `doctor_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `doctor_id` (`doctor_id`),
  CONSTRAINT `patients_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patients`
--

LOCK TABLES `patients` WRITE;
/*!40000 ALTER TABLE `patients` DISABLE KEYS */;
INSERT INTO `patients` VALUES (1,'Rahul Verma',24,'Male','9876543210',1,'2026-02-25 09:18:46'),(2,'Sneha Iyer',29,'Female','9876543211',2,'2026-02-25 09:18:46'),(3,'Karthik R',32,'Male','9876543212',3,'2026-02-25 09:18:46'),(4,'Divya Menon',27,'Female','9876543213',4,'2026-02-25 09:18:46'),(5,'Arun Prakash',45,'Male','9876543214',1,'2026-02-25 09:18:46'),(6,'Neha Gupta',31,'Female','9876543215',2,'2026-02-25 09:18:46'),(7,'Manoj Singh',36,'Male','9876543216',3,'2026-02-25 09:18:46'),(8,'Lakshmi Devi',50,'Female','9876543217',4,'2026-02-25 09:18:46'),(9,'Rohit Jain',40,'Male','9876543218',1,'2026-02-25 09:18:46'),(10,'Ananya Rao',22,'Female','9876543219',2,'2026-02-25 09:18:46'),(11,'Test Patient',28,'Female','9999999999',1,'2026-02-26 04:41:53');
/*!40000 ALTER TABLE `patients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pharmacists`
--

DROP TABLE IF EXISTS `pharmacists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pharmacists` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pharmacists`
--

LOCK TABLES `pharmacists` WRITE;
/*!40000 ALTER TABLE `pharmacists` DISABLE KEYS */;
INSERT INTO `pharmacists` VALUES (1,'Ravi Kumar','ravi.kumar@diasrx.com','$2b$10$I9HzfQnNj0JR.2AiqyODaOAsbIbLLT5prhHHt/AGL4UMtGskppXuW','2026-02-25 09:13:36'),(2,'Anita Joseph','anita.joseph@diasrx.com','$2b$10$I9HzfQnNj0JR.2AiqyODaOAsbIbLLT5prhHHt/AGL4UMtGskppXuW','2026-02-25 09:13:36');
/*!40000 ALTER TABLE `pharmacists` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prescription`
--

DROP TABLE IF EXISTS `prescription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int DEFAULT NULL,
  `doctor_id` int DEFAULT NULL,
  `diagnosis` text,
  `status` varchar(50) DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `patient_id` (`patient_id`),
  KEY `doctor_id` (`doctor_id`),
  CONSTRAINT `prescription_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`),
  CONSTRAINT `prescription_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prescription`
--

LOCK TABLES `prescription` WRITE;
/*!40000 ALTER TABLE `prescription` DISABLE KEYS */;
INSERT INTO `prescription` VALUES (1,1,1,'Acute Dental Abscess','pending','2026-02-25 15:37:03'),(2,5,2,'Chronic Gingivitis','pending','2026-02-25 15:37:03'),(3,9,4,'Pulpitis','pending','2026-02-25 15:37:03'),(4,2,3,'Periodontitis','pending','2026-02-25 15:37:03'),(5,6,1,'Tooth Infection','pending','2026-02-25 15:37:03'),(6,1,1,'Tooth Infection','pending','2026-02-25 15:52:17'),(7,1,1,'Tooth infection','pending','2026-02-26 04:09:11'),(8,1,1,'Tooth infection','pending','2026-02-26 04:15:59'),(9,1,1,'Dental Infection','pending','2026-02-26 04:42:54');
/*!40000 ALTER TABLE `prescription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prescription_drugs`
--

DROP TABLE IF EXISTS `prescription_drugs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription_drugs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `prescription_id` int DEFAULT NULL,
  `drug_id` int DEFAULT NULL,
  `dosage` varchar(50) DEFAULT NULL,
  `frequency` varchar(50) DEFAULT NULL,
  `duration` varchar(50) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `prescription_id` (`prescription_id`),
  KEY `drug_id` (`drug_id`),
  CONSTRAINT `prescription_drugs_ibfk_1` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`),
  CONSTRAINT `prescription_drugs_ibfk_2` FOREIGN KEY (`drug_id`) REFERENCES `drug_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prescription_drugs`
--

LOCK TABLES `prescription_drugs` WRITE;
/*!40000 ALTER TABLE `prescription_drugs` DISABLE KEYS */;
INSERT INTO `prescription_drugs` VALUES (1,1,1,'500mg','3 times/day','5 days','2026-02-25 15:37:24'),(2,1,2,'400mg','2 times/day','3 days','2026-02-25 15:37:24'),(3,2,3,'250mg','2 times/day','7 days','2026-02-25 15:37:24'),(4,3,4,'500mg','3 times/day','5 days','2026-02-25 15:37:24'),(5,3,5,'10ml','2 times/day','5 days','2026-02-25 15:37:24'),(6,4,6,'500mg','2 times/day','5 days','2026-02-25 15:37:24'),(7,5,7,'625mg','3 times/day','5 days','2026-02-25 15:37:24'),(8,6,1,'500mg','3 times/day','5 days','2026-02-25 15:53:22'),(9,8,1,'500mg','TDS','5 days','2026-02-26 04:18:10'),(10,9,1,'500mg','TDS','5 days','2026-02-26 04:43:39');
/*!40000 ALTER TABLE `prescription_drugs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'dias_rx'
--

--
-- Dumping routines for database 'dias_rx'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-27 14:38:46
