CREATE TABLE route (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255),
  route_date DATE,
  collector_id BIGINT,
  status VARCHAR(32) NOT NULL,
  expected_start_time TIMESTAMP NULL,
  expected_end_time TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE route_stop (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  route_id BIGINT NOT NULL,
  collection_request_id BIGINT NOT NULL,
  stop_order INT NOT NULL,
  status VARCHAR(32) NOT NULL,
  completed_at TIMESTAMP NULL,
  failure_type VARCHAR(64) NULL,
  failure_reason VARCHAR(1024) NULL,
  notes VARCHAR(1024) NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_route_stop_route FOREIGN KEY (route_id) REFERENCES route(id) ON DELETE CASCADE,
  CONSTRAINT fk_route_stop_request FOREIGN KEY (collection_request_id) REFERENCES collection_request(id)
);
