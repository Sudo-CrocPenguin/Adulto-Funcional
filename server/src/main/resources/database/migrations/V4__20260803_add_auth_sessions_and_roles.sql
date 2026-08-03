CREATE TABLE account_roles
(
  account_role_fk_account_id   CHAR(36)      NOT NULL,
  account_role_name            VARCHAR(30)   NOT NULL,

  PRIMARY KEY (account_role_fk_account_id, account_role_name),
  CONSTRAINT fk_account_roles_account
    FOREIGN KEY (account_role_fk_account_id)
    REFERENCES accounts (account_id)
    ON DELETE CASCADE,
  CONSTRAINT chk_account_role_name
    CHECK (account_role_name IN ('USER', 'ADMIN'))
);

INSERT INTO account_roles (account_role_fk_account_id, account_role_name)
SELECT account_id, 'USER'
FROM accounts;

CREATE TABLE auth_sessions
(
  auth_session_id                    CHAR(36)       NOT NULL PRIMARY KEY,
  auth_session_fk_account_id         CHAR(36)       NOT NULL,
  auth_session_current_refresh_hash  CHAR(64)       NOT NULL,
  auth_session_previous_refresh_hash CHAR(64)       NULL,
  auth_session_previous_rotated_at   TIMESTAMP(6)   NULL,
  auth_session_refresh_expires_at    TIMESTAMP(6)   NOT NULL,
  auth_session_access_jti            CHAR(36)       NOT NULL,
  auth_session_access_expires_at     TIMESTAMP(6)   NOT NULL,
  auth_session_created_at            TIMESTAMP(6)   NOT NULL,
  auth_session_last_seen_at          TIMESTAMP(6)   NOT NULL,
  auth_session_revoked_at            TIMESTAMP(6)   NULL,
  auth_session_version               BIGINT         NOT NULL DEFAULT 0,

  CONSTRAINT uq_auth_session_current_refresh_hash
    UNIQUE (auth_session_current_refresh_hash),
  CONSTRAINT fk_auth_sessions_account
    FOREIGN KEY (auth_session_fk_account_id)
    REFERENCES accounts (account_id)
    ON DELETE CASCADE,
  CONSTRAINT chk_auth_session_refresh_expiry
    CHECK (auth_session_refresh_expires_at > auth_session_created_at),
  CONSTRAINT chk_auth_session_access_expiry
    CHECK (auth_session_access_expires_at > auth_session_created_at)
);

CREATE INDEX idx_auth_sessions_account_active
  ON auth_sessions (auth_session_fk_account_id, auth_session_revoked_at);

CREATE INDEX idx_auth_sessions_previous_refresh
  ON auth_sessions (auth_session_previous_refresh_hash);
