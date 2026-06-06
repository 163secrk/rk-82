CREATE TABLE IF NOT EXISTS "user" (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  role VARCHAR(20) NOT NULL,
  avatar VARCHAR(255),
  balance DECIMAL(10,2) DEFAULT 0.00,
  rating DECIMAL(3,2) DEFAULT 5.00,
  description TEXT,
  skills VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title VARCHAR(200) NOT NULL,
  description TEXT NOT NULL,
  category VARCHAR(50),
  budget_min DECIMAL(10,2) NOT NULL,
  budget_max DECIMAL(10,2) NOT NULL,
  deadline DATETIME,
  requirements TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  client_id INTEGER NOT NULL,
  freelancer_id INTEGER,
  agreed_price DECIMAL(10,2),
  start_date DATETIME,
  end_date DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (client_id) REFERENCES "user"(id),
  FOREIGN KEY (freelancer_id) REFERENCES "user"(id)
);

CREATE TABLE IF NOT EXISTS bid (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  project_id INTEGER NOT NULL,
  freelancer_id INTEGER NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  delivery_days INTEGER NOT NULL,
  proposal TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES project(id),
  FOREIGN KEY (freelancer_id) REFERENCES "user"(id)
);

CREATE TABLE IF NOT EXISTS message (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  project_id INTEGER NOT NULL,
  sender_id INTEGER NOT NULL,
  receiver_id INTEGER NOT NULL,
  content TEXT NOT NULL,
  type VARCHAR(20) DEFAULT 'TEXT',
  file_url VARCHAR(255),
  is_read BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES project(id),
  FOREIGN KEY (sender_id) REFERENCES "user"(id),
  FOREIGN KEY (receiver_id) REFERENCES "user"(id)
);

CREATE TABLE IF NOT EXISTS payment (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  project_id INTEGER,
  payer_id INTEGER NOT NULL,
  payee_id INTEGER NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  type VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  transaction_id VARCHAR(100),
  remark TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES project(id),
  FOREIGN KEY (payer_id) REFERENCES "user"(id),
  FOREIGN KEY (payee_id) REFERENCES "user"(id)
);

CREATE TABLE IF NOT EXISTS review (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  project_id INTEGER NOT NULL,
  reviewer_id INTEGER NOT NULL,
  reviewee_id INTEGER NOT NULL,
  rating INTEGER NOT NULL,
  comment TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES project(id),
  FOREIGN KEY (reviewer_id) REFERENCES "user"(id),
  FOREIGN KEY (reviewee_id) REFERENCES "user"(id)
);

CREATE INDEX IF NOT EXISTS idx_project_status ON project(status);
CREATE INDEX IF NOT EXISTS idx_project_client ON project(client_id);
CREATE INDEX IF NOT EXISTS idx_project_freelancer ON project(freelancer_id);
CREATE INDEX IF NOT EXISTS idx_bid_project ON bid(project_id);
CREATE INDEX IF NOT EXISTS idx_bid_freelancer ON bid(freelancer_id);
CREATE INDEX IF NOT EXISTS idx_message_project ON message(project_id);
CREATE INDEX IF NOT EXISTS idx_message_unread ON message(receiver_id, is_read);

INSERT OR IGNORE INTO "user" (email, password, nickname, role, balance) VALUES 
('client@test.com', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '测试发包方', 'CLIENT', 10000.00),
('freelancer@test.com', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '测试接包方', 'FREELANCER', 0.00);

INSERT OR IGNORE INTO project (title, description, category, budget_min, budget_max, deadline, status, client_id) VALUES 
('企业官网开发', '需要一个现代化的企业展示官网，包含首页、产品介绍、关于我们、联系我们等页面。要求响应式设计，支持移动端访问。', '网站开发', 5000.00, 8000.00, '2024-12-31 23:59:59', 'PUBLISHED', 1),
('微信小程序开发', '开发一个电商类微信小程序，包含商品展示、购物车、订单管理、支付等功能。', '小程序开发', 15000.00, 25000.00, '2025-01-31 23:59:59', 'PUBLISHED', 1),
('UI设计项目', '为一款移动应用提供完整的UI设计服务，包含原型设计、视觉设计、交互设计等。', 'UI设计', 8000.00, 12000.00, '2024-12-15 23:59:59', 'PUBLISHED', 1);
