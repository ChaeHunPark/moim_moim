-- 1. Region Table (지역 정보)
CREATE TABLE region (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    depth INT,
    parent_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Member Table (회원 정보)
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    age INT,
    introduction TEXT,
    region_id BIGINT,
    role VARCHAR(20) NOT NULL,
    points INT DEFAULT 0,
    level INT DEFAULT 1,
    profile_image VARCHAR(255),
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE',
    last_login DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_region FOREIGN KEY (region_id) REFERENCES region(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Category Table (카테고리 정보)
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. MeetingPost Table (모임 게시글)
CREATE TABLE meeting_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,           -- [일관성] content 대신 description
    capacity INT NOT NULL,               -- [일관성] 정원
    current_participants INT DEFAULT 1,  -- [추가] 현재 인원 (엔티티 일치)
    view_count INT DEFAULT 0,            -- [추가] 조회수 (엔티티 일치)
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'RECRUITING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_creator FOREIGN KEY (creator_id) REFERENCES member(id),
    CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Participation Table (참여 내역)
CREATE TABLE participation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    meeting_post_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,    -- 'ORGANIZER', 'PARTICIPANT'
    status VARCHAR(50) NOT NULL,  -- 'APPROVED', 'PENDING'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_part_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_part_post FOREIGN KEY (meeting_post_id) REFERENCES meeting_post(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;