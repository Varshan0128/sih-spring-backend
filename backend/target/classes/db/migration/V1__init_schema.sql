-- Flyway Migration V1: AICTE Approval Intelligence & Early-Warning System Core Schema

-- 1. Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20),
    region VARCHAR(50),
    bureau VARCHAR(100),
    designation VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 3. Institutions
CREATE TABLE IF NOT EXISTS institutions (
    id BIGSERIAL PRIMARY KEY,
    permanent_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    region VARCHAR(50) NOT NULL,
    pin_code VARCHAR(10) NOT NULL,
    institution_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Approval Types
CREATE TABLE IF NOT EXISTS approval_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    aph_version VARCHAR(50) NOT NULL DEFAULT 'APH 2026-27',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Applications
CREATE TABLE IF NOT EXISTS applications (
    id BIGSERIAL PRIMARY KEY,
    application_number VARCHAR(50) NOT NULL UNIQUE,
    institution_id BIGINT NOT NULL REFERENCES institutions(id),
    approval_type_id BIGINT NOT NULL REFERENCES approval_types(id),
    academic_year VARCHAR(20) NOT NULL DEFAULT '2026-2027',
    current_stage VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    submission_date TIMESTAMP WITH TIME ZONE,
    sla_deadline TIMESTAMP WITH TIME ZONE,
    is_sla_breached BOOLEAN NOT NULL DEFAULT FALSE,
    risk_score INTEGER DEFAULT 0,
    risk_level VARCHAR(20) DEFAULT 'LOW',
    delay_probability NUMERIC(5,2) DEFAULT 0.0,
    primary_bottleneck TEXT,
    submitted_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for frequent application queries
CREATE INDEX IF NOT EXISTS idx_apps_institution ON applications(institution_id);
CREATE INDEX IF NOT EXISTS idx_apps_stage ON applications(current_stage);
CREATE INDEX IF NOT EXISTS idx_apps_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_apps_sla_deadline ON applications(sla_deadline);
CREATE INDEX IF NOT EXISTS idx_apps_risk ON applications(risk_level, risk_score);

-- 6. Application Stage History
CREATE TABLE IF NOT EXISTS application_stage_history (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    stage_name VARCHAR(50) NOT NULL,
    entered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exited_at TIMESTAMP WITH TIME ZONE,
    duration_hours INTEGER,
    completed_by_user_id BIGINT REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_stage_hist_app ON application_stage_history(application_id);

-- 7. Workflow Tasks
CREATE TABLE IF NOT EXISTS workflow_tasks (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    task_name VARCHAR(100) NOT NULL,
    stage_name VARCHAR(50) NOT NULL,
    assigned_to_user_id BIGINT REFERENCES users(id),
    assigned_role VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    comments TEXT,
    confidential_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_user ON workflow_tasks(assigned_to_user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_app_status ON workflow_tasks(application_id, status);

-- 8. SLA Rules
CREATE TABLE IF NOT EXISTS sla_rules (
    id BIGSERIAL PRIMARY KEY,
    stage_name VARCHAR(50) NOT NULL,
    approval_type_code VARCHAR(50),
    max_duration_days INTEGER NOT NULL,
    warning_threshold_days INTEGER NOT NULL,
    escalation_role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_stage_approval_type UNIQUE (stage_name, approval_type_code)
);

-- 9. SLA Breach Records
CREATE TABLE IF NOT EXISTS sla_breach_records (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    stage_name VARCHAR(50) NOT NULL,
    breach_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sla_days INTEGER NOT NULL,
    elapsed_days INTEGER NOT NULL,
    escalated_to_role VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ESCALATED',
    resolution_notes TEXT,
    resolved_at TIMESTAMP WITH TIME ZONE
);

-- 10. EVC Records
CREATE TABLE IF NOT EXISTS evc_records (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_SCHEDULED',
    scheduled_date TIMESTAMP WITH TIME ZONE,
    inspection_mode VARCHAR(30) DEFAULT 'PHYSICAL',
    committee_chairman_id BIGINT REFERENCES users(id),
    committee_members TEXT,
    report_url VARCHAR(255),
    observations TEXT,
    compliance_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 11. Compliance Requirements
CREATE TABLE IF NOT EXISTS compliance_requirements (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    due_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verified_by_user_id BIGINT REFERENCES users(id),
    verified_at TIMESTAMP WITH TIME ZONE,
    verification_remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 12. Undertakings
CREATE TABLE IF NOT EXISTS undertakings (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    institution_id BIGINT NOT NULL REFERENCES institutions(id),
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    is_signed BOOLEAN NOT NULL DEFAULT FALSE,
    signed_at TIMESTAMP WITH TIME ZONE,
    signed_by_user_id BIGINT REFERENCES users(id),
    deadline TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 13. Document Metadata
CREATE TABLE IF NOT EXISTS document_metadata (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    institution_id BIGINT NOT NULL REFERENCES institutions(id),
    document_type VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    object_storage_key VARCHAR(255) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    issue_date DATE,
    expiry_date DATE,
    is_expired BOOLEAN NOT NULL DEFAULT FALSE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by_user_id BIGINT REFERENCES users(id),
    verified_at TIMESTAMP WITH TIME ZONE,
    upload_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_OCR',
    uploaded_by BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_doc_app ON document_metadata(application_id);
CREATE INDEX IF NOT EXISTS idx_doc_expiry ON document_metadata(expiry_date, is_expired);

-- 14. Document Extraction Results
CREATE TABLE IF NOT EXISTS document_extraction_results (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL UNIQUE REFERENCES document_metadata(id) ON DELETE CASCADE,
    extracted_institution_name VARCHAR(255),
    extracted_doc_type VARCHAR(100),
    extracted_issue_date DATE,
    extracted_expiry_date DATE,
    confidence_score NUMERIC(5,2),
    suspicious_flags TEXT,
    raw_extracted_text TEXT,
    validation_status VARCHAR(50) NOT NULL DEFAULT 'HUMAN_REVIEW_REQUIRED',
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 15. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    recipient_role VARCHAR(50),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_notif_recipient ON notifications(recipient_user_id, is_read);

-- 16. Notification Delivery Attempts
CREATE TABLE IF NOT EXISTS notification_delivery_attempts (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    channel VARCHAR(30) NOT NULL,
    attempt_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    error_message TEXT
);

-- 17. Escalation History
CREATE TABLE IF NOT EXISTS escalation_history (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    from_role VARCHAR(50) NOT NULL,
    to_role VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    triggered_by VARCHAR(50) NOT NULL DEFAULT 'AUTO_SLA',
    escalated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    resolution_summary TEXT,
    resolved_at TIMESTAMP WITH TIME ZONE
);

-- 18. AI Risk Assessments
CREATE TABLE IF NOT EXISTS ai_risk_assessments (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    risk_score INTEGER NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    delay_probability NUMERIC(5,2) NOT NULL,
    primary_bottleneck TEXT,
    contributing_factors TEXT,
    model_version VARCHAR(50) NOT NULL,
    assessment_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 19. AI Recommendations
CREATE TABLE IF NOT EXISTS ai_recommendations (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    recommendation_type VARCHAR(100) NOT NULL,
    recommended_action TEXT NOT NULL,
    rationale TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_REVIEW',
    reviewed_by_user_id BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 20. Tamper-Resistant Audit Events
CREATE TABLE IF NOT EXISTS audit_events (
    id BIGSERIAL PRIMARY KEY,
    correlation_id VARCHAR(100) NOT NULL,
    actor_user_id BIGINT REFERENCES users(id),
    actor_username VARCHAR(100) NOT NULL,
    actor_role VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    previous_state TEXT,
    new_state TEXT,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    record_hash VARCHAR(64)
);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_events(entity_name, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_actor ON audit_events(actor_username);
CREATE INDEX IF NOT EXISTS idx_audit_correlation ON audit_events(correlation_id);
