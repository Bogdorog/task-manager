ALTER TABLE companies
    ALTER COLUMN created_at DROP NOT NULL;

ALTER TABLE company_roles
    ALTER COLUMN created_at DROP NOT NULL;

ALTER TABLE company_memberships
    ALTER COLUMN joined_at DROP NOT NULL;