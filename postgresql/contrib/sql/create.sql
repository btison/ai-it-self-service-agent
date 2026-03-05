SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;


--- enums
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'integrationtype') THEN
        CREATE TYPE public.integrationtype AS ENUM (
            'SLACK', 'WEB', 'CLI', 'TOOL', 'EMAIL', 'SMS', 'WEBHOOK', 'TEAMS', 'DISCORD', 'TEST'
        );
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sessionstatus') THEN
        CREATE TYPE public.sessionstatus AS ENUM ('ACTIVE', 'INACTIVE', 'EXPIRED', 'ARCHIVED');
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'deliverystatus') THEN
        CREATE TYPE public.deliverystatus AS ENUM ('PENDING', 'DELIVERED', 'FAILED', 'RETRYING', 'EXPIRED');
    END IF;
END
$$;

-- users
CREATE TABLE IF NOT EXISTS public.users (
    user_id         UUID NOT NULL,
    primary_email   VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER
);
ALTER TABLE ONLY public.users ADD CONSTRAINT pkey_users PRIMARY KEY (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS ix_users_primary_email ON public.users (primary_email);

ALTER TABLE public.users OWNER TO $POSTGRESQL_USER;

-- request_sessions
CREATE TABLE IF NOT EXISTS public.request_sessions (
    id                           BIGINT NOT NULL,
    session_id                   VARCHAR(36) NOT NULL,
    user_id                      UUID NOT NULL REFERENCES public.users (user_id),
    integration_type             public.integrationtype NOT NULL,
    status                       public.sessionstatus NOT NULL,
    channel_id                   VARCHAR(255),
    thread_id                    VARCHAR(255),
    integration_metadata         JSONB,
    total_requests               INTEGER NOT NULL DEFAULT 0,
    last_request_at              TIMESTAMPTZ,
    expires_at                   TIMESTAMPTZ,
    created_at                   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    external_session_id          VARCHAR(255),
    current_agent_id             VARCHAR(255),
    conversation_thread_id       VARCHAR(255),
    user_context                 JSONB,
    conversation_context         JSONB,
    last_request_id              VARCHAR(36),
    total_input_tokens            INTEGER NOT NULL DEFAULT 0,
    total_output_tokens           INTEGER NOT NULL DEFAULT 0,
    total_tokens                  INTEGER NOT NULL DEFAULT 0,
    llm_call_count                INTEGER NOT NULL DEFAULT 0,
    max_input_tokens_per_call     INTEGER NOT NULL DEFAULT 0,
    max_output_tokens_per_call    INTEGER NOT NULL DEFAULT 0,
    max_total_tokens_per_call     INTEGER NOT NULL DEFAULT 0,
    version                       INTEGER,
    CONSTRAINT uq_request_sessions_session_id UNIQUE (session_id)
);
ALTER TABLE ONLY public.request_sessions ADD CONSTRAINT pkey_request_sessions PRIMARY KEY (id);
CREATE INDEX IF NOT EXISTS ix_session_id ON public.request_sessions (session_id);
CREATE INDEX IF NOT EXISTS ix_request_sessions_user_id ON public.request_sessions (user_id);

ALTER TABLE public.request_sessions OWNER TO $POSTGRESQL_USER;

CREATE SEQUENCE public.request_sessions_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.request_sessions_seq OWNER TO $POSTGRESQL_USER;

-- request_logs
CREATE TABLE IF NOT EXISTS public.request_logs (
    id                  BIGINT NOT NULL,
    request_id          VARCHAR(36) NOT NULL,
    session_id          VARCHAR(36) NOT NULL REFERENCES public.request_sessions (session_id),
    request_type        VARCHAR(50) NOT NULL,
    request_content     TEXT NOT NULL,
    normalized_request  JSONB,
    agent_id            VARCHAR(255),
    processing_time_ms  INTEGER,
    response_content    TEXT,
    response_metadata   JSONB,
    cloudevent_id       VARCHAR(36),
    cloudevent_type     VARCHAR(100),
    pod_name            VARCHAR(255),
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_request_logs_request_id UNIQUE (request_id)
);
ALTER TABLE ONLY public.request_logs ADD CONSTRAINT pkey_request_logs PRIMARY KEY (id);
CREATE INDEX IF NOT EXISTS ix_request_id ON public.request_logs (request_id);
CREATE INDEX IF NOT EXISTS ix_request_logs_session_id ON public.request_logs (session_id);
CREATE INDEX IF NOT EXISTS ix_request_logs_agent_id ON public.request_logs (agent_id);
CREATE INDEX IF NOT EXISTS ix_request_logs_pod_name ON public.request_logs (pod_name);

ALTER TABLE public.request_logs OWNER TO $POSTGRESQL_USER;

CREATE SEQUENCE public.request_logs_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.request_logs_seq OWNER TO $POSTGRESQL_USER;

-- user_integration_configs
CREATE TABLE IF NOT EXISTS public.user_integration_configs (
    id                  BIGINT NOT NULL,
    user_id             UUID NOT NULL REFERENCES public.users (user_id),
    integration_type    public.integrationtype NOT NULL,
    enabled             BOOLEAN NOT NULL,
    config              JSONB NOT NULL,
    priority            INTEGER NOT NULL,
    retry_count         INTEGER NOT NULL,
    retry_delay_seconds INTEGER NOT NULL,
    created_by          VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_integration UNIQUE (user_id, integration_type)
);
ALTER TABLE ONLY public.user_integration_configs ADD CONSTRAINT pkey_user_integration_configs PRIMARY KEY (id);
CREATE INDEX IF NOT EXISTS ix_user_integration_configs_user_id ON public.user_integration_configs (user_id);
CREATE INDEX IF NOT EXISTS ix_user_integration_configs_integration_type ON public.user_integration_configs (integration_type);

ALTER TABLE public.user_integration_configs OWNER TO $POSTGRESQL_USER;

CREATE SEQUENCE public.user_integration_configs_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.user_integration_configs_seq OWNER TO $POSTGRESQL_USER;

-- integration_credentials
CREATE TABLE IF NOT EXISTS public.integration_credentials (
    id              BIGINT NOT NULL,
    integration_type public.integrationtype NOT NULL,
    credential_name VARCHAR(100) NOT NULL,
    encrypted_value TEXT NOT NULL,
    description     TEXT,
    created_by      VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_integration_credential UNIQUE (integration_type, credential_name)
);
ALTER TABLE ONLY public.integration_credentials ADD CONSTRAINT pkey_integration_credentials PRIMARY KEY (id);

ALTER TABLE public.integration_credentials OWNER TO $POSTGRESQL_USER;

CREATE SEQUENCE public.integration_credentials_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.integration_credentials OWNER TO $POSTGRESQL_USER;

-- delivery_logs
CREATE TABLE IF NOT EXISTS public.delivery_logs (
    id                      BIGINT NOT NULL,
    request_id              VARCHAR(36) NOT NULL,
    session_id              VARCHAR(36) NOT NULL,
    user_id                 UUID NOT NULL REFERENCES public.users (user_id),
    integration_config_id   BIGINT REFERENCES public.user_integration_configs (id) ON DELETE CASCADE,
    integration_type        public.integrationtype NOT NULL,
    subject                 TEXT,
    content                 TEXT NOT NULL,
    status                  public.deliverystatus NOT NULL,
    attempts                INTEGER NOT NULL,
    max_attempts            INTEGER NOT NULL,
    first_attempt_at        TIMESTAMPTZ,
    last_attempt_at         TIMESTAMPTZ,
    delivered_at            TIMESTAMPTZ,
    expires_at              TIMESTAMPTZ,
    error_message           TEXT,
    error_details           JSONB,
    integration_metadata    JSONB,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL
);
ALTER TABLE ONLY public.delivery_logs ADD CONSTRAINT pkey_delivery_logs PRIMARY KEY (id);
CREATE INDEX IF NOT EXISTS ix_delivery_logs_request_id ON public.delivery_logs (request_id);
CREATE INDEX IF NOT EXISTS ix_delivery_logs_session_id ON public.delivery_logs (session_id);
CREATE INDEX IF NOT EXISTS ix_delivery_logs_user_id ON public.delivery_logs (user_id);

ALTER TABLE public.delivery_logs OWNER TO $POSTGRESQL_USER;

CREATE SEQUENCE public.delivery_logs_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.delivery_logs_seq OWNER TO $POSTGRESQL_USER;

-- processed_events
CREATE TABLE IF NOT EXISTS public.processed_events (
    id                  BIGINT NOT NULL,
    event_id            VARCHAR(255) NOT NULL,
    event_type          VARCHAR(255) NOT NULL,
    event_source        VARCHAR(255) NOT NULL,
    request_id          VARCHAR(255),
    session_id          VARCHAR(255),
    processed_by        VARCHAR(100) NOT NULL,
    processing_result   VARCHAR(50) NOT NULL,
    error_message       TEXT,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_processed_events_event_id UNIQUE (event_id)
);
ALTER TABLE ONLY public.processed_events ADD CONSTRAINT pkey_processed_events PRIMARY KEY (id);
CREATE INDEX IF NOT EXISTS ix_processed_events_event_id ON public.processed_events (event_id);
CREATE INDEX IF NOT EXISTS ix_processed_events_request_id ON public.processed_events (request_id);
CREATE INDEX IF NOT EXISTS ix_processed_events_created_at ON public.processed_events (created_at);

ALTER TABLE public.processed_events OWNER TO $POSTGRESQL_USER;

CREATE SEQUENCE public.processed_events_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.processed_events_seq OWNER TO $POSTGRESQL_USER;

-- integration_default_configs
CREATE TABLE IF NOT EXISTS public.integration_default_configs (
    id                  BIGINT NOT NULL,
    integration_type    public.integrationtype NOT NULL,
    enabled             BOOLEAN NOT NULL,
    config              JSONB NOT NULL,
    priority            INTEGER NOT NULL,
    retry_count         INTEGER NOT NULL,
    retry_delay_seconds INTEGER NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_integration_default_type UNIQUE (integration_type)
);
ALTER TABLE ONLY public.integration_default_configs ADD CONSTRAINT pkey_integration_default_configs PRIMARY KEY (id);

ALTER TABLE public.integration_default_configs OWNER TO $POSTGRESQL_USER;

CREATE SEQUENCE public.integration_default_configs_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.integration_default_configs_seq OWNER TO $POSTGRESQL_USER;

-- user_integration_mappings
CREATE TABLE IF NOT EXISTS public.user_integration_mappings (
    id                   BIGINT NOT NULL,
    user_email           VARCHAR(255) NOT NULL,
    integration_type     public.integrationtype NOT NULL,
    integration_user_id  VARCHAR(255) NOT NULL,
    user_id              UUID NOT NULL REFERENCES public.users (user_id),
    last_validated_at    TIMESTAMPTZ,
    validation_attempts  INTEGER NOT NULL DEFAULT 0,
    last_validation_error TEXT,
    created_by           VARCHAR(255) DEFAULT 'system',
    created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_integration_mapping UNIQUE (user_id, integration_type),
    CONSTRAINT uq_integration_user_id_type UNIQUE (integration_user_id, integration_type)
);
CREATE INDEX IF NOT EXISTS ix_user_integration_mappings_email_type ON public.user_integration_mappings (user_email, integration_type);
CREATE INDEX IF NOT EXISTS ix_user_integration_mappings_user_email ON public.user_integration_mappings (user_email);
CREATE INDEX IF NOT EXISTS ix_user_integration_mappings_user_id ON public.user_integration_mappings (user_id);
CREATE INDEX IF NOT EXISTS ix_user_integration_mappings_user_type ON public.user_integration_mappings (user_id, integration_type);
CREATE INDEX IF NOT EXISTS ix_user_integration_mappings_integration ON public.user_integration_mappings (integration_user_id, integration_type);

ALTER TABLE public.user_integration_mappings OWNER TO $POSTGRESQL_USER;

CREATE SEQUENCE public.user_integration_mappings_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.user_integration_mappings_seq OWNER TO $POSTGRESQL_USER;

-- Langraph4J Postgres Saver
CREATE TABLE IF NOT EXISTS public.LG4JThread (
    thread_id UUID PRIMARY KEY,
    thread_name VARCHAR(255),
    is_released BOOLEAN DEFAULT FALSE NOT NULL
);

ALTER TABLE public.LG4JThread OWNER TO $POSTGRESQL_USER;

CREATE TABLE IF NOT EXISTS public.LG4JCheckpoint (
    checkpoint_id UUID PRIMARY KEY,
    parent_checkpoint_id UUID,
    thread_id UUID NOT NULL,
    node_id VARCHAR(255),
    next_node_id VARCHAR(255),
    state_data JSONB NOT NULL,
    state_content_type VARCHAR(100) NOT NULL, 
    saved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_thread
        FOREIGN KEY(thread_id)
        REFERENCES public.LG4JThread(thread_id)
        ON DELETE CASCADE
);

ALTER TABLE public.LG4JCheckpoint OWNER TO $POSTGRESQL_USER;

CREATE INDEX IF NOT EXISTS idx_lg4jcheckpoint_thread_id ON public.LG4JCheckpoint(thread_id);
CREATE INDEX IF NOT EXISTS idx_lg4jcheckpoint_thread_id_saved_at_desc ON public.LG4JCheckpoint(thread_id, saved_at DESC);
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_lg4jthread_thread_name_unreleased  ON public.LG4JThread(thread_name) WHERE is_released = FALSE;
