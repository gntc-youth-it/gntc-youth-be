-- Clean up existing data
DELETE FROM post_image;
DELETE FROM post_hashtag;
DELETE FROM post_church;
DELETE FROM post_like;
DELETE FROM post_comment;
DELETE FROM post;
DELETE FROM uploaded_file;
DELETE FROM user_profile;
DELETE FROM advent_verses;
DELETE FROM advent_persons;
DELETE FROM app_user;
DELETE FROM church;
DELETE FROM books;

-- Test Church
INSERT INTO church (id, created_at, updated_at)
VALUES ('ANYANG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO church (id, created_at, updated_at)
VALUES ('SUWON', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test User
INSERT INTO app_user (id, email, name, provider, provider_user_id, role, created_at, updated_at)
VALUES (1, 'test@example.com', '테스트유저', 'KAKAO', 'kakao_123456', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO app_user (id, email, name, provider, provider_user_id, role, church_id, created_at, updated_at)
VALUES (2, 'leader@example.com', '리더유저', 'KAKAO', 'kakao_789012', 'LEADER', 'ANYANG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO app_user (id, email, name, provider, provider_user_id, role, church_id, created_at, updated_at)
VALUES (3, 'master@example.com', '마스터유저', 'KAKAO', 'kakao_345678', 'MASTER', 'ANYANG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 수원 성전 소속 일반 사용자 (역할 변경 테스트용)
INSERT INTO app_user (id, email, name, provider, provider_user_id, role, church_id, created_at, updated_at)
VALUES (4, 'suwon-user@example.com', '수원유저', 'KAKAO', 'kakao_111222', 'USER', 'SUWON', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 안양 성전 소속 일반 사용자 (역할 변경 테스트용 - 기존 리더 강등 확인)
INSERT INTO app_user (id, email, name, provider, provider_user_id, role, church_id, created_at, updated_at)
VALUES (5, 'anyang-user@example.com', '안양유저', 'KAKAO', 'kakao_333444', 'USER', 'ANYANG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test UserProfile (리더유저에게 프로필 추가)
INSERT INTO user_profile (id, user_id, generation, phone_number, gender, created_at, updated_at)
VALUES (100, 2, 45, '010-1234-5678', 'MALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
ALTER TABLE user_profile ALTER COLUMN id RESTART WITH 101;

-- Test Book
INSERT INTO books (id, canon_order, book_name, name)
VALUES (1, 1, 'GENESIS', '창세기');

-- Test Advent Person and Verses
INSERT INTO advent_persons (id, name, temple, batch)
VALUES (1, '테스트', 'Brisbane', 2024);

INSERT INTO advent_verses (id, person_id, sequence, book_name, chapter, verse)
VALUES (1, 1, 1, 'GENESIS', 1, 1);

-- Gallery Test: Uploaded Files
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (901, 'gallery1.jpg', 'stored_gallery1.jpg', 'uploads/gallery1.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (902, 'gallery2.jpg', 'stored_gallery2.jpg', 'uploads/gallery2.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (903, 'gallery3.jpg', 'stored_gallery3.jpg', 'uploads/gallery3.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (904, 'gallery4.jpg', 'stored_gallery4.jpg', 'uploads/gallery4.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Gallery Test: APPROVED post (RETREAT_2026_WINTER, by master user 3)
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (901, 3, 'RETREAT_2026_WINTER', 'APPROVED', '승인된 수련회 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Gallery Test: APPROVED post (NONE, by master user 3)
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (902, 3, 'NONE', 'APPROVED', '승인된 기타 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Gallery Test: PENDING_REVIEW post (RETREAT_2026_WINTER, by regular user 1)
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (903, 1, 'RETREAT_2026_WINTER', 'PENDING_REVIEW', '검수대기 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

-- Gallery Test: Images for APPROVED post 901 (RETREAT_2026_WINTER)
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (901, 901, 901, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (902, 901, 902, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Gallery Test: Image for APPROVED post 902 (NONE)
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (903, 902, 903, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Gallery Test: Image for PENDING_REVIEW post 903 (should NOT appear in gallery)
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (904, 903, 904, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

-- Church Tag: post 901 -> ANYANG, post 902 -> SUWON
INSERT INTO post_church (post_id, church_id) VALUES (901, 'ANYANG');
INSERT INTO post_church (post_id, church_id) VALUES (902, 'SUWON');
