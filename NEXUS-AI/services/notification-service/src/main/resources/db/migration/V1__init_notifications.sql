CREATE TABLE notifications (
                               id BIGSERIAL PRIMARY KEY,
                               user_id VARCHAR(255) NOT NULL,
                               content VARCHAR(1024) NOT NULL,
                               type VARCHAR(255) NOT NULL,
                               read_flag BOOLEAN NOT NULL DEFAULT false,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);