CREATE TABLE accounts 
(
	account_id                    CHAR(36)          PRIMARY KEY DEFAULT(UUID_V7()),

  account_names                 VARCHAR(50)       NOT NULL,
  account_lastnames             VARCHAR(50)       NOT NULL,
  account_email                 VARCHAR(255)      NOT NULL          UNIQUE,
  account_phone                 VARCHAR(20)       NOT NULL,
  account_password              VARCHAR(60)       NOT NULL,
  account_master_key            VARCHAR(24)       NULL,
  account_created_at            TIMESTAMP         DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories 
(
  category_id                   CHAR(36)          PRIMARY KEY DEFAULT(UUID_V7()),

  category_name                 VARCHAR(20)       NOT NULL,
  category_type                 VARCHAR(20)       NOT NULL,
  category_created_at           TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
  category_deleted_at           TIMESTAMP         NULL DEFAULT NULL
);

CREATE TABLE movements
(
	movement_id                   CHAR(36)          PRIMARY KEY DEFAULT(UUID_V7()),
 
	movement_type                 VARCHAR(7)        NOT NULL,
  movement_amount               DECIMAL(10,2)     NOT NULL,
  movement_register_date        TIMESTAMP         NOT NULL          DEFAULT CURRENT_TIMESTAMP,
  movement_description          TEXT              NULL,
  movement_date                 DATE              NOT NULL,

  movement_fk_account_id        CHAR(36),
  movement_fk_category_id       CHAR(36),

  FOREIGN KEY (movement_fk_account_id)            REFERENCES        accounts      (account_id),
  FOREIGN KEY (movement_fk_category_id)           REFERENCES        categories    (category_id)
);

CREATE TABLE fixed_expenses 
(
	fixed_expense_id              CHAR(36)          PRIMARY KEY DEFAULT(UUID_V7()),

  fixed_expense_name            VARCHAR(20)       NOT NULL,
  fixed_expense_frequency       VARCHAR(15)       NOT NULL,
  fixed_expense_amount          DECIMAL(10,2)     NOT NULL,
  fixed_expense_status          VARCHAR(15)       NOT NULL,
  fixed_expense_closing_date    DATE              NOT NULL,

  fixed_expense_fk_category_id  CHAR(36),
  fixed_expense_fk_account_id   CHAR(36),

  FOREIGN KEY (fixed_expense_fk_account_id)       REFERENCES        accounts      (account_id),
  FOREIGN KEY (fixed_expense_fk_category_id)      REFERENCES        categories    (category_id)
);

CREATE TABLE events 
(
	event_id                      CHAR(36)          PRIMARY KEY DEFAULT(UUID_V7()),

  event_title                   VARCHAR(35)       NOT NULL,
  event_priority                VARCHAR(15)       NULL               DEFAULT 'Media',
  event_date                    DATE              NOT NULL,
  event_frequency               INT               NOT NULL,
  event_reminder                DATETIME          NOT NULL,
  event_start_hour              DATETIME          NOT NULL,
  event_end_hour                DATETIME          NOT NULL,
  event_description             TEXT              NULL,
  event_status                  VARCHAR(20)       DEFAULT 'Pendiente',

  event_fk_category_id          CHAR(36),
  event_fk_account_id           CHAR(36),

  FOREIGN KEY (event_fk_account_id)               REFERENCES        accounts      (account_id),
  FOREIGN KEY (event_fk_category_id)              REFERENCES        categories    (category_id)
);

CREATE TABLE passwords
(
  password_id                   CHAR(36)          PRIMARY KEY DEFAULT(UUID_V7()),

  password_application_name     VARCHAR(35)       NOT NULL,
  password_application          VARCHAR(20)       NOT NULL,
  password_last_change_date     DATE,

  passwords_fk_account_id       CHAR(36),

  FOREIGN KEY (passwords_fk_account_id)           REFERENCES        accounts      (account_id)
);
