CREATE TABLE accounts 
(
	account_id                    CHAR(36)          NOT NULL          PRIMARY KEY,

  account_names                 VARCHAR(50)       NOT NULL,
  account_lastnames             VARCHAR(50)       NOT NULL,
  account_email                 VARCHAR(255)      NOT NULL          UNIQUE,
  account_phone                 VARCHAR(20)       NOT NULL,
  account_password              VARCHAR(255)      NOT NULL,
  account_master_key            VARCHAR(255)      NULL,
  account_created_at            TIMESTAMP         NOT NULL
);

CREATE TABLE categories 
(
  category_id                   CHAR(36)          NOT NULL          PRIMARY KEY,

  category_name                 VARCHAR(50)       NOT NULL,
  category_type                 VARCHAR(20)       NOT NULL,
  category_created_at           TIMESTAMP         NOT NULL,
  category_deleted_at           TIMESTAMP         NULL DEFAULT NULL
);

CREATE TABLE movements
(
	movement_id                   CHAR(36)          NOT NULL          PRIMARY KEY,
 
	movement_type                 VARCHAR(20)       NOT NULL,
  movement_amount               DECIMAL(10,2)     NOT NULL,
  movement_register_date        TIMESTAMP         NOT NULL,
  movement_description          TEXT              NULL,
  movement_date                 DATE              NOT NULL,

  movement_fk_account_id        CHAR(36)          NOT NULL,
  movement_fk_category_id       CHAR(36),

  FOREIGN KEY (movement_fk_account_id)            REFERENCES        accounts      (account_id),
  FOREIGN KEY (movement_fk_category_id)           REFERENCES        categories    (category_id)
);

CREATE TABLE fixed_expenses 
(
	fixed_expense_id              CHAR(36)          NOT NULL          PRIMARY KEY,

  fixed_expense_name            VARCHAR(50)       NOT NULL,
  fixed_expense_frequency       VARCHAR(15)       NOT NULL,
  fixed_expense_amount          DECIMAL(10,2)     NOT NULL,
  fixed_expense_status          VARCHAR(15)       NOT NULL,
  fixed_expense_start_date      DATE              NOT NULL,
  fixed_expense_next_due_date   DATE              NOT NULL,
  fixed_expense_reminder_days   INT               NOT NULL,

  fixed_expense_fk_account_id   CHAR(36)          NOT NULL,
  fixed_expense_fk_category_id  CHAR(36),

  FOREIGN KEY (fixed_expense_fk_account_id)       REFERENCES        accounts      (account_id),
  FOREIGN KEY (fixed_expense_fk_category_id)      REFERENCES        categories    (category_id)
);

CREATE TABLE events 
(
	event_id                      CHAR(36)          NOT NULL          PRIMARY KEY,

  event_title                   VARCHAR(35)       NOT NULL,
  event_priority                VARCHAR(15)       NULL              DEFAULT 'Media',
  event_date                    DATE              NOT NULL,
  event_frequency               INT               NOT NULL,
  event_reminder                DATETIME          NOT NULL,
  event_start_hour              DATETIME          NOT NULL,
  event_end_hour                DATETIME          NOT NULL,
  event_description             TEXT              NULL,
  event_status                  VARCHAR(20)       DEFAULT 'Pendiente',

  event_fk_account_id           CHAR(36)          NOT NULL,
  event_fk_category_id          CHAR(36),

  FOREIGN KEY (event_fk_account_id)               REFERENCES        accounts      (account_id),
  FOREIGN KEY (event_fk_category_id)              REFERENCES        categories    (category_id)
);

CREATE TABLE passwords
(
  password_id                   CHAR(36)          NOT NULL          PRIMARY KEY,

  password_application_name     VARCHAR(35)       NOT NULL,
  password_salt                 VARCHAR(255)      NOT NULL,
  password_iv                   BINARY(16)        NOT NULL,
  password_ciphertext           VARBINARY(2048)   NOT NULL,
  password_last_change_date     DATE,

  passwords_fk_account_id       CHAR(36)          NOT NULL,

  FOREIGN KEY (passwords_fk_account_id)           REFERENCES        accounts      (account_id)
);

CREATE INDEX idx_movements_account ON movements(movement_fk_account_id);
CREATE INDEX idx_fixed_expenses_account ON fixed_expenses(fixed_expense_fk_account_id);
CREATE INDEX idx_events_account ON events(event_fk_account_id);
CREATE INDEX idx_passwords_account ON passwords(passwords_fk_account_id);
CREATE INDEX idx_movements_date ON movements(movement_date);
CREATE INDEX idx_events_date ON events(event_date);
