CREATE TABLE dentist (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    cro VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE patient (
    id UUID PRIMARY KEY,
    dentist_id UUID NOT NULL REFERENCES dentist(id),
    name VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL,
    birth_date DATE NOT NULL,
    phone VARCHAR(20),
    allergies TEXT,
    systemic_diseases TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT unique_dentist_patient_cpf UNIQUE (dentist_id, cpf)
);

CREATE TABLE procedure (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patient(id),
    date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    tooth VARCHAR(10),
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
