SET @manual_description_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'manuals'
      AND column_name = 'description'
);

SET @add_manual_description_sql = IF(
    @manual_description_exists = 0,
    'ALTER TABLE manuals ADD COLUMN description VARCHAR(1000) NULL AFTER title',
    'SELECT 1'
);

PREPARE add_manual_description_stmt FROM @add_manual_description_sql;
EXECUTE add_manual_description_stmt;
DEALLOCATE PREPARE add_manual_description_stmt;

SET @version_description_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'manual_versions'
      AND column_name = 'description'
);

SET @add_version_description_sql = IF(
    @version_description_exists = 0,
    'ALTER TABLE manual_versions ADD COLUMN description VARCHAR(1000) NULL AFTER title',
    'SELECT 1'
);

PREPARE add_version_description_stmt FROM @add_version_description_sql;
EXECUTE add_version_description_stmt;
DEALLOCATE PREPARE add_version_description_stmt;

SET @content_diff_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'manual_versions'
      AND column_name = 'content_diff'
);

SET @add_content_diff_sql = IF(
    @content_diff_exists = 0,
    'ALTER TABLE manual_versions ADD COLUMN content_diff LONGTEXT NULL AFTER content',
    'SELECT 1'
);

PREPARE add_content_diff_stmt FROM @add_content_diff_sql;
EXECUTE add_content_diff_stmt;
DEALLOCATE PREPARE add_content_diff_stmt;
