DROP DATABASE clinicHelperDb2;
CREATE DATABASE clinicHelperDb2;
use clinicHelperDb2;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    hashed_password  VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    role ENUM('DOCTOR', 'PATIENT', 'RECEPTIONIST', 'ADMIN') NOT NULL DEFAULT 'patient',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users 
MODIFY COLUMN role ENUM('DOCTOR', 'PATIENT', 'RECEPTIONIST', 'ADMIN') 
NOT NULL DEFAULT 'PATIENT';
UPDATE users SET role = UPPER(role);


CREATE TABLE doctor_profile (
  user_id INT PRIMARY KEY,                    
  specialization VARCHAR(255),
  bio TEXT,
  image MEDIUMBLOB,                            
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);



CREATE TABLE patient_profile (
  user_id INT PRIMARY KEY,
  dob DATE,
  insurance_number VARCHAR(100),
  notes TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE clinic (
  clinic_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  phone VARCHAR(11),
  address VARCHAR(255)
);



CREATE TABLE doctor_clinic (
  doctor_id INT NOT NULL,
  clinic_id INT NOT NULL,
  PRIMARY KEY (doctor_id, clinic_id),
  FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (clinic_id) REFERENCES clinic(clinic_id) ON DELETE CASCADE
);


CREATE TABLE doctor_working_time (
  id INT AUTO_INCREMENT PRIMARY KEY,
  doctor_id INT NOT NULL,
  clinic_id INT NOT NULL,
  day_of_week TINYINT NOT NULL,        -- 0: sunday .. 6:Saturday
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (clinic_id) REFERENCES clinic(clinic_id) ON DELETE CASCADE,
  UNIQUE KEY ux_doctor_clinic_day_time (doctor_id, clinic_id, day_of_week, start_time)
);

CREATE TABLE receptionist_profile (
  user_id INT PRIMARY KEY,
  hire_date DATE,
  notes TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE receptionist_clinic (
  receptionist_id INT NOT NULL,
  clinic_id INT NOT NULL,
  assigned_by_doctor INT NOT NULL,
  assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (receptionist_id, clinic_id),
  FOREIGN KEY (receptionist_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (clinic_id) REFERENCES clinic(clinic_id) ON DELETE CASCADE,
  FOREIGN KEY (assigned_by_doctor) REFERENCES users(id) ON DELETE CASCADE
);



CREATE TABLE appointment (
  appointment_id INT AUTO_INCREMENT PRIMARY KEY,
  appointment_datetime DATETIME NOT NULL,
  status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
  patient_id INT NOT NULL,          
  doctor_id INT NOT NULL,  
  clinic_id INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (clinic_id) REFERENCES clinic(clinic_id) ON DELETE SET NULL,
  UNIQUE KEY uq_doctor_datetime (doctor_id, appointment_datetime)
);


CREATE TABLE medical_note (
  note_id INT AUTO_INCREMENT PRIMARY KEY,
  appointment_id INT, 
  diagnosis_text TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id) ON DELETE CASCADE
);



/*  to add doctor with his clinic:
      formClinic -> name, phone, address,
      fromUser -> doctorName, email, password, phone, role, 
      fromDoctorProfile -> takeIdFrom User added it to doctorProfile, specialization, bio, image,
      from doctor_working_time -> i will need to take days doctor will work in , start time, end time
*/



-- for register doctor with his clinic
DELIMITER $$

DROP PROCEDURE IF EXISTS RegisterClinicAndDoctor$$

CREATE PROCEDURE RegisterClinicAndDoctor(
  IN in_clinic_name VARCHAR(255),
  IN in_clinic_phone VARCHAR(20),
  IN in_clinic_address VARCHAR(255),
  
  IN in_doctor_name VARCHAR(100),
  IN in_doctor_email VARCHAR(255),
  IN in_hashed_password VARCHAR(255),
  IN in_doctor_phone VARCHAR(30),
  
  IN in_specialization VARCHAR(255),
  IN in_bio TEXT,
  IN in_image MEDIUMBLOB,
  
  IN in_start_day TINYINT,   -- e.g., 1 (sun)
  IN in_end_day TINYINT,     -- e.g., 5 (thursday)
  IN in_start_time TIME,     -- e.g., '06:00:00' after el fager:)
  IN in_end_time TIME        -- e.g., '15:00:00' el        3sr:)
)
BEGIN
  DECLARE v_clinic_id INT;
  DECLARE v_user_id INT;
  DECLARE v_current_day INT;

  -- Error Handler: Rollback if anything fails
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL; -- Re-throw the error so Java knows it failed
  END;

  START TRANSACTION;

  -- 1. Insert Clinic
  INSERT INTO clinic(name, phone, address)
  VALUES (in_clinic_name, in_clinic_phone, in_clinic_address);
  
  SET v_clinic_id = LAST_INSERT_ID();

  -- 2. Insert User (Doctor)
  INSERT INTO users (name, email, hashed_password, phone, role)
  VALUES (in_doctor_name, in_doctor_email, in_hashed_password, in_doctor_phone, 'DOCTOR');
  
  SET v_user_id = LAST_INSERT_ID();

  -- 3. Insert Doctor Profile (Linked to User)
  INSERT INTO doctor_profile (user_id, specialization, bio, image)
  VALUES (v_user_id, in_specialization, in_bio, in_image);

  -- 4. Link Doctor to Clinic
  INSERT INTO doctor_clinic (doctor_id, clinic_id)
  VALUES (v_user_id, v_clinic_id);

  -- 5. Insert Working Times (Loop from Start Day to End Day)
  SET v_current_day = in_start_day;

  WHILE v_current_day <= in_end_day DO
    INSERT INTO doctor_working_time (doctor_id, clinic_id, day_of_week, start_time, end_time)
    VALUES (v_user_id, v_clinic_id, v_current_day, in_start_time, in_end_time);
    
    SET v_current_day = v_current_day + 1;
  END WHILE;

  COMMIT;
END$$

DELIMITER ;



-- delete Doctor and related data : 
DELIMITER $$
DROP PROCEDURE IF EXISTS delete_doctor_and_related$$
CREATE PROCEDURE delete_doctor_and_related(IN in_user_id INT)
BEGIN
    DECLARE user_role VARCHAR(20);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT role INTO user_role FROM users WHERE id = in_user_id;
    IF user_role IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User not found';
    ELSEIF user_role != 'DOCTOR' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User is not a doctor';
    END IF;
    DELETE FROM appointment WHERE doctor_id = in_user_id;
    DELETE FROM doctor_working_time WHERE doctor_id = in_user_id;
    DELETE FROM doctor_clinic WHERE doctor_id = in_user_id;
    DELETE FROM doctor_profile WHERE user_id = in_user_id;
    DELETE FROM users WHERE id = in_user_id;
    COMMIT;
END$$
DELIMITER ;

-- delete Patient and related data : 
DELIMITER $$
DROP PROCEDURE IF EXISTS delete_patient_and_related$$
CREATE PROCEDURE delete_patient_and_related(IN in_user_id INT)
BEGIN
    DECLARE user_role VARCHAR(20);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT role INTO user_role FROM users WHERE id = in_user_id;
    IF user_role IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User not found';
    ELSEIF user_role != 'PATIENT' AND user_role != 'patient' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User is not a patient';
    END IF;
    DELETE mn FROM medical_note mn
    INNER JOIN appointment a ON mn.appointment_id = a.appointment_id
    WHERE a.patient_id = in_user_id;
    DELETE FROM appointment WHERE patient_id = in_user_id;
    DELETE FROM patient_profile WHERE user_id = in_user_id;
    DELETE FROM users WHERE id = in_user_id;
    COMMIT;
END$$
DELIMITER ;

-- delete receptionist and related data : 
DELIMITER $$
DROP PROCEDURE IF EXISTS delete_receptionist_and_related$$
CREATE PROCEDURE delete_receptionist_and_related(IN in_user_id INT)
BEGIN
    DECLARE user_role VARCHAR(20);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT role INTO user_role FROM users WHERE id = in_user_id;
    IF user_role IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User not found';
    ELSEIF user_role != 'RECEPTIONIST' AND user_role != 'receptionist' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User is not a receptionist';
    END IF;
    DELETE FROM receptionist_profile WHERE user_id = in_user_id;
    DELETE FROM receptionist_clinic WHERE receptionist_id = in_user_id;
    DELETE FROM users WHERE id = in_user_id;
    COMMIT;
END$$
DELIMITER ;


DELIMITER $$

DROP PROCEDURE IF EXISTS check_doctor_clinic_ownership$$
CREATE PROCEDURE check_doctor_clinic_ownership(
  IN in_doctor_id INT,
  IN in_clinic_id INT,
  OUT is_owner BOOLEAN
)
BEGIN
  DECLARE count_ownership INT;
  SELECT COUNT(*) INTO count_ownership 
  FROM doctor_clinic 
  WHERE doctor_id = in_doctor_id 
    AND clinic_id = in_clinic_id;
  IF count_ownership > 0 THEN
    SET is_owner = TRUE;
  ELSE
    SET is_owner = FALSE;
  END IF;
END$$
DELIMITER ;
