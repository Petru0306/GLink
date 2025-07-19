# Flyway Migration Guide - GreenLink

## Overview

GreenLink now uses Flyway for database version control and migration management. This ensures consistent database schema across all environments and provides a reliable way to track and apply database changes.

## Migration Files Structure

All migration files are located in `src/main/resources/db/migration/` and follow the naming convention:
- `V{version}__{description}.sql`

### Current Migration Sequence

1. **V1__baseline_schema.sql** - Initial database schema
2. **V2__add_sample_data.sql** - Empty (handled by V20)
3. **V5__add_seller_to_product.sql** - Add seller relationship to products
4. **V6__add_messaging_tables.sql** - Create messaging system tables
5. **V7__add_sample_conversations.sql** - Add sample conversation data
6. **V9__add_new_challenge_types.sql** - Add challenge type constraints
7. **V10__add_sample_challenges.sql** - Add sample challenges
8. **V11__fix_challenge_schema.sql** - Fix challenge table structure
9. **V12__add_missing_listing_challenges.sql** - Add missing marketplace challenges
10. **V13__remove_specific_default_challenges.sql** - Remove specific challenges
11. **V14__remove_eco_avatar_challenge.sql** - Remove avatar challenges
12. **V15__remove_eco_cult_leader_challenge.sql** - Remove cult leader challenge
13. **V15_1__remove_first_item_listed_challenge.sql** - Remove first item challenge
14. **V16__add_seen_column_to_user_challenges.sql** - Add seen column
15. **V17__add_level_and_point_events.sql** - Add level system and point events
16. **V18__add_stripe_and_sale_fields.sql** - Add Stripe and sale fields
17. **V19__add_oauth2_fields.sql** - Add OAuth2 fields
18. **V20__initial_data.sql** - Initial data population

## Configuration

### Development
```properties
# application.properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.table=flyway_schema_history
```

### Production
```properties
# application-prod.properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.table=flyway_schema_history
spring.sql.init.mode=never
```

## Migration Commands

### Check Migration Status
```bash
# View current migration status
./mvnw flyway:info
```

### Apply Migrations
```bash
# Apply all pending migrations
./mvnw flyway:migrate
```

### Validate Migrations
```bash
# Validate migration files
./mvnw flyway:validate
```

### Clean Database (Development Only)
```bash
# WARNING: This will delete all data
./mvnw flyway:clean
```

## Creating New Migrations

### 1. Create Migration File
Create a new file in `src/main/resources/db/migration/` with the naming convention:
```
V{next_version}__{description}.sql
```

Example: `V21__add_user_preferences.sql`

### 2. Write Migration SQL
```sql
-- V21__add_user_preferences.sql
CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    theme VARCHAR(20) DEFAULT 'light',
    notifications_enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
```

### 3. Test Migration
```bash
# Test locally first
./mvnw flyway:migrate
```

## Best Practices

### 1. Migration Naming
- Use descriptive names that explain what the migration does
- Use lowercase with underscores for descriptions
- Example: `V21__add_user_preferences.sql`

### 2. SQL Standards
- Use PostgreSQL syntax (BIGSERIAL, etc.)
- Always include proper foreign key constraints
- Add indexes for performance where needed
- Use `IF NOT EXISTS` for safety

### 3. Data Migrations
- Use `WHERE NOT EXISTS` to prevent duplicate data
- Include proper error handling
- Test with existing data

### 4. Rollback Considerations
- Flyway doesn't support automatic rollbacks
- Plan migrations carefully
- Keep backups before major changes

## Troubleshooting

### Common Issues

#### 1. Migration Already Applied
```
Error: Found non-empty schema "public" without metadata table
```
**Solution**: Use `spring.flyway.baseline-on-migrate=true`

#### 2. Checksum Mismatch
```
Error: Validate failed: Migration checksum mismatch
```
**Solution**: Check if migration file was modified after being applied

#### 3. Duplicate Migration Version
```
Error: Found more than one migration with version
```
**Solution**: Ensure unique version numbers

### Debug Commands

```bash
# Check migration info
./mvnw flyway:info

# Validate migrations
./mvnw flyway:validate

# Repair checksums (if needed)
./mvnw flyway:repair
```

## Production Deployment

### 1. Pre-deployment Checklist
- [ ] All migrations tested in staging
- [ ] Database backup created
- [ ] Migration files validated
- [ ] Rollback plan prepared

### 2. Deployment Steps
```bash
# 1. Deploy application with new migrations
# 2. Flyway will automatically apply pending migrations
# 3. Monitor application logs for migration status
# 4. Verify database schema
```

### 3. Verification
```sql
-- Check migration status
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Verify tables exist
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' ORDER BY table_name;
```

## Migration History

| Version | Description | Date | Status |
|---------|-------------|------|--------|
| V1 | Baseline schema | - | ✅ Applied |
| V2 | Sample data (empty) | - | ✅ Applied |
| V5 | Add seller to product | - | ✅ Applied |
| V6 | Messaging tables | - | ✅ Applied |
| V7 | Sample conversations | - | ✅ Applied |
| V9 | Challenge type constraints | - | ✅ Applied |
| V10 | Sample challenges | - | ✅ Applied |
| V11 | Fix challenge schema | - | ✅ Applied |
| V12 | Missing listing challenges | - | ✅ Applied |
| V13 | Remove specific challenges | - | ✅ Applied |
| V14 | Remove avatar challenges | - | ✅ Applied |
| V15 | Remove cult leader challenge | - | ✅ Applied |
| V15_1 | Remove first item challenge | - | ✅ Applied |
| V16 | Add seen column | - | ✅ Applied |
| V17 | Level and point events | - | ✅ Applied |
| V18 | Stripe and sale fields | - | ✅ Applied |
| V19 | OAuth2 fields | - | ✅ Applied |
| V20 | Initial data | - | ✅ Applied |

## Support

For migration issues:
1. Check the migration logs in application startup
2. Verify database connectivity
3. Test migrations in a clean environment
4. Review the Flyway documentation 