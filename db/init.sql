CREATE TABLE IF NOT EXISTS employees (
    id uuid NOT NULL UNIQUE,
    name varchar NOT NULL,
    surname varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS claims (
    id uuid NOT NULL UNIQUE,
    type char NOT NULL,
    employeeId uuid NOT NULL,
    expenses JSON NOT NULL
);

CREATE TABLE IF NOT EXISTS expensesheets (
    id uuid NOT NULL UNIQUE,
    type char NOT NULL,
    employeeId uuid NOT NULL,
    expenses JSON NOT NULL
);