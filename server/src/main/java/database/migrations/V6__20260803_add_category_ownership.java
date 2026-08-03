package database.migrations;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.adultofuncional.main.shared.normalization.CategoryNameNormalizer;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Migra el catálogo global a categorías SYSTEM/PERSONAL con backfill NFKC. */
public class V6__20260803_add_category_ownership extends BaseJavaMigration {

  @Override
  public void migrate(Context context) throws SQLException {
    try (Statement statement = context.getConnection().createStatement()) {
      statement.execute("""
          ALTER TABLE categories
            ADD COLUMN owner_account_id CHAR(36) NULL,
            ADD COLUMN category_scope VARCHAR(8) NOT NULL DEFAULT 'SYSTEM',
            ADD COLUMN normalized_name VARCHAR(150) NULL
          """);
    }

    backfillNormalizedNames(context);

    try (Statement statement = context.getConnection().createStatement()) {
      statement.execute("""
          ALTER TABLE categories
            MODIFY COLUMN normalized_name VARCHAR(150) NOT NULL,
            ADD COLUMN ownership_discriminator VARCHAR(36)
              GENERATED ALWAYS AS (
                IF(category_scope = 'SYSTEM', 'SYSTEM', RTRIM(owner_account_id))
              ) PERSISTENT,
            ADD CONSTRAINT fk_categories_owner
              FOREIGN KEY (owner_account_id) REFERENCES accounts(account_id)
              ON DELETE CASCADE,
            ADD CONSTRAINT chk_categories_scope
              CHECK (category_scope IN ('SYSTEM', 'PERSONAL')),
            ADD CONSTRAINT chk_categories_scope_owner
              CHECK (
                (category_scope = 'SYSTEM' AND owner_account_id IS NULL)
                OR (category_scope = 'PERSONAL' AND owner_account_id IS NOT NULL)
              ),
            ADD CONSTRAINT chk_categories_type
              CHECK (category_type IN ('FINANCES', 'AGENDA')),
            ADD CONSTRAINT uk_categories_scope_owner_type_name
              UNIQUE (category_scope, ownership_discriminator, category_type, normalized_name)
          """);
    }
  }

  private void backfillNormalizedNames(Context context) throws SQLException {
    try (Statement select = context.getConnection().createStatement();
        ResultSet categories = select.executeQuery(
            "SELECT category_id, category_name FROM categories");
        PreparedStatement update = context.getConnection().prepareStatement(
            "UPDATE categories SET normalized_name = ? WHERE category_id = ?")) {
      while (categories.next()) {
        update.setString(1, CategoryNameNormalizer.normalize(categories.getString("category_name")));
        update.setString(2, categories.getString("category_id"));
        update.addBatch();
      }
      update.executeBatch();
    }
  }
}
