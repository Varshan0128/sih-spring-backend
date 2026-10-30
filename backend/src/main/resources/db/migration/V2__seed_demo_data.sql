-- Flyway Migration V2: AICTE Approval Intelligence & Early-Warning System Demo Seed Data

-- 1. Insert Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_INSTITUTION', 'Authorized Institution User / Principal / Registrar'),
(2, 'ROLE_REGIONAL_OFFICER', 'AICTE Regional Office In-charge & Monitoring Officer'),
(3, 'ROLE_BUREAU_OFFICER', 'AICTE Bureau Processing Officer (Engineering, Management, etc.)'),
(4, 'ROLE_SCRUTINY_COMMITTEE', 'Member of Scrutiny / Processing Committee'),
(5, 'ROLE_EVC_COMMITTEE', 'Expert Visiting Committee (EVC) Member / Inspector'),
(6, 'ROLE_SAC_COMMITTEE', 'Standing Appellate Committee / Standing Hearing Committee Member'),
(7, 'ROLE_COMPETENT_AUTHORITY', 'AICTE Chairman / Member Secretary / Statutory Decision Maker'),
(8, 'ROLE_SYSTEM_ADMIN', 'Platform & Workflow Configuration Administrator')
ON CONFLICT (id) DO NOTHING;

-- 2. Insert Users (Default password is 'Password@123', BCrypt hash: $2b$10$/wMXLzidxSWPt1ygvkuBEuUiU8L09WvLYSanW4ozR4JAUiclPVz9a)
INSERT INTO users (id, username, email, password_hash, full_name, phone_number, region, bureau, designation, is_active) VALUES
(1, 'inst_dit', 'registrar@dit.edu.in', '$2b$10$/wMXLzidxSWPt1ygvkuBEuUiU8L09WvLYSanW4ozR4JAUiclPVz9a', 'Dr. Ramesh Sharma', '+91-9876543210', 'Northern', 'Technical', 'Registrar', true),
(2, 'reg_officer_north', 'ro.north@aicte-india.org', '$2b$10$/wMXLzidxSWPt1ygvkuBEuUiU8L09WvLYSanW4ozR4JAUiclPVz9a', 'Smt. Ananya Sen', '+91-9812345678', 'Northern', 'All', 'Regional Officer', true),
(3, 'bureau_officer_eng', 'bo.eng@aicte-india.org', '$2b$10$/wMXLzidxSWPt1ygvkuBEuUiU8L09WvLYSanW4ozR4JAUiclPVz9a', 'Dr. Vikramaditya Roy', '+91-9823456789', 'Northern', 'Engineering & Technology', 'Bureau Officer', true),
(4, 'scrutiny_comm_chair', 'scrutiny.chair@aicte-india.org', '$2b$10$/wMXLzidxSWPt1ygvkuBEuUiU8L09WvLYSanW4ozR4JAUiclPVz9a', 'Prof. Arvind Kulkarni', '+91-9834567890', 'Northern', 'Engineering & Technology', 'Scrutiny Committee Chair', true),
(5, 'evc_expert_chair', 'evc.chair@aicte-india.org', '$2b$10$/wMXLzidxSWPt1ygvkuBEuUiU8L09WvLYSanW4ozR4JAUiclPVz9a', 'Prof. Meenakshi Sundaram', '+91-9845678901', 'Northern', 'Engineering & Technology', 'EVC Chairman', true),
(6, 'sac_committee_lead', 'sac.lead@aicte-india.org', '$2b$10$/wMXLzidxSWPt1ygvkuBEuUiU8L09WvLYSanW4ozR4JAUiclPVz9a', 'Hon. Justice (Retd.) B. N. Rao', '+91-9856789012', 'HQ', 'Appellate', 'SAC Chairman', true),
(7, 'competent_auth', 'member.secretary@aicte-india.org', '$2b$10$/wMXLzidxSWPt1ygvkuBEuUiU8L09WvLYSanW4ozR4JAUiclPVz9a', 'Prof. Rajive Kumar', '+91-9867890123', 'HQ', 'Administration', 'Member Secretary', true),
(8, 'sys_admin', 'admin@aicte-india.org', '$2b$10$/wMXLzidxSWPt1ygvkuBEuUiU8L09WvLYSanW4ozR4JAUiclPVz9a', 'Platform Administrator', '+91-9878901234', 'HQ', 'IT Cell', 'System Administrator', true)
ON CONFLICT (id) DO NOTHING;

-- Map Users to Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- inst_dit -> ROLE_INSTITUTION
(2, 2), -- reg_officer_north -> ROLE_REGIONAL_OFFICER
(3, 3), -- bureau_officer_eng -> ROLE_BUREAU_OFFICER
(4, 4), -- scrutiny_comm_chair -> ROLE_SCRUTINY_COMMITTEE
(5, 5), -- evc_expert_chair -> ROLE_EVC_COMMITTEE
(6, 6), -- sac_committee_lead -> ROLE_SAC_COMMITTEE
(7, 7), -- competent_auth -> ROLE_COMPETENT_AUTHORITY
(8, 8)  -- sys_admin -> ROLE_SYSTEM_ADMIN
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 3. Insert Institutions
INSERT INTO institutions (id, permanent_id, name, address, city, state, region, pin_code, institution_type, status, user_id) VALUES
(1, '1-102938475', 'Delhi Institute of Technology & Science', 'Plot 14, Institutional Area, Sector 62', 'Noida', 'Uttar Pradesh', 'Northern', '201309', 'Engineering & Technology', 'ACTIVE', 1),
(2, '1-209384756', 'Mumbai Institute of Management Studies', 'Bandra-Kurla Complex', 'Mumbai', 'Maharashtra', 'Western', '400051', 'Management', 'ACTIVE', NULL),
(3, '1-309485761', 'Bangalore Technological University Campus', 'Electronic City Phase 1', 'Bengaluru', 'Karnataka', 'Southern', '560100', 'Engineering & Technology', 'ACTIVE', NULL)
ON CONFLICT (id) DO NOTHING;

-- 4. Insert Approval Types
INSERT INTO approval_types (id, code, name, description, aph_version, is_active) VALUES
(1, 'NEW_INSTITUTE', 'Setting up New Technical Institution', 'Establishment of new institution offering technical programmes', 'APH 2026-27', true),
(2, 'EXTENSION_OF_APPROVAL', 'Extension of Approval (EoA) 2026-27', 'Annual continuation of approved programmes and intake', 'APH 2026-27', true),
(3, 'INCREASE_IN_INTAKE', 'Increase in Intake / New Courses', 'Expansion of student intake capacity or starting new departments', 'APH 2026-27', true),
(4, 'CLOSURE', 'Closure of Institution / Course', 'Orderly progressive or complete closure of approved institutes', 'APH 2026-27', true)
ON CONFLICT (id) DO NOTHING;

-- 5. Insert SLA Rules
INSERT INTO sla_rules (id, stage_name, approval_type_code, max_duration_days, warning_threshold_days, escalation_role, is_active) VALUES
(1, 'SCRUTINY', 'NEW_INSTITUTE', 15, 3, 'ROLE_REGIONAL_OFFICER', true),
(2, 'DEFICIENCY_COMMUNICATED', 'NEW_INSTITUTE', 10, 2, 'ROLE_BUREAU_OFFICER', true),
(3, 'EVC_PENDING', 'NEW_INSTITUTE', 14, 3, 'ROLE_REGIONAL_OFFICER', true),
(4, 'EVC_SCHEDULED', 'NEW_INSTITUTE', 7, 2, 'ROLE_BUREAU_OFFICER', true),
(5, 'SAC_REVIEW', 'NEW_INSTITUTE', 10, 2, 'ROLE_COMPETENT_AUTHORITY', true),
(6, 'APPROVAL_PENDING', 'NEW_INSTITUTE', 7, 2, 'ROLE_COMPETENT_AUTHORITY', true),
(7, 'SCRUTINY', 'EXTENSION_OF_APPROVAL', 10, 2, 'ROLE_REGIONAL_OFFICER', true),
(8, 'APPROVAL_PENDING', 'EXTENSION_OF_APPROVAL', 5, 1, 'ROLE_COMPETENT_AUTHORITY', true)
ON CONFLICT (id) DO NOTHING;

-- 6. Insert Sample Applications
INSERT INTO applications (id, application_number, institution_id, approval_type_id, academic_year, current_stage, status, submission_date, sla_deadline, is_sla_breached, risk_score, risk_level, delay_probability, primary_bottleneck, submitted_by) VALUES
(1, 'AICTE-2026-APP-1001', 1, 2, '2026-2027', 'SCRUTINY', 'IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP + INTERVAL '7 days', false, 18, 'LOW', 0.15, 'None - within SLA timeline', 1),
(2, 'AICTE-2026-APP-1024', 1, 1, '2026-2027', 'EVC_PENDING', 'IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '18 days', CURRENT_TIMESTAMP - INTERVAL '2 days', true, 82, 'HIGH', 0.88, 'Primary bottleneck: EVC scheduling pending for 6 days and Fire Safety NOC expiring in 15 days', 1),
(3, 'AICTE-2026-APP-1045', 2, 3, '2026-2027', 'SUBMITTED', 'IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '1 days', CURRENT_TIMESTAMP + INTERVAL '14 days', false, 45, 'MEDIUM', 0.42, 'Land document pending verification', 1),
(4, 'AICTE-2026-APP-1090', 3, 2, '2026-2027', 'APPROVED', 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '40 days', CURRENT_TIMESTAMP - INTERVAL '15 days', false, 10, 'LOW', 0.05, 'Completed - Approval Issued', 1)
ON CONFLICT (id) DO NOTHING;

-- 7. Insert Stage History for Case 1024 (Demonstrating timeline progression)
INSERT INTO application_stage_history (application_id, stage_name, entered_at, exited_at, duration_hours, completed_by_user_id, status, remarks) VALUES
(2, 'SUBMITTED', CURRENT_TIMESTAMP - INTERVAL '18 days', CURRENT_TIMESTAMP - INTERVAL '17 days', 24, 1, 'COMPLETED', 'Application submitted by institution with required undertakings'),
(2, 'SCRUTINY', CURRENT_TIMESTAMP - INTERVAL '17 days', CURRENT_TIMESTAMP - INTERVAL '6 days', 264, 4, 'COMPLETED', 'Scrutiny committee noted land and building documents adequate, recommended physical EVC'),
(2, 'EVC_PENDING', CURRENT_TIMESTAMP - INTERVAL '6 days', NULL, NULL, NULL, 'IN_PROGRESS', 'Awaiting committee chairman confirmation for inspection dates');

-- 8. Workflow Tasks
INSERT INTO workflow_tasks (id, application_id, task_name, stage_name, assigned_to_user_id, assigned_role, status, comments, confidential_notes) VALUES
(1, 1, 'Conduct Document Scrutiny', 'SCRUTINY', 4, 'ROLE_SCRUTINY_COMMITTEE', 'PENDING', 'Verify AICTE EoA checklist 2026-27', 'No negative flags on land title; check audited accounts'),
(2, 2, 'Schedule Expert Visiting Committee', 'EVC_PENDING', 2, 'ROLE_REGIONAL_OFFICER', 'PENDING', 'Urgent EVC required. Application breached stage SLA by 2 days.', 'Inspection panel recommended: Dr. Kulkarni & Dr. Roy. Confidential: Ensure strict lab equipment audit.'),
(3, 3, 'Initial Intake Feasibility Review', 'SUBMITTED', 3, 'ROLE_BUREAU_OFFICER', 'PENDING', 'Assess proposed additional 60 seats in Artificial Intelligence & Data Science', NULL)
ON CONFLICT (id) DO NOTHING;

-- 9. EVC Record for Application 1024
INSERT INTO evc_records (id, application_id, is_required, status, scheduled_date, inspection_mode, committee_chairman_id, committee_members, observations, compliance_status) VALUES
(1, 2, true, 'OVERDUE', NULL, 'PHYSICAL', 5, '["Prof. Meenakshi Sundaram", "Dr. Vikramaditya Roy"]', 'Physical inspection mandated due to new technical programme introduction', 'PENDING')
ON CONFLICT (id) DO NOTHING;

-- 10. Compliance Requirements
INSERT INTO compliance_requirements (id, application_id, title, category, description, due_date, status, verified_by_user_id) VALUES
(1, 2, 'Student to Faculty Cadre Ratio 1:15', 'FACULTY_RATIO', 'Certified faculty muster roll approved by affiliating university', CURRENT_TIMESTAMP + INTERVAL '10 days', 'SUBMITTED', NULL),
(2, 2, 'Valid Fire Safety NOC', 'FIRE_SAFETY', 'Current Fire Safety NOC issued by District Fire Officer', CURRENT_TIMESTAMP + INTERVAL '15 days', 'PENDING', NULL),
(3, 2, 'Land Title & Mutation Certificate', 'LAND_TITLE', 'Registered lease/ownership deed for 5.0 acres contiguous land', CURRENT_TIMESTAMP - INTERVAL '10 days', 'VERIFIED', 4)
ON CONFLICT (id) DO NOTHING;

-- 11. Undertakings
INSERT INTO undertakings (id, application_id, institution_id, title, content, is_signed, signed_at, signed_by_user_id, deadline, status) VALUES
(1, 2, 1, 'Anti-Ragging Compliance Undertaking', 'The institution hereby affirms zero tolerance for ragging per AICTE regulations.', true, CURRENT_TIMESTAMP - INTERVAL '18 days', 1, CURRENT_TIMESTAMP + INTERVAL '30 days', 'ACCEPTED'),
(2, 2, 1, 'Tuition Fee Waiver Scheme Compliance', 'The institution commits to 5% supernumerary seats under TFW scheme.', true, CURRENT_TIMESTAMP - INTERVAL '18 days', 1, CURRENT_TIMESTAMP + INTERVAL '30 days', 'ACCEPTED')
ON CONFLICT (id) DO NOTHING;

-- 12. Document Metadata
INSERT INTO document_metadata (id, application_id, institution_id, document_type, file_name, file_size, content_type, object_storage_key, version, issue_date, expiry_date, is_expired, is_verified, upload_timestamp, status, uploaded_by) VALUES
(1, 2, 1, 'LAND_DOCUMENT', 'Registered_Sale_Deed_Land.pdf', 2450000, 'application/pdf', 'docs/1/2/land_deed_v1.pdf', 1, '2020-01-15', '2050-01-14', false, true, CURRENT_TIMESTAMP - INTERVAL '18 days', 'VERIFIED', 1),
(2, 2, 1, 'FIRE_SAFETY_NOC', 'Fire_Safety_NOC_2025_2026.pdf', 1150000, 'application/pdf', 'docs/1/2/fire_noc_v1.pdf', 1, '2025-11-10', CURRENT_DATE + INTERVAL '15 days', false, false, CURRENT_TIMESTAMP - INTERVAL '18 days', 'OCR_PROCESSED', 1)
ON CONFLICT (id) DO NOTHING;

-- 13. Document Extraction Results
INSERT INTO document_extraction_results (id, document_id, extracted_institution_name, extracted_doc_type, extracted_issue_date, extracted_expiry_date, confidence_score, suspicious_flags, raw_extracted_text, validation_status) VALUES
(1, 2, 'Delhi Institute of Technology & Science', 'FIRE_SAFETY_NOC', '2025-11-10', CURRENT_DATE + INTERVAL '15 days', 94.50, 'WARNING: Document expires within 15 days of academic session start', 'OFFICE OF CHIEF FIRE OFFICER... NOC valid till Nov 2026...', 'HUMAN_REVIEW_REQUIRED')
ON CONFLICT (id) DO NOTHING;

-- 14. Notifications
INSERT INTO notifications (id, recipient_user_id, recipient_role, title, message, notification_type, priority, is_read, entity_type, entity_id) VALUES
(1, 2, 'ROLE_REGIONAL_OFFICER', 'URGENT SLA BREACH: Application AICTE-2026-APP-1024', 'Stage EVC_PENDING has exceeded the 14-day SLA limit by 2 days. Case escalated for regional intervention.', 'ESCALATION', 'CRITICAL', false, 'APPLICATION', 2),
(2, 1, 'ROLE_INSTITUTION', 'Document Expiry Warning: Fire Safety NOC', 'Your uploaded Fire Safety NOC expires in 15 days. Please initiate renewal with District Fire Office.', 'DOCUMENT_EXPIRY', 'HIGH', false, 'DOCUMENT', 2)
ON CONFLICT (id) DO NOTHING;

-- 15. AI Risk Assessments
INSERT INTO ai_risk_assessments (id, application_id, risk_score, risk_level, delay_probability, primary_bottleneck, contributing_factors, model_version) VALUES
(1, 2, 82, 'HIGH', 0.88, 'Primary bottleneck: EVC scheduling pending for 6 days and Fire Safety NOC expiring in 15 days', '["EVC_SCHEDULE_DELAY_6_DAYS", "FIRE_NOC_EXPIRING_15_DAYS", "SLA_BREACH_2_DAYS_OVERDUE", "STAGE_AGE_EXCEEDS_REGIONAL_AVG_BY_35%"]', 'v1.0.0-rules-baseline')
ON CONFLICT (id) DO NOTHING;

-- 16. Tamper-Resistant Audit Events
INSERT INTO audit_events (id, correlation_id, actor_user_id, actor_username, actor_role, action, entity_name, entity_id, previous_state, new_state, ip_address, timestamp, record_hash) VALUES
(1, 'corr-init-001', 1, 'inst_dit', 'ROLE_INSTITUTION', 'APPLICATION_SUBMITTED', 'APPLICATION', '2', 'DRAFT', 'SUBMITTED', '127.0.0.1', CURRENT_TIMESTAMP - INTERVAL '18 days', 'd2c1f9b3e7a884a6c9d5e4b2a1c7f8e9d0a3b2c1d4e5f6a7b8c9d0e1f2a3b4c5'),
(2, 'corr-init-002', 4, 'scrutiny_comm_chair', 'ROLE_SCRUTINY_COMMITTEE', 'SCRUTINY_COMPLETED', 'APPLICATION', '2', 'SCRUTINY', 'EVC_PENDING', '127.0.0.1', CURRENT_TIMESTAMP - INTERVAL '6 days', 'a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2')
ON CONFLICT (id) DO NOTHING;

-- Reset Sequences to ensure next inserts start safely after highest seed ID
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('institutions_id_seq', (SELECT MAX(id) FROM institutions));
SELECT setval('approval_types_id_seq', (SELECT MAX(id) FROM approval_types));
SELECT setval('sla_rules_id_seq', (SELECT MAX(id) FROM sla_rules));
SELECT setval('applications_id_seq', (SELECT MAX(id) FROM applications));
SELECT setval('workflow_tasks_id_seq', (SELECT MAX(id) FROM workflow_tasks));
SELECT setval('evc_records_id_seq', (SELECT MAX(id) FROM evc_records));
SELECT setval('compliance_requirements_id_seq', (SELECT MAX(id) FROM compliance_requirements));
SELECT setval('undertakings_id_seq', (SELECT MAX(id) FROM undertakings));
SELECT setval('document_metadata_id_seq', (SELECT MAX(id) FROM document_metadata));
SELECT setval('document_extraction_results_id_seq', (SELECT MAX(id) FROM document_extraction_results));
SELECT setval('notifications_id_seq', (SELECT MAX(id) FROM notifications));
SELECT setval('ai_risk_assessments_id_seq', (SELECT MAX(id) FROM ai_risk_assessments));
SELECT setval('audit_events_id_seq', (SELECT MAX(id) FROM audit_events));
