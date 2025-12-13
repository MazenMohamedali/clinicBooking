CREATE DATABASE clinicHelperDb;
use clinicHelperDb;
-- USERS (single table for all roles)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    hashed_password  VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    role ENUM('doctor','patient','receptionist','admin') NOT NULL DEFAULT 'patient',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

insert into users (name, email, hashed_password, phone, role) VALUES('mazen mohamed', 'mazenqwe347@gmail.com', '$2a$10$8LZdUB1IuOshBrpKu9LhgOg23d8CnDgEqDUvEBcB8Ksu6UjmtOPAC', '01008429400', 'admin');


-- DOCTOR PROFILE (one-to-one with users where role='doctor')
CREATE TABLE doctor_profile (
  user_id INT PRIMARY KEY,                      -- same id as users.id
  specialization VARCHAR(255),
  bio TEXT,
  image MEDIUMBLOB,                             -- option A: store binary image in DB
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


-- PATIENT PROFILE (optional extra patient info)
CREATE TABLE patient_profile (
  user_id INT PRIMARY KEY,
  dob DATE,
  insurance_number VARCHAR(100),
  notes TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- CLINIC
CREATE TABLE clinic (
  clinic_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  phone VARCHAR(11),
  address VARCHAR(255)
);



-- DOCTOR_CLINIC (many-to-many: doctor works on clinic)
CREATE TABLE doctor_clinic (
  doctor_id INT NOT NULL,
  clinic_id INT NOT NULL,
  PRIMARY KEY (doctor_id, clinic_id),
  FOREIGN KEY (doctor_id) REFERENCES doctor_profile(user_id) ON DELETE CASCADE,
  FOREIGN KEY (clinic_id) REFERENCES clinic(clinic_id) ON DELETE CASCADE
);

-- DOCTOR WORKING TIME (a schedule per doctor per clinic / day)
CREATE TABLE doctor_working_time (
  id INT AUTO_INCREMENT PRIMARY KEY,
  doctor_id INT NOT NULL,
  clinic_id INT NOT NULL,
  day_of_week TINYINT NOT NULL,        -- 0=Sunday .. 6=Saturday (or use ENUM)
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  FOREIGN KEY (doctor_id) REFERENCES doctor_profile(user_id) ON DELETE CASCADE,
  FOREIGN KEY (clinic_id) REFERENCES clinic(clinic_id) ON DELETE CASCADE,
  UNIQUE KEY ux_doctor_clinic_day_time (doctor_id, clinic_id, day_of_week, start_time)
);


-- APPOINTMENT
CREATE TABLE appointment (
  appointment_id INT AUTO_INCREMENT PRIMARY KEY,
  appointment_datetime DATETIME NOT NULL,
  status ENUM('scheduled','completed','cancelled') NOT NULL DEFAULT 'scheduled',
  patient_id INT NOT NULL,               -- references users (patient)
  doctor_id INT NOT NULL,                -- references users (doctor) or doctor_profile.user_id
  clinic_id INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (clinic_id) REFERENCES clinic(clinic_id) ON DELETE SET NULL,
  -- ensure a doctor can't have two appointments at same time
  UNIQUE KEY uq_doctor_datetime (doctor_id, appointment_datetime)
);

-- MEDICAL NOTE
CREATE TABLE medical_note (
  note_id INT AUTO_INCREMENT PRIMARY KEY,
  appointment_id INT,                      -- note belongs to appointment
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


CREATE PROCEDURE RegisterClinicAndDoctor(
  IN clinic_name VARCHAR(255),
  IN clinic_phone VARCHAR(11),
  IN clinic_address VARCHAR(255),

  IN doctor_name VARCHAR(100),
  IN doctor_email VARCHAR(255),
  IN hashed_password VARCHAR(255),
  IN doctor_phone VARCHAR(30),

  IN specialization VARCHAR(255),
  IN bio TEXT,
  IN doctor_name VARCHAR(100),
);


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
  
  IN in_start_day TINYINT,   -- e.g., 1 (Monday)
  IN in_end_day TINYINT,     -- e.g., 5 (Friday)
  IN in_start_time TIME,     -- e.g., '09:00:00'
  IN in_end_time TIME        -- e.g., '17:00:00'
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
  VALUES (in_doctor_name, in_doctor_email, in_hashed_password, in_doctor_phone, 'doctor');
  
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