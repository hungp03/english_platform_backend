-- Create instructor_certificate_proofs table
CREATE TABLE instructor_certificate_proofs (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_certificate_proofs_request
        FOREIGN KEY (request_id)
        REFERENCES instructor_requests(id)
        ON DELETE CASCADE
);

-- Create index on request_id for faster lookups
CREATE INDEX idx_certificate_proofs_request ON instructor_certificate_proofs(request_id);
